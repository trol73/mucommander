package com.mucommander.commons.file.impl.webdav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.ProtocolFile;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mathias
 */
public class WebDAVFile extends ProtocolFile {

    private final Sardine sardine = SardineFactory.begin();
    private URI PATH;

    protected AbstractFile parent;
    private boolean parentSet;

    private final static String SEPARATOR = "/";

    WebDAVFile(FileURL fileURL) throws UnsupportedEncodingException, URISyntaxException {
        super(fileURL);
               
        String scheme = "http";
        
        if (fileURL.getPort() == 443){
            scheme = "https";
        }

        PATH = new URI(scheme, fileURL.getLogin() + ":" + fileURL.getPassword(), fileURL.getHost(), fileURL.getPort(), fileURL.getPath(), null, null);
    }

    @Override
    public long getLastModifiedDate() {
        return 1L;
    }

    @Override
    public void setLastModifiedDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getSize() {
        return 1L;
    }

    @Override
    public AbstractFile getParent() {
        if (!parentSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if (parentFileURL != null) {
                try {
                    parent = FileFactory.getFile(parentFileURL, this);
                } catch (IOException e) {
                    // No parent
                }
            }

            parentSet = true;
        }

        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentSet = true;
    }

    @Override
    public boolean exists() {
        List<DavResource> resources;
        try {
            resources = sardine.getResources(PATH.toString());
        } catch (IOException ex) {
            Logger.getLogger(WebDAVFile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return !resources.isEmpty();
    }

    @Override
    public FilePermissions getPermissions() {
        return new SimpleFilePermissions(448);
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getOwner() {
        return "N/A";
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public String getGroup() {
        return "N/A";
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        List<DavResource> resources;
        try {
            resources = sardine.getResources(PATH.toString());
        } catch (IOException ex) {
            Logger.getLogger(WebDAVFile.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }

        return !resources.isEmpty() && resources.get(0).isDirectory();
    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {

        List<DavResource> files;
        try {
            files = sardine.getResources(PATH.toString());
        } catch (SardineException e) {
            return new AbstractFile[]{};
        }

        if (files == null || files.size() == 0) {
            return new AbstractFile[]{};
        }

        AbstractFile children[] = new AbstractFile[files.size()];
        AbstractFile child;
        FileURL childURL;
        String childName;
        int nbFiles = files.size();
        int fileCount = 0;
        String parentPath = fileURL.getPath();
        if (!parentPath.endsWith(SEPARATOR)) {
            parentPath += SEPARATOR;
        }

        for (DavResource file : files) {
            if (file == null) {
                continue;
            }

            childName = file.getName();

            //Skip current path (Like skipping "." and ".."
            if (parentPath.equals(file.getPath())) {
                continue;
            }

            // Note: properties and credentials are cloned for every children's url
            childURL = (FileURL) fileURL.clone();
            childURL.setPath(parentPath + childName);

            child = FileFactory.getFile(childURL, this, file);
            children[fileCount++] = child;
        }

        // Create new array of the exact file count
        if (fileCount < nbFiles) {
            AbstractFile newChildren[] = new AbstractFile[fileCount];
            System.arraycopy(children, 0, newChildren, 0, fileCount);
            return newChildren;
        }

        return children;
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
    }

    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }

    @Override
    public Object getUnderlyingFileObject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    @UnsupportedFileOperation
    public short getReplication() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_REPLICATION);
    }
    @Override
    @UnsupportedFileOperation
    public void changeReplication(short replication) throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_REPLICATION);
    }

    @Override
    @UnsupportedFileOperation
    public long getBlocksize() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_BLOCKSIZE);
    }


	@Override
	public boolean isSystem() {
		return false;
	}
}
