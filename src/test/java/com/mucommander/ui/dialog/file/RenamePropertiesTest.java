package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class RenamePropertiesTest {

    @Test
    public void testRenameFile() throws IOException {
    	String filename = "testRenamefile";
        String newFilename = "testNewnamefile";
        
    //creates a temp file on the drive
        
        File tmpFile = File.createTempFile(filename, ".tmp");
        
        //creates a file object that muCommander can understand
        AbstractFile abstractFile = FileFactory.getFile(tmpFile.getAbsolutePath());

        //runs the rename file methode from the PropertiesDialog class
        PropertiesDialog.renameFile(abstractFile, newFilename + ".tmp");
        
        //creates a standard Java fil object from the muCommander file that was created before, this is the renamed file
        File newFile = new File(abstractFile.getParent().getAbsolutePath() + newFilename + ".tmp");
        //checks if the file exists using the exists() methode. 
        assertTrue(newFile.exists()); 
        // cleans out the file from the drive after the test
        newFile.delete();
    }

}