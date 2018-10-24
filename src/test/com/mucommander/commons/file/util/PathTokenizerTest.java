package com.mucommander.commons.file.util;

import org.testng.annotations.Test;

/**
 * Runs tests on {@link PathTokenizer}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class PathTokenizerTest {
    @Test
    public void testEmtpy() {
        test("");
    }

    @Test
    public void testUnknown() {
        test("blah");
    }

    @Test
    public void testComplexMixed() {
        test("/C:\\\\this///is/not\\\\a\\valid//path//but/we\\let//it\\parse/");
    }

    @Test
    public void testSimpleMixed() {
        test("/C:\\temp");
    }

    @Test
    public void testWindowsRootWithoutTrailingSeparator() {
        test("C:");
    }

    @Test
    public void testWindowsRootWithTrailingSeparator() {
        test("C:\\");
    }

    @Test
    public void testWindowsWithTrailingSeparator() {
        test("C:\\temp\\");
    }

    @Test
    public void testWindowsWithoutTrailingSeparator() {
        test("C:\\temp");
    }

    @Test
    public void testUnixWithTrailingSeparator() {
        test("/Users/maxence/Temp/");
    }

    @Test
    public void testUnixWithoutTrailingSeparator() {
        test("/Users/maxence/Temp");
    }

    @Test
    public void testUnixRoot() {
        test("/");
    }

    private static void test(String path) {
        for(boolean reverseOrder=false; ; reverseOrder=true) {
            PathTokenizer pt = new PathTokenizer(path, PathTokenizer.DEFAULT_SEPARATORS, reverseOrder);
            
            String reconstructedPath = pt.getLastSeparator();

            while(pt.hasMoreFilenames()) {
                String nextToken = pt.nextFilename();
                String lastSeparator = pt.getLastSeparator();

                if(!reverseOrder)
                    reconstructedPath += nextToken+lastSeparator;
            }

            assert reverseOrder || reconstructedPath.equals(path);

            if(reverseOrder)
                break;
        }
    }
}
