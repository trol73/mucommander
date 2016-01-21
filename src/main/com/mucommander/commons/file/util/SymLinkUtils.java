/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Trifonov
 * Created on 10/11/14.
 */
public class SymLinkUtils {

    /**
     * Returns symbolic link target value
     * @param symLink symbolic link path
     * @return symbolic link target path
     */
    public static String getTargetPath(AbstractFile symLink) {
        Path path = FileSystems.getDefault().getPath(symLink.getAbsolutePath(), "");
        if (!Files.isSymbolicLink(path)) {
            return symLink.getAbsolutePath();
        }
        try {
            Path linkTargetPath = Files.readSymbolicLink(path);
            return linkTargetPath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return symLink.getAbsolutePath();
        }
    }

    public static boolean createSymlink(AbstractFile symLink, String target) {
        Path linkPath = FileSystems.getDefault().getPath(symLink.getAbsolutePath(), "");
        Path targetPath = FileSystems.getDefault().getPath(target, "");
        try {
            Files.createSymbolicLink(linkPath, targetPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param symLink symlink path
     * @param target target file/directory path
     * @throws IOException if an I/O error occurs.
     *      java.nio.file.AccessDeniedException
     *      java.nio.file.FileAlreadyExistsException
     */
    public static void createSymlink(String symLink, String target) throws IOException {
        Path linkPath = FileSystems.getDefault().getPath(symLink, "");
        Path targetPath = FileSystems.getDefault().getPath(target, "");
        Files.createSymbolicLink(linkPath, targetPath);
    }

    public static boolean editSymlink(AbstractFile symLink, String target) {
        Path linkPath = FileSystems.getDefault().getPath(symLink.getAbsolutePath(), "");
        Path targetPath = FileSystems.getDefault().getPath(target, "");
        try {
            Files.delete(linkPath);
            Files.createSymbolicLink(linkPath, targetPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


}
