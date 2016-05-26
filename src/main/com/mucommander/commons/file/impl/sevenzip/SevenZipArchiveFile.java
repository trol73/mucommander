/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.sevenzip;

import com.mucommander.commons.file.*;
import com.mucommander.commons.util.CircularByteBuffer;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created on 23/05/14.
 * @author Oleg Trifonov
 */
public class SevenZipArchiveFile extends AbstractROArchiveFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipArchiveFile.class);

    private ISevenZipInArchive inArchive;
    private ArchiveOpenVolumeCallback archiveOpenVolumeCallback;

    /**
     * Creates an AbstractROArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     */
    protected SevenZipArchiveFile(AbstractFile file) {
        super(file);
    }

    private ISevenZipInArchive openSevenZipFile() throws IOException, SevenZipException {
        if (inArchive == null) {
            archiveOpenVolumeCallback = new ArchiveOpenVolumeCallback();
            SevenZipRandomAccessFile in = new SevenZipRandomAccessFile(file);
            inArchive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, in);
            //new VolumedArchiveInStream(in, archiveOpenVolumeCallback));
        }
        return inArchive;
    }


    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
        try {
            final ISevenZipInArchive sevenZipFile = openSevenZipFile();
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
                System.err.println("Error closing archive: " + e);
            }
            inArchive = null;
        }

    }

    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        final int[] in = new int[1];
        in[0] = (Integer)entry.getEntryObject();
        final CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
        new Thread() {
            @Override
            public void run() {
                try {
                    final ISevenZipInArchive sevenZipFile = openSevenZipFile();
                    sevenZipFile.extract(in, false, new ExtractCallback(inArchive, cbb.getOutputStream()));
                } catch (SevenZipException | IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inArchive != null) {
                        try {
                            inArchive.close();
                        } catch (SevenZipException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        cbb.getOutputStream().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    inArchive = null;
                }
            }
        }.start();

        return cbb.getInputStream();
    }




    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZipEntry}
     *
     * @param i the index of entry
     * @return an ArchiveEntry whose attributes are fetched from the given SevenZipEntry
     */
    private ArchiveEntry createArchiveEntry(int i) throws IOException, SevenZipException {
        final ISevenZipInArchive sevenZipFile = openSevenZipFile();
        String path = sevenZipFile.getStringProperty(i, PropID.PATH);
        boolean isDirectory = (Boolean)sevenZipFile.getProperty(i, PropID.IS_FOLDER);
        Date time = (Date)sevenZipFile.getProperty(i, PropID.LAST_WRITE_TIME);
        long size = (Long)sevenZipFile.getProperty(i, PropID.SIZE);
        ArchiveEntry result = new ArchiveEntry(path, isDirectory, time.getTime(), size, true);
        result.setEntryObject(i);
        return result;
    }

    private static class ArchiveOpenVolumeCallback implements IArchiveOpenVolumeCallback {

        /**
         * Cache for opened file streams
         */
        private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<>();

        /**
         * This method doesn't needed, if using with VolumedArchiveInStream
         * and pass the name of the first archive in constructor.
         * (Use two argument constructor)
         *
         * @see IArchiveOpenVolumeCallback#getProperty(PropID)
         */
        public Object getProperty(PropID propID) throws SevenZipException {
            return null;
        }

        /**
         *
         * The name of the required volume will be calculated out of the
         * name of the first volume and volume index. If you need
         * need volume index (integer) you will have to parse filename
         * and extract index.
         *
         * <pre>
         * int index = filename.substring(filename.length() - 3,
         *         filename.length());
         * </pre>
         *
         */
        public IInStream getStream(String filename) throws SevenZipException {
            try {
                // We use caching of opened streams, so check cache first
                RandomAccessFile randomAccessFile = openedRandomAccessFileList.get(filename);
                if (randomAccessFile != null) { // Cache hit.
                    // Move the file pointer back to the beginning
                    // in order to emulating new stream
                    randomAccessFile.seek(0);
                    return new RandomAccessFileInStream(randomAccessFile);
                }

                // Nothing useful in cache. Open required volume.
                randomAccessFile = new RandomAccessFile(filename, "r");

                // Put new stream in the cache
                openedRandomAccessFileList.put(filename, randomAccessFile);

                return new RandomAccessFileInStream(randomAccessFile);
            } catch (FileNotFoundException fileNotFoundException) {
                // Required volume doesn't exist. This happens if the volume:
                // 1. never exists. 7-Zip doesn't know how many volumes should
                //    exist, so it have to try each volume.
                // 2. should be there, but doesn't. This is an error case.

                // Since normal and error cases are possible,
                // we can't throw an error message
                return null; // We return always null in this case
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Close all opened streams
         */
        void close() throws IOException {
            for (RandomAccessFile file : openedRandomAccessFileList.values()) {
                file.close();
            }
        }
    }



    public static class ExtractCallback implements IArchiveExtractCallback {
        private int hash = 0;
        private long size = 0;
        private int index;
        private boolean skipExtraction;
        private ISevenZipInArchive inArchive;
        private OutputStream os;

        public ExtractCallback(ISevenZipInArchive inArchive, OutputStream os) {
            this.inArchive = inArchive;
            this.os = os;
        }

        public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
            this.index = index;
            skipExtraction = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
            if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) {
                return null;
            }
            return data -> {
                hash ^= Arrays.hashCode(data);
                size += data.length;
                try {
                    os.write(data);
                } catch (IOException e) {
                    throw new SevenZipException(e);
                }
                return data.length; // Return amount of proceed data
            };
        }

        public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
//System.out.println("prepare  " + index);
        }

        public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
            if (skipExtraction) {
                return;
            }
            if (extractOperationResult != ExtractOperationResult.OK) {
                System.err.println("Extraction error  = " + extractOperationResult);
            } else {
//System.out.println(String.format("%9X | %10s | %s", hash, size, inArchive.getProperty(index, PropID.PATH)));
                hash = 0;
                size = 0;
            }
        }

        public void setCompleted(long completeValue) throws SevenZipException {
//System.out.println("completed  " + completeValue);
        }

        public void setTotal(long total) throws SevenZipException {
//System.out.println("total  " + index + "   " + total);
        }
    }
}