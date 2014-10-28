package com.mucommander.ui.terminal;

import com.jediterm.terminal.display.BackBuffer;
import com.jediterm.terminal.display.StyleState;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.mucommander.ui.main.MainFrame;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

/**
 * @author Oleg Trifonov
 * Created on 27/10/14.
 */
public class JediTerminalPanelEx extends com.jediterm.terminal.ui.TerminalPanel {

    private final MainFrame mainFrame;

    public JediTerminalPanelEx(@NotNull SettingsProvider settingsProvider, @NotNull BackBuffer backBuffer,
                               @NotNull StyleState styleState, MainFrame mainFrame) {
        super(settingsProvider, backBuffer, styleState);
        this.mainFrame = mainFrame;
    }


    public Dimension getCharSize() {
        return myCharSize;
    }


    @Override
    public void processKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F2 && e.getModifiers() == KeyEvent.SHIFT_MASK) {
            mainFrame.showTerminalPanel(false);
            return;
        }
        super.processKeyEvent(e);
    }
}
