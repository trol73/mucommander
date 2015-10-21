package com.mucommander.share.impl.imgur;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.share.impl.imgur.ImgurAPI.Callback;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Mathias
 */
public class ImgurJobTest {

    private ImgurAPI mockImgurAPI;
    private ProgressDialog mockProgressDialog;
    private MainFrame mockMainFrame;
    private FileSet mockFiles;
    private Future mockFuture;

    private ImgurJob instance;

    public ImgurJobTest() {
    }

    @BeforeClass
    public void setUpClass() throws Exception {

        //Uses localized strings
        try {
            Translator.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Mock Future
        mockFuture = mock(Future.class);
        when(mockFuture.isDone()).thenReturn(true);

        //Mock ImgurAPI
        mockImgurAPI = mock(ImgurAPI.class);
        when(mockImgurAPI.uploadAsync(any(File.class), any(Callback.class))).thenReturn(mockFuture);

        //Mock ProgressDialog
        mockProgressDialog = mock(ProgressDialog.class);

        //Mock MainFrame
        mockMainFrame = mock(MainFrame.class);

        //Mock FileSet
        mockFiles = mock(FileSet.class);

    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new ImgurJob(mockImgurAPI, mockProgressDialog, mockMainFrame, mockFiles);
    }


    /**
     * Test of hasFolderChanged method, of class ImgurJob.
     */
    @Test
    public void testHasFolderChanged() {

        System.out.println("hasFolderChanged");
        AbstractFile folder = mock(AbstractFile.class);

        boolean expResult = false;
        boolean result = instance.hasFolderChanged(folder);
        assertEquals(result, expResult);
    }

    @Test
    public void successLocalFile() throws IOException {

        File tmp = File.createTempFile("test", ".tmp");
        tmp.deleteOnExit();

        AbstractFile file = FileFactory.getFile(tmp.getAbsolutePath());

        boolean result = instance.processFile(file, null);
        boolean expResult = true;
        assertEquals(result, expResult);
    }

    /**
     * Test of getStatusString method, of class ImgurJob.
     */
    @Test
    public void testGetStatusString() {
        System.out.println("getStatusString");

        String status = instance.getStatusString();
        assertNotNull(status);
        assertTrue(!status.equals(""));
    }

}
