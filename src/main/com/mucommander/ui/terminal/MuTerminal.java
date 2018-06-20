/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.terminal;

import ch.qos.logback.classic.BasicConfigurator;
import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalPanelListener;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.TerminalWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.mucommander.cache.WindowsStorage;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.main.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trolsoft.utils.FileUtils;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

/**
 * @author Oleg Trifonov
 * Created on 24/10/14.
 */
public class MuTerminal {

    private static Logger logger;

    private final MainFrame mainFrame;
    private final TerminalWidget termWidget;

    private static final String STORAGE_KEY = "TerminalPanel";

    public MuTerminal(final MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        final SettingsProvider settingsProvider = new TerminalSettingsProvider();
        try {
            prepareLibraries();
        } catch (IOException e) {
            getLogger().error(e.getMessage(), e);
        }
        final MuTerminalTtyConnector ttyConnector = createTtyConnector(getCurrentFolder());

        BasicConfigurator.configureDefaultContext();

        termWidget = new JediTermWidget(settingsProvider) {
            @Override
            protected com.jediterm.terminal.ui.TerminalPanel createTerminalPanel(SettingsProvider settingsProvider, StyleState styleState, TerminalTextBuffer textBuffer) {
                return new JediTerminalPanelEx(settingsProvider, textBuffer, styleState, mainFrame);
            }
        };

        termWidget.setTerminalPanelListener(new TerminalPanelListener() {
            @Override
            public void onPanelResize(final Dimension pixelDimension, final RequestOrigin origin) {
            }

            @Override
            public void onSessionChanged(final TerminalSession currentSession) {
                updateTitle();
            }

            @Override
            public void onTitleChanged(String title) {
                updateTitle();
            }
        });

        termWidget.getComponent().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                updateTitle();
            }

            @Override
            public void focusLost(FocusEvent e) {
                mainFrame.updateWindowTitle();
            }
        });

        if (termWidget.canOpenSession()) {
            openSession(termWidget, ttyConnector);
        }
    }


    private MuTerminalTtyConnector createTtyConnector(String directory) {
        try {
            return new MuTerminalTtyConnector(directory) {
                @Override
                public void close() {
                    super.close();
                    mainFrame.closeTerminalSession();
                }
            };
        } catch (IOException e) {
            getLogger().error(e.getMessage(), e);
            // TODO
            return null;
        }
    }

    public void openSession(TerminalWidget terminal, TtyConnector ttyConnector) {
        TerminalSession session = terminal.createTerminalSession(ttyConnector);
        session.start();
    }


    public void storeHeight(int height) {
        WindowsStorage.getInstance().put(STORAGE_KEY, new WindowsStorage.Record(0, 0, 0, height));
    }

    public int loadHeight() {
        WindowsStorage.Record rec = WindowsStorage.getInstance().get(STORAGE_KEY);
        return rec != null ? rec.height : -1;
    }


    public JComponent getComponent() {
        return termWidget.getComponent();
    }

    public void show(boolean show) {
        termWidget.getComponent().setVisible(show);
    }


    private String getCurrentFolder() {
        String currentFolder = mainFrame.getActivePanel().getCurrentFolder().getAbsolutePath();
        return currentFolder.contains("://") ? null : currentFolder;
    }


    public void updateTitle() {
        mainFrame.setTitle(termWidget.getCurrentSession().getSessionName());
    }


    private void prepareLibraries() throws IOException {
        String jarPath = FileUtils.getJarPath();

        switch (OsFamily.getCurrent()) {
            case WINDOWS:
                FileUtils.copyJarFile("win/x86/libwinpty.dll", jarPath);
                FileUtils.copyJarFile("win/x86/winpty-agent.exe", jarPath);
                break;
            case MAC_OS_X:
                FileUtils.copyJarFile("macosx/x86/libpty.dylib", jarPath);
                FileUtils.copyJarFile("macosx/x86_64/libpty.dylib", jarPath);
                break;
            default:
                FileUtils.copyJarFile("linux/x86/libpty.so", jarPath);
                FileUtils.copyJarFile("linux/x86_64/libpty.so", jarPath);
                break;
        }
    }

    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(MuTerminal.class);
        }
        return logger;
    }

}
