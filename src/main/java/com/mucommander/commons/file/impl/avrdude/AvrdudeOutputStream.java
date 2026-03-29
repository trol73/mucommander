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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.avrdude;

import com.mucommander.commons.HasProgress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Trifonov
 * Created on 31/03/16.
 */
public class AvrdudeOutputStream extends OutputStream implements HasProgress {
    private StreamType type;
    private Avrdude avrdude;
    private AvrdudeConfiguration config;
    private Avrdude.Operation operation;
    private ByteArrayOutputStream data;


    public AvrdudeOutputStream(StreamType type, AvrdudeConfiguration config, Avrdude.Operation operation) {
        this.type = type;
        this.config = config;
        this.operation = operation;
        this.data = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        if (data == null) {
            throw new IOException("Stream is closed");
        }
        data.write(b);
    }

    @Override
    public void close() throws IOException {
        if (data == null) {
            throw new IOException("Stream is closed");
        }
        writeToDevice();
        data.close();
        data = null;
    }

    private void writeToDevice() throws IOException {
        this.avrdude = new Avrdude(new ByteArrayInputStream(data.toByteArray()));
        try {
            avrdude.execute(config, operation, type);
            avrdude.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    @Override
    public int getProgress() {
        return avrdude == null ? 0 : avrdude.getProgress();
    }

    @Override
    public boolean hasProgress() {
        return true;
    }
}
