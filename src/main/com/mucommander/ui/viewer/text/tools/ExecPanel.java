/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.viewer.text.tools;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.shell.Shell;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintStream;

public class ExecPanel extends JPanel implements ProcessListener {

    private JTextArea outputTextArea;
    private SpinningDial dial;
    private JLabel lblDial;
    private final Runnable onClose;

    /** Stream used to send characters to the process' stdin process. */
    private PrintStream processInput;
    /** Process currently running, <code>null</code> if none. */
    private AbstractProcess currentProcess;


    public ExecPanel(Runnable onClose) {
        super();
        this.onClose = onClose;
        setLayout(new BorderLayout());
        //this.add(new JLabel("top"), BorderLayout.NORTH);
        this.add(createOutputArea(), BorderLayout.CENTER);
        //this.add(new JLabel("south"), BorderLayout.SOUTH);
        lblDial = new JLabel(dial = new SpinningDial());
        this.add(lblDial, BorderLayout.SOUTH);
    }

    private JScrollPane createOutputArea() {
        // Creates and initialises the output area.
        outputTextArea = new JTextArea();
        outputTextArea.setLineWrap(true);
        outputTextArea.setCaretPosition(0);
        outputTextArea.setRows(10);
        outputTextArea.setEditable(false);
        //outputTextArea.addKeyListener(this);

        // Applies the current theme to the shell output area.
        outputTextArea.setForeground(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        outputTextArea.setCaretColor(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        outputTextArea.setBackground(ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND_COLOR));
        outputTextArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR));
        outputTextArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR));
        outputTextArea.setFont(ThemeManager.getCurrentFont(Theme.SHELL_FONT));
        outputTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (onClose != null) {
                        onClose.run();
                    }
                    e.consume();
                }
            }
        });

        // Creates a scroll pane on the shell output area.
        return new JScrollPane(outputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public void runCommand(AbstractFile folder, String command) {
        try {
            // Starts the spinning dial.
            dial.setAnimated(true);
            lblDial.setVisible(true);

            // Change 'Run' button to 'Stop'
            //this.btnRunStop.setText(i18n("run_dialog.stop"));

            // Resets the process output area.
            outputTextArea.setText("");
            outputTextArea.setCaretPosition(0);
            outputTextArea.getCaret().setVisible(true);
            outputTextArea.requestFocus();

            // No new command can be entered while a process is running.
            //inputCombo.setEnabled(false);
            currentProcess = Shell.execute(command, folder, this);
            processInput = new PrintStream(currentProcess.getOutputStream(), true);

            // Repaints the dialog.
            repaint();
        } catch (Exception e) {
            // Notifies the user that an error occurred and resets to normal state.
            e.printStackTrace();
            addToTextArea("generic_error " + e.getMessage());
//            switchToRunState();
        }
    }

    /**
     * Appends the specified string to the shell output area.
     * @param s string to append to the shell output area.
     */
    private void addToTextArea(String s) {
        outputTextArea.append(s);
        outputTextArea.setCaretPosition(outputTextArea.getText().length());
        outputTextArea.getCaret().setVisible(true);
        outputTextArea.repaint();
    }

    @Override
    public void processDied(int returnValue) {
        dial.setAnimated(false);
        lblDial.setVisible(false);
    }

    @Override
    public void processOutput(String output) {
        addToTextArea(output);
    }

    @Override
    public void processOutput(byte[] buffer, int offset, int length) {

    }
}
