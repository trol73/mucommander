/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package ru.trolsoft.ui;

import com.mucommander.commons.runtime.OsFamily;

import javax.swing.*;
import java.awt.*;

/**
 * JSeparator that fixed visibility issue on linux with Cinnamon
 *
 * @author Oleg Trifonov
 * Created on 21.09.16.
 */
public class TMenuSeparator extends JSeparator {

    public TMenuSeparator() {
        super();
        if (OsFamily.LINUX.isCurrent() && getPreferredSize().height <= 0) {
            Dimension d = getPreferredSize();
            d.height = 1;
            setPreferredSize(d);
        }
    }
}
