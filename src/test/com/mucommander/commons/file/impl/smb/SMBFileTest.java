package com.mucommander.commons.file.impl.smb;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.IOException;

/**
 * An {@link AbstractFileTest} implementation for {@link com.mucommander.commons.file.impl.smb.SMBFile}.
 * The SMB temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
@Test
public class SMBFileTest extends AbstractFileTest {

    /** The system property that holds the URI to the temporary SMB folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.smb_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    static {
        // Configure jCIFS for maximum compatibility
        SMBProtocolProvider.setSmbLmCompatibility(0);
        SMBProtocolProvider.setExtendedSecurity(false);

        // Turn off attribute caching completely, otherwise tests will fail
        SMBFile.setAttributeCachingPeriod(0);
    }
    @BeforeClass()
    public static void setupTemporaryFolder() {
        tempFolder = getTemporaryFolder(TEMP_FOLDER_PROPERTY);
    }



    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(SMBFileTest.class.getName()));
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.WRITE_FILE,
            FileOperation.APPEND_FILE,
            FileOperation.RANDOM_WRITE_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.COPY_REMOTELY,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION,
            FileOperation.GET_FREE_SPACE,
        };
    }
}
