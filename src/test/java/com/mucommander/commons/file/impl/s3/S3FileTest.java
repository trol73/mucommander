package com.mucommander.commons.file.impl.s3;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.impl.sftp.SFTPFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

/**
 * An {@link AbstractFileTest} implementation for the Amazon S3 file implementation.
 * The S3 temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
public class S3FileTest extends AbstractFileTest {
    @TempDir
    private static File tempDir;

    /** Base temporary folder */
    private static AbstractFile tempFolder;
    @BeforeAll
    public static void setupTemporaryFolder() {
        tempFolder = getTemporaryFolder(tempDir);
    }



    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(S3FileTest.class.getName()));
    }

    @Override
    protected Class <? extends AbstractFile> getTestFileClass() {
        return S3File.class;
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.COPY_REMOTELY,
        };
    }
}
