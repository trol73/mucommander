package com.mucommander.ui.dialog.pref.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.swing.JButton;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.ColorChangeEvent;
import com.mucommander.ui.dialog.pref.PreferencesPanel;

/**
 * <code>CopyColorsButton</code> is a {@link JButton} that copies the values of {@link ColorButton}s from one panel to
 * another. Colors are mapped from active to inactive colors and vice versa by {@link FilePanelColorIds}.
 */
class CopyFilePanelColorsButton extends JButton {

	private static final long serialVersionUID = 1L;

	/**
	 * {@link ColorButton}s of the source file panel by their colorId
	 */
	private final Map<Integer, ColorButton> sourceColorButtons = new HashMap<>();

	/**
	 * {@link ColorButton}s of the target file panel by their colorId
	 */
	private final Map<Integer, ColorButton> targetColorButtons = new HashMap<>();

	/**
	 * Constructs an instance that copies all colors from the other panel set with {@link #setSource(Container)} to the
	 * corresponding {@link ColorButton}s in <code>targetColorButtonsContainer</code>.
	 * 
	 * @param targetColorButtonsContainer
	 */
	public CopyFilePanelColorsButton(Container targetColorButtonsContainer, boolean isTargetActive) {
		super();
		findColorButtons(targetColorButtonsContainer, targetColorButtons);

		FilePanelColorIds colorIds = new FilePanelColorIds();

		addActionListener(e -> {

			for (Integer targetColorId : targetColorButtons.keySet()) {

				final boolean isSourceActive = !isTargetActive;
				int sourceColorId = isTargetActive
		                ? colorIds.getIdByActive(isSourceActive, targetColorId)
		                : colorIds.getIdByInactive(isSourceActive, targetColorId);

				ColorButton sourceButton = sourceColorButtons.get(sourceColorId);

				if (sourceButton != null) {
					Color sourceColor = sourceButton.getCurrentColor();

					ColorButton targetButton = targetColorButtons.get(targetColorId);
					targetButton.colorChanged(new ColorChangeEvent(this, sourceColor));
				}
			}
		});
	}

	/**
	 * Sets the source container having all the {@link ColorButton}s to copy the colors from.
	 * 
	 * @param otherPanelsColorButtonsContainer
	 */
	public void setSource(PreferencesPanel otherPanelsColorButtonsContainer) {

		findColorButtons(otherPanelsColorButtonsContainer, sourceColorButtons);

		setText(Translator.get("theme_editor.copy_colors", otherPanelsColorButtonsContainer.getTitle()));
	}

	/**
	 * Finds all {@link ColorButton}s in <code>otherPanelsColorButtonsContainer</code> and its descendants and stores
	 * them in <code>colorButtons</code>.
	 * 
	 * @param otherPanelsColorButtonsContainer
	 * @param colorButtons
	 */
	private void findColorButtons(final Container otherPanelsColorButtonsContainer,
	        final Map<Integer, ColorButton> colorButtons) {
		final Queue<Component> children = new LinkedList<>();
		children.add(otherPanelsColorButtonsContainer);

		for (Component component = children.poll(); component != null; component = children.poll()) {

			if (component instanceof ColorButton) {

				final ColorButton colorButton = (ColorButton) component;
				colorButtons.put(colorButton.getColorId(), colorButton);

			} else if (component instanceof Container) {
				final Container container = (Container) component;

				final Component[] components;
				synchronized (container.getTreeLock()) {
					components = container.getComponents();
				}
				children.addAll(Arrays.asList(components));
			}
		}
	}
}
