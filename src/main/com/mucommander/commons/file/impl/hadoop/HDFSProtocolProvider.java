package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

/**
 * A file protocol provider for the Hadoop HDFS filesystem.
 *
 * @author Maxence Bernard
 */
public class HDFSProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length==0
            ?new HDFSFile(url)
            :new HDFSFile(url, (FileSystem)instantiationParams[0], (FileStatus)instantiationParams[1]);
    }
}
