/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mucommander.commons.file.archiver;

import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.google.common.io.Files;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.impl.iso.IsoArchiveFile;
import com.mucommander.commons.file.impl.iso.MuCreateISOTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jeppe
 */
public class ISOArchiverNGTest {
    private static File tempFile1 = null;
    private static File tempFile2 = null;
    private static File tempFile3 = null;
    private static File tempDir1;
    private static HashMap<String, File> files = new HashMap();
    private static File archiveFile = null;
    private static ISOArchiver instance = null;
    
    public ISOArchiverNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
         //Create a archive
        
        tempFile1 = MuCreateISOTest.createTempFile("tempFile1",10000);
        files.put(tempFile1.getName(), tempFile1);
        tempFile1.deleteOnExit();
        
        tempFile2 = MuCreateISOTest.createTempFile("tempFile2",20000);
        files.put(tempFile2.getName(), tempFile2);
        tempFile2.deleteOnExit();
        
        tempDir1 = Files.createTempDir();
        tempDir1.deleteOnExit();
        
        tempFile3 = MuCreateISOTest.createTempFile("tempFile3",40000);
        files.put(tempDir1.getName() + File.separator + tempFile3.getName(), tempFile3);
        tempFile3.deleteOnExit();
        
        archiveFile = File.createTempFile("MuCreateISOTest", ".iso");
        archiveFile.deleteOnExit();
        
        AbstractFile abstractArchiveFile = FileFactory.getFile(archiveFile.getPath());
        instance = new ISOArchiver(abstractArchiveFile);
        
        instance.createEntry(tempDir1.getName(), FileFactory.getFile(tempDir1.getPath()));
        for(String filePath : files.keySet()){
            instance.createEntry(filePath, FileFactory.getFile(files.get(filePath).getPath()));
        }
        
        //Archive the files
        instance.postProcess();
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

    /**
     * Test of createEntry method, of class ISOArchiver.
     */
    @Test
    public void testCreateEntry() throws Exception {
        
        //Get access to method in order to test if it correctly listed the files
        Method method = instance.getClass().getDeclaredMethod("getParentDirectory", String.class);
        method.setAccessible(true);
        
        for(String filePath : files.keySet()){
            String[] split = filePath.split("\\\\");
            StringBuilder path = new StringBuilder();
            path.append(split[0]);
            for(int i = 0; i < split.length; i++){
                if(i == 0){
                    Object invoke = method.invoke(instance, path.toString());
                    //assert it is root dir
                    assert invoke instanceof ISO9660RootDirectory;
                } else {
                    path.append(File.separator).append(split[i]);
                    Object invoke = method.invoke(instance, path.toString());
                    //assert it is not root dir
                    assert !(invoke instanceof ISO9660RootDirectory);
                    assert invoke instanceof ISO9660Directory;
                    ISO9660Directory dir = (ISO9660Directory) invoke;
                    //assert the name is correct of the parent directory
                    Assert.assertEquals(dir.getName(),split[i-1]);
                }
            }
        }
    }

    /**
     * Test of getProcessingFile method, of class ISOArchiver.
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
     * Test of totalWrittenBytes method, of class ISOArchiver.
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

    /**
     * Test of testWrittenBytesCurrentFile method, of class ISOArchiver.
     */
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
     * Test of currentFileLength method, of class ISOArchiver.
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

    /**
     * Test of postProcess method, of class ISOArchiver.
     */
    @Test
    public void testPostProcess() throws Exception {
        //instance.postProcess(); was called in class setup
        
        //complete check of file content
        IsoArchiveFile archive = new IsoArchiveFile(FileFactory.getFile(archiveFile.getPath()));
        
        for(String fileName : files.keySet()){
            System.out.println("Testing: "+fileName);
            //File that should be saved in the archive
            AbstractFile archiveEntryFile = archive.getArchiveEntryFile(fileName);
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
     * Test of close method, of class ISOArchiver.
     */
//    @Test
    public void testClose() throws Exception {
    }
    
}
