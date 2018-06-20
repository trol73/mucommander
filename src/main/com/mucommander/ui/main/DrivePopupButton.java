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

package com.mucommander.ui.main;

import com.mucommander.adb.AdbUtils;
import com.mucommander.adb.AndroidMenu;
import com.mucommander.bonjour.BonjourDirectory;
import com.mucommander.bonjour.BonjourMenu;
import com.mucommander.bonjour.BonjourService;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.filter.PathFilter;
import com.mucommander.commons.file.filter.RegexpPathFilter;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.action.impl.SetCurrentFolderToLeftAction;
import com.mucommander.ui.action.impl.SetCurrentFolderToRightAction;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.dialog.server.FTPPanel;
import com.mucommander.ui.dialog.server.HDFSPanel;
import com.mucommander.ui.dialog.server.HTTPPanel;
import com.mucommander.ui.dialog.server.NFSPanel;
import com.mucommander.ui.dialog.server.S3Panel;
import com.mucommander.ui.dialog.server.SFTPPanel;
import com.mucommander.ui.dialog.server.SMBPanel;
import com.mucommander.ui.dialog.server.ServerConnectDialog;
import com.mucommander.ui.dialog.server.ServerPanel;
import com.mucommander.ui.dialog.server.VSpherePanel;
import com.mucommander.ui.dialog.server.WebDAVPanel;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.menu.JScrollMenu;
import com.mucommander.utils.FileIconsCache;
import com.mucommander.utils.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trolsoft.ui.TMenuSeparator;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * <code>DrivePopupButton</code> is a button which, when clicked, pops up a menu with a list of volumes items that be used
 * to change the current folder.
 *
 * @author Maxence Bernard
 */
public class DrivePopupButton extends PopupButton implements BookmarkListener, ConfigurationListener, LocationListener {

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * This action pops up {@link com.mucommander.ui.dialog.server.ServerConnectDialog} for a specified
     * protocol.
     */
    private class ServerConnectAction extends AbstractAction {
        private Class<? extends ServerPanel> serverPanelClass;

        private ServerConnectAction(String label, Class<? extends ServerPanel> serverPanelClass) {
            super(label);
            this.serverPanelClass = serverPanelClass;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            new ServerConnectDialog(folderPanel, serverPanelClass).showDialog();
        }
    }

    /**
     * This modified {@link OpenLocationAction} changes the current folder on the {@link FolderPanel} that contains
     * this button, instead of the currently active {@link FolderPanel}.
     */
    private class CustomOpenLocationAction extends OpenLocationAction {

        CustomOpenLocationAction(MainFrame mainFrame, Bookmark bookmark) {
            super(mainFrame, new HashMap<>(), bookmark);
        }

        CustomOpenLocationAction(MainFrame mainFrame, AbstractFile file) {
            super(mainFrame, new HashMap<>(), file);
        }

        CustomOpenLocationAction(MainFrame mainFrame, BonjourService bs) {
            super(mainFrame, new HashMap<>(), bs);
        }

        CustomOpenLocationAction(MainFrame mainFrame, FileURL url) {
            super(mainFrame, new HashMap<>(), url);
        }

        ////////////////////////
        // Overridden methods //
        ////////////////////////

        @Override
        protected FolderPanel getFolderPanel() {
            return folderPanel;
        }
    }

    /**
     * Logger reference
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DrivePopupButton.class);

    /**
     * FolderPanel instance that contains this button
     */
    private FolderPanel folderPanel;

    /**
     * MainFrame instance that contains this button
     */
    private MainFrame mainFrame;

    /**
     * Current volumes
     */
    private static AbstractFile volumes[];

    /**
     * static FileSystemView instance, has a (non-null) value only under Windows
     */
    private static FileSystemView fileSystemView;

    /**
     * Caches extended drive names, has a (non-null) value only under Windows
     */
    private static Map<AbstractFile, String> extendedNameCache;

    /**
     * Caches drive icons
     */
    private static Map<AbstractFile, Icon> iconCache = new HashMap<>();

    /**
     * Filters out volumes from the list based on the exclude regexp defined in the configuration, null if the regexp
     * is not defined.
     */
    private static PathFilter volumeFilter;

    static {
        if (OsFamily.WINDOWS.isCurrent()) {
            fileSystemView = FileSystemView.getFileSystemView();
            extendedNameCache = new HashMap<>();
        }

        try {
            String excludeRegexp = MuConfigurations.getPreferences().getVariable(MuPreference.VOLUME_EXCLUDE_REGEXP);
            if (excludeRegexp != null) {
                volumeFilter = new RegexpPathFilter(excludeRegexp, true);
                volumeFilter.setInverted(true);
            }
        } catch (PatternSyntaxException e) {
            LOGGER.info("Invalid regexp for conf variable " + MuPreferences.VOLUME_EXCLUDE_REGEXP, e);
        }

        // Initialize the volumes list
        volumes = getDisplayableVolumes();
    }

    /**
     * Creates a new <code>DrivePopupButton</code> which is to be added to the given FolderPanel.
     *
     * @param folderPanel the FolderPanel instance this button will be added to
     */
    DrivePopupButton(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
        mainFrame = this.folderPanel.getMainFrame();

        // Listen to location events to update the button when the current folder changes
        folderPanel.getLocationManager().addLocationListener(this);

        // Listen to bookmark changes to update the button if a bookmark corresponding to the current folder
        // has been added/edited/removed
        BookmarkManager.addBookmarkListener(this);

        // Listen to configuration changes to update the button if the system file icons policy has changed
        MuConfigurations.addPreferencesListener(this);
    }

    /**
     * Updates the button's label and icon to reflect the current folder and match one of the current volumes:
     * <<ul>
     * <li>If the specified folder corresponds to a bookmark, the bookmark's name will be displayed
     * <li>If the specified folder corresponds to a local file, the enclosing volume's name will be displayed
     * <li>If the specified folder corresponds to a remote file, the protocol's name will be displayed
     * </ul>
     * The button's icon will be the current folder's one.
     */
    private void updateButton() {
        AbstractFile currentFolder = folderPanel.getCurrentFolder();
        String currentPath = currentFolder.getAbsolutePath();
        FileURL currentURL = currentFolder.getURL();

        String newLabel = null;

        // First tries to find a bookmark matching the specified folder
        List<Bookmark> bookmarks = BookmarkManager.getBookmarks();

        for (Bookmark b : bookmarks) {
            if (currentPath.equals(b.getLocation())) {
                // Note: if several bookmarks match current folder, the first one will be used
                newLabel = b.getName();
                break;
            }
        }

        // If no bookmark matched current folder
        if (newLabel == null) {
            String protocol = currentURL.getScheme();
            // Remote file, use the protocol's name
            if (!protocol.equals(FileProtocols.FILE)) {
                newLabel = protocol.toUpperCase();
            }
            // Local file, use volume's name 
            else {
                // Patch for Windows UNC network paths (weakly characterized by having a host different from 'localhost'):
                // display 'SMB' which is the underlying protocol
                if (OsFamily.WINDOWS.isCurrent() && !FileURL.LOCALHOST.equals(currentURL.getHost())) {
                    newLabel = "SMB";
                } else {
                    // getCanonicalPath() must be avoided under Windows for the following reasons:
                    // a) it is not necessary, Windows doesn't have symlinks
                    // b) it triggers the dreaded 'No disk in drive' error popup dialog.
                    // c) when network drives are present but not mounted (e.g. X:\ mapped onto an SMB share),
                    // getCanonicalPath which is I/O bound will take a looooong time to execute

                    if (OsFamily.WINDOWS.isCurrent()) {
                        currentPath = currentFolder.getAbsolutePath(false).toLowerCase();
                    } else {
                        currentPath = currentFolder.getCanonicalPath(false).toLowerCase();
                    }

                    int bestLength = -1;
                    int bestIndex = 0;
                    for (int i = 0; i < volumes.length; i++) {
                        String temp;
                        if (OsFamily.WINDOWS.isCurrent()) {
                            temp = volumes[i].getAbsolutePath(false);
                        } else {
                            temp = volumes[i].getCanonicalPath(false);
                        }
                        temp = temp.toLowerCase();

                        int len = temp.length();
                        if (currentPath.startsWith(temp) && len > bestLength) {
                            bestIndex = i;
                            bestLength = len;
                        }
                    }
                    newLabel = volumes[bestIndex].getName();
                }
            }
        }

        setText(newLabel);
        // Set the folder icon based on the current system icons policy
        setIcon(FileIcons.getFileIcon(currentFolder));
    }

    /**
     * Returns the extended name of the given local file, e.g. "Local Disk (C:)" for C:\. The returned value is
     * interesting only under Windows. This method is I/O bound and very slow so it should not be called from the main
     * event thread.
     *
     * @param localFile the file for which to return the extended name
     * @return the extended name of the given local file
     */
    private static String getExtendedDriveName(AbstractFile localFile) {
        // Note: fileSystemView.getSystemDisplayName(java.io.File) is unfortunately very very slow
        String name = fileSystemView.getSystemDisplayName((java.io.File) localFile.getUnderlyingFileObject());

        if (name == null || name.isEmpty()) {   // This happens for CD/DVD drives when they don't contain any disc
            return localFile.getName();
        }

        return name;
    }

    /**
     * Returns the list of volumes to be displayed in the popup menu.
     * <p>
     * <p>The raw list of volumes is fetched using {@link LocalFile#getVolumes()} and then
     * filtered using the regexp defined in the {@link MuPreferences#VOLUME_EXCLUDE_REGEXP} configuration variable
     * (if defined).
     *
     * @return the list of volumes to be displayed in the popup menu
     */
    private static AbstractFile[] getDisplayableVolumes() {
        AbstractFile[] volumes = LocalFile.getVolumes();

        if (volumeFilter != null) {
            return volumeFilter.filter(volumes);
        }

        return volumes;
    }

    ////////////////////////////////
    // PopupButton implementation //
    ////////////////////////////////

    @Override
    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        JMenuItem item;
        MuAction action;

        final boolean right = this.folderPanel.equals(mainFrame.getRightPanel());
        final JMenuItem setSameFolderItem = new JMenuItem(ActionManager.getActionInstance(
                right ?
                        SetCurrentFolderToRightAction.Descriptor.ACTION_ID :
                        SetCurrentFolderToLeftAction.Descriptor.ACTION_ID,
                mainFrame));
        setMnemonic(setSameFolderItem, mnemonicHelper);
        popupMenu.add(setSameFolderItem);

        popupMenu.add(new TMenuSeparator());

        // Update the list of volumes in case new ones were mounted
        volumes = getDisplayableVolumes();
        // Add volumes
        int nbVolumes = volumes.length;
        boolean useExtendedDriveNames = fileSystemView != null;
        List<JMenuItem> itemsV = new ArrayList<>();

        for (int i = 0; i < nbVolumes; i++) {
            action = new CustomOpenLocationAction(mainFrame, volumes[i]);
            String volumeName = volumes[i].getName();

            // If several volumes have the same filename, use the volume's path for the action's label instead of the
            // volume's path, to disambiguate
            for (int j = 0; j < nbVolumes; j++) {
                if (j != i && volumes[j].getName().equalsIgnoreCase(volumeName)) {
                    action.setLabel(volumes[i].getAbsolutePath());
                    break;
                }
            }

            item = popupMenu.add(action);
            setMnemonic(item, mnemonicHelper);

            // Set icon from cache
            Icon icon = iconCache.get(volumes[i]);
            if (icon != null) {
                item.setIcon(icon);
            }

            if (useExtendedDriveNames) {
                // Use the last known value (if any) while we update it in a separate thread
                String previousExtendedName = extendedNameCache.get(volumes[i]);
                if (previousExtendedName != null) {
                    item.setText(previousExtendedName);
                }

            }
            itemsV.add(item);   // JMenu offers no way to retrieve a particular JMenuItem, so we have to keep them
        }

        new RefreshDriveNamesAndIcons(popupMenu, itemsV).start();

        popupMenu.add(new TMenuSeparator());

        // Add bookmarks
        List<Bookmark> bookmarks = BookmarkManager.getBookmarks();
        if (!bookmarks.isEmpty()) {
            final JScrollMenu bookmarksMenu = new JScrollMenu(Translator.get("bookmarks_menu"));
            bookmarksMenu.setIcon(IconManager.getIcon(IconManager.IconSet.FILE, CustomFileIconProvider.BOOKMARK_ICON_NAME));
            popupMenu.add(bookmarksMenu);
            bookmarksMenu.getPopupMenu().setMaximumVisibleRows(20);

            for (Bookmark bookmark : bookmarks) {
                if (bookmark.getName().equals(BookmarkManager.BOOKMARKS_SEPARATOR) && bookmark.getLocation().isEmpty()) {
                    bookmarksMenu.add(new TMenuSeparator());
                    continue;
                }

                item = bookmarksMenu.add(new CustomOpenLocationAction(mainFrame, bookmark));
                String location = bookmark.getLocation();
                if (!location.contains("://")) {
                    AbstractFile file = FileFactory.getFile(location);
                    if (file != null) {
                        Icon icon = FileIconsCache.getInstance().getIcon(file);
                        if (icon != null) {
                            item.setIcon(icon);
                        }
                    }
                } else if (FileProtocols.NETWORK_PROTOCOLS.stream().anyMatch(s -> location.startsWith(s + "://"))) {
                    item.setIcon(IconManager.getIcon(IconManager.IconSet.FILE, CustomFileIconProvider.NETWORK_ICON_NAME));
                }
                setMnemonic(item, mnemonicHelper);
            }
        } else {
            // No bookmark : add a disabled menu item saying there is no bookmark
            popupMenu.add(Translator.get("bookmarks_menu.no_bookmark")).setEnabled(false);
        }

        popupMenu.add(new TMenuSeparator());

        // Add 'Network shares' shortcut
        if (FileFactory.isRegisteredProtocol(FileProtocols.SMB)) {
            action = new CustomOpenLocationAction(mainFrame, new Bookmark(Translator.get("drive_popup.network_shares"), "smb:///"));
            action.setIcon(IconManager.getIcon(IconManager.IconSet.FILE, CustomFileIconProvider.NETWORK_ICON_NAME));
            setMnemonic(popupMenu.add(action), mnemonicHelper);
        }

        if (BonjourDirectory.isActive()) {
            // Add Bonjour services menu
            setMnemonic(popupMenu.add(new BonjourMenu() {
                @Override
                public MuAction getMenuItemAction(BonjourService bs) {
                    return new CustomOpenLocationAction(mainFrame, bs);
                }
            }), mnemonicHelper);
        }

        if (AdbUtils.checkAdb()) {
            setMnemonic(popupMenu.add(new AndroidMenu() {
                @Override
                public MuAction getMenuItemAction(String deviceSerial) {
                    FileURL url = null;
                    try {
                        url = FileURL.getFileURL("adb://" + deviceSerial);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return new CustomOpenLocationAction(mainFrame, url);
                }
            }), mnemonicHelper);
        }

        popupMenu.add(new TMenuSeparator());

        // Add 'connect to server' shortcuts
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.FTP.toUpperCase() + " ...", FTPPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.HDFS.toUpperCase() + " ...", HDFSPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.HTTP.toUpperCase() + " ...", HTTPPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.NFS.toUpperCase() + " ...", NFSPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.S3.toUpperCase() + " ...", S3Panel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.SFTP.toUpperCase() + " ...", SFTPPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.SMB.toUpperCase() + " ...", SMBPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.WEBDAV.toUpperCase() + " ...", WebDAVPanel.class)), mnemonicHelper);
        setMnemonic(popupMenu.add(new ServerConnectAction(FileProtocols.VSPHERE.toUpperCase() + " ...", VSpherePanel.class)), mnemonicHelper);

        return popupMenu;
    }

    /**
     * Calls to getExtendedDriveName(String) are very slow, so they are performed in a separate thread so as
     * to not lock the main even thread. The popup menu gets first displayed with the short drive names, and
     * then refreshed with the extended names as they are retrieved.
     */
    private class RefreshDriveNamesAndIcons extends Thread {

        private JPopupMenu popupMenu;
        private List<JMenuItem> items;

        RefreshDriveNamesAndIcons(JPopupMenu popupMenu, List<JMenuItem> items) {
            super("RefreshDriveNamesAndIcons");
            this.popupMenu = popupMenu;
            this.items = items;
        }

        @Override
        public void run() {
            final boolean useExtendedDriveNames = fileSystemView != null;
            for (int i = 0; i < items.size(); i++) {
                final JMenuItem item = items.get(i);

                String extendedName = null;
                if (useExtendedDriveNames) {
                    // Under Windows, show the extended drive name (e.g. "Local Disk (C:)" instead of just "C:") but use
                    // the simple drive name for the mnemonic (i.e. 'C' instead of 'L').
                    extendedName = getExtendedDriveName(volumes[i]);

                    // Keep the extended name for later (see above)
                    extendedNameCache.put(volumes[i], extendedName);
                }
                final String extendedNameFinal = extendedName;

                // Set system icon for volumes, only if system icons are available on the current platform
                final Icon icon = FileIcons.hasProperSystemIcons() ? FileIcons.getSystemFileIcon(volumes[i]) : null;
                if (icon != null) {
                    iconCache.put(volumes[i], icon);
                }

                SwingUtilities.invokeLater(() -> {
                    if (useExtendedDriveNames) {
                        item.setText(extendedNameFinal);
                    }
                    if (icon != null) {
                        item.setIcon(icon);
                    }
                });

            }

            // Re-calculate the popup menu's dimensions
            SwingUtilities.invokeLater(() -> {
                popupMenu.invalidate();
                popupMenu.pack();
            });
        }

    }

    /**
     * Convenience method that sets a mnemonic to the given JMenuItem, using the specified MnemonicHelper.
     *
     * @param menuItem       the menu item for which to set a mnemonic
     * @param mnemonicHelper the MnemonicHelper instance to be used to determine the mnemonic's character.
     */
    private void setMnemonic(JMenuItem menuItem, MnemonicHelper mnemonicHelper) {
        menuItem.setMnemonic(mnemonicHelper.getMnemonic(menuItem.getText()));
    }

    //////////////////////////////
    // BookmarkListener methods //
    //////////////////////////////

    @Override
    public void bookmarksChanged() {
        // Refresh label in case a bookmark with the current location was changed
        updateButton();
    }

    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    @Override
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Update the button's icon if the system file icons policy has changed
        if (var.equals(MuPreferences.USE_SYSTEM_FILE_ICONS)) {
            updateButton();
        }
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public Dimension getPreferredSize() {
        // Limit button's maximum width to something reasonable and leave enough space for location field,
        // as bookmarks name can be as long as users want them to be.
        // Note: would be better to use JButton.setMaximumSize() but it doesn't seem to work
        Dimension d = super.getPreferredSize();
        if (d.width > 160) {
            d.width = 160;
        }
        return d;
    }

    /**********************************
     * LocationListener Implementation
     **********************************/

    @Override
    public void locationChanged(LocationEvent e) {
        // Update the button's label to reflect the new current folder
        updateButton();
    }

    @Override
    public void locationChanging(LocationEvent locationEvent) {
    }

    @Override
    public void locationCancelled(LocationEvent locationEvent) {
    }

    @Override
    public void locationFailed(LocationEvent locationEvent) {
    }

}
