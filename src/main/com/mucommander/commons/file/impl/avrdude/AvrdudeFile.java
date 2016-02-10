/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
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

import com.mucommander.commons.file.*;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Trifonov
 * Created on 09/02/16.
 */
public class AvrdudeFile extends ProtocolFile {

    protected AvrdudeFile(FileURL url) {
        super(url);
    }

    @Override
    public long getDate() {
        return 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException {

    }

    @Override
    public void changeReplication(short replication) throws IOException {

    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public AbstractFile getParent() {
        return null;
    }

    @Override
    public void setParent(AbstractFile parent) {

    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public FilePermissions getPermissions() {
        return null;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return null;
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException {

    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public short getReplication() throws UnsupportedFileOperationException {
        return 0;
    }

    @Override
    public long getBlocksize() throws UnsupportedFileOperationException {
        return 0;
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return new AbstractFile[0];
    }

    @Override
    public void mkdir() throws IOException {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException {
        return null;
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return null;
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        return null;
    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {

    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException {

    }

    @Override
    public long getFreeSpace() throws IOException {
        return 0;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return 0;
    }

    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }
}
