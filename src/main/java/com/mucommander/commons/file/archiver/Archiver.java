/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.archiver;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.io.BufferedRandomOutputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import org.apache.hadoop.io.compress.bzip2.CBZip2OutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Archiver is an abstract class that represents a generic file archiver and abstracts the underlying
 * compression method and specifics of the format.
 *
 * <p>Subclasses implement specific archive formats (Zip, Tar...) but cannot be instantiated directly.
 * Instead, the <code>getArchiver</code> methods can be used to retrieve an Archiver
 * instance for a specified archive format. A list of available archive formats can be dynamically retrieved
 * using {@link #getFormats(boolean) getFormats}.
 *
 * <p>Archive formats fall into 2 categories:
 * <ul>
 * <li><i>Single entry formats:</i> Formats that can only store one entry without any directory structure, e.g. Gzip or Bzip2.
 * <li><i>Many entries formats:</i> Formats that can store multiple entries along with a directory structure, e.g. Zip or Tar.
 * </ul>
 *
 * @author Maxence Bernard
 */
public abstract class Archiver {


    /** The underlying stream this archiver is writing to */
    protected OutputStream out;
    /** Archive format of this Archiver */
    protected ArchiveFormat format;
    /** Support output stream for archiving files */
    boolean supportStream;
	
    /**
     * Creates a new Archiver.
     *
     * @param out the OutputStream this Archiver will write to
     */
    Archiver(OutputStream out) {
        this.out = out;
        this.supportStream = true;
    }

    /**
     * Returns the <code>OutputStream</code> this Archiver is writing to.
     *
     * @return the OutputStream this Archiver is writing to
     */
    public OutputStream getOutputStream() {
        return out;
    }

    
    /**
     * Returns the archiver format used by this Archiver. See format constants.
     * @return archiver format code
     */
    public ArchiveFormat getFormat() {
        return this.format;
    }
	
    /**
     * Sets the archiver format used by this Archiver, for internal use only.
     */
    private void setFormat(ArchiveFormat format) {
        this.format = format;
    }
	

    /**
     * Checks if the format used by this Archiver can store an optional comment.
     * @return true if the format used by this Archiver can store an optional comment.
     */
    public boolean supportsComment() {
        return formatSupportsComment(this.format);
    }

    /**
     * @return true if the archiver supports writing with streams
     */
    public boolean supportsStream() {
        return supportStream;
    }

    /**
     * Sets an optional comment in the archive, the {@link #supportsComment()} or
     * {@link #formatSupportsComment(ArchiveFormat)} must first be called to make sure
     * the archive format supports comment, otherwise calling this method will have no effect.
     *
     * <p>Implementation note: Archiver implementations must override this method to handle comments
     *
     * @param comment the comment to be stored in the archive
     */
    public void setComment(String comment) {
        // No-op
    }


    /**
     * Normalizes the entry path, that is :
     * <ul>
     *   <li>replace any \ character occurrence by / as this usually is the default separator for archive files
     *   <li>if the entry is a directory, add a trailing slash to the path if it doesn't have one already
     * </ul>
     *
     * @param entryPath
	  * @param isDirectory
	  *
	  * @return normalized path
     */
    String normalizePath(String entryPath, boolean isDirectory) {
        // Replace any \ character by /
        entryPath = entryPath.replace('\\', '/');
		
        // If entry is a directory, make sure the path contains a trailing / 
        if (isDirectory && !entryPath.endsWith("/"))
            entryPath += "/";
		
        return entryPath;
    }


    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns an Archiver for the specified format and that uses the given {@link AbstractFile} to write entries to.
     * <code>null</code> is returned if the specified format is not valid.
     *
     * <p>This method will first attempt to get a {@link RandomAccessOutputStream} if the given file is able to supply
     * one, and if not, fall back to a regular <code>OutputStream</code>. Note that if the file exists, its contents
     * will be overwritten. Write bufferring is used under the hood to improve performance.
     *
     * @param file the AbstractFile which the returned Archiver will write entries to
     * @param format an archive format
     * @return an Archiver for the specified format and that uses the given {@link AbstractFile} to write entries to ;
     * null if the specified format is not valid.
     * @throws IOException if the file cannot be opened for write, or if an error occurred while intializing the archiver
     * @throws UnsupportedFileOperationException if the underlying filesystem does not support write operations
     */
    public static Archiver getArchiver(AbstractFile file, ArchiveFormat format) throws IOException, UnsupportedFileOperationException {
//        switch(format) {
//            case ISO:
//                return new ISOArchiver(file);
//        }
        OutputStream out = null;

        if (file.isFileOperationSupported(FileOperation.RANDOM_WRITE_FILE)) {
            try {
                // Important: if the file exists, it has to be overwritten as AbstractFile#getRandomAccessOutputStream()
                // does NOT overwrite the file. This fixes bug #30.
                if (file.exists()) {
                    file.delete();
                }
                out = new BufferedRandomOutputStream(file.getRandomAccessOutputStream());
            } catch (IOException e) {
                // Fall back to a regular OutputStream
            }
        }

        if (out == null) {
            out = new BufferedOutputStream(file.getOutputStream());
        }

        return getArchiver(out, format);
    }


    /**
     * Returns an Archiver for the specified format and that uses the given <code>OutputStream</code> to write entries to.
     * <code>null</code> is returned if the specified format is not valid. Whenever possible, a
     * {@link RandomAccessOutputStream} should be supplied as some formats take advantage of having a random write access.
     *
     * @param out the OutputStream which the returned Archiver will write entries to
     * @param format an archive format
     * @return an Archiver for the specified format and that uses the given {@link AbstractFile} to write entries to ;
     * null if the specified format is not valid.
     * @throws IOException if an error occurred while initializing the archiver
     */
    private static Archiver getArchiver(OutputStream out, ArchiveFormat format) throws IOException {
        Archiver archiver;

        switch (format) {
            case ZIP:
                archiver = new ZipArchiver(out);
                break;
            case GZ:
                archiver = new SingleFileArchiver(new GZIPOutputStream(out));
                break;
            case BZ2:
                archiver = new SingleFileArchiver(createBzip2OutputStream(out));
                break;
            case TAR:
                archiver = new TarArchiver(out);
                break;
            case TAR_GZ:
                archiver = new TarArchiver(new GZIPOutputStream(out));
                break;
            case TAR_BZ2:
                archiver = new TarArchiver(createBzip2OutputStream(out));
                break;
//            case ISO:
//                throw new IllegalStateException("ISO archiving not supported by stream");

            default:
                return null;
        }
		
        archiver.setFormat(format);

        return archiver;
    }

    /**
     * Creates and returns a Bzip2 <code>OutputStream</code> using the given <code>OutputStream</code> as the underlying
     * stream.
     *
     * @param out the underlying stream
     * @return a Bzip2 OutputStream
     * @throws IOException if an error occurred while initializing the Bzip2 OutputStream
     */
    private static OutputStream createBzip2OutputStream(OutputStream out) throws IOException {
        // Writes the 2 magic bytes 'BZ', as required by CBZip2OutputStream. A quote from CBZip2OutputStream's Javadoc:
        // "Attention: The caller is responsible to write the two BZip2 magic bytes "BZ" to the specified stream
        // prior to calling this constructor."

        out.write('B');
        out.write('Z');

        return new CBZip2OutputStream(out);
    }


    /**
     * Returns an array of available archive formats, single entry formats or many entries formats
     * depending on the value of the specified boolean parameter. 
     *
     * @param manyEntries if true, a list of many entries formats (a subset of single entry formats) will be returned
     * @return an array of available archive formats
     */
    public static ArchiveFormat[] getFormats(boolean manyEntries) {
        if (!manyEntries) {
            return ArchiveFormat.values();
        }
        int cnt = 0;
        for (ArchiveFormat af : ArchiveFormat.values()) {
            if (af.supportManyEntries) {
                cnt++;
            }
        }
        ArchiveFormat[] result = new ArchiveFormat[cnt];
        int i = 0;
        for (ArchiveFormat af : ArchiveFormat.values()) {
            if (af.supportManyEntries) {
                result[i++] = af;
            }
        }
        return result;
    }


    /**
     * Returns true if the specified archive format can store an optional comment.
     *
     * @param format an archive format
     * @return true if the specified archive format can store an optional comment
     */
    public static boolean formatSupportsComment(ArchiveFormat format) {
        return format == ArchiveFormat.ZIP;
    }
	
	
    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Creates a new entry in the archive using the given relative path and file attributes, and returns an
     * <code>OutputStream</code> to write the entry's contents. The specified file attributes are used to determine
     * whether the entry is a directory or a regular file, and to set the entry's size, permissions and date.
     * 
     * <p>If the entry is a regular file (not a directory), an OutputStream which can be used to write the contents
     * of the entry will be returned, <code>null</code> otherwise. The OutputStream <b>must not</b> be closed once
     * it has been used (Archiver takes care of this), only the {@link #close() close} method has to be called when
     * all entries have been created.
     *
     * <p>If this Archiver uses a single entry format, the specified path and file won't be used at all.
     * Also in this case, this method must be invoked only once (single entry), it will throw an IOException
     * if invoked more than once.
     *
     * @param entryPath the path to be used to create the entry in the archive. This parameter is simply ignored if the
     * archive is a single entry format.
     * @param attributes used to determine whether the entry is a directory or regular file, and to retrieve its
     * date and size
     * @return <code>OutputStream</code> to write the entry's contents.
     * @throws IOException if this Archiver failed to write the entry, or in the case of a single entry archiver, if
     * this method was called more than once.
     */
    public abstract OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException;


    /**
     * @return Name of current file being processed
     */
    public String getProcessingFile() {
        return null;
    }
    
    /**
     * Written bytes in total without the current file progress
     * @return number of bytes written as a long
     */
    public long totalWrittenBytes() {
        return -1;
    }
    
    /**
     * Written bytes to the current file being processed, will be the same size as the
     * file if complete.
     * @return number of bytes written as a long
     */
    public long writtenBytesCurrentFile() {
        return -1;
    }
    
    /**
     * @return Size of the current file being processed in bytes
     */
    public long currentFileLength() {
        return -1;
    }
    
    /**
     * Finish the archiving process when all files have been added.
     */
    public abstract void postProcess() throws IOException;
    
    /**
     * Closes the underlying OuputStream and ressources used by this Archiver to write the archive. This method
     * must be called when all entries have been added to the archive.
     */
    public abstract void close() throws IOException;
}
