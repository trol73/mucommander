package com.mucommander.commons.file.impl.avrdude;

/**
 * @author Oleg Trifonov
 * Created on 24/06/16.
 */
public enum StreamType {
    BIN('r'),
    HEX('i');

    private final char avrdudeName;

    StreamType(char avrdudeName) {
        this.avrdudeName = avrdudeName;
    }

    public char getAvrdudeName() {
        return avrdudeName;
    }
}
