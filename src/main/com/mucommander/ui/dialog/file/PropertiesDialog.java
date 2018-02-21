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

package com.mucommander.ui.dialog.file;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.OSXFileUtils;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.job.FileJob;
import com.mucommander.job.PropertiesJob;
import com.mucommander.utils.text.SizeFormat;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowFilePropertiesAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FileLabel;
import com.mucommander.ui.text.MultiLineLabel;
import com.mucommander.utils.Convert;

/**
 * This dialog shows properties of a file or a group of files : number of files, file kind,
 * combined size and location.
 *
 * @author Maxence Bernard
 */
public class PropertiesDialog extends FocusDialog implements Runnable, ActionListener {
    private PropertiesJob job;
    private Thread repaintThread;
    private SpinningDial dial;
	
	private JTextField textfield;
    private JLabel counterLabel;
    private JLabel sizeLabel;
    private JLabel ownerLabel;
    private JLabel groupLabel;
	private JLabel lastMod;
	private JLabel createTimeLabel;
	private JLabel lastAccessLabel;
	AbstractFile file;
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private JButton okCancelButton;
	private String newName;
	private JTextField edtNewName;
    // Dialog width is constrained to 320, height is not an issue (always the same)
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360, 0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1024, 800);

    /** How often should progress information be refreshed (in ms) */
    private final static int REFRESH_RATE = 500;

    /** Dimension of the large file icon displayed on left side of the dialog */
    private final static Dimension ICON_DIMENSION = new Dimension(64, 64);

	
    public PropertiesDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame,
              files.size() > 1 ? ActionProperties.getActionLabel(ShowFilePropertiesAction.Descriptor.ACTION_ID) :
                      i18n("properties_dialog.file_properties", files.elementAt(0).getName()), mainFrame);

        this.job = new PropertiesJob(files, mainFrame);
		
        Container contentPane = getContentPane();

        JPanel fileDetailsPanel = new JPanel(new BorderLayout());

        Icon icon;
        boolean isSingleFile = files.size()==1;
        AbstractFile singleFile = isSingleFile?files.elementAt(0):null;
        if (isSingleFile) {
            icon = FileIcons.getFileIcon(singleFile, ICON_DIMENSION);
        } else {
            ImageIcon imageIcon = IconManager.getIcon(IconManager.IconSet.COMMON, "many_files.png");
            icon = IconManager.getScaledIcon(imageIcon, (float)ICON_DIMENSION.getWidth()/imageIcon.getIconWidth());
        }

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        fileDetailsPanel.add(iconLabel, BorderLayout.WEST);

        XAlignedComponentPanel labelPanel = new XAlignedComponentPanel(10);

		// Name of file for renaming
		file = files.elementAt(0);
		textfield = new JTextField();
		labelPanel.addRow("Name" + ":", textfield, 6);
		textfield.setText(file.getName());
		textfield.addActionListener(this);
		textfield.setEditable(true);
        // Contents (set later)
        counterLabel = new JLabel("");
        labelPanel.addRow(i18n("properties_dialog.contents")+":", counterLabel, 6);

        // Location (set here)
        labelPanel.addRow(i18n("location")+":", new FileLabel(files.getBaseFolder(), true), 6);

        // Combined size (set later)
        JPanel sizePanel;
        sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.add(sizeLabel = new JLabel(""));
        sizePanel.add(new JLabel(dial = new SpinningDial()));
        labelPanel.addRow(i18n("size") + ":", sizePanel, 6);

		// more information
		lastMod = new JLabel("");
		labelPanel.addRow("Last Modified" + ":", lastMod, 6);
		createTimeLabel = new JLabel("");
		labelPanel.addRow("Created" + ":", createTimeLabel, 6);
		lastAccessLabel = new JLabel("");
		labelPanel.addRow("Last Accessed" + ":", lastAccessLabel, 6);

        if (isSingleFile) {
            if (singleFile.canGetOwner()) {
                String owner = singleFile.getOwner();
                if (owner != null) {
                    labelPanel.addRow(i18n("owner") + ":", new JLabel(owner), 6);
                }
            }
            if (singleFile.canGetGroup()) {
                String group = singleFile.getGroup();
                if (group != null) {
                    labelPanel.addRow(i18n("group") + ":", new JLabel(group), 6);
                }
            }
            if (singleFile.isFileOperationSupported(FileOperation.GET_REPLICATION)) {
                short replication = 0;
                try {
                    replication = singleFile.getReplication();
                    labelPanel.addRow(i18n("replication") + ":", new JLabel(Short.toString(replication)), 6);
                } catch (UnsupportedFileOperationException e) {
                    e.printStackTrace();
                }
            }
            if (singleFile.isFileOperationSupported(FileOperation.GET_BLOCKSIZE)) {
                long blocksize = 0;
                try {
                    blocksize = singleFile.getBlocksize();
                    labelPanel.addRow(i18n("blocksize") + ":", new JLabel(Convert.readableFileSize(blocksize)), 6);
                } catch (UnsupportedFileOperationException e) {
                    e.printStackTrace();
                }
            }
        }

        if (OsVersion.MAC_OS_X_10_4.isCurrentOrHigher() && isSingleFile && singleFile.hasAncestor(LocalFile.class)) {
            String comment = OSXFileUtils.getSpotlightComment(singleFile);
            JLabel commentLabel = new JLabel(i18n("comment")+":");
            commentLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
            commentLabel.setVerticalAlignment(SwingConstants.TOP);

            labelPanel.addRow(commentLabel, new MultiLineLabel(comment), 6);
        }

        updateLabels();

        fileDetailsPanel.add(labelPanel, BorderLayout.CENTER);

        YBoxPanel yPanel = new YBoxPanel(5);
        yPanel.add(fileDetailsPanel);
        contentPane.add(yPanel, BorderLayout.NORTH);

        okCancelButton = new JButton(i18n("cancel"));
        contentPane.add(DialogToolkit.createOKPanel(okCancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // OK button will receive initial focus
        setInitialFocusComponent(okCancelButton);		
		
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
        start();
    }


    private void updateLabels() {
        int nbFiles = job.getNbFilesRecurse();
        int nbFolders = job.getNbFolders();
        counterLabel.setText(
                             (nbFiles > 0 ? i18n("nb_files", ""+nbFiles) : "")
                             +(nbFiles > 0 && nbFolders > 0 ? ", ":"")
                             +(nbFolders > 0 ? i18n("nb_folders", ""+nbFolders):"")
                             );
        sizeLabel.setText(SizeFormat.format(job.getTotalBytes(), SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB) +
			  " (" + SizeFormat.format(job.getTotalBytes(), SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE) + ")");

		
		//adding last modification time and date
		lastMod.setText(sdf.format(file.getLastModifiedDate()));


		
//		BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getAbsolutePath())),
//				BasicFileAttributes.class);
//		createTimeLabel.setText(attr.creationTime().toString());
//
//		lastacces.setText(attr.lastAccessTime().toString());

        long lastAccessTime;
        try {
            lastAccessTime = file.getLastAccessDate();
        } catch (IOException e) {
            lastAccessTime = -1;
        }
        lastAccessLabel.setText(lastAccessTime > 0 ? sdf.format(lastAccessTime) : i18n("unknown"));
        long createTime;
        try {
            createTime = file.getCreationDate();
        } catch (IOException e) {
            createTime = -1;
        }
        createTimeLabel.setText(createTime > 0 ? sdf.format(createTime) : i18n("unknown"));
        counterLabel.repaint(REFRESH_RATE);
        sizeLabel.repaint(REFRESH_RATE);
    }

		protected AbstractFile createDestinationFile(AbstractFile destFolder, String destFileName) {
		AbstractFile destFile;
		do { // Loop for retry
			try {
				destFile = destFolder.getDirectChild(destFileName);
				break;
			} catch (IOException ignore) {}
		} while (true);
		return destFile;
	}
	static void renameFile(AbstractFile destFile, String newName){

		AbstractFile destination = FileFactory.getFile(destFile.getParent()+"/"+newName);
		
		try {
			destFile.moveTo(destination);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}


    public void start() {
        job.start();
		
        repaintThread = new Thread(this, "com.mucommander.ui.dialog.file.PropertiesDialog's Thread");
        repaintThread.start();
    }

	
    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        dial.setAnimated(true);
        while (repaintThread != null && job.getState()!= FileJob.State.FINISHED) {
            updateLabels();
			
            try {
                Thread.sleep(REFRESH_RATE);
            } catch(InterruptedException ignore) {}
        }

        // Updates button labels and stops spinning dial.
        updateLabels();
        okCancelButton.setText(i18n("ok"));
        dial.setAnimated(false);
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okCancelButton) {
            renameFile(file,textfield.getText());
		}
		dispose();
    }


    ///////////////////////////////////////
    // Overridden WindowListener methods // 
    ///////////////////////////////////////

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
		
        // Stop threads
        job.interrupt();
        repaintThread = null;
    }
}
