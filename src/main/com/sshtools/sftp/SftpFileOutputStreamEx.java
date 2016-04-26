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
package com.sshtools.sftp;

import com.sshtools.ssh.SshException;

/**
 * Modified SftpFileOutputStream with position argument in constructor
 *
 * @author Oleg Trifonov
 * Created on 26/04/16.
 */
public class SftpFileOutputStreamEx extends SftpFileOutputStream {

    public SftpFileOutputStreamEx(SftpFile file, long pos) throws SftpStatusException, SshException {
        super(file);
        this.position = pos;
    }
}
