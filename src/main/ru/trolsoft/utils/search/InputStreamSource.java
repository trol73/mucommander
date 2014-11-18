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
package ru.trolsoft.utils.search;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Trifonov
 * Created on 18/11/14.
 */
public class InputStreamSource implements SearchSourceStream {

    private final InputStream is;
    private int next;
    private boolean closed;

    public InputStreamSource(InputStream is) {
        this.is = new BufferedInputStream(is);
    }

    @Override
    public boolean hasNext() throws SearchException {
        try {
            next = is.read();
        } catch (IOException e) {
            throw new SearchException(e);
        }
        return next >= 0;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        try {
            is.close();
            closed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int next() throws SearchException {
        if (next < 0) {
            throw new SearchException("next < 0");
        }
        return next;
    }

}
