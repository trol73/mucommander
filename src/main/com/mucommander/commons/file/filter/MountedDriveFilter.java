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
package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;

/**
 * @author Oleg Trifonov
 * Created on 26/01/16.
 */
public class MountedDriveFilter extends AbstractFileFilter {
    @Override
    public boolean accept(AbstractFile file) {
        if (file == null || !OsFamily.MAC_OS_X.isCurrent()) {
            return false;
        }
        for (AbstractFile f : LocalFile.getVolumes()) {
            if (file.equals(f)) {
                return !file.isSymlink();
            }
        }
        return false;
    }
}
