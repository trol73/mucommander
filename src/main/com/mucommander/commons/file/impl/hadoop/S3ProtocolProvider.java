package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

/**
 * A file protocol provider for the Amazon S3 protocol, provided by the Hadoop virtual filesystem.
 *
 * <p>Even though it is working for the most part, it is flawed in several ways and should not be used.
 * See the {@link com.mucommander.commons.file.impl.s3} package for a better implementation of the Amazon S3 protocol.</p>
 *
 * @deprecated  
 * @author Maxence Bernard
 */
public class S3ProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length==0
            ?new S3File(url)
            :new S3File(url, (FileSystem)instantiationParams[0], (FileStatus)instantiationParams[1]);
    }
}
