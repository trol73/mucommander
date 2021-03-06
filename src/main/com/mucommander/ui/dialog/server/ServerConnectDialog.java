/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.dialog.server;

import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ConnectToServerAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Dialog that assists the user in connecting to a filesystem. It contains tabs and associated panels for each of the
 * supported protocols.
 *
 * @author Maxence Bernard
 */
public class ServerConnectDialog extends FocusDialog implements ActionListener, ChangeListener {

    private final FolderPanel folderPanel;
	
    private final JButton btnCancel;
    private ServerPanel pnlCurrentServer;

    private final JTabbedPane tabbedPane;
    private final List<ServerPanel> serverPanels = new ArrayList<>();

    private final JLabel lblUrl;
    private final JCheckBox cbSaveCredentials;

    // Dialog's width has to be at least 320
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(520, 0);
	
    private static Class<? extends ServerPanel> lastPanelClass = FTPPanel.class;


    /**
     * Creates a new <code>ServerConnectDialog</code> that changes the current folder on the specified {@link FolderPanel}.
     *
     * @param folderPanel the panel on which to change the current folder
     */
    public ServerConnectDialog(FolderPanel folderPanel) {
        this(folderPanel, lastPanelClass);
    }
	
		
    /**
     * Creates a new <code>ServerConnectDialog</code> that changes the current folder on the specified {@link FolderPanel}.
     * The specified panel is selected when the dialog appears.
     *
     * @param folderPanel the panel on which to change the current folder
     * @param selectPanelClass class of the ServerPanel to select
     */
    public ServerConnectDialog(FolderPanel folderPanel, Class<? extends ServerPanel> selectPanelClass) {
        super(folderPanel.getMainFrame(), ActionProperties.getActionLabel(ConnectToServerAction.Descriptor.ACTION_ID), folderPanel.getMainFrame());
        this.folderPanel = folderPanel;
        lastPanelClass = selectPanelClass;

        MainFrame mainFrame = folderPanel.getMainFrame();
        Container contentPane = getContentPane();
		
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        addTab(FileProtocols.FTP, new FTPPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.HDFS, new HDFSPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.HTTP, new HTTPPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.NFS, new NFSPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.S3, new S3Panel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.SFTP, new SFTPPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.SMB, new SMBPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.WEBDAV, new WebDAVPanel(this, mainFrame), selectPanelClass);
        addTab(FileProtocols.VSPHERE, new VSpherePanel(this, mainFrame), selectPanelClass);

        pnlCurrentServer = getCurrentServerPanel();

        // Listen to tab change events
        tabbedPane.addChangeListener(this);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
		
        YBoxPanel yPanel = new YBoxPanel();
        XBoxPanel xPanel = new XBoxPanel();
        xPanel.add(new JLabel(i18n("server_connect_dialog.server_url")+":"));
        xPanel.addSpace(5);
        lblUrl = new JLabel("");
        updateURLLabel();
        xPanel.add(lblUrl);
        yPanel.add(xPanel);

        yPanel.addSpace(10);

        this.cbSaveCredentials = new JCheckBox(i18n("auth_dialog.store_credentials"), false);
        // Enables 'save credentials' checkbox only if server panel/protocol uses credentials
        cbSaveCredentials.setEnabled(pnlCurrentServer.usesCredentials());
        yPanel.add(cbSaveCredentials);

        JButton okButton = new JButton(i18n("server_connect_dialog.connect"));
        btnCancel = new JButton(i18n("cancel"));
        yPanel.add(DialogToolkit.createOKCancelPanel(okButton, btnCancel, getRootPane(), this));

        contentPane.add(yPanel, BorderLayout.SOUTH);
		
        // initial focus
        setInitialFocusComponent(pnlCurrentServer);
		
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }


    public void addTab(String protocol, ServerPanel serverPanel, Class<? extends ServerPanel> selectPanelClass) {
        if (!FileFactory.isRegisteredProtocol(protocol)) {
            return;
        }

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(serverPanel, BorderLayout.NORTH);
        tabbedPane.addTab(protocol.toUpperCase(), northPanel);

        if (selectPanelClass.equals(serverPanel.getClass())) {
            tabbedPane.setSelectedComponent(northPanel);
        }
        serverPanels.add(serverPanel);
    }


    void updateURLLabel() {
        try {
            FileURL url = pnlCurrentServer.getServerURL();
            lblUrl.setText(url == null ? " " : url.toString(false));
        }
        catch(MalformedURLException ex) {
            lblUrl.setText(" ");
        }
    }

    private ServerPanel getCurrentServerPanel() {
        return serverPanels.get(tabbedPane.getSelectedIndex());
    }

	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnCancel) {
            dispose();
            return;
        }

        try {
            pnlCurrentServer.dialogValidated();

            FileURL serverURL = pnlCurrentServer.getServerURL();	// Can throw a MalformedURLException

            // create a CredentialsMapping instance and pass to Folder so that it uses it to connect to the folder and
            // adds to CredentialsManager once the folder has been successfully changed
            Credentials credentials = serverURL.getCredentials();
            CredentialsMapping credentialsMapping;
            if (credentials != null) {
                credentialsMapping = new CredentialsMapping(credentials, serverURL, cbSaveCredentials.isSelected());
            } else {
                credentialsMapping = null;
            }

            dispose();

            // Change the current folder
            folderPanel.tryChangeCurrentFolder(serverURL, credentialsMapping);
        } catch(IOException ex) {
            InformationDialog.showErrorDialog(this, i18n("table.folder_access_error_title"), i18n("folder_does_not_exist"));
        }
    }
	

    public void stateChanged(ChangeEvent e) {
        pnlCurrentServer = getCurrentServerPanel();
        lastPanelClass = pnlCurrentServer.getClass();

        // Enables 'save credentials' checkbox only if server panel/protocol uses credentials
        cbSaveCredentials.setEnabled(pnlCurrentServer.usesCredentials());

        updateURLLabel();
        FocusRequester.requestFocus(pnlCurrentServer);
    }
}
