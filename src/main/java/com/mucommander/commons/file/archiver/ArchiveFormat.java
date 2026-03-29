/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package com.mucommander.commons.file.archiver;

/**
 * @author Oleg Trifonov
 * Created on 15/03/17.
 */
public enum ArchiveFormat {
    ZIP("Zip", "zip", true),
    GZ("Gzip", "gz", false),
    BZ2("Bzip2", "bz2", false),
    TAR("Tar", "tar", true),
    TAR_GZ("Tar/Gzip", "tar.gz", true),
    TAR_BZ2("Tar/Bzip2", "tar.bz2", true);
//    ISO("ISO", "iso", true);

    /**
     * The name of the given archive format, can be used for display in a GUI.
     */
    public final String name;
    /**
     * The default archive format extension. Note: some formats such as Tar/Gzip have several common
     * extensions (e.g. tar.gz or tgz), the most common one will be returned
     */
    public final String ext;
    /**
     * true if the format used by this Archiver can store more than one entry
     */
    public final boolean supportManyEntries;

    ArchiveFormat(String name, String ext, boolean supportManyEntries) {
        this.name = name;
        this.ext = ext;
        this.supportManyEntries = supportManyEntries;
    }
}
