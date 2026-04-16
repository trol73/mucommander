package com.mucommander.commons.file.impl.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class WindowsTest {

    @Test
    public void testWindows() {
        long start, end;
        
        start = System.currentTimeMillis();
        File[] fileRoots = File.listRoots();
        end = System.currentTimeMillis();
        log.info("roots : {}", fileRoots.length);
        log.info("**** option 1 = {}", end - start);
        //  3/4 === 21000

        start = System.currentTimeMillis();
        List<FileStore> stores = new ArrayList<>();
        FileSystems.getDefault().getFileStores().forEach(stores::add);

//        FileStore[] stores = new FileStore[26];
        int count = stores.size();
//        for (FileStore store: FileSystems.getDefault().getFileStores()) {
//            stores[count++] = store;
//        }
        end = System.currentTimeMillis();
        log.info("stores : {}", count);
        for (int i=0; i<count; i++) {
            log.info(stores.get(i).name());
        }
        log.info("**** option 2 = {}", end - start);
        //  3/4 === 126

        start = System.currentTimeMillis();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            log.info("path {}", p);
        }
        end = System.currentTimeMillis();
        log.info("**** option 3 = {}", end - start);
        //  4/4 === 3

        start = System.currentTimeMillis();
        for (char c = 'A'; c <= 'Z'; ++c) {
            if (new File(c + ":").exists()) {
                log.info(c + ":");
            }
        }
        end = System.currentTimeMillis();
        log.info("**** option 4 = {}", end - start);
        //  3/4 === 170

        start = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd /c wmic logicaldisk get caption".split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader inStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = inStream.readLine()) != null) {
                log.info(line);
            }
        }
        catch (IOException e) {
            log.error("error", e);
        }
        end = System.currentTimeMillis();
        log.info("**** option 5 = {}", end - start);
        //  4/4 === 179

        // jni
    }

}
