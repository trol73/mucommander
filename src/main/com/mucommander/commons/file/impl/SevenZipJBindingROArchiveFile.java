package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.impl.sevenzip.SevenZipArchiveFile.ExtractCallback;
import com.mucommander.commons.file.impl.sevenzip.SignatureCheckedRandomAccessFile;
import com.mucommander.commons.file.impl.sevenzip.multivolume.InArchiveWrapper;
import com.mucommander.commons.file.impl.sevenzip.multivolume.SevenZipMultiVolumeCallbackHandler;
import com.mucommander.commons.file.impl.sevenzip.multivolume.SevenZipRarMultiVolumeCallbackHandler;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.CircularByteBuffer;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.VolumedArchiveInStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class SevenZipJBindingROArchiveFile extends AbstractROArchiveFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipJBindingROArchiveFile.class);

    private static final Pattern MULTI_PART_RAR_PATTERN = Pattern.compile("[.]part\\d+[.]rar");

    private static final String MULTI_PART_7Z_EXT = ".7z.001";

   private static volatile boolean libraryInit = false;
    protected IInArchive inArchive;
    private ArchiveFormat sevenZipJBindingFormat;
    private final SevenZipArchiveFormatDetector formatDetector;

    private final byte[] formatSignature;

    /**
     * Creates an AbstractROArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     *
     * @see <a href="http://sevenzipjbind.sourceforge.net/javadoc/net/sf/sevenzipjbinding/ArchiveFormat.html">
     *      ArchiveFormat</a>
     */
    public SevenZipJBindingROArchiveFile(AbstractFile file, ArchiveFormat sevenZipJBindingFormat, byte[] formatSignature) {
        super(file);
        this.sevenZipJBindingFormat = sevenZipJBindingFormat;
        this.formatSignature = formatSignature;
        this.formatDetector = null;
        initSevenZipBindings();
    }

    public SevenZipJBindingROArchiveFile(AbstractFile file, SevenZipArchiveFormatDetector formatDetector) {
        super(file);
        this.sevenZipJBindingFormat = null;
        this.formatSignature = new byte[] {};
        this.formatDetector = formatDetector;
        initSevenZipBindings();
    }


    private static void initSevenZipBindings() {
        if (!libraryInit) {
            synchronized (SevenZipJBindingROArchiveFile.class) {
                try {
                    if (OsFamily.getCurrent() == OsFamily.MAC_OS_X && OsFamily.isAarch64()) {
                        SevenZip.initSevenZipFromPlatformJAR("Mac-arm64");
                    } else if (OsFamily.getCurrent() == OsFamily.MAC_OS_X) {
                        SevenZip.initSevenZipFromPlatformJAR("Mac-x86_64");
                    } else if (OsFamily.getCurrent() == OsFamily.LINUX && OsFamily.isAmd64()) {
                        SevenZip.initSevenZipFromPlatformJAR("Linux-amd64");
                    } else if (OsFamily.getCurrent() == OsFamily.LINUX) {
                        SevenZip.initSevenZipFromPlatformJAR("Linux-i386");
                    } else {
                        System.out.println("Arch: " + System.getProperty("os.arch"));
                        SevenZip.initSevenZipFromPlatformJAR();
                    }
                    libraryInit = true;
                } catch (SevenZipNativeInitializationException ex) {
                    throw new RuntimeException("Unable to init 7-Zip-JBinding library bindings", ex);
                }
            }
        }
    }

//    private IInArchive openInArchive() throws IOException {
//        if (inArchive == null) {
//            if (formatDetector != null) {
//                sevenZipJBindingFormat = formatDetector.detect(file);
//            }
//            SignatureCheckedRandomAccessFile in = new SignatureCheckedRandomAccessFile(file, formatSignature);
//            inArchive = SevenZip.openInArchive(sevenZipJBindingFormat, in);
//        }
//        return inArchive;
//    }

    /**
     * Open the file and check its signature compared to the one provided in {@link #SevenZipJBindingROArchiveFile(AbstractFile, ArchiveFormat, byte[])}
     * @return this {@code SevenZipJBindingROArchiveFile} instance when file signature matches the specified signature
     * @throws IOException in case the file cannot be opened or its signature differs from the specified signature
     */
    public SevenZipJBindingROArchiveFile check() throws IOException {
        openInArchive();
        return this;
    }

    private IInArchive openInArchive() throws IOException {
        if (inArchive == null) {
            boolean multiPartRar = MULTI_PART_RAR_PATTERN.matcher(file.getName()).find();
            boolean multiPartSevenZip = file.getName().toLowerCase().endsWith(MULTI_PART_7Z_EXT);


//            if (formatDetector != null) {
//                sevenZipJBindingFormat = formatDetector.detect(file);
//            }
//            SignatureCheckedRandomAccessFile in = new SignatureCheckedRandomAccessFile(file, formatSignature);
//            inArchive = SevenZip.openInArchive(sevenZipJBindingFormat, in);

            if (multiPartRar) {
                SevenZipRarMultiVolumeCallbackHandler handler = new SevenZipRarMultiVolumeCallbackHandler(formatSignature, password);
                IInStream firstStream = handler.getStream(file.getAbsolutePath());
                IInArchive tmpInArchive = SevenZip.openInArchive(sevenZipJBindingFormat, firstStream, handler);
                inArchive = new InArchiveWrapper(tmpInArchive, handler);
            } else if (multiPartSevenZip) {
                SevenZipMultiVolumeCallbackHandler handler = new SevenZipMultiVolumeCallbackHandler(formatSignature, file, password);
                IInArchive tmpInArchive = SevenZip.openInArchive(sevenZipJBindingFormat, new VolumedArchiveInStream(handler));
                if (isEnc(tmpInArchive) && password == null) {
                    // Throwing this exception to trigger password dialog
                    throw new IOException(String.format("Password protected file but password is null [file = %s]", file.getName()));
                }
                inArchive = new InArchiveWrapper(tmpInArchive, handler);
            } else {
                SignatureCheckedRandomAccessFile in = new SignatureCheckedRandomAccessFile(file, formatSignature);
                IInArchive tmpInArchive = SevenZip.openInArchive(sevenZipJBindingFormat, in, password);
                inArchive = new InArchiveWrapper(tmpInArchive, in);
            }
        }
        return inArchive;
    }

    private boolean isEnc(IInArchive archive) {
        try {
            if (Boolean.TRUE.equals(archive.getArchiveProperty(PropID.ENCRYPTED))) {
                return true;
            }

            for (int i = 0; i < archive.getNumberOfItems(); i++) {
                if (Boolean.TRUE.equals(archive.getProperty(i, PropID.ENCRYPTED))) {
                    return true;
                }
            }
        } catch (SevenZipException e) {
            LOGGER.error("Error checking if file is encrypted", e);
        }
        return false;
    }

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
        try {
            final IInArchive sevenZipFile = openInArchive();
            int nbEntries = sevenZipFile.getNumberOfItems();
            List<ArchiveEntry> entries = new ArrayList<>();
            for (int i = 0; i < nbEntries; i++) {
                entries.add(createArchiveEntry(i));
            }
            return new WrapperArchiveEntryIterator(entries.iterator());
        } catch (SevenZipException e) {
            throw new IOException(e);
        } finally {
            try {
                if (inArchive != null) {
                    inArchive.close();
                }
            } catch (SevenZipException e) {
                LOGGER.error("Error closing archive", e);
            }
            inArchive = null;
        }
    }

    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) {
        final int[] in = new int[1];
        in[0] = (Integer)entry.getEntryObject();
        final CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
        new Thread(() -> {
            synchronized (SevenZipJBindingROArchiveFile.this) {
                try {
                    final IInArchive sevenZipFile = openInArchive();
                    sevenZipFile.extract(in, false, new ExtractCallback(inArchive, cbb.getOutputStream()));
                } catch (IOException e) {
                    LOGGER.error("Can't open archive", e);
                } finally {
                    if (inArchive != null) {
                        try {
                            inArchive.close();
                        } catch (SevenZipException e) {
                            LOGGER.error("Can't close archive", e);
                        }
                    }
                    try {
                        cbb.getOutputStream().close();
                    } catch (IOException e) {
                        LOGGER.error("Can't close outputstream", e);
                    }
                    inArchive = null;
                }
            }
        }).start();

        return cbb.getInputStream();
    }

    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZipEntry}
     *
     * @param i the index of entry
     * @return an ArchiveEntry whose attributes are fetched from the given SevenZipEntry
     */
    private ArchiveEntry createArchiveEntry(int i) throws IOException {
        final IInArchive sevenZipFile = openInArchive();
        String path = sevenZipFile.getStringProperty(i, PropID.PATH);
        boolean isDirectory = (Boolean)sevenZipFile.getProperty(i, PropID.IS_FOLDER);
        Date time = (Date) sevenZipFile.getProperty(i, PropID.LAST_MODIFICATION_TIME);
        Long size = (Long) sevenZipFile.getProperty(i, PropID.SIZE);
        if (org.apache.commons.lang.StringUtils.isEmpty(path)) {
            path = file.getNameWithoutExtension();
        }
        path = path.replace(File.separatorChar, ArchiveEntry.SEPARATOR_CHAR);
        ArchiveEntry result = new ArchiveEntry(path, isDirectory,
                time == null ? -1 : time.getTime(),
                size == null ? -1 : size, true);
        result.setEntryObject(i);
        return result;
    }

}
