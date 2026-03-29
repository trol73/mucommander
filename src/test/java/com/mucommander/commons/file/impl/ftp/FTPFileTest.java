package com.mucommander.commons.file.impl.ftp;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileOperation;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

/**
 * An {@link AbstractFileTest} implementation for {@link com.mucommander.commons.file.impl.ftp.FTPFile}.
 * The FTP temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
public class FTPFileTest extends AbstractFileTest {
    @TempDir
    private static File tempDir;

    /** The system property that holds the URI to the temporary FTP folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.ftp_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    static {
        // Attribute caching can be enabled or disabled, it doesn't matter, tests should pass in both cases
        // Todo: use JUnit's DataPoint to test both cases (with and without caching) but it requires Java 1.5's
        // annotations which we don't use for java 1.4 backward compatibility.
//        FTPFile.setAttributeCachingPeriod(5000);
    }
    @BeforeAll
    public static void setupTemporaryFolder() {
//        tempFolder = FileFactory.getFile(Files.createTempDir().getAbsolutePath());
        tempFolder = getTemporaryFolder(tempDir);
    }
    

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(FTPFileTest.class.getName()));
    }

    @Override
    protected Class <? extends AbstractFile> getTestFileClass() {
        return FTPFile.class;
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.WRITE_FILE,
            FileOperation.APPEND_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION
        };
    }
}
