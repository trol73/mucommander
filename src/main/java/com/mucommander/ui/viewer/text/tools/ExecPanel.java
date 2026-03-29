/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2018 Oleg Trifonov
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
import ru.trolsoft.utils.StringStream;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;


public class ExecPanel extends JPanel implements ProcessListener {

    private ExecOutputTextPane outputPane;
    private SpinningDial dial;
    private JLabel lblDial;
    private final StringStream stringStream = new StringStream();

    /** Stream used to send characters to the process' stdin process. */
    private PrintStream processInput;
    /** Process currently running, <code>null</code> if none. */
    private AbstractProcess currentProcess;


    public ExecPanel(Runnable onClose, OnClickFileHandler onFileClickHandler) {
        super();
        setLayout(new BorderLayout());
        outputPane = new ExecOutputTextPane(onClose, onFileClickHandler);
        JScrollPane scrollPane = new JScrollPane(outputPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);
        lblDial = new JLabel(dial = new SpinningDial());
        this.add(lblDial, BorderLayout.SOUTH);
    }

    public void runCommand(AbstractFile folder, String command) {
        try {
            // Starts the spinning dial.
            dial.setAnimated(true);
            lblDial.setVisible(true);

            // Change 'Run' button to 'Stop'
            //this.btnRunStop.setText(i18n("run_dialog.stop"));

            // Resets the process outputPane area.
            outputPane.clear();
            stringStream.clear();

            // No new command can be entered while a process is running.
            //inputCombo.setEnabled(false);
            currentProcess = Shell.execute(command, folder, this);
            processInput = new PrintStream(currentProcess.getOutputStream(), true);

            // Repaints the dialog.
            repaint();
        } catch (Exception e) {
            // Notifies the user that an error occurred and resets to normal state.
            e.printStackTrace();
            outputPane.addLine("generic_error " + e.getMessage()); // TODO
//            switchToRunState();
        }
    }


    @Override
    public void processDied(int returnValue) {
        dial.setAnimated(false);
        lblDial.setVisible(false);
        if (stringStream.hasRemains()) {
            outputPane.addLine(stringStream.getRemains());
        }
    }

    @Override
    public void processOutput(String output) {
        stringStream.add(output);
        while (stringStream.hasCompleted()) {
            outputPane.addLine(stringStream.getNext());
        }
    }

    @Override
    public void processOutput(byte[] buffer, int offset, int length) {

    }
}
