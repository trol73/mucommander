/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
package com.mucommander.commons.file.impl.adb;

import com.mucommander.commons.file.*;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 09/09/15.
 */
public class AdbFile extends ProtocolFile {

    private final RemoteFile remoteFile;

    /**
     * Creates a new file instance with the given URL.
     *
     * @param url the FileURL instance that represents this file's location
     */
    protected AdbFile(FileURL url, RemoteFile remoteFile) throws IOException {
        super(url);

        if (remoteFile == null) {
            JadbConnection connection = new JadbConnection();
            JadbDevice device = null;
            try {
                List<JadbDevice> devices = connection.getDevices();
                final String host = url.getHost();
                for (JadbDevice dev : devices) {
                    if (dev.getSerial().equalsIgnoreCase(host)) {
                        device = dev;
                        break;
                    }
                }
            } catch (JadbException e) {
                e.printStackTrace();
            }
            final String path = url.getPath();
            try {
                System.out.println(device.list("/"));
            } catch (JadbException e) {
                e.printStackTrace();
            }
            System.out.println("device " + device);
            System.out.println("url " + url);
            System.out.println("fileName " + url.getFilename());
            System.out.println("path " + url.getPath());
            System.out.println("host " + url.getHost());
        }
        this.remoteFile = remoteFile;
    }


    protected AdbFile(FileURL url) throws IOException {
        this(url, null);
    }


    @Override
    public long getDate() {
        return remoteFile.getLastModified();
    }

    @Override
    public void changeDate(long lastModified) throws IOException {
    }

    @Override
    public void changeReplication(short replication) throws IOException {

    }

    @Override
    public long getSize() {
        return remoteFile.getSize();
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
        return true;
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
        return remoteFile.isDirectory();
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
