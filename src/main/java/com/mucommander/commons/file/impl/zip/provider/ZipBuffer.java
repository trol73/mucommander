package com.mucommander.commons.file.impl.zip.provider;

/**
 * ZipBuffer is a C struct-like class that holds byte buffers that are used to convert Java values to Big Endian byte
 * arrays. It allows to reuse the same byte buffers instead of instanciating new ones for each conversion.
 *
 * @see ZipShort#getBytes(int, byte[], int)
 * @see ZipLong#getBytes(long, byte[], int)
 * @author Maxence Bernard
 */
public class ZipBuffer {

    /**  2-byte buffer that can hold a Zip short value */
    byte[] shortBuffer = new byte[2];

    /**  2-byte buffer that can hold a Zip long value */
    byte[] longBuffer = new byte[4];
}
