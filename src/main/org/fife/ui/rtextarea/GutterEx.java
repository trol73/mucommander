/*
 * This file is part of trolCommander, http://www.trolsoft.ru/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package org.fife.ui.rtextarea;

/**
 * Child of org.fife.ui.rtextarea.Gutter with some public methods
 *
 * @author Oleg Trifonov
 * Created on 19/04/16.
 */
public class GutterEx extends Gutter {

    public GutterEx(RTextArea textArea) {
        super(textArea);
    }

    public void setIconRowHeaderEnabled(boolean enabled) {
        super.setIconRowHeaderEnabled(enabled);
    }

    public void setLineNumbersEnabled(boolean enabled) {
        super.setLineNumbersEnabled(enabled);
    }

    public void setTextArea(RTextArea textArea) {
        super.setTextArea(textArea);
    }
}
