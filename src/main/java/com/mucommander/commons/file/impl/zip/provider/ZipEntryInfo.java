package com.mucommander.commons.file.impl.zip.provider;

/**
 * ZipEntryInfo is a C struct-like class that holds the information about an entry that is used for parsing and writing
 * the Zip file.
 *
 * @author Maxence Bernard
 */
final class ZipEntryInfo {

    /** Offset to the central file header */
    long centralHeaderOffset = -1;

    /** Length of the central file header */
    long centralHeaderLen = -1;

    /** Offset to the local file header */
    long headerOffset = -1;

    /** Offset to the start of file data */
    long dataOffset = -1;

    /** <code>true</code> if this entry has a data descriptor in the Zip file */
    boolean hasDataDescriptor;

    /** The encoding used for filename and comment fields */
    String encoding;

    /** The filename's bytes */
    byte filename[];

    /** The comment's bytes */
    byte comment[];
}
