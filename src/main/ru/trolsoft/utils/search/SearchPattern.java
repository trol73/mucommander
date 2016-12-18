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
package ru.trolsoft.utils.search;

/**
 * @author Oleg Trifonov
 * Created on 16/11/14.
 */
public interface SearchPattern {

    /**
     *
     * @return length of the search pattern
     */
    int length();

    /**
     *
     * @param index offset in pattern
     * @param val compared value
     * @return true if pattern[index] == val
     */
    boolean checkByte(int index, int val);


    boolean checkSelf(int index1, int index2);
}
