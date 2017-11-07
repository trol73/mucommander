package com.mucommander.ui.dialog.server;

import java.net.MalformedURLException;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.impl.vsphere.VSphereFile;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.main.MainFrame;

/**
 * This ServerPanel helps initiate VSpherePanel connections.
 * 
 * @author Yuval Kohavi, yuval@intigua.com
 */
public class VSpherePanel extends ServerPanel {

	private static final String IP_UUID_INSTANCE_UUID = "IP\\UUID\\Instance UUID";

	private static final long serialVersionUID = -3850165192515539062L;
	
	private JTextField vsphereField;
	private JTextField usernameField;
	private JPasswordField passwordField;

	private JTextField guestField;
	private JTextField guestUsernameField;
	private JPasswordField guestPasswordField;

	private JTextField dirField;
	private static String lastVsphere = "";
	private static String lastGuest = "";
	private static String lastDir = "";
	private static String lastUsername = "";
	private static String lastGuestUsername = "";

	VSpherePanel(ServerConnectDialog dialog, MainFrame mainFrame) {
		super(dialog, mainFrame);

		// Server field
		vsphereField = new JTextField(lastVsphere);
		vsphereField.selectAll();
		addTextFieldListeners(vsphereField, true);
		addRow(Translator.get("server_connect_dialog.server"), vsphereField, 5);

		// Username field
		usernameField = new JTextField(lastUsername);
		usernameField.selectAll();
		addTextFieldListeners(usernameField, false);
		addRow(Translator.get("server_connect_dialog.username"), usernameField,
				5);

		// Password field
		passwordField = new JPasswordField();
		addTextFieldListeners(passwordField, false);
		addRow(Translator.get("password"), passwordField, 0);

		// Server field
		guestField = new JTextField(lastGuest);
		guestField.selectAll();
		addTextFieldListeners(guestField, true);
		addRow(Translator.get("vsphere_connections_dialog.guest_server",  IP_UUID_INSTANCE_UUID), guestField, 5);

		// Username field
		guestUsernameField = new JTextField(lastGuestUsername);
		guestUsernameField.selectAll();
		addTextFieldListeners(guestUsernameField, false);
		addRow(Translator.get("vsphere_connections_dialog.guest_user"),
				guestUsernameField, 5);

		// Password field
		guestPasswordField = new JPasswordField();
		addTextFieldListeners(guestPasswordField, false);
		addRow(Translator.get("vsphere_connections_dialog.guest_password"), guestPasswordField, 0);

		// Share field
		dirField = new JTextField(lastDir);
		dirField.selectAll();
		addTextFieldListeners(dirField, true);
		addRow(Translator.get("server_connect_dialog.initial_dir"), dirField,
				15);

	}

	private void updateValues() {
		lastVsphere = vsphereField.getText();
		lastUsername = usernameField.getText();
		lastGuest = guestField.getText();
		lastGuestUsername = guestUsernameField.getText();
		lastDir = dirField.getText();
	}

	// //////////////////////////////
	// ServerPanel implementation //
	// //////////////////////////////

	@Override
	FileURL getServerURL() throws MalformedURLException {
		updateValues();
		FileURL url = FileURL.getFileURL(FileProtocols.VSPHERE + "://"
				+ lastVsphere + "/" + lastGuest
				+ "/" + PathUtils.removeLeadingSeparator(lastDir));
		url.setCredentials(new Credentials(lastUsername,  new String(passwordField.getPassword())));
		url.setProperty(VSphereFile.GUEST_CREDENTIALS, lastGuestUsername + ":" + new String(guestPasswordField.getPassword()));

		return url;
	}

	@Override
	boolean usesCredentials() {
		return true;
	}

	@Override
	public void dialogValidated() {
		updateValues();
	}
}
