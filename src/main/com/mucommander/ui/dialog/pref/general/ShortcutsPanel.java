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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.commons.util.StringUtils;
import com.mucommander.conf.MuPreference;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.combobox.MuComboBox;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.text.KeyStrokeUtils;
import com.mucommander.ui.theme.ThemeCache;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

/**
 * 'Shortcuts' preferences panel.
 * 
 * @author Arik Hadas, Johann Schmitz
 */
public class ShortcutsPanel extends PreferencesPanel {

    private class Filter extends ShortcutsTable.ActionFilter {

        private ActionCategory actionCategory;
        private String text;

        Filter() {
            this.actionCategory = ActionCategory.ALL;
        }

        @Override
        public boolean accept(String actionId) {
            ActionDescriptor descriptor = ActionProperties.getActionDescriptor(actionId);
            boolean containsText = text == null || text.isEmpty() || descriptor.getDescription().toLowerCase().contains(text) ||
                    descriptor.getLabel().toLowerCase().contains(text);
            return actionCategory.contains(actionId) && containsText;
        }

        void setActionCategory(ActionCategory actionCategory) {
            this.actionCategory = actionCategory;
        }

        void setText(String text) {
            this.text = text != null ? text.toLowerCase() : null;
        }
    }

    private Filter filter = new Filter();
	
	// The table with action mappings
	private ShortcutsTable shortcutsTable;
	
	// Area in which tooltip texts and error messages are shown below the table
	private TooltipBar tooltipBar;
	
	ShortcutsPanel(PreferencesDialog parent) {
		super(parent, Translator.get("shortcuts_panel" + ".title"));
		initUI();
		setPreferredSize(new Dimension(0, 0));
		
		shortcutsTable.addDialogListener(parent);
	}
	
	// - UI initialization ------------------------------------------------------
    // --------------------------------------------------------------------------
	private void initUI() {
		setLayout(new BorderLayout());

		tooltipBar = new TooltipBar();
		shortcutsTable = new ShortcutsTable(tooltipBar);
		
		add(createNorthPanel(), BorderLayout.NORTH);
		add(createCenterPanel(), BorderLayout.CENTER);
		add(createSouthPanel(), BorderLayout.SOUTH);
	}
	
	/**
	 * Returns a panel that contains combo-box of action categories which is used for filtering 
	 * the actions shown at the shortcuts editor table.
	 */
	private JPanel createNorthPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder());
		
		panel.add(createFilteringPanel(), BorderLayout.WEST);
		
		return panel;
	}

	/**
	 * Returns a panel that contains the shortcuts editor table.
	 */
	private JPanel createCenterPanel() {
		JPanel panel = new JPanel(new GridLayout(1,0));
		shortcutsTable.setPreferredColumnWidths(new double[] {0.6, 0.2, 0.2});
		panel.add(new JScrollPane(shortcutsTable));
		return panel;
	}
	
	/**
	 * Returns a panel that contain the tooltip bar and the shortcuts editor buttons below it.
	 */
	private JPanel createSouthPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel tooltipBarPanel = new JPanel(new BorderLayout());
		tooltipBarPanel.add(tooltipBar, BorderLayout.WEST);
		
		panel.add(tooltipBarPanel);
		panel.add(createButtonsPanel());
		
		return panel;
	}
	
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		
		RemoveButton removeButton = new RemoveButton();	
		
		final JButton restoreDefaultButton = new JButton();
		restoreDefaultButton.setAction(new AbstractAction(Translator.get("shortcuts_panel" + ".restore_defaults")) {
			
			public void actionPerformed(ActionEvent e) {
				shortcutsTable.restoreDefaults();
			}
		});
		
		panel.add(removeButton);
		panel.add(restoreDefaultButton);
		
		return panel;
	}

	
	private JPanel createFilteringPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.add(new JLabel(Translator.get("shortcuts_panel.show") + ":"));
		
		final MuComboBox<ActionCategory> combo = new MuComboBox<>();
		combo.addItem(ActionCategory.ALL);
	    for (ActionCategory category : ActionProperties.getActionCategories()) {
            combo.addItem(category);
        }
	    
	    combo.addActionListener(e -> {
                filter.setActionCategory((ActionCategory)combo.getSelectedItem());
				shortcutsTable.updateModel(filter);
				tooltipBar.showDefaultMessage();
        });

	    combo.setSelectedIndex(0);
		
		panel.add(combo);
        panel.add(new JLabel(Translator.get("shortcuts_panel.search") + ":"));

        final JTextField searchText = new JTextField(16);
        panel.add(searchText);

        JTextField shortcutText = new JTextField(15);
        resetShortcutFilterText(shortcutText);
        shortcutText.setHorizontalAlignment(JTextField.CENTER);
        shortcutText.setEditable(false);
        shortcutText.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED]);
        shortcutText.setForeground(ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED][ThemeCache.PLAIN_FILE]);
        // It is required to disable the traversal keys in order to support keys combination that include the TAB key
        setFocusTraversalKeysEnabled(false);
        panel.add(shortcutText);

        addCategoryFilter(searchText, combo, shortcutText);
        addSearchTextFilter(searchText, combo, shortcutText);
        addShortcutFilter(shortcutText);


        return panel;
	}

    private void resetShortcutFilterText(JTextField shortcutText) {
        shortcutText.setText(Translator.get("shortcuts_table.type_in_a_shortcut"));
    }

    private void addShortcutFilter(final JTextField shortcutText) {
        shortcutText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
                if (keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META) {
                    return;
                }

                final KeyStroke pressedKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);

                shortcutText.setText(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(pressedKeyStroke));
                updateFilter(pressedKeyStroke);
                keyEvent.consume();
            }

            public void keyReleased(KeyEvent e) {e.consume();}

   			public void keyTyped(KeyEvent e) {e.consume();}
        });
    }

    private void resetShortcutFilterWhenFocusGained(JComponent componentGainingFocus, final JTextField searchText, final JComboBox<ActionCategory> categoryCombo, final JTextField shortcutText) {
        componentGainingFocus.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {}
            @Override
            public void focusGained(FocusEvent e) {
                updateFilter(searchText, categoryCombo, shortcutText);
            }
        });
    }


    private void addCategoryFilter(final JTextField searchText, final JComboBox<ActionCategory> combo, final JTextField shortcutText) {
        combo.addActionListener(e -> updateFilter(searchText, combo, shortcutText));
        resetShortcutFilterWhenFocusGained(combo, searchText, combo, shortcutText);
            }


    private void addSearchTextFilter(final JTextField searchText, final JComboBox<ActionCategory> categoryCombo, final JTextField shortcutText) {
        searchText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter(searchText, categoryCombo, shortcutText);
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter(searchText, categoryCombo, shortcutText);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        resetShortcutFilterWhenFocusGained(searchText, searchText, categoryCombo, shortcutText);
            }

    private void updateFilter(final JTextField searchText, final JComboBox<ActionCategory> categoryCombo, JTextField shortcutText) {
        final ActionCategory selectedActionCategory = (ActionCategory) categoryCombo.getSelectedItem();
        final String filterText = searchText.getText();

        shortcutsTable.updateModel(new ShortcutsTable.ActionFilter() {
            Locale currentLang = Locale.forLanguageTag(getVariable(MuPreference.LANGUAGE));
            @Override
            public boolean accept(String actionId) {
                return selectedActionCategory.contains(actionId) && (
                    StringUtils.isNullOrBlank(filterText)
                    || StringUtils.containsIgnoreCase(ActionProperties.getActionLabel(actionId), filterText, currentLang)
                    || StringUtils.containsIgnoreCase(ActionProperties.getActionTooltip(actionId), filterText, currentLang)
                    // also search for id's to find typical english computer terms even in non english languages
                    || StringUtils.containsIgnoreCase(ActionProperties.getActionLabelKey(actionId), filterText, currentLang)
                    || StringUtils.containsIgnoreCase(actionId, filterText, currentLang));
            }
        });
        resetShortcutFilterText(shortcutText);
                tooltipBar.showDefaultMessage();
            }

   	private void updateFilter(final KeyStroke pressedKeyStroke) {
        shortcutsTable.updateModel(shortcutsTable.createCurrentAcceleratorsActionFilter(pressedKeyStroke));
    }
		
	///////////////////////
    // PrefPanel methods //
    ///////////////////////
	
	@Override
    protected void commit() {
		shortcutsTable.commitChanges();
		ActionKeymapIO.setModified();
	}
	
	class TooltipBar extends JLabel {
		private String lastActionTooltipShown;
		private String DEFAULT_MESSAGE;
		private static final int MESSAGE_SHOWING_TIME = 3000;
		private MessageRemoverThread currentRemoverThread;
		
		TooltipBar() {
			DEFAULT_MESSAGE = Translator.get("shortcuts_panel.default_message");
			Font tableFont = UIManager.getFont("TableHeader.font");
			setFont(new Font(tableFont.getName(), Font.BOLD, tableFont.getSize()));
			setHorizontalAlignment(JLabel.LEFT);
			setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
			setText(DEFAULT_MESSAGE);
		}
		
		void showActionTooltip(String text) {
			setText(lastActionTooltipShown = text == null ? " " : text);
		}
		
		void showDefaultMessage() {
			setText(DEFAULT_MESSAGE);
		}
		
		void showErrorMessage(String text) {
			setText(text);
			createMessageRemoverThread();
		}
		
		private void createMessageRemoverThread() {
			if (currentRemoverThread != null)
				currentRemoverThread.neutralize();
			(currentRemoverThread = new MessageRemoverThread()).start();
		}
		
		private class MessageRemoverThread extends Thread {
			private boolean stopped = false;
			
			void neutralize() {
				stopped = true;
			}
		
			@Override
            public void run() {
				try {
					Thread.sleep(MESSAGE_SHOWING_TIME);
				} catch (InterruptedException e) {
                    //
                }
				
				if (!stopped)
					showActionTooltip(lastActionTooltipShown);
			}
		}
	}
	
	private class RemoveButton extends JButton implements ListSelectionListener, TableModelListener {
		
		RemoveButton() {
			setEnabled(false);
			setAction(new AbstractAction(Translator.get("remove")) {
				
				public void actionPerformed(ActionEvent e) {
					shortcutsTable.setValueAt(ShortcutsTable.DELETE, shortcutsTable.getSelectedRow(), shortcutsTable.getSelectedColumn());
					shortcutsTable.repaint();
					shortcutsTable.requestFocus();
				}
			});
			
			shortcutsTable.getSelectionModel().addListSelectionListener(this);
			shortcutsTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
			shortcutsTable.getModel().addTableModelListener(this);
		}

		public void valueChanged(ListSelectionEvent e) {
			updateButtonState();
		}

		public void tableChanged(TableModelEvent e) {
			updateButtonState();			
		}
		
		private void updateButtonState() {
			int column = shortcutsTable.getSelectedColumn();
			int row = shortcutsTable.getSelectedRow();
			boolean canRemove = (column == ShortcutsTable.ACCELERATOR_COLUMN_INDEX || column == ShortcutsTable.ALTERNATE_ACCELERATOR_COLUMN_INDEX)
								&& row != -1 && shortcutsTable.getValueAt(shortcutsTable.getSelectedRow(), column) != null;
			setEnabled(canRemove);
		}
	}
}
