package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.search.SearchTask;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.SearchFilesAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.utils.Callback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * @author sstolpovskiy
 */
public class SearchDialog extends FocusDialog implements ActionListener, KeyListener, MouseListener {

    // - UI components -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Main frame this dialog depends on.
     */
    private MainFrame mainFrame;

    /**
     * Text field used for input search directory.
     */
    private JTextField folderInputField;

    /**
     * Text field used for search string input.
     */
    private JTextField searchStringInputField;

    /**
     * Run/stop button.
     */
    private JButton runStopButton;
    /**
     * Cancel button.
     */
    private JButton cancelButton;

    /**
     * Text area used to display the shell output.
     */
    private JList outputTextArea;

    /**
     * Used to let the user known that the command is still running.
     */
    private SpinningDial dial;


    // - Misc. class variables -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Minimum dimensions for the dialog.
     */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(600, 400);

    private SearchTask currentProcess;
    private DefaultListModel listModel;

    public SearchDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(SearchFilesAction.Descriptor.ACTION_ID), mainFrame);
        this.mainFrame = mainFrame;
        // Initializes the dialog's UI.
        Container contentPane = getContentPane();
        contentPane.add(createInputArea(), BorderLayout.NORTH);
        contentPane.add(createOutputArea(), BorderLayout.CENTER);
        contentPane.add(createButtonsArea(), BorderLayout.SOUTH);

        // Sets default items.
        setInitialFocusComponent(searchStringInputField);
        getRootPane().setDefaultButton(runStopButton);

        FileSet selectedFiles = mainFrame.getActiveTable().getSelectedFiles();
        folderInputField.setText(selectedFiles.getBaseFolder().getAbsolutePath());
        searchStringInputField.setText("*.*");

        // Makes sure that any running process will be killed when the dialog is closed.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (currentProcess != null) {
                    currentProcess.cancel(true);
                }
            }
        });

        // Sets the dialog's minimum size.
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }

    /**
     * Creates the shell input part of the dialog.
     *
     * @return the shell input part of the dialog.
     */
    private YBoxPanel createInputArea() {
        YBoxPanel mainPanel;

        mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("search_dialog.folder_label")));
        mainPanel.add(folderInputField = new JTextField());
        folderInputField.setEnabled(true);
        mainPanel.add(new JLabel(Translator.get("search_dialog.file_label")));
        mainPanel.add(searchStringInputField = new JTextField());
        searchStringInputField.setEnabled(true);

        // Adds a textual description of the shell output area.
        mainPanel.addSpace(10);

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel(dial = new SpinningDial()));
        mainPanel.add(labelPanel);

        return mainPanel;
    }

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Creates the dialog's shell output area.
     *
     * @return a scroll pane containing the dialog's shell output area.
     */
    private JScrollPane createOutputArea() {
        // Creates and initialises the output area.
        listModel = new DefaultListModel();
        outputTextArea = new JList(listModel);
        outputTextArea.addKeyListener(this);
        outputTextArea.addMouseListener(this);
        outputTextArea.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // Applies the current theme to the shell output area.
        outputTextArea.setForeground(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        outputTextArea.setBackground(ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND_COLOR));
        outputTextArea.setFont(ThemeManager.getCurrentFont(Theme.SHELL_FONT));

        // Creates a scroll pane on the shell output area.
        return new JScrollPane(outputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Creates a panel containing the dialog's buttons.
     *
     * @return a panel containing the dialog's buttons.
     */
    private XBoxPanel createButtonsArea() {
        // Buttons panel
        XBoxPanel buttonsPanel;

        buttonsPanel = new XBoxPanel();

        // 'Clear history' button.
        JLabel statusLabel = new JLabel();
        buttonsPanel.add(statusLabel);

        // Separator.
        buttonsPanel.add(Box.createHorizontalGlue());

        // 'Run / stop' and 'Cancel' buttons.
        buttonsPanel.add(DialogToolkit.createOKCancelPanel(
                runStopButton = new JButton(Translator.get("search_dialog.do_search")),
                cancelButton = new JButton(Translator.get("cancel")),
                getRootPane(),
                this));


        return buttonsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == runStopButton) {
            runSearchTask(folderInputField.getText(), searchStringInputField.getText());
        } else if (source == cancelButton) {
            if (currentProcess != null) {
                currentProcess.cancel(true);
            }
            dial.setAnimated(false);
            repaint();
        }
    }

    private void runSearchTask(String targetFolder, String searchString) {
        // Starts the spinning dial.
        dial.setAnimated(true);
        // Resets the process output area.
        listModel.clear();
        outputTextArea.requestFocus();

        // No new command can be entered while a process is running.
        folderInputField.setEnabled(false);
        searchStringInputField.setEnabled(false);
        currentProcess = new SearchTask(targetFolder, searchString, listModel, new Callback() {
            @Override
            public void call() {
                dial.setAnimated(false);
                outputTextArea.requestFocus();

                // No new command can be entered while a process is running.
                folderInputField.setEnabled(true);
                searchStringInputField.setEnabled(true);
                try {
                    currentProcess.get();
                } catch (ExecutionException e) {
                    InformationDialog.showErrorDialog(SearchDialog.this, "Something Bad Happened", "Search file " + searchStringInputField.getText() + " failed", e.getMessage() + ". See exception details.", e);
                } catch (Exception ignored) {
                    //interrupted should be here
                }
            }
        });
        currentProcess.execute();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            e.consume();
            int index = outputTextArea.locationToIndex(e.getPoint());
            if (index >= 0) {
                Object o = outputTextArea.getModel().getElementAt(index);
                FolderPanel folderPanel = this.mainFrame.getActivePanel();
                File file = new File(o.toString());
                folderPanel.tryChangeCurrentFolder(file.isDirectory() ? file.getAbsolutePath() : file.getParent());
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
