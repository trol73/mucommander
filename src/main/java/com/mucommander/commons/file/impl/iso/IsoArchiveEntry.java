package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.ArchiveEntry;

/**
 * This class represents an archive entry within an ISO archive.
 *
 * @author Maxence Bernard
 */
class IsoArchiveEntry extends ArchiveEntry {

    private long index;
    private int sectSize;
    private long shiftOffset;
    private boolean audio;

    IsoArchiveEntry(String path, boolean directory, long date, long size, long index, int sectSize, long shiftOffset, boolean audio) {
        super(path, directory, date, size, true);

        this.index = index;
        this.sectSize = sectSize;
        this.shiftOffset = shiftOffset;
        this.audio = audio;
    }

    long getIndex() {
        return index;
    }

    public int getSectSize() {
        return sectSize;
    }

    public long getShiftOffset() {
        return shiftOffset;
    }

    public boolean getAudio() {
        return audio;
    }

}
