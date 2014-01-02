package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.job.FileJob;
import com.mucommander.job.FindFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.FindFileAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.text.FilePathField;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.ui.viewer.ViewerRegistrar;

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
 * Created by trol on 18/12/13.
 */
public class FindFileDialog extends FocusDialog implements ActionListener, DocumentListener {

    private static String fileName = "";
    private static String searchText = "";

    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(640, 480);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1024, 10000);

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
    private JTextField edtText;
    private JTextField edtFromDirectory;

    private JCheckBox cbSearchSubdirectories;
    private JCheckBox cbCaseSensitive;
    private JCheckBox cbIgnoreHidden;

    private DefaultListModel<AbstractFile> listModel = new DefaultListModel<AbstractFile>();
    private JList<AbstractFile> list;
    protected JLabel lblTotal;

    private class UpdateRunner extends SwingWorker<List<AbstractFile>, AbstractFile> {

        @Override
        protected List<AbstractFile> doInBackground() throws Exception {
            btnNewSearch.setEnabled(false);
            while (job != null && job.getState() != FileJob.FINISHED) {
                checkUpdates();
                try {
                    Thread.sleep(REFRESH_RATE);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            checkUpdates();
            job = null;
            return null;
        }

        @Override
        protected void done() {
            super.done();
            showProgress(false);
            updateButtons();
        }

        @Override
        protected void process(List<AbstractFile> chunks) {
            for (AbstractFile f : chunks) {
                listModel.addElement(f);
                lblTotal.setText(Translator.get("find_dialog.found") + ": " + listModel.size() + " ");
            }
        }


        private void checkUpdates() {
            if (job == null) {
                return;
            }
            List<AbstractFile> jobResults = job.getResults();
            synchronized (jobResults) {
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


        //JPanel fileDetailsPanel = new JPanel(new BorderLayout());

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Add filename field
        this.edtFileName = new JTextField(fileName);
        edtFileName.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("find_dialog.name")+":", edtFileName, 5);

        // Add contains field
        this.edtText = new JTextField(searchText);
        edtText.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("find_dialog.contains")+":", edtText, 5);

        // Create a path field with auto-completion capabilities
        this.edtFromDirectory = new FilePathField();
        this.edtFromDirectory.setText(currentFolder.toString());
        edtFromDirectory.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("find_dialog.initial_directory")+":", edtFromDirectory, 10);

        // Checkboxes
        cbSearchSubdirectories = new JCheckBox(Translator.get("find_dialog.search_subdirectories"));
        cbSearchSubdirectories.setSelected(true);
        cbCaseSensitive = new JCheckBox(Translator.get("find_dialog.case_sensitive"));
        cbIgnoreHidden = new JCheckBox(Translator.get("find_dialog.ignore_hidden"));
        compPanel.addRow("", cbSearchSubdirectories, 10);
        compPanel.addRow("", cbCaseSensitive, 10);
        compPanel.addRow("", cbIgnoreHidden, 10);

        yPanel.add(compPanel);


        // Search results
        yPanel.add(new JLabel(Translator.get("find_dialog.search_results")));
        list = new JList<AbstractFile>(listModel);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final AbstractFile file = getSelectedFile();
                if (file == null) {
                    return;
                }
                final FileTable table = mainFrame.getActivePanel().getFileTable();

                if (e.getClickCount() >= 2) {
                    mainFrame.getActivePanel().tryChangeCurrentFolder(file.getParent());
                    new Thread() {
                        @Override
                        public void run() {
                            for (int i = 0; i < 30; i++) {
                                if ( table.selectFile(file) ) {
                                    break;
                                }
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }.start();
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
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_F3:
                        ViewerRegistrar.createViewerFrame(mainFrame, file, IconManager.getImageIcon(file.getIcon()).getImage());

                        break;
                    case KeyEvent.VK_F4:
                        EditorRegistrar.createEditorFrame(mainFrame, file, IconManager.getImageIcon(file.getIcon()).getImage());
                        break;

                }
            }

        });
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

        btnNewSearch = new JButton(Translator.get("Search"));
        btnNewSearch.addActionListener(this);
        btnNewSearch.setMnemonic(mnemonicHelper.getMnemonic(btnNewSearch));
        buttonGroupPanel.add(btnNewSearch);

        btnStop = new JButton(Translator.get("Stop"));
        btnStop.addActionListener(this);
        btnStop.setMnemonic(mnemonicHelper.getMnemonic(btnStop));
        buttonGroupPanel.add(btnStop);

        btnClean = new JButton(Translator.get("Clean"));
        btnClean.addActionListener(this);
        btnClean.setMnemonic(mnemonicHelper.getMnemonic(btnClean));
        buttonGroupPanel.add(btnClean);

        btnClose = new JButton(Translator.get("Close"));
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


    private void updateButtons() {
        btnNewSearch.setEnabled(job == null);
        btnStop.setEnabled(!btnNewSearch.isEnabled());
        btnClean.setEnabled(listModel.size() > 0);
    }

    private void start() {
        showProgress(true);
        clearResults();
        job = new FindFileJob(mainFrame);
        job.setStartDirectory(FileFactory.getFile(edtFromDirectory.getText()));
        job.setup(edtFileName.getText(), edtText.getText(), cbSearchSubdirectories.isSelected(), cbCaseSensitive.isSelected(), cbIgnoreHidden.isSelected());
        job.start();
        updateButtons();
        new UpdateRunner().execute();
    }

    private void clearResults() {
        listModel.clear();
        lblTotal.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnNewSearch) {
            if ( job == null ) {
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


    @Override
    protected void saveState() {
        super.saveState();
        fileName = edtFileName.getText();
        searchText = edtText.getText();
    }

    private AbstractFile getSelectedFile() {
        int index = list.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        final AbstractFile file = listModel.get(index);
        return file;
    }


}
