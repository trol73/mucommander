package com.mucommander.commons.file;

/**
 * Super class of all file protocol implementations (by opposition to {@link AbstractArchiveFile archive file} 
 * implementations).
 *
 * @see ProtocolProvider
 * @author Maxence Bernard
 */
public abstract class ProtocolFile extends AbstractFile {

    protected ProtocolFile(FileURL url) {
        super(url);
    }
    

    /**
     * This implementation always returns <code>false</code>.
     *
     * @return <code>false</code>, always
     */
    @Override
    public boolean isArchive() {
        return false;
    }

    /*@Override
    public boolean canGetReplication() {
        return false;
    }

    @Override
    public boolean canGetBlocksize() {
        return false;
    }

    @Override
    public short getReplication() {
        return 0;
    }

    @Override
    public long getBlocksize() {
        return 0;
    }

    @Override
    public void changeReplication(short replication) throws IOException {

    }*/

}
