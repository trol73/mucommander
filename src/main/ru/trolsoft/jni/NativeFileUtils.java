/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2018 Oleg Trifonov
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
package ru.trolsoft.jni;

import ru.trolsoft.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class NativeFileUtils {

    private static final int VERSION = 1;

    public static final int FA_MASK_EXISTS = 1;
    public static final int FA_MASK_DIRECTORY =	2;
    public static final int FA_MASK_HIDDEN = 4;


    private static boolean init = false;
    private static boolean installed = false;


    private static void prepareLibrary(boolean overwrite) {
        String jarPath = FileUtils.getJarPath();
        try {
            FileUtils.copyJarFile("libtrolsoft.jnilib", jarPath, overwrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean init() {
        if (!init) {
            init = true;
            prepareLibrary(false);
            try {
                String path = FileUtils.getJarPath() + File.separator;
                //System.loadLibrary("trolsoft");
                System.load(path + "libtrolsoft.jnilib");
                installed = true;
                if (getLibraryVersion() < VERSION) {
                    prepareLibrary(true);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.out.println("java.library.path=" + System.getenv("java.library.path"));
                installed = false;
            }
        }
        return installed;
    }

    native private static int getLibraryVersion();

    native public static int getLocalFileAttributes(String path);

    native public static boolean isLocalFileHidden(String path);

    native public static boolean isLocalDirectory(String path);

    native public static boolean isLocalFileExecutable(String path);
}
