/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.ui.terminal;

import ch.qos.logback.classic.BasicConfigurator;
import com.google.common.collect.Maps;
import com.jediterm.pty.PtyMain;
import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.display.BackBuffer;
import com.jediterm.terminal.display.JediTerminal;
import com.jediterm.terminal.display.StyleState;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.TerminalWidget;
import com.jediterm.terminal.ui.UIUtil;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.mucommander.cache.WindowsStorage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.nio.charset.Charset;
import java.util.Map;

import com.jediterm.terminal.ui.JediTermWidget;
import com.mucommander.ui.main.MainFrame;
import com.pty4j.PtyProcess;

/**
 * @author Oleg Trifonov
 * Created on 24/10/14.
 */
public class TerminalPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JediTermWidget termWidget;
    private final SettingsProvider settingsProvider;

    private static final String STORAGE_KEY = "TerminalPanel";

    public TerminalPanel(final MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        setBackground(Color.BLACK);
        settingsProvider = new TerminalSettingsProvider();
        termWidget = new JediTermWidget(new Dimension(120, 10), settingsProvider) {
            @Override
            protected com.jediterm.terminal.ui.TerminalPanel createTerminalPanel(SettingsProvider settingsProvider, StyleState styleState, BackBuffer backBuffer) {
                return new JediTerminalPanelEx(settingsProvider, backBuffer, styleState, mainFrame);
            }
        };

        BasicConfigurator.configureDefaultContext();
        if (termWidget.canOpenSession()) {
            openSession(termWidget, createTtyConnector());
        }

        add(termWidget);

        final ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateTerminalSize();
                    }
                });
            }
        };
        addComponentListener(componentListener);
    }

    public void openSession(TerminalWidget terminal, TtyConnector ttyConnector) {
        TerminalSession session = terminal.createTerminalSession(ttyConnector);
        session.start();
    }

    public TtyConnector createTtyConnector() {
        try {
            Map<String, String> envs = Maps.newHashMap(System.getenv());
            envs.put("TERM", "xterm");
            String[] command = new String[]{"/bin/zsh", "--login"};

            if (UIUtil.isWindows) {
                command = new String[]{"cmd.exe"};
            }

            PtyProcess process = PtyProcess.exec(command, envs, null);

            return new PtyMain.LoggingPtyProcessTtyConnector(process, Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public void storeHeight(int height) {
        WindowsStorage.getInstance().put(STORAGE_KEY, new WindowsStorage.Record(0, 0, getWidth(), height));
    }

    public int loadHeight() {
        WindowsStorage.Record rec = WindowsStorage.getInstance().get(STORAGE_KEY);
        return rec != null ? rec.height : -1;
    }


    public void updateTerminalSize() {
        Dimension panelSize = getSize();
        Dimension terminalSize = calcDimension(panelSize.width, panelSize.height);
        termWidget.getTerminalDisplay().requestResize(terminalSize, RequestOrigin.User, 0, new JediTerminal.ResizeHandler() {
            @Override
            public void sizeUpdated(int termWidth, int termHeight, int cursorY) {
                System.out.println("=> " + termWidth + 'x' + termHeight + ' ' + cursorY);
            }
        });

    }


    private Dimension calcDimension(int pixelWidth, int pixelHeight) {
        JediTerminalPanelEx panel = (JediTerminalPanelEx)termWidget.getTerminalPanel();
        Dimension charSize = panel.getCharSize();
        return new Dimension(pixelWidth/charSize.width-2, pixelHeight/charSize.height);
    }

}
