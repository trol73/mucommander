package com.mucommander.commons.file.impl.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsTest.class);

    @Test
    public void testWindows() {
        long start, end;
        
        start = System.currentTimeMillis();
        File[] fileRoots = File.listRoots();
        end = System.currentTimeMillis();
        LOGGER.info("roots : "+fileRoots.length);
        LOGGER.info("**** option 1 = " + (end - start));
        //  3/4 === 21000

        start = System.currentTimeMillis();
        FileStore[] stores = new FileStore[26];
        int count = 0;
        for (FileStore store: FileSystems.getDefault().getFileStores()) {
            stores[count++] = store;
        }
        end = System.currentTimeMillis();
        LOGGER.info("stores : "+count);
        for (int i=0; i<count; i++) {
            LOGGER.info(stores[i].name()+" "+stores[i].name());
        }
        LOGGER.info("**** option 2 = " + (end - start));
        //  3/4 === 126

        start = System.currentTimeMillis();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            LOGGER.info("path "+p);
        }
        end = System.currentTimeMillis();
        LOGGER.info("**** option 3 = " + (end - start));
        //  4/4 === 3

        start = System.currentTimeMillis();
        for (char c = 'A'; c <= 'Z'; ++c) {
            if (new File(c + ":").exists()) {
                LOGGER.info(c + ":");
            }
        }
        end = System.currentTimeMillis();
        LOGGER.info("**** option 4 = " + (end - start));
        //  3/4 === 170

        start = System.currentTimeMillis();
        try {
            Process theProcess = Runtime.getRuntime().exec("cmd /c wmic logicaldisk get caption");
            BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()));
            String line = null;
            while ((line = inStream.readLine()) != null) {
                LOGGER.info(line);
            }
        }
        catch (IOException e) {
            LOGGER.error("error", e);
        }
        end = System.currentTimeMillis();
        LOGGER.info("**** option 5 = " + (end - start));
        //  4/4 === 179

        // jni
    }

}
