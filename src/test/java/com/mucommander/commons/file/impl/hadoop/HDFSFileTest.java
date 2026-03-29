package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.impl.sftp.SFTPFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class HDFSFileTest extends AbstractFileTest {
    @TempDir
    private static File tempDir;

    /** The system property that holds the URI to the temporary HDFS folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.hdfs_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    @BeforeAll
    public static void setupTemporaryFolder() {
        //tempFolder = FileFactory.getFile("hdfs://emoroozv@hbase01dev/user/emorozov");
		tempFolder = getTemporaryFolder(tempDir);
    }


    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(HDFSFileTest.class.getName()));
    }

    @Override
    protected Class <? extends AbstractFile> getTestFileClass() {
        return HDFSFile.class;
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
            FileOperation.GET_BLOCKSIZE,
            FileOperation.GET_REPLICATION,
            FileOperation.CHANGE_REPLICATION,
            FileOperation.GET_TOTAL_SPACE
        };
    }
}
