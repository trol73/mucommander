/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package com.mucommander.ui.viewer.hex;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.viewer.FileViewer;
import ru.trolsoft.hexeditor.data.AbstractByteBuffer;
import ru.trolsoft.hexeditor.data.MuCommanderByteBuffer;
import ru.trolsoft.hexeditor.events.OnOffsetChangeListener;
import ru.trolsoft.hexeditor.search.ByteBufferSearchUtils;
import ru.trolsoft.hexeditor.ui.HexTable;
import ru.trolsoft.hexeditor.ui.ViewerHexTableModel;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Hex dump viewer
 * @author Oleg Trifonov
 */
public class HexViewer extends FileViewer {

    private static final String DEFAULT_ENCODING = "windows-1252";

    private HexTable hexTable;
    private ViewerHexTableModel model;
    private AbstractByteBuffer byteBuffer;
    private StatusBar statusBar;
    private String encoding = DEFAULT_ENCODING;
    private byte[] lastSearchBytes;

    private JMenu menuView;
    private JMenuItem gotoItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPrevItem;

    private GotoDialog dlgGoto;
    private FindDialog dlgFind;


    HexViewer() {
        super();

        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        menuView = MenuToolkit.addMenu(Translator.get("hex_viewer.view"), menuMnemonicHelper, null);

        gotoItem = MenuToolkit.addMenuItem(menuView, Translator.get("hex_viewer.goto"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_G, getCtrlOrMetaMask()), this);
        findItem = MenuToolkit.addMenuItem(menuView, Translator.get("hex_viewer.search"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, getCtrlOrMetaMask()), this);
        findNextItem = MenuToolkit.addMenuItem(menuView, Translator.get("hex_viewer.searchNext"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), this);
        findPrevItem = MenuToolkit.addMenuItem(menuView, Translator.get("hex_viewer.searchPrev"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), this);
    }

    private int getCtrlOrMetaMask() {
        if (OsFamily.getCurrent() != OsFamily.MAC_OS_X) {
            return KeyEvent.CTRL_MASK;
        } else {
            return KeyEvent.META_MASK;
        }
    }

    private OnOffsetChangeListener onOffsetChangeListener = new OnOffsetChangeListener() {
        @Override
        public void onChange(long offset) {
            if (statusBar != null) {
                statusBar.setOffset(offset);
                try {
                    if (byteBuffer.getFileSize() > 0) {
                        statusBar.setByteValue(byteBuffer.getByte(offset));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void show(AbstractFile file) throws IOException {
        try {
            byteBuffer = new MuCommanderByteBuffer(file);
            model = new ViewerHexTableModel(byteBuffer);
            model.load();
            hexTable = new HexTable(model);
            hexTable.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
            hexTable.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
            //hexTable.setAlternateBackground(ThemeManager.getCurrentColor(Theme.EDITOR_CURRENT_BACKGROUND_COLOR));
            hexTable.setAlternateBackground(new Color(20, 20, 20));
            hexTable.setOffsetColumnColor(new Color(0, 255, 255));
            hexTable.setAsciiColumnColor(new Color(255, 0, 255));
            hexTable.setHighlightSelectionInAsciiDumpColor(new Color(0, 0, 255));
            hexTable.setAlternateRowBackground(true);

            hexTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
            hexTable.getTableHeader().setFont(new Font("Monospaced", Font.PLAIN, 12));

            hexTable.setOnOffsetChangeListener(onOffsetChangeListener);
            onOffsetChangeListener.onChange(0);

            if (statusBar != null) {
                statusBar.setMaxOffset(file.getSize() - 1);
                statusBar.setOffset(hexTable.getCurrentAddress());
            }

            setComponentToPresent(hexTable);
            getViewport().setBackground(hexTable.getBackground());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar();
            statusBar.setEncoding(encoding);
        }
        return statusBar;
    }

    @Override
    public JMenuBar getMenuBar() {
        JMenuBar menuBar = super.getMenuBar();
        menuBar.add(menuView);
        setMainKeyListener(this, menuBar);
        return menuBar;
    }

    @Override
    protected void saveStateOnClose() {
        try {
            byteBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            getCurrentFile().closePushbackInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void restoreStateOnStartup() {

    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == gotoItem && gotoItem.isEnabled()) {
            gotoOffset();
        } else if (source == findItem && findItem.isEnabled()) {
            findFirst();
        } else if (source == findNextItem && findNextItem.isEnabled()) {
            findNext();
        } else if (source == findPrevItem && findPrevItem.isEnabled()) {
            findPrev();
        } else {
            super.actionPerformed(e);
        }
    }


    private void findFirst() {
        if (dlgFind != null && dlgFind.isVisible()) {
            return;
        }
        dlgFind = new FindDialog(getFrame(), encoding) {
            @Override
            protected void doSearch(byte[] bytes) {
                doSearchFromPos(bytes, 0, true);
            }
        };
        dlgFind.setSearchBytes(lastSearchBytes);
        dlgFind.showDialog();
    }

    private void doSearchFromPos(byte[] bytes, long pos, boolean next) {
        lastSearchBytes = bytes;
        try {
            long lastSearchResult;
            if (next) {
                lastSearchResult = ByteBufferSearchUtils.indexOf(byteBuffer, bytes, pos);
            } else {
                lastSearchResult = ByteBufferSearchUtils.indexOfBackward(byteBuffer, bytes, pos);
            }
            if (lastSearchResult >= 0) {
                hexTable.gotoOffset(lastSearchResult);
                if (statusBar != null) {
                    statusBar.clearStatusMessage();
                }
            } else {
                if (statusBar != null) {
                    statusBar.setStatusMessage(Translator.get("hex_viewer.search_not_found"));
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    private void findNext() {
        if (lastSearchBytes != null && lastSearchBytes.length > 0) {
            long pos = hexTable.getCurrentAddress()+1;
            doSearchFromPos(lastSearchBytes, pos, true);
        }
    }

    private void findPrev() {
        if (lastSearchBytes != null && lastSearchBytes.length > 0) {
            long pos = hexTable.getCurrentAddress()-1;
            doSearchFromPos(lastSearchBytes, pos, false);
        }
    }

    private void gotoOffset() {
        if (dlgGoto != null && dlgGoto.isVisible()) {
            return;
        }
        dlgGoto = new GotoDialog(getFrame(), model.getSize() - 1) {
            @Override
            protected void doGoto(long value) {
                hexTable.gotoOffset(value);
            }
        };
        dlgGoto.showDialog();
    }


    @Override
    public void setSearchedText(String searchedText) {
        try {
            lastSearchBytes = searchedText.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSearchedBytes(byte[] searchedBytes) {
        if (searchedBytes != null) {
            lastSearchBytes = searchedBytes;
        }
    }
}