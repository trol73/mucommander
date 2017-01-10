/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.rar;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.mucommander.commons.file.AbstractFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * 
 * @author Arik Hadas
 */
public class RarFile {
	
    /** Interface to junrar library */
    private Archive archive;
    private AbstractFile file;

    RarFile(AbstractFile file) throws IOException, RarException {
        this.file = file;
        //try (InputStream fileIn = file.getInputStream()) {
        archive = new Archive(new File(file.getPath()));
        //}
    }


    public Collection<FileHeader> getEntries() {
    	return archive.getFileHeaders();
    }


    
    public InputStream getEntryInputStream(String path) throws IOException, RarException {
        // reopen archive to prevent crc error on reading if the file was reopened (issue for text files only)
        try {
            archive.close();
            archive = new Archive(new File(file.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    	final FileHeader header = findFileHeader(path);
        if (header == null) {
            return null;
        }
        return archive.getInputStream(header);


        // If the file that is going to be extracted is divided and continued in another archive
        // part - don't extract it and throw corresponding exception to raise an error.
//        if (header.isSplitAfter())
//    		throw new RarException(RarException.RarExceptionType.notImplementedYet);
//
//        final CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
//
//        new Thread(
//    		    new Runnable(){
//    		      public void run() {
//    		    	try {
//                        archive.extractFile(header, cbb.getOutputStream());
//					} catch (RarException e) {
//                        e.printStackTrace();
//					}
//    		    	finally {
//    		    		try {
//							cbb.getOutputStream().close();
//						} catch (IOException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//    		    	}
//    		      }
//    		    }
//    		  ).start();
//
//        return cbb.getInputStream();
    }


    private FileHeader findFileHeader(String path) {
        for (FileHeader h : archive.getFileHeaders()) {
            String fileName = h.getFileNameW().isEmpty() ? h.getFileNameString() : h.getFileNameW();
            if (fileName.equals(path)) {
                return h;
            }
        }
        return null;
    }
}
