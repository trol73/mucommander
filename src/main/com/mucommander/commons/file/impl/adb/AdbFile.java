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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 09/09/15.
 */
public class AdbFile extends ProtocolFile {

    private final RemoteFile remoteFile;
    private List<RemoteFile> childs;
    private AbstractFile parent;
    private JadbConnection jadbConnection;

    private static FileURL lastModifiedPath;        // FIXME that's a bad way to detect directory changes

    /**
     * Creates a new file instance with the given URL.
     *
     * @param url the FileURL instance that represents this file's location
     */
    protected AdbFile(FileURL url, RemoteFile remoteFile) throws IOException {
        super(url);

        if (remoteFile == null) {
            JadbDevice device = getDevice(url);
            if (device == null) {
                throw new IOException("ADB file error");
            }

            String path = url.getPath();
            if (path.isEmpty() || "\\".equals(path)) {
                path = "/";
            }

            try {
                List<RemoteFile> files = device.list(path);
                childs = new ArrayList<>();
                for (RemoteFile rf : files) {
                    if (".".equals(rf.getPath())) {
                        remoteFile = rf;
                    } else {
                        childs.add(rf);
                    }
                }
            } catch (JadbException e) {
                e.printStackTrace();
            }
            closeConnection();
        } else {
            if (remoteFile.isDirectory()) {
                rebuildChildrenList(url);
            }
        }
        this.remoteFile = remoteFile;
    }

    private void rebuildChildrenList(FileURL url) throws IOException {
        try {
            JadbDevice device = getDevice(url);
            List<RemoteFile> files = device.list("/" + url.getPath());
            childs = new ArrayList<>();
            for (RemoteFile rf : files) {
                if (!".".equals(rf.getPath())) {
                    childs.add(rf);
                }
            }
        } catch (JadbException e) {
            e.printStackTrace();
        }
        closeConnection();
    }

    JadbDevice getDevice(FileURL url) throws IOException {
        closeConnection();
        jadbConnection = new JadbConnection();
        JadbDevice device = null;
        try {
            List<JadbDevice> devices = jadbConnection.getDevices();
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
        return device;
    }

    void closeConnection() throws IOException {
        if (jadbConnection != null) {
            jadbConnection.close();
            jadbConnection = null;
        }
    }


    protected AdbFile(FileURL url) throws IOException {
        this(url, null);
    }


    @Override
    public long getDate() {
        if (remoteFile == null) {
            return 0;
        }
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
        if (remoteFile == null) {
            return 0;
        }
        return remoteFile.getSize();
    }

    @Override
    public AbstractFile getParent() {
        if (parent == null && !"/".equals(getURL().getPath())) {
            try {
                parent = new AdbFile(getURL().getParent(), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {

    }

    @Override
    public boolean exists() {
        AdbFile adbParent = (AdbFile)getParent();
        if (adbParent == null) {
            String path = getURL().getPath();
            if ("/".equals(path)) {
                return true;
            }
            return false;
        }
        for (RemoteFile rf : adbParent.childs) {
            if (getName().equals(rf.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FilePermissions getPermissions() {
        return childs == null ? FilePermissions.DEFAULT_FILE_PERMISSIONS : FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS;
        // TODO !!!
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
        return remoteFile != null && remoteFile.isDirectory();
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
        if (getURL().equals(lastModifiedPath)) {
            rebuildChildrenList(lastModifiedPath);
            lastModifiedPath = null;
        }
        if (childs == null) {
            return null;
        }
        AbstractFile[] result = new AbstractFile[childs.size()-1];  // skip ".."
        int index = 0;
        for (RemoteFile rf : childs) {
            if ("..".equals(rf.getName())) {
                continue;
            }
            FileURL url = FileURL.getFileURL(getURL() + "/" + rf.getPath());
            AdbFile adbFile = new AdbFile(url, rf);
            adbFile.parent = this;
            result[index++] = adbFile;
        }
        return result;
    }

    @Override
    public void mkdir() throws IOException {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new AdbInputStream(this);
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
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        try {
            if (isDirectory()) {
                device.deleteDir(getURL().getPath());
            } else {
                device.delete(getURL().getPath());
            }
        } catch (JadbException e) {
            closeConnection();
            e.printStackTrace();
            throw new IOException(e);
        }
        // TODO    doesn't work without this delay    FIXME
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closeConnection();
        if (getParent() instanceof AdbFile) {
            AdbFile parent = (AdbFile)getParent();
            lastModifiedPath = parent.getURL();
            parent.rebuildChildrenList(parent.getURL());
        }
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


    @Override
    public boolean isFileOperationSupported(FileOperation op) {
        return op != FileOperation.WRITE_FILE && super.isFileOperationSupported(op);
    }

    public void pushTo(AbstractFile destFile) throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        try {
            device.pull(getURL().getPath(), destFile.getOutputStream());
        } catch (JadbException e) {
            throw new IOException(e);
        }
        closeConnection();
    }

    public void pullFrom(AbstractFile sourceFile) throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        long lastModified = sourceFile.getDate();
        int mode = 0664;
        try {
            device.push(sourceFile.getInputStream(), lastModified, mode, getURL().getPath());
        } catch (JadbException e) {
            closeConnection();
            e.printStackTrace();
            throw new IOException(e);
        }
        closeConnection();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (getParent() instanceof AdbFile) {
            AdbFile parent = (AdbFile)getParent();
            lastModifiedPath = parent.getURL();
            parent.rebuildChildrenList(parent.getURL());
        }
    }


}
