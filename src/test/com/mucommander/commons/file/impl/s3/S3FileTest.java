package com.mucommander.commons.file.impl.s3;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.IOException;

/**
 * An {@link AbstractFileTest} implementation for the Amazon S3 file implementation.
 * The S3 temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
@Test
public class S3FileTest extends AbstractFileTest {

    /** The system property that holds the URI to the temporary S3 folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.s3_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;
    @BeforeClass()
    public static void setupTemporaryFolder() {
        tempFolder = getTemporaryFolder(TEMP_FOLDER_PROPERTY);
    }


    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(S3FileTest.class.getName()));
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
