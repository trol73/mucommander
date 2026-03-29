package com.mucommander.commons.file.impl.vsphere;

import java.io.IOException;

import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * Manage VSphere connections.
 * 
 * @author Yuval Kohavi, yuval.kohavi@intigua.com
 * 
 */
public class VsphereConnHandler extends ConnectionHandler {

	private VSphereClient client = null;
	private FileURL location;

	VSphereClient getClient() {
		return client;
	}

	VsphereConnHandler(FileURL serverURL) {
		super(serverURL);
		location = serverURL;
	}

	private void initClientIfNeeded() throws RuntimeFaultFaultMsg,
			InvalidLocaleFaultMsg, InvalidLoginFaultMsg {
		if (client == null) {
			client = new VSphereClient(location.getHost(), location
					.getCredentials().getLogin(), location.getCredentials()
					.getPassword());
			client.connect();
		}
	}

	@Override
	public void startConnection() throws IOException {
		try {
			initClientIfNeeded();
		} catch (RuntimeFaultFaultMsg | InvalidLocaleFaultMsg e) {
			throw new IOException(e);
		} catch (InvalidLoginFaultMsg e) {
			throw new AuthException(location, e.getMessage());
		}
	}

	@Override
	public boolean isConnected() {
		return client != null && client.isConnected();
	}

	@Override
	public void closeConnection() {
		try {
			client.disconnect();
		} catch (RuntimeFaultFaultMsg e) {
			// nothing we can do... ignore..
			e.printStackTrace();
		}
		client = null;
	}

	@Override
	public void keepAlive() {

		if (client != null) {
			try {
				doKeepAlive();
			} catch (RuntimeFaultFaultMsg e) {
				client = null;
			}
		}
	}

	private void doKeepAlive() throws RuntimeFaultFaultMsg {
		// do nothing, to keep alive
		client.getVimPort().currentTime(client.getServiceInstance());
	}
}
