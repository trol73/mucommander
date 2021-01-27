/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.avrdude;

import com.mucommander.commons.HasProgress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Trifonov
 * Created on 31/03/16.
 */
public class AvrDudeInputStream extends InputStream implements HasProgress {

    private StreamType type;
    private Avrdude avrdude;
    private AvrdudeConfiguration config;
    private Avrdude.Operation operation;
    private ByteArrayInputStream data;


    public AvrDudeInputStream(StreamType type, AvrdudeConfiguration config, Avrdude.Operation operation) {
        this.type = type;
        this.config = config;
        this.operation = operation;
        this.avrdude = new Avrdude();
    }


    @Override
    public int read() throws IOException {
        if (avrdude.getStatus() == Avrdude.Status.NONE) {
            readAll();
        }
        return data.read();
    }

    @Override
    public int available() {
        return data.available();
    }

    @Override
    public synchronized void reset() {
        data.reset();
    }

    @Override
    public boolean markSupported() {
        return data.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        data.mark(readlimit);
    }

    @Override
    public int getProgress() {
        return avrdude.getProgress();
    }

    @Override
    public boolean hasProgress() {
        return true;
    }

    void readAll() throws IOException {
        try {
            avrdude.execute(config, operation, type);
            avrdude.waitFor();
            if (type == StreamType.HEX) {
                data = new ByteArrayInputStream(avrdude.getHexOutput().getBytes());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
