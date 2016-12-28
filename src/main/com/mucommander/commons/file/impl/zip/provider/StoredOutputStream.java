package com.mucommander.commons.file.impl.zip.provider;

import java.io.IOException;
import java.io.OutputStream;


/**
 * StoredOutputStream compresses data using the STORED compression method (i.e. no compression).
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Maxence Bernard
 */
public class StoredOutputStream extends ZipEntryOutputStream {

    /** Number of bytes in/out so far */
    private int storedCount;


    /**
     * Creates a new <code>StoredOutputStream</code> that writes compressed data to the given <code>OutputStream</code>
     * and automatically updates the supplied CRC32 checksum.
     *
     * @param out the OutputStream where the compressed data is sent to
     */
    public StoredOutputStream(OutputStream out) {
        super(out, ZipConstants.STORED);
    }


    /////////////////////////////////////////
    // ZipEntryOutputStream implementation //
    /////////////////////////////////////////

    @Override
    public int getTotalIn() {
        return storedCount;
    }

    @Override
    public int getTotalOut() {
        return storedCount;
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        out.write(b, offset, length);
        storedCount += length;

        crc.update(b, offset, length);
    }
}
