package com.mucommander.share.impl.imgur;

import com.google.common.collect.Sets;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.FileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import java.util.Set;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Mathias
 */
public class ImgurProviderTest {
    
    private ImgurProvider instance;
    
    public ImgurProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //Uses localized strings
        try {
            Translator.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new ImgurProvider();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void checkSupportedFiletypes() {
        System.out.println("checkSupportedFiletypes");
        Set<String> extensions = Sets.newHashSet("JPEG", "PNG", "BMP");
        
        boolean expResult = true;
        boolean result = instance.supportsFiletypes(extensions);
        assertEquals(result, expResult);
    }
    
    @Test
    public void checkUnsupportedFiletypes() {
        System.out.println("checkUnsupportedFiletypes");
        Set<String> extensions = Sets.newHashSet("INK", "TMP", "TXT");
        
        boolean expResult = false;
        boolean result = instance.supportsFiletypes(extensions);
        assertEquals(result, expResult);
    }
    
    @Test
    public void checkMixedFiletypes() {
        System.out.println("checkMixedFiletypes");
        Set<String> extensions = Sets.newHashSet("JPEG", "TMP", "TXT");
        
        boolean expResult = true;
        boolean result = instance.supportsFiletypes(extensions);
        assertEquals(result, expResult);
    }

    @Test
    public void testSupportedFiletype() {
        System.out.println("testSupportedFiletype");
        String extension = "JPEG";
        boolean expResult = true;
        boolean result = instance.supportsFiletype(extension);
        assertEquals(result, expResult);
    }
    
    @Test
    public void testUnsupportedFiletype() {
        System.out.println("testUnsupportedFiletype");
        String extension = "TXT";
        boolean expResult = false;
        boolean result = instance.supportsFiletype(extension);
        assertEquals(result, expResult);
    }
    
    @Test
    public void testCasesensitity() {
        System.out.println("testUnsupportedFiletype");
        
        String extension = "TXT";
        boolean result1 = instance.supportsFiletype(extension.toLowerCase());
        boolean result2 = instance.supportsFiletype(extension.toUpperCase());
        assertEquals(result1, result2);
    }
    
    @Test
    public void testCasesensitity2() {
        System.out.println("testUnsupportedFiletype2");
        
        String extension = "JPEG";
        boolean result1 = instance.supportsFiletype(extension.toLowerCase());
        boolean result2 = instance.supportsFiletype(extension.toUpperCase());
        assertEquals(result1, result2);
    }

    /**
     * Test of getDisplayName method, of class ImgurProvider.
     */
    @Test
    public void testGetDisplayName() {
        System.out.println("getDisplayName");
        String expResult = "imgur.com";
        String result = instance.getDisplayName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getJob method, of class ImgurProvider.
     */
    @Test
    public void testGetJob() {
        System.out.println("getJob");
        ProgressDialog progressDialog = mock(ProgressDialog.class);
        MainFrame mainFrame = mock(MainFrame.class);
        FileSet selectedFiles = mock(FileSet.class);
        
        FileJob result = instance.getJob(progressDialog, mainFrame, selectedFiles);
        assertNotNull(result);
    }

    /**
     * Test of getImgurAPI method, of class ImgurProvider.
     */
    @Test
    public void testGetImgurAPI() {
        System.out.println("getImgurAPI");

        ImgurAPI result = instance.getImgurAPI();
        assertNotNull(result);
    }
    
}
