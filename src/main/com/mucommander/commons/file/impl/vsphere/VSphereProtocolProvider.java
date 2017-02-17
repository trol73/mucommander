package com.mucommander.commons.file.impl.vsphere;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;

import java.io.IOException;

/**
 * This class is the provider for the VSphere filesystem implemented by
 * {@link com.mucommander.commons.file.impl.vsphere.VSphereFile}.
 * 
 * @author Yuval Kohavi yuval.kohavi@intigua.com
 * @see com.mucommander.commons.file.impl.vsphere.VSphereFile
 */
public class VSphereProtocolProvider implements ProtocolProvider {

	// ///////////////////////////////////
	// ProtocolProvider Implementation //
	// ///////////////////////////////////

	public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
		return new VSphereFile(url);
	}
}
