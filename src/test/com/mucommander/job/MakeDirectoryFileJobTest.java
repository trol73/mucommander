package com.mucommander.job;

import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import java.io.File;
import org.mockito.Mockito;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class MakeDirectoryFileJobTest {

    MakeDirectoryFileJob makeDirectoryFileJob;
    private File file;

    @BeforeClass
    public void setup() throws Exception {

        //Uses localized strings
        try {
            Translator.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Mock object for first parameter
        ProgressDialog progressDialog = Mockito.mock(ProgressDialog.class);

        //Mock object for second parameter
        MainFrame mainFrame = Mockito.mock(MainFrame.class);

        //The third parameter
        FileSet fileSet = new FileSet();

        makeDirectoryFileJob = new MakeDirectoryFileJob(progressDialog, mainFrame, fileSet, -1, false);
    }
    @Test
    public void testProcessFile() {
        
        //Create a temp file
        String tempDir = System.getProperty("java.io.tmpdir");
        String tempFile = tempDir + "tmpfile_lKw2fhKkWt.tmp";
        //Gives the tempFile
        makeDirectoryFileJob.processFile(FileFactory.getFile(tempFile), null);
        file = new File(tempFile);
        //Tests if the file exists
        assertTrue(file.exists());       
    }
    //Deletes the file created
    @AfterClass
    public void afterClass(){
        file.delete();
    }

}
