package com.mucommander.commons.file.impl.s3;

import com.mucommander.commons.file.*;
import com.mucommander.commons.io.RandomAccessInputStream;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>S3Root</code> represents the Amazon S3 root resource, also known as 'service'.
 *
 * @author Maxence Bernard
 */
public class S3Root extends S3File {

    private SimpleFileAttributes atts;

    /** Default permissions for the S3 root */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(448);   // rwx------

    protected S3Root(FileURL url, S3Service service) {
        super(url, service);

        atts = new SimpleFileAttributes();
        atts.setPath("/");
        atts.setExists(true);
        atts.setDate(0);
        atts.setSize(0);
        atts.setDirectory(true);
        atts.setPermissions(DEFAULT_PERMISSIONS);
        atts.setOwner(null);
        atts.setGroup(null);
    }


    ///////////////////////////
    // S3File implementation //
    ///////////////////////////

    @Override
    public FileAttributes getFileAttributes() {
        return atts;
    }


    /////////////////////////////////
    // ProtocolFile implementation //
    /////////////////////////////////

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        try {
            org.jets3t.service.model.S3Bucket buckets[] = service.listAllBuckets();
            int nbBuckets = buckets.length;

            AbstractFile bucketFiles[] = new AbstractFile[nbBuckets];
            FileURL bucketURL;
            for(int i=0; i<nbBuckets; i++) {
                bucketURL = (FileURL)fileURL.clone();
                bucketURL.setPath("/"+buckets[i].getName());

                bucketFiles[i] = FileFactory.getFile(bucketURL, null, service, buckets[i]);
            }

            return bucketFiles;
        }
        catch(S3ServiceException e) {
            throw getIOException(e);
        }
    }

    // Unsupported operations

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public InputStream getInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    @UnsupportedFileOperation
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RENAME);
    }
    @Override
    @UnsupportedFileOperation
    public short getReplication() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_REPLICATION);
    }

    @Override
    @UnsupportedFileOperation
    public long getBlocksize() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_BLOCKSIZE);
    }

    @Override
    @UnsupportedFileOperation
    public void changeReplication(short replication) throws IOException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_REPLICATION);
    }

}
