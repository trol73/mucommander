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

package com.mucommander.ui.viewer.text;

import com.mucommander.cache.TextHistory;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.viewer.text.tools.ExecPanel;
import com.mucommander.ui.viewer.text.tools.ExecUtils;
import com.mucommander.ui.viewer.text.tools.ProcessParams;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.main.quicklist.ViewedAndEditedFilesQL;
import com.mucommander.ui.theme.*;
import com.mucommander.ui.viewer.EditorRegistrar;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.ui.viewer.ViewerRegistrar;
import com.mucommander.ui.viewer.text.search.FindDialog;
import com.mucommander.ui.viewer.text.search.ReplaceDialog;
import com.mucommander.ui.viewer.text.search.SearchEvent;
import com.mucommander.ui.viewer.text.search.SearchListener;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

/**
 * Text editor implementation used by {@link TextViewer} and {@link TextEditor}.
 *
 * @author Maxence Bernard, Mariusz Jakubowski, Nicolas Rinaudo, Arik Hadas, Oleg Trifonov
 */
class TextEditorImpl implements ThemeListener {

    private static final Insets INSETS = new Insets(4, 3, 4, 3);

	FileFrame frame;

    private TextArea textArea;

    private SearchContext searchContext;

	/** Indicates whether there is a line separator in the original file */
	private boolean lineSeparatorExists;

    private StatusBar statusBar;

    /**
     * Full path to editable file with slash at end
     */
    private AbstractFile file;

    /**
     * If not null then contains included file under cursor that will ber opened by Ctrl(Cmd)+Enter hotkey
     */
    private AbstractFile selectedIncludeFile;

    private JSplitPane splitPane;
    private ExecPanel pnlBuild;
    private int storedSplitDividerSize;
    private int storedSplitterPos;
    private ProcessParams buildParams;


    private KeyListener textAreaKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == KeyEvent.ALT_MASK) {
                if (selectedIncludeFile == null) {
                    return;
                }
                if (textArea.isEditable()) {
                    ImageIcon icon = MuAction.getStandardIcon(EditAction.class);
                    Image img = icon == null ? null : icon.getImage();
                    EditorRegistrar.createEditorFrame(frame.getMainFrame(), selectedIncludeFile, img, (fileFrame -> fileFrame.returnFocusTo(frame)));
                } else {
                    ImageIcon icon = MuAction.getStandardIcon(ViewAction.class);
                    Image img = icon == null ? null : icon.getImage();
                    ViewerRegistrar.createViewerFrame(frame.getMainFrame(), selectedIncludeFile, img, (fileFrame -> fileFrame.returnFocusTo(frame)));
                }
                return;
            }

            int mask = OsFamily.getCurrent() == OsFamily.MAC_OS_X ? KeyEvent.ALT_MASK : KeyEvent.CTRL_MASK;
            if (textArea.isEditable() && e.getKeyChar() == KeyEvent.VK_TAB && e.getModifiers() == mask) {
                ViewedAndEditedFilesQL viewedAndEditedFilesQL = new ViewedAndEditedFilesQL(frame, frame.getFilePresenter().getCurrentFile());
                viewedAndEditedFilesQL.show();
                e.consume();
            }
        }
    };
    private TextMenuHelper menuHelper;


    ////////////////////
	// Initialization //
	////////////////////

	TextEditorImpl(boolean isEditable, StatusBar statusBar) {
		// Initialize text area
        this.textArea = createTextArea(isEditable);
        this.statusBar = statusBar;
        // Listen to theme changes to update the text area if it is visible
		ThemeManager.addCurrentThemeListener(this);
	}

	private TextArea createTextArea(boolean isEditable) {
        TextArea textArea = new TextArea() {
            @Override
            public Insets getInsets() {
                return INSETS;
            }
        };
        textArea.setCurrentLineHighlightColor(ThemeManager.getCurrentColor(Theme.EDITOR_CURRENT_BACKGROUND_COLOR));
        textArea.setAntiAliasingEnabled(true);
		textArea.setEditable(isEditable);
        textArea.addKeyListener(textAreaKeyListener);

        try {
            ThemeManager.readEditorTheme(ThemeManager.getCurrentSyntaxThemeName()).apply(textArea);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //textArea.setCodeFoldingEnabled(true);

		// Use theme colors and font
		textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
		textArea.setCaretColor(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        Color background = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
        textArea.setBackground(background);

        for (int i = 1; i <= textArea.getSecondaryLanguageCount(); i++) {
            textArea.setSecondaryLanguageBackground(i, background);
        }
		textArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
		textArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
		textArea.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));
        textArea.setCodeFoldingEnabled(true);

		textArea.setWrapStyleWord(true);

		textArea.addMouseWheelListener(e -> {
            boolean isCtrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
            if (isCtrlPressed) {
                Font currentFont = textArea.getFont();
                int currentFontSize = currentFont.getSize();
                boolean rotationUp = e.getWheelRotation() < 0;
                if ((!rotationUp && currentFontSize > 1) || rotationUp) {
                    Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFontSize + (rotationUp ? 1 : -1));
                    textArea.setFont(newFont);
                }
            } else {
                textArea.getParent().dispatchEvent(e);
            }
		});

        textArea.addCaretListener(new TextEditorCaretListener(this));
        return textArea;
	}

	/////////////////
	// Search code //
	/////////////////

    void find() {
        SearchListener searchListener = new SearchListener() {
            @Override
            public void searchEvent(SearchEvent e) {
                searchContext = e.getSearchContext();
                String searchString = searchContext.getSearchFor();
                TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, searchString, true);
                SearchResult result = SearchEngine.find(textArea, searchContext);
                if (!result.wasFound()) {
                    beep();
                    setStatusMessage(Translator.get("text_editor.text_not_found"));
                } else {
                    setStatusMessage(Translator.get("text_editor.found") + " " + result.getMarkedCount() + " " + Translator.get("text_editor.matches"));
                    textArea.setCaretPosition(textArea.getSelectionStart());
                }

                // Request the focus on the text area which could be lost after the Find dialog was disposed
                textArea.requestFocus();
            }

            @Override
            public String getSelectedText() {
                return textArea.getSelectedText();
            }
        };
        FindDialog dlg = new FindDialog(frame, searchListener);
        dlg.setSearchString(searchContext != null ? searchContext.getSearchFor() : "");
        dlg.showDialog();
	}


    private void findMore(boolean forward) {
        if (searchContext == null) {
            beep();
            return;
        }
        if (!forward && textArea.getCaretPosition() == 0) {
            beep();
            return;
        }
        searchContext.setSearchForward(forward);
        int savedCaretPosition = textArea.getCaretPosition();
        try {
            int pos = textArea.getSelectionStart();
            if (forward) {
                pos += searchContext.getSearchFor().length();
            }
            textArea.setCaretPosition(pos);
        } catch (IllegalArgumentException ignore) {}
        SearchResult result = SearchEngine.find(textArea, searchContext);
        if (!result.wasFound()) {
            textArea.setCaretPosition(savedCaretPosition);
            setStatusMessage(Translator.get("text_editor.text_not_found"));
            beep();
        } else {
            setStatusMessage("");
            textArea.setCaretPosition(textArea.getSelectionStart());
        }
    }

	void findNext() {
        if (searchContext != null) {
            findMore(true);
            return;
        }
        String last = FindDialog.getLastSearchStr();
        if (last != null) {
            setupSearchContext(FindDialog.getLastSearchStr());
        }
        find();
	}

    void replace() {
        SearchListener searchListener = new SearchListener() {
            @Override
            public void searchEvent(SearchEvent e) {
                searchContext = e.getSearchContext();
                String searchString = searchContext.getSearchFor();
                TextHistory.getInstance().add(TextHistory.Type.TEXT_SEARCH, searchString, true);
                SearchResult result;
                switch (e.getType()) {
                    case FIND:
                        result = SearchEngine.find(textArea, searchContext);
                        break;
                    case REPLACE:
                        result = SearchEngine.replace(textArea, searchContext);
                        break;
                    case REPLACE_ALL:
                        result = SearchEngine.replaceAll(textArea, searchContext);
                        break;
                    default:
                        result = null;
                }
                if (result == null) {
                    return;
                }
                if (!result.wasFound()) {
                    beep();
                    setStatusMessage(Translator.get("text_editor.text_not_found"));
                } else {
                    if (e.getType() == SearchEvent.Type.REPLACE_ALL) {
                        setStatusMessage(Translator.get("text_editor.replaced") + " " + result.getCount() + " " + Translator.get("text_editor.occurrences"));
                    } else {
                        setStatusMessage(Translator.get("text_editor.found") + " " + result.getMarkedCount() + " " + Translator.get("text_editor.matches"));
                    }
                }
            }

            @Override
            public String getSelectedText() {
                return textArea.getSelectedText();
            }
        };
        ReplaceDialog dlg = new ReplaceDialog(frame, searchListener);
        dlg.setSearchString(searchContext != null ? searchContext.getSearchFor() : "");
        dlg.showDialog();
    }


	void findPrevious() {
        if (searchContext == null) {
            String last = FindDialog.getLastSearchStr();
            if (last != null) {
                setupSearchContext(FindDialog.getLastSearchStr());
            }
            find();
        } else {
            findMore(false);
        }
	}

    void gotoLine() {
        GotoLineDialog dlgGoto = new GotoLineDialog(frame, textArea.getLineCount()) {
            @Override
            protected void doGoto(int value) {
                textArea.gotoLine(value);
            }
        };
        dlgGoto.showDialog();
    }


	public boolean isWrap() {
		return textArea.getLineWrap();
	}

	////////////////////////////
	// Package-access methods //
	////////////////////////////

	void wrap(boolean isWrap) {
		textArea.setLineWrap(isWrap);
		textArea.repaint();
	}

	void copy() {
		textArea.copy();
	}

	void cut() {
		textArea.cut();
	}

	void paste() {
		textArea.paste();
	}

	void selectAll() {
		textArea.selectAll();
	}

    void undo() {
        textArea.undoLastAction();
    }

    void redo() {
        textArea.redoLastAction();
    }

	void requestFocus() {
		textArea.requestFocus();
	}

    TextArea getTextArea() {
		return textArea;
	}

    JComponent getEditorComponent() {
//        if (splitPane == null) {
//            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//            splitPane.add(textArea);
//        }
//        return splitPane;
        return textArea;
    }

	void addDocumentListener(DocumentListener documentListener) {
		textArea.getDocument().addDocumentListener(documentListener);
	}

	void read(Reader reader) throws IOException {
		// Feed the file's contents to text area
		textArea.read(reader, null);

		// If there are more than one lines, there is a line separator
		lineSeparatorExists = textArea.getLineCount() > 1;

		// Move cursor to the top
//		textArea.setCaretPosition(0);

	}

	void write(Writer writer) throws IOException {
		Document document = textArea.getDocument();

		// According to the documentation in DefaultEditorKit, the line separator is set to be as the system property
		// if no other line separator exists in the file, but in practice it is not, so this is a workaround for it
		if (!lineSeparatorExists)
			document.putProperty(DefaultEditorKit.EndOfLineStringProperty, System.getProperty("line.separator"));

		try {
			textArea.getUI().getEditorKit(textArea).write(new BufferedWriter(writer), document, 0, document.getLength());
		} catch(BadLocationException e) {
			throw new IOException(e.getMessage());
		}
	}

	//////////////////////////////////
	// ThemeListener implementation //
	//////////////////////////////////

	/**
	 * Receives theme color changes notifications.
	 */
	public void colorChanged(ColorChangedEvent event) {
		switch (event.getColorId()) {
            case Theme.EDITOR_FOREGROUND_COLOR:
                textArea.setForeground(event.getColor());
                break;

            case Theme.EDITOR_BACKGROUND_COLOR:
                textArea.setBackground(event.getColor());
                break;

            case Theme.EDITOR_SELECTED_FOREGROUND_COLOR:
                textArea.setSelectedTextColor(event.getColor());
                break;

            case Theme.EDITOR_SELECTED_BACKGROUND_COLOR:
                textArea.setSelectionColor(event.getColor());
                break;

            case Theme.EDITOR_CURRENT_BACKGROUND_COLOR:
                textArea.setCurrentLineHighlightColor(event.getColor());
                break;
		}
	}

	/**
	 * Receives theme font changes notifications.
	 */
	public void fontChanged(FontChangedEvent event) {
		if (event.getFontId() == Theme.EDITOR_FONT) {
            textArea.setFont(event.getFont());
        }
	}



    void build() {
	    if (buildParams == null) {
	        return;
        }
        showBuildPanel();

        ProcessParams params = ExecUtils.getBuilderParams(getFile());
        if (params != null) {
            pnlBuild.runCommand(params.folder, params.command);
        }
    }

    private void showBuildPanel() {
        if (splitPane == null) {
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.add(frame.getFilePresenter());
            splitPane.setDividerLocation(frame.getHeight()*2/3);
            splitPane.setOneTouchExpandable(true);
            pnlBuild = new ExecPanel(this::closeBuildPanel);

            splitPane.add(pnlBuild);
            frame.getContentPane().remove(frame.getFilePresenter());
            frame.getContentPane().add(splitPane, BorderLayout.CENTER);
            frame.getContentPane().doLayout();
        }
        if (splitPane.getDividerSize() <= 0) {
            splitPane.setDividerSize(storedSplitDividerSize);
        }
        if (storedSplitterPos > 0) {

            splitPane.setDividerLocation(storedSplitterPos);
        }
        pnlBuild.setVisible(true);
        splitPane.doLayout();
        pnlBuild.doLayout();
    }

    private void closeBuildPanel() {
	    if (splitPane == null || pnlBuild == null) {
	        return;
        }
        if (splitPane.getDividerSize() > 0) {
	        storedSplitDividerSize = splitPane.getDividerSize();
            storedSplitterPos = splitPane.getDividerLocation();
        }
	    if (pnlBuild != null) {
	        pnlBuild.setVisible(false);
        }
        splitPane.setDividerSize(0);
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    void setStatusBar(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    void setSyntaxType(FileType fileType) {
        textArea.setFileType(fileType);
        if (statusBar != null) {
            statusBar.setSyntax(fileType.getName());
        }
    }

    void setStatusMessage(String message) {
        if (getStatusBar() != null) {
            getStatusBar().setStatusMessage(message);
        }
    }

    private void beep() {
        // The beep method is called from a separate thread because this method seems to lock until the beep has
        // been played entirely. If the 'Find next' shortcut is left pressed, a series of beeps will be played when
        // the end of the file is reached, and we don't want those beeps to played one after the other as to:
        // 1/ not lock the event thread
        // 2/ have those beeps to end rather sooner than later
        new Thread(() -> Toolkit.getDefaultToolkit().beep()).start();
    }


    void setupSearchContext(String searchStr) {
        searchContext = new SearchContext(searchStr);
    }

    void prepareForEdit(AbstractFile file) {
        this.file = file;
        this.buildParams = ExecUtils.getBuilderParams(file);
        if (menuHelper != null) {
            menuHelper.setBuildable(buildParams != null);
        }
    }

    public void setFrame(FileFrame frame) {
        this.frame = frame;
    }

    void selectIncludeFile(AbstractFile file) {
	    this.selectedIncludeFile = file;
    }

    AbstractFile getFile() {
	    return file;
    }


    void setMenuHelper(TextMenuHelper menuHelper) {
        this.menuHelper = menuHelper;
    }

}
