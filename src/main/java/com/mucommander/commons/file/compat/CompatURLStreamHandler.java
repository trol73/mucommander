package com.mucommander.commons.file.compat;

import com.mucommander.commons.file.AbstractFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


/**
 * @author Maxence Bernard
 */
public class CompatURLStreamHandler extends URLStreamHandler {

    protected AbstractFile file;

    public CompatURLStreamHandler() {
    }

    public CompatURLStreamHandler(AbstractFile file) {
        this.file = file;
    }


    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new CompatURLConnection(url, file);      // Note: file may be null
    }
}
