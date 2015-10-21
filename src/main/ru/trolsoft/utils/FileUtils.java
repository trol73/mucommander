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
package ru.trolsoft.utils;

import com.mucommander.commons.io.StreamUtils;
import java.io.*;
import java.net.URISyntaxException;
import org.raisercostin.jedi.FileLocation;
import org.raisercostin.jedi.Locations;

/**
 * Created on 08.01.15.
 * @author Oleg Trifonov
 */
public class FileUtils {

    public static void copyFileFromJar(String src, String dest) throws IOException {
        File fileDest = new File(dest);
        if (fileDest.exists() && fileDest.length() > 0) {
            return;
        }
        fileDest.getParentFile().mkdirs();
        InputStream is = FileUtils.class.getResourceAsStream(src);
        OutputStream os = new FileOutputStream(dest);
        StreamUtils.copyStream(is, os);
        is.close();
        os.close();
    }

    public static void copyJarFile(String name, String jarPath) throws IOException {
        final String outFile = jarPath + File.separatorChar + name;
        if (!new File(outFile).exists()) {
            FileUtils.copyFileFromJar('/' + name, outFile);
        }
    }


    public static String getJarPath() {
//        try {
        	FileLocation loc = Locations.file(System.getProperty("user.home")).child(".trolcommander").child("var");
        	loc.mkdirIfNecessary();
            //return new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        	//return new File(loc.absolute());
        	return loc.absolute();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            return null;
//        }
    }

}
