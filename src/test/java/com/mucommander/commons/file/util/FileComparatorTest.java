package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.impl.TestFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

/**
 * A test case for {@link FileComparator}.
 * @author Mariusz Jakubowski
 *
 */
public class FileComparatorTest {
  
    AbstractFile[] files;
    private TestFile A;
    private TestFile B;
    private TestFile C;
    private TestFile D;
    

    @BeforeEach
    protected void setUp() throws Exception {
        A = new TestFile(FileFactory.getTemporaryFolder() + "A",       false, 500, 1, null);
        B = new TestFile(FileFactory.getTemporaryFolder() + "B.e9.e1", true, 0, 2, null);
        C = new TestFile(FileFactory.getTemporaryFolder() + "C.e3",    false, 200, 3, null);
        D = new TestFile(FileFactory.getTemporaryFolder() + "D.e2",    true, 0, 4, null);
        files = new AbstractFile[] {C, D, A, B};
    }

    @Test
    public void testCompareNameDir() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, true, false,true));
        assertEquals(A, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(D, files[3]);
    }

    @Test
    public void testCompareNameDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, false, false,true));

        assertEquals(D, files[0]);
        assertEquals(C, files[1]);
        assertEquals(B, files[2]);
        assertEquals(A, files[3]);
    }

    @Test
    public void testCompareName() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, true, false,false));
        assert A.equals(files[0]);
        assert B.equals(files[1]);
        assert C.equals(files[2]);
        assert D.equals(files[3]);
    }

    @Test
    public void testCompareNameDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, false, false,false));
        assert D.equals(files[0]);
        assert C.equals(files[1]);
        assert B.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareSizeDir() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, true, false,true));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareSize() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, true, false,false));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareSizeDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, false, false,true));
        assertEquals(A, files[0]);  // 500
        assertEquals(C, files[1]);  // 200
        assertEquals(D, files[2]);  // DIR
        assertEquals(B, files[3]);  // DIR

    }

    @Test
    public void testCompareSizeDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, false, false,false));
        assert A.equals(files[0]);
        assert C.equals(files[1]);
        assert D.equals(files[2]);
        assert B.equals(files[3]);
    }

    @Test
    public void testCompareDateDir() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, true, false,true));
        assertEquals(A, files[0]);  // 1
        assertEquals(B, files[1]);  // 2
        assertEquals(C, files[2]);  // 3
        assertEquals(D, files[3]);  // 4
    }

    @Test
    public void testCompareDate() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, true, false,false));
        assertEquals(A, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(D, files[3]);
    }

    @Test
    public void testCompareDateDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, false, false,true));
        assertEquals(D, files[0]);  // 4
        assertEquals(C, files[1]);  // 3
        assertEquals(B, files[2]);  // 2
        assertEquals(A, files[3]);  // 1
    }

    @Test
    public void testCompareDateDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, false, false,false));
        assertEquals(D, files[0]);  // 4
        assertEquals(C, files[1]);  // 3
        assertEquals(B, files[2]);  // 2
        assertEquals(A, files[3]);  // 1
    }

    @Test
    public void testCompareExtDir() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, true, false,true));
        assertEquals(A, files[0]);  // A
        assertEquals(B, files[1]);  // B.e9.e1
        assertEquals(D, files[2]);  // D.e2
        assertEquals(C, files[3]);  // C.e3
    }

    @Test
    public void testCompareExt() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, true, false,false));
        assertEquals(A, files[0]);
        assertEquals(B, files[1]);
        assertEquals(D, files[2]);
        assertEquals(C, files[3]);
    }

    @Test
    public void testCompareExtDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, false, false,true));
        assertEquals(C, files[0]);  // C.e3
        assertEquals(D, files[1]);  // D.e2
        assertEquals(B, files[2]);  // B.e9.e1
        assertEquals(A, files[3]);  // A
    }

    @Test
    public void testCompareExtDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, false, false,false));
        assertEquals(C, files[0]);
        assertEquals(D, files[1]);
        assertEquals(B, files[2]);
        assertEquals(A, files[3]);
    }
    
}
