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
package com.mucommander.commons.file.impl.avrdude.files;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Trifonov
 * Created on 09/02/16.
 *
 * File hierarhy:
 *   DEVICE-NAME
 *    |- config.properties
 *    |- flash
 *    |   |- flash.bin
 *    |   |- flash.hex
 *    |- eeprom
 *    |   |- eeprom.bin
 *    |   |- eeprom.hex
 *    |- fuses
 *        |- fuses.bin
 *        |- fuses.hex
 *
 */
public abstract class AvrdudeFile extends ProtocolFile {

    private static final String STORAGE_DIR = "avr";
    protected static final String CONFIG_FILE_EXT = ".conf";

    protected final String path;
    protected final String name;

    AvrdudeFile(FileURL url, String path, String name) throws IOException {
        super(url);
        this.path = path;
        this.name = name;
        AbstractFile baseFolder = PlatformManager.getPreferencesFolder().getChild(STORAGE_DIR);
        if (!baseFolder.exists()) {
            baseFolder.mkdir();
        }
//        if (path.isEmpty() || path.equals("/") || path.equals("\\")) {
//
//        }
//        System.out.println("path " + path + " [" + getClass().getName());
//        System.out.println("baseFolder " + baseFolder + getClass().getName());
    }

    static AbstractFile getBaseFolder() throws IOException {
        AbstractFile baseFolder = PlatformManager.getPreferencesFolder().getChild(STORAGE_DIR);
        if (!baseFolder.exists()) {
            baseFolder.mkdir();
        }
        return baseFolder;
    }

    static AbstractFile[] getConfigFiles() throws IOException {
        return getBaseFolder().ls(new ExtensionFilenameFilter(CONFIG_FILE_EXT));
    }

    public String getDeviceName() {
        if (path == null) {
            return null;
        }
        int from = path.startsWith("/") || path.startsWith("\\") ? 1 : 0;
        int pos1 = path.indexOf('\\', from);
        int pos2 = path.indexOf('/', from);
        int pos;
        if (pos1 < 0 && pos2 < 0) {
            pos = -1;
        } else if (pos1 < 0) {
            pos = pos2;
        } else if (pos2 < 0) {
            pos = pos1;
        } else {
            pos = pos1 < pos2 ? pos1 : pos2;
        }
        return pos < 0 ? "" : path.substring(0, pos);
    }

    AbstractFile getLocalConfigFile() throws IOException {
System.out.println("::>"+getURL());
System.out.println("::>"+getBaseFolder());
System.out.println(getDeviceName() + CONFIG_FILE_EXT);
System.out.println("::>"+getBaseFolder().getChild(getDeviceName() + CONFIG_FILE_EXT));
        return getBaseFolder().getChild(getDeviceName() + CONFIG_FILE_EXT);
    }

    @Override
    public boolean isFileOperationSupported(FileOperation op) {
        if (op == FileOperation.CHANGE_DATE || op == FileOperation.CHANGE_PERMISSION) {
            return false;
        }
        return super.isFileOperationSupported(op);
    }

    @Override
    public long getDate() {
        return System.currentTimeMillis();
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

//    @Override
//    public boolean isDirectory() {
//        return false;
//    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
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
