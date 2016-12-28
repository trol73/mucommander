package com.mucommander.commons.file;

import com.mucommander.commons.file.impl.local.LocalFileTest;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * A test case for {@link com.mucommander.commons.file.SimpleFileAttributes}.
 *
 * @see com.mucommander.commons.file.SimpleFileAttributes
 * @author Maxence Bernard
 */
public class SimpleFileAttributesTest {

    /**
     * Creates a SimpleFileAttributes instance from an AbstractFile and ensures that the values returned by
     * SimpleFileAttributes' getters match those of AbstractFile.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testAccessors() throws IOException {
        LocalFileTest lft = new LocalFileTest();

        // File doesn't exist
        AbstractFile tempFile = lft.getTemporaryFile();
        assertAttributesMatch(tempFile, new SimpleFileAttributes(tempFile));

        // File exists as a regular file
        tempFile.mkfile();
        assertAttributesMatch(tempFile, new SimpleFileAttributes(tempFile));

        // File exists as a directory
        tempFile.delete();
        tempFile.mkdir();
        assertAttributesMatch(tempFile, new SimpleFileAttributes(tempFile));
    }

    /**
     * Asserts that the attributes of the given AbstractFile and SimpleFileAttributes match.
     */
    private void assertAttributesMatch(AbstractFile file, SimpleFileAttributes attrs) {
        assert file.getAbsolutePath().equals(attrs.getPath());
        assert file.exists() == attrs.exists();
        assert file.getLastModifiedDate() == attrs.getLastModifiedDate();
        assert file.getSize() == attrs.getSize();
        assert file.isDirectory() == attrs.isDirectory();
        assert file.getPermissions() == attrs.getPermissions();
        assert file.getOwner() == null ? attrs.getOwner() == null : file.getOwner().equals(attrs.getOwner());
        assert file.getGroup() == null ? attrs.getGroup() == null : file.getGroup().equals(attrs.getGroup());
    }

    /**
     * Creates a SimpleFileAttributes instance with the no-arg constructor and ensures that the default values returned
     * by SimpleFileAttributes' getters are as specified by {@link FileAttributes}.
     */
    @Test
    public void testDefaultValues() {
        SimpleFileAttributes attrs = new SimpleFileAttributes();
        assert attrs.getPath() == null;
        assert !attrs.exists();
        assert 0 == attrs.getLastModifiedDate();
        assert 0 ==  attrs.getSize();
        assert !attrs.isDirectory();
        assert attrs.getPermissions() == null;
        assert attrs.getOwner() == null;
        assert attrs.getGroup() == null;
    }
}
