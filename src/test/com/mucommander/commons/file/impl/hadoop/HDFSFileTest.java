package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.IOException;

/**
 * @author Maxence Bernard
 */
@Test
public class HDFSFileTest extends AbstractFileTest {

    /** The system property that holds the URI to the temporary HDFS folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.hdfs_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    @BeforeClass
    public static void setupTemporaryFolder() {
        //tempFolder = FileFactory.getFile("hdfs://emoroozv@hbase01dev/user/emorozov");
		tempFolder = getTemporaryFolder(TEMP_FOLDER_PROPERTY);
    }



    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(HDFSFileTest.class.getName()));
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.WRITE_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION,
        };
    }
}
