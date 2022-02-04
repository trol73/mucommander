package com.mucommander.commons.file;

/**
 * This class is a simple implementation of the {@link com.mucommander.commons.file.FileAttributes} interface, where all
 * the attributes are stored as protected members of the class.
 *
 * @author Maxence Bernard
 */
public class SimpleFileAttributes implements MutableFileAttributes {

    /** Path attribute */
    private String path;

    /** Exists attribute */
    private boolean exists;

    /** Date attribute */
    private long date;

    /** Size attribute */
    private long size;

    /** Directory attribute */
    private boolean directory;

    /** Permissions attribute */
    private FilePermissions permissions;

    /** Owner attribute */
    private String owner;

    /** Group attribute */
    private String group;

    /** Replication attribute */
    private short replication = 1;

    /** BlockSize attribute */
    private long blockSize = 0;

    /**
     * Creates a new SimpleFileAttributes instance with unspecified/null attribute values.
     */
    public SimpleFileAttributes() {
    }


    /**
     * Creates a new SimpleFileAttributes instance whose attributes are set to those of the given AbstractFile.
     * Note that the path attribute is set to the file's {@link com.mucommander.commons.file.AbstractFile#getAbsolutePath() absolute path}.
     *
     * @param file the file from which to fetch the attribute values
     */
    public SimpleFileAttributes(AbstractFile file) {
        setPath(file.getAbsolutePath());
        setExists(file.exists());
        setDate(file.getLastModifiedDate());
        setSize(file.getSize());
        setDirectory(file.isDirectory());
        setPermissions(file.getPermissions());
        setOwner(file.getOwner());
        setGroup(file.getGroup());
    }


    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public void setExists(boolean exists) {
        this.exists = exists;
    }

    @Override
    public long getLastModifiedDate() {
        return date;
    }

    @Override
    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public FilePermissions getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(FilePermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    public short getReplication() {
        return replication;
    }

    public void setReplication(short replication) {
        this.replication = replication;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

}
