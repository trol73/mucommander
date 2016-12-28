package com.mucommander.commons.file;

import org.testng.annotations.Test;

import java.io.IOException;

/**
 * A test case for {@link FileFactory}.
 *
 * @author Maxence Bernard
 */
public class FileFactoryTest {

    /**
     * Tests {@link com.mucommander.commons.file.FileFactory#getTemporaryFolder()}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testTemporaryFolder() throws IOException {
        // Assert that the returned file is a folder that exists
        AbstractFile temporaryFolder = FileFactory.getTemporaryFolder();
        assert temporaryFolder != null;
        assert temporaryFolder.isDirectory();
        assert temporaryFolder.exists();

        // Assert that the temporary folder is the parent folder of temporary files
        AbstractFile temporaryFile = FileFactory.getTemporaryFile(false);
        assert temporaryFile.getParent().equals(temporaryFolder);
    }

    /**
     * Tests {@link com.mucommander.commons.file.FileFactory#getTemporaryFile(String, boolean)}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testTemporaryFiles() throws IOException {
        String desiredName = System.currentTimeMillis()+".ext";

        // Assert that #getTemporaryFile returns a non-existing file with the desired name
        AbstractFile temporaryFile1 = FileFactory.getTemporaryFile(desiredName, true);
        assert temporaryFile1 != null;
        assert !temporaryFile1.exists();
        assert desiredName.equals(temporaryFile1.getName());

        // Assert that #getTemporaryFile returns a new temporary file if the requested file already exists, and that the
        // extension matches the desired one.
        temporaryFile1.mkfile();

        AbstractFile temporaryFile2 = FileFactory.getTemporaryFile(desiredName, true);
        assert temporaryFile2 != null;
        assert !temporaryFile2.exists();
        assert !temporaryFile2.getName().equals(desiredName);
        assert temporaryFile1.getExtension().equals(temporaryFile2.getExtension());

        // Note: the temporary file should normally be deleted on VM shutdown, but we have no (easy) way to assert that

        // Perform some basic tests on #getTemporaryFile when called without a desired name
        temporaryFile1 = FileFactory.getTemporaryFile(true);
        assert temporaryFile1 != null;
        assert !temporaryFile1.exists();
    }
}
