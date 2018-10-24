package com.mucommander.commons.file.compat;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Maxence Bernard
*/
class CompatURLConnection extends URLConnection {

    protected AbstractFile file;

    public CompatURLConnection(URL url) {
        super(url);

        // Not connected yet
    }

    public CompatURLConnection(URL url, AbstractFile file) {
        super(url);

        if(file!=null) {
            this.file = file;
            connected = true;
        }
    }

    /**
     * Checks if this <code>URLConnection</code> is connected and if it isn't, calls {@link #connect()} to connect it.
     *
     * @throws IOException if an error occurred while connecting this URLConnection
     */
    private void checkConnected() throws IOException {
        if(!connected)
            connect();
    }


    //////////////////////////////////
    // URLConnection implementation //
    //////////////////////////////////

    /**
     * Creates the {@link AbstractFile} instance corresponding to the URL location, only if no <code>AbstractFile</code>
     * has been specified when this <code>CompatURLConnection</code> was created.
     *
     * @throws IOException if an error occurred while instanciating the AbstractFile
     */
    @Override
    public void connect() throws IOException {
        if(!connected) {
            file = FileFactory.getFile(url.toString(), true);
            connected = true;
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public InputStream getInputStream() throws IOException {
        checkConnected();

        return file.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        checkConnected();

        return file.getOutputStream();
    }

    @Override
    public long getLastModified() {
        try {
            checkConnected();

            return file.getLastModifiedDate();
        }
        catch(IOException e) {
            return 0;
        }
    }

    @Override
    public long getDate() {
        return getLastModified();
    }

    @Override
    public int getContentLength() {
        try {
            checkConnected();

            return (int)file.getSize();
        }
        catch(IOException e) {
            return -1;
        }
    }
}
