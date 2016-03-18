/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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

import com.jidesoft.hints.ListDataIntelliHints;
import com.mucommander.cache.TextHistory;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferencesAPI;
import com.mucommander.job.FileJob;
import com.mucommander.job.FindFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.FindFileAction;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.encoding.EncodingPreferences;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.ui.viewer.ViewerRegistrar;
import ru.trolsoft.ui.InputField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;

/**
 * Find file dialog
 */
public class FindFileDialog extends FocusDialog implements ActionListener, DocumentListener {

    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(640, 480);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(10000, 1024);

    /** How often should progress information be refreshed (in ms) */
    private final static int REFRESH_RATE = 200;

    private MainFrame mainFrame;
    private FindFileJob job;
    private SpinningDial dial;

    private JButton btnNewSearch;
    private JButton btnStop;
    private JButton btnClean;
    private JButton btnClose;

    private JTextField edtFileName;
    private InputField edtText;
    private JTextField edtFromDirectory;

    private JCheckBox cbSearchSubdirectories;
    private JCheckBox cbSearchArchives;
    private JCheckBox cbIgnoreHidden;
    private JCheckBox cbCaseSensitive;
    private JCheckBox cbSearchHex;
    private JComboBox<String> cbEncoding;

    private DefaultListModel<AbstractFile> listModel = new DefaultListModel<>();
    private JList<AbstractFile> list;
    protected JLabel lblTotal;

    private AbstractFile startDirectory;

    private ListDataIntelliHints textHints, hexHints;
    private UpdateRunner updateRunner;

    private class UpdateRunner extends SwingWorker<List<AbstractFile>, AbstractFile> {

        @Override
        protected List<AbstractFile> doInBackground() throws Exception {
            btnNewSearch.setEnabled(false);
            while (job != null && job.getState() != FileJob.State.FINISHED) {
                checkUpdates();
                try {
                    Thread.sleep(REFRESH_RATE);
                } catch(InterruptedException ignore) {}
            }
            checkUpdates();
            job = null;
            return null;
        }

        @Override
        protected void done() {
            showProgress(false);
            updateButtons();
            super.done();
        }

        @Override
        protected void process(List<AbstractFile> chunks) {
            for (AbstractFile f : chunks) {
                if (isCancelled()) {
                    break;
                }
                listModel.addElement(f);
                updateResultLabel();
            }
        }

        private void checkUpdates() {
            if (job == null) {
                return;
            }
            final List<AbstractFile> jobResults = job.getResults();
            synchronized (job) {
                for (int i = listModel.size(); i < jobResults.size(); i++) {
                    AbstractFile f = jobResults.get(i);
                    publish(f);
                }
            }
        }

    }



    public FindFileDialog(final MainFrame mainFrame, AbstractFile currentFolder) {
        super(mainFrame, ActionProperties.getActionLabel(FindFileAction.Descriptor.ACTION_ID), mainFrame);
        this.mainFrame = mainFrame;
        Container contentPane = getContentPane();

        YBoxPanel yPanel = new YBoxPanel(10);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Add filename field
        this.edtFileName = new JTextField();
        edtFileName.getDocument().addDocumentListener(this);
        List<String> filesHistory = TextHistory.getInstance().getList(TextHistory.Type.FILE_NAME);
        new ListDataIntelliHints<>(edtFileName, filesHistory).setCaseSensitive(false);
        edtFileName.setText("");
        compPanel.addRow(Translator.get("find_dialog.name")+":", edtFileName, 5);

        // Add contains field
        this.edtText = new InputField();
        edtText.getDocument().addDocumentListener(this);
//        List<String> textHistory = TextHistory.getInstance().getList(TextHistory.Type.TEXT_SEARCH);
//        new ListDataIntelliHints<>(edtText, textHistory).setCaseSensitive(false);
//        edtText.setText("");
        compPanel.addRow(Translator.get("find_dialog.contains")+":", edtText, 5);

        // Add encoding field
        this.cbEncoding = new SaneComboBox<>();

        List<String> encodings = EncodingPreferences.getPreferredEncodings();
        for (String encoding: encodings) {
            cbEncoding.addItem(encoding);
        }
        compPanel.addRow(Translator.get("find_dialog.encoding")+":", cbEncoding, 5);

        // create a path field with auto-completion capabilities
        this.edtFromDirectory = new FilePathField();
        this.edtFromDirectory.setText(currentFolder.toString());
        edtFromDirectory.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("find_dialog.initial_directory")+":", edtFromDirectory, 10);

        ProportionalGridPanel gridPanel = new ProportionalGridPanel(3);

        // Checkboxes
        this.cbSearchSubdirectories = new JCheckBox(Translator.get("find_dialog.search_subdirectories"));
        this.cbSearchArchives = new JCheckBox(Translator.get("find_dialog.search_archives"));
        this.cbCaseSensitive = new JCheckBox(Translator.get("find_dialog.case_sensitive"));
        this.cbIgnoreHidden = new JCheckBox(Translator.get("find_dialog.ignore_hidden"));
        this.cbSearchHex = new JCheckBox(Translator.get("find_dialog.search_hex"));

        MuPreferencesAPI prefs = MuConfigurations.getPreferences();
        cbSearchSubdirectories.setSelected(prefs.getVariable(MuPreference.FIND_FILE_SUBDIRECTORIES, true));
        cbSearchArchives.setSelected(prefs.getVariable(MuPreference.FIND_FILE_ARCHIVES, false));
        cbCaseSensitive.setSelected(prefs.getVariable(MuPreference.FIND_FILE_CASE_SENSITIVE, false));
        cbIgnoreHidden.setSelected(prefs.getVariable(MuPreference.FIND_FILE_IGNORE_HIDDEN, false));
        cbSearchHex.setSelected(prefs.getVariable(MuPreference.FIND_FILE_SEARCH_HEX, false));
        cbEncoding.setSelectedItem(prefs.getVariable(MuPreference.FIND_FILE_ENCODING, "UTF-8"));

        cbSearchHex.addActionListener(e -> setHexMode(cbSearchHex.isSelected()));
        setHexMode(cbSearchHex.isSelected());

        gridPanel.add(cbSearchSubdirectories);
        gridPanel.add(cbSearchArchives);
        gridPanel.add(cbIgnoreHidden);
        gridPanel.add(cbCaseSensitive);
        gridPanel.add(cbSearchHex);

        compPanel.addRow(gridPanel, 0);

        yPanel.add(compPanel);


        // Search results
        yPanel.add(new JLabel(Translator.get("find_dialog.search_results")));
        list = new JList<>(listModel);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final AbstractFile file = getSelectedFile();
                if (file == null) {
                    return;
                }
                //final FileTable table = mainFrame.getActivePanel().getFileTable();

                if (e.getClickCount() >= 2) {
                    mainFrame.getActivePanel().tryChangeCurrentFolder(file.getParent(), file, false);
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                final AbstractFile file = getSelectedFile();
                if (file == null) {
                    return;
                }
                FileFrame fileFrame;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F3:
                        fileFrame = ViewerRegistrar.createViewerFrame(mainFrame, file, IconManager.getImageIcon(file.getIcon()).getImage())
                                .returnFocusTo(getFocusOwner());
                        if (cbSearchHex.isSelected()) {
                            fileFrame.setSearchedBytes(edtText.getBytes());
                        } else {
                            fileFrame.setSearchedText(edtText.getText());
                        }
                        break;

                    case KeyEvent.VK_F4:
                        fileFrame = EditorRegistrar.createEditorFrame(mainFrame, file, IconManager.getImageIcon(file.getIcon()).getImage())
                                .returnFocusTo(getFocusOwner());
                        if (cbSearchHex.isSelected()) {
                            fileFrame.setSearchedBytes(edtText.getBytes());
                        } else {
                            fileFrame.setSearchedText(edtText.getText());
                        }
                        break;

                    case KeyEvent.VK_SPACE:
                        mainFrame.getActivePanel().tryChangeCurrentFolder(file.getParent(), file, false);
                        break;

                    case KeyEvent.VK_F5:
                        new CopyDialog(mainFrame, getSelectedFiles()).returnFocusTo(getFocusOwner()).showDialog();
                        break;

                    case KeyEvent.VK_F6:
                        new MoveDialog(mainFrame, getSelectedFiles()).returnFocusTo(getFocusOwner()).showDialog();
                        break;

                    case KeyEvent.VK_F8:
                        new DeleteDialog(mainFrame, getSelectedFiles(), false).returnFocusTo(getFocusOwner()).showDialog();
                        break;

                }
            }

        });
        list.setCellRenderer(new FindFileResultRenderer());
        list.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL]);
        JScrollPane scrollPane = new JScrollPane(list);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Bottom line
        MnemonicHelper mnemonicHelper = new MnemonicHelper();

//        yPanel.add(new JLabel(dial = new SpinningDial()));
        XBoxPanel buttonsPanel = new XBoxPanel();
        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttonsPanel.add(lblTotal = new JLabel());
        buttonsPanel.add(new JLabel(dial = new SpinningDial()));
        buttonsPanel.add(Box.createHorizontalGlue());

        btnNewSearch = new JButton(Translator.get("search"));
        btnNewSearch.addActionListener(this);
        btnNewSearch.setMnemonic(mnemonicHelper.getMnemonic(btnNewSearch));
        buttonGroupPanel.add(btnNewSearch);

        btnStop = new JButton(Translator.get("stop"));
        btnStop.addActionListener(this);
        btnStop.setMnemonic(mnemonicHelper.getMnemonic(btnStop));
        buttonGroupPanel.add(btnStop);

        btnClean = new JButton(Translator.get("clean"));
        btnClean.addActionListener(this);
        btnClean.setMnemonic(mnemonicHelper.getMnemonic(btnClean));
        buttonGroupPanel.add(btnClean);

        btnClose = new JButton(Translator.get("close"));
        btnClose.addActionListener(this);
        btnClose.setMnemonic(mnemonicHelper.getMnemonic(btnClose));
        buttonGroupPanel.add(btnClose);

        buttonsPanel.add(buttonGroupPanel);

        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        contentPane.add(yPanel, BorderLayout.NORTH);

        setInitialFocusComponent(edtFileName);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
        updateButtons();
        getRootPane().setDefaultButton(btnNewSearch);

        setModal(false);
    }


    private void setHexMode(boolean hexMode) {
        cbEncoding.setEnabled(!hexMode);
        edtText.setText("");
        if (textHints != null) {
            textHints.setAutoPopup(false);
            textHints = null;
        }
        if (hexHints != null) {
            hexHints.setAutoPopup(false);
            hexHints = null;
        }
        edtText.setFilterType(cbSearchHex.isSelected() ? InputField.FilterType.HEX_DUMP : InputField.FilterType.ANY_TEXT);
        if (hexMode) {
            List<String> hexHistory = TextHistory.getInstance().getList(TextHistory.Type.HEX_DATA_SEARCH);
            textHints = new ListDataIntelliHints<>(edtText, hexHistory);
            textHints.setCaseSensitive(false);
        } else {
            List<String> textHistory = TextHistory.getInstance().getList(TextHistory.Type.TEXT_SEARCH);
            hexHints = new ListDataIntelliHints<>(edtText, textHistory);
            hexHints.setCaseSensitive(false);
        }
    }


    private void updateButtons() {
        btnNewSearch.setEnabled(job == null);
        btnStop.setEnabled(!btnNewSearch.isEnabled());
        btnClean.setEnabled(!listModel.isEmpty());
    }

    private void start() {
        TextHistory.getInstance().add(TextHistory.Type.FILE_NAME, edtFileName.getText(), true);
        TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, edtText.getText(), true);
        showProgress(true);
        clearResults();
        job = new FindFileJob(mainFrame);
        startDirectory = FileFactory.getFile(edtFromDirectory.getText());
        job.setStartDirectory(startDirectory);
        job.setup(edtFileName.getText(), edtText.getText(), cbSearchSubdirectories.isSelected(), cbSearchArchives.isSelected(),
                cbCaseSensitive.isSelected(), cbIgnoreHidden.isSelected(), cbEncoding.getSelectedItem().toString(),
                cbSearchHex.isSelected(), cbSearchHex.isSelected() ? edtText.getBytes() : null);
        updateResultLabel();
        job.start();
        updateButtons();
        updateRunner = new UpdateRunner();
        updateRunner.execute();
    }

    private void clearResults() {
        if (listModel != null) {
            listModel.clear();
        }
        lblTotal.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnNewSearch) {
            if (job == null) {
                start();
            }
        } else if (e.getSource() == btnStop) {
            if (job != null) {
                job.interrupt();
            }
            job = null;
        } else if (e.getSource() == btnClean) {
            clearResults();
        } else if (e.getSource() == btnClose) {
            cancel();
        }
    }

    private void showProgress(boolean show) {
        dial.setAnimated(show);
    }


    @Override
    public void insertUpdate(DocumentEvent e) {

    }

    @Override
    public void removeUpdate(DocumentEvent e) {

    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }


    private AbstractFile getSelectedFile() {
        int index = list.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        return listModel.get(index);
    }


    private FileSet getSelectedFiles() {
        FileSet files = new FileSet(startDirectory);
        int[] selectedIx = list.getSelectedIndices();
        for (int aSelectedIx : selectedIx) {
            AbstractFile file = listModel.get(aSelectedIx);
            files.add(file);
        }
        return files;
    }


    private void updateResultLabel() {
        lblTotal.setText(Translator.get("find_dialog.found") + ": " + listModel.size() + " ");
    }


    @Override
    public void cancel() {
        if (job != null) {
            job.interrupt();
        }
        MuPreferencesAPI prefs = MuConfigurations.getPreferences();
        prefs.setVariable(MuPreference.FIND_FILE_ARCHIVES, cbSearchArchives.isSelected());
        prefs.setVariable(MuPreference.FIND_FILE_CASE_SENSITIVE, cbCaseSensitive.isSelected());
        prefs.setVariable(MuPreference.FIND_FILE_IGNORE_HIDDEN, cbIgnoreHidden.isSelected());
        prefs.setVariable(MuPreference.FIND_FILE_SEARCH_HEX, cbSearchHex.isSelected());
        prefs.setVariable(MuPreference.FIND_FILE_SUBDIRECTORIES, cbSearchSubdirectories.isSelected());
        prefs.setVariable(MuPreference.FIND_FILE_ENCODING, cbEncoding.getSelectedItem().toString());

        super.cancel();
    }


    @Override
    public void dispose() {
        super.dispose();
        if (updateRunner != null) {
            try {
                updateRunner.cancel(true);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        clearResults();
        updateRunner = null;
        listModel = null;
        job = null;
        list = null;
    }
}
