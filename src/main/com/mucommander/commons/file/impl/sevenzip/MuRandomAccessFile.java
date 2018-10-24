package com.mucommander.commons.file.impl.sevenzip;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.IInStream;

/**
 * TODO: comment
 * 
 * @author Arik Hadas
 */
public class MuRandomAccessFile extends IInStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(MuRandomAccessFile.class);
	
	private AbstractFile file;
	
	private InputStream stream;
	
	private long position;
	
	public MuRandomAccessFile(AbstractFile file) {
		super();
		position = 0;
		this.file = file;
		try {
			stream = file.getInputStream();
		} catch (IOException e) {
			LOGGER.trace("Error", e);
		}
	}

	@Override
    public long Seek(long offset, int seekOrigin) throws IOException {
		if (seekOrigin == STREAM_SEEK_SET) {
			stream.close();
            stream = file.getInputStream();
            stream.skip(offset);
            position = offset;
        }
        else if (seekOrigin == STREAM_SEEK_CUR) {
            stream.skip(offset);
            position += offset;
        }
        return position;
	}

	@Override
    public int read() throws IOException {
		int read = stream.read();
		position += read;
		return read;
	}

	@Override
    public int read(byte [] data, int off, int size) throws java.io.IOException {
        int read = stream.read(data, off, size);
        position += read;
        return read;
    }
        
    public int read(byte [] data, int size) throws java.io.IOException {
        int read = stream.read(data,0,size);
        position += read;
        return read;
    }
    
    @Override
    public void close() throws java.io.IOException {
        stream.close();
    }
    
    @Override
    public long skip(long offset) {
    	long skipped = 0;
    	try {
			skipped = stream.skip(offset);
		} catch (IOException e) {
            LOGGER.trace("Error", e);
		}
		return skipped;
    }
    
    @Override
    public int available () {
    	int available = 0;
    	try {
			available = stream.available();
		} catch (IOException e) {
            LOGGER.trace("Error", e);
		}
		return available;
    }
}
