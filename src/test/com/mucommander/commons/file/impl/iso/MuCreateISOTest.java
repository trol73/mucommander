/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mucommander.commons.file.impl.iso;

import com.github.stephenc.javaisotools.eltorito.impl.ElToritoConfig;
import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.google.common.io.Files;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class MuCreateISOTest {
	private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MuCreateISOTest.class);
    private static File tempFile1 = null;
    private static File tempFile2 = null;
    private static File tempFile3 = null;
    private static File tempDir1;
    private static HashMap<String, File> files = new HashMap();
    private static File archiveFile = null;
    private static MuCreateISO instance = null;
    
    public MuCreateISOTest(){
        
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        //Setup testing files and dirs
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        tempFile1 = createTempFile("tempFile1",10000);
        root.addFile(tempFile1);
        files.put(tempFile1.getName(), tempFile1);
        tempFile1.deleteOnExit();
        
        tempFile2 = createTempFile("tempFile2",20000);
        root.addFile(tempFile2);
        files.put(tempFile2.getName(), tempFile2);
        tempFile2.deleteOnExit();
        
        tempDir1 = Files.createTempDir();
        ISO9660Directory dir = root.addDirectory(tempDir1);
        tempDir1.deleteOnExit();
        
        tempFile3 = createTempFile("tempFile3",40000);
        dir.addFile(tempFile3);
        files.put(tempDir1.getName() + File.separator + tempFile3.getName(), tempFile3);
        tempFile3.deleteOnExit();
        
        //Create archive file
        archiveFile = File.createTempFile("MuCreateISOTest", ".iso");
        archiveFile.deleteOnExit();
        
        //Setup iso archiver
        ISO9660Config iso9660Config = new ISO9660Config();
        try {
            iso9660Config.allowASCII(false);
            iso9660Config.setInterchangeLevel(1);
            iso9660Config.restrictDirDepthTo8(false);
            iso9660Config.setPublisher(System.getProperty("user.name"));
            iso9660Config.setVolumeID(archiveFile.getName());
            iso9660Config.setDataPreparer(System.getProperty("user.name"));
            iso9660Config.forceDotDelimiter(true);
        } catch (ConfigException ex) {
            logger.error("", ex);
        }
        
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(false);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);
        
        JolietConfig jolietConfig = new JolietConfig();
        try {
            if(iso9660Config.getPublisher() instanceof String){
                jolietConfig.setPublisher((String) iso9660Config.getPublisher());
            } else {
                try {
                    jolietConfig.setPublisher((File) iso9660Config.getPublisher());
                } catch (HandlerException ex) {
                    logger.error("",ex);
                }
            } 
            jolietConfig.setVolumeID(iso9660Config.getVolumeID());
            jolietConfig.forceDotDelimiter(true);
        } catch (ConfigException ex) {
        	logger.error("",ex);
        }
        
        ElToritoConfig elToritoConfig = null;
        
        instance = new MuCreateISO(new ISOImageFileHandler(archiveFile), root);
        //testProcess will check if it actually did it correctly, but other tests 
        //need it to also have been ran
        instance.process(iso9660Config, rrConfig, jolietConfig, elToritoConfig);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        for(File file : files.values()){
            file.delete();
        }
        archiveFile.delete();
        files.clear();
        instance = null;
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    //Create a temp file with data within the ASCII range
    public static File createTempFile(String name, int fileSize){
        File file = null;
        try {
            file = File.createTempFile(name, "test");
            //Make sure data is always the same
            Random random = new Random(fileSize);
            //Generate data to be filled into the file
            byte[] chars = new byte[fileSize];
            random.nextBytes(chars);
            
            PrintWriter pw = new PrintWriter(file);

            for (byte aChar : chars) {
                //Since java use signed bytes, add 128 in order to
                //get a byte range from 0 to 255
                pw.write(aChar + 128);
            }
            pw.flush();
            pw.close();
            
        } catch (IOException ex) {
        	logger.error("",ex);
        }
        return file;
    }
    
    

    /**
     * Test of process method, of class MuCreateISO.
     */
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");
        //complete check of file content
        IsoArchiveFile archive = new IsoArchiveFile(FileFactory.getFile(archiveFile.getPath()));
        
        for(String fileName : files.keySet()){
            System.out.println("Testing: "+fileName);
            //File that should be saved in the archive
            AbstractFile archiveEntryFile = archive.getArchiveEntryFile(fileName);
            //See if file actually exists
            assert archiveEntryFile.exists();
            //Archive entry inputstream
            FileInputStream fis = new FileInputStream(files.get(fileName));
            InputStream is = archiveEntryFile.getInputStream();
            //See if file content is equal
            Assert.assertEquals(is.available(), fis.available());
            while(fis.available() > 0){
                //See if data is identical
                Assert.assertEquals(is.read(), fis.read());
            }
        }
    }

    /**
     * Test of getProcessingFile method, of class MuCreateISO.
     */
    @Test
    public void testGetProcessingFile() throws Exception {
        System.out.println("getProcessingFile");
        
        //Can't be sure which file is is
        boolean found = false;
        for(File file : files.values()){
            if(file.getName().equals(instance.getProcessingFile())){
                found = true;
            }
        }
        assert found;
    }

    /**
     * Test of totalWrittenBytes method, of class MuCreateISO.
     */
    @Test
    public void testTotalWrittenBytes() throws Exception {
        System.out.println("totalWrittenBytes");
        
        long totalSize = 0;
        for(File file : files.values()){
            totalSize += file.length();
        }
        Assert.assertEquals(instance.totalWrittenBytes(), totalSize);
    }

    @Test
    public void testWrittenBytesCurrentFile() throws Exception {
        System.out.println("writtenBytesCurrentFile");
        
        //Can't be sure which file is is
        boolean found = false;
        for(File file : files.values()){
            if(
                    file.getName().equals(instance.getProcessingFile()) 
                 && file.length() == instance.writtenBytesCurrentFile())
            {
                found = true;
            }
        }
        assert found;
    }

    /**
     * Test of currentFileLength method, of class MuCreateISO.
     */
    @Test
    public void testCurrentFileLength() {
        System.out.println("currentFileLength");
        
        //Can't be sure which file is is
        boolean found = false;
        for(File file : files.values()){
            if(
                    file.getName().equals(instance.getProcessingFile()) 
                 && file.length() == instance.currentFileLength())
            {
                found = true;
            }
        }
        assert found;
    }
    
}
