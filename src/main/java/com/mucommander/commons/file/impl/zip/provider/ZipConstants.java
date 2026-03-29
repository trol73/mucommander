package com.mucommander.commons.file.impl.zip.provider;

import java.util.zip.Deflater;

/**
 * Contains the various constants that are used by several classes of this package.
 *
 * @author Maxence Bernard
 */
public interface ZipConstants {

    /**
     * DEFLATED compression method
     */
    int DEFLATED = java.util.zip.ZipEntry.DEFLATED;

    /**
     * STORED compression method (raw storage, no compression)
     */
    int STORED = java.util.zip.ZipEntry.STORED;

    /**
     * Default compression level for DEFLATED compression
     */
    int DEFAULT_DEFLATER_COMPRESSION = Deflater.DEFAULT_COMPRESSION;

    /**
     * Default size of the buffer used by Deflater.
     */
    // /!\ For some unknown reason, using a larger buffer *hurts* performance.
    int DEFAULT_DEFLATER_BUFFER_SIZE = 512;

    /**
     * Maximum size of a Zip32 entry or a Zip32 file as a whole, i.e. (2^32)-1.
     * */
    long MAX_ZIP32_SIZE = 4294967295l;

    /**
     * Size of write buffers
     */
    int WRITE_BUFFER_SIZE = 65536;

    /**
     * UTF-8 encoding String
     */
    String UTF_8 = "UTF-8";

    /**
     * Local file header signature
     */
    byte[] LFH_SIG = ZipLong.getBytes(0X04034B50L);

    /**
     * Data descriptor signature
     */
    byte[] DD_SIG = ZipLong.getBytes(0X08074B50L);

    /**
     * Central file header signature
     */
    byte[] CFH_SIG = ZipLong.getBytes(0X02014B50L);

    /**
     * End of central dir signature
     */
    byte[] EOCD_SIG = ZipLong.getBytes(0X06054B50L);
}
