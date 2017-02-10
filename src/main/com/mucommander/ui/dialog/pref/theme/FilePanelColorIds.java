package com.mucommander.ui.dialog.pref.theme;

import static com.mucommander.ui.theme.ThemeData.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapping between colors of the active file panel and colors of the inactive panel.
 */
class FilePanelColorIds {

	private final Map<Integer, Integer> colorMap;
	private final Map<Integer, Integer> reverseColorMap;

	FilePanelColorIds() {
		super();

		colorMap = new HashMap<>();
		add(FILE_TABLE_BACKGROUND_COLOR, FILE_TABLE_INACTIVE_BACKGROUND_COLOR);
		add(FILE_TABLE_SELECTED_BACKGROUND_COLOR, FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR);
		add(FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR);
		add(FOLDER_FOREGROUND_COLOR, FOLDER_INACTIVE_FOREGROUND_COLOR);
		add(FOLDER_SELECTED_FOREGROUND_COLOR, FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(FILE_FOREGROUND_COLOR, FILE_INACTIVE_FOREGROUND_COLOR);
		add(FILE_SELECTED_FOREGROUND_COLOR, FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ARCHIVE_FOREGROUND_COLOR, ARCHIVE_INACTIVE_FOREGROUND_COLOR);
		add(ARCHIVE_SELECTED_FOREGROUND_COLOR, ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(HIDDEN_FILE_FOREGROUND_COLOR, HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR);
		add(HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(SYMLINK_FOREGROUND_COLOR, SYMLINK_INACTIVE_FOREGROUND_COLOR);
		add(SYMLINK_SELECTED_FOREGROUND_COLOR, SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(MARKED_FOREGROUND_COLOR, MARKED_INACTIVE_FOREGROUND_COLOR);
		add(MARKED_SELECTED_FOREGROUND_COLOR, MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(EXECUTABLE_FOREGROUND_COLOR, EXECUTABLE_INACTIVE_FOREGROUND_COLOR);
		add(EXECUTABLE_SELECTED_FOREGROUND_COLOR, EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(FILE_TABLE_BORDER_COLOR, FILE_TABLE_INACTIVE_BORDER_COLOR);
		add(FILE_TABLE_SELECTED_OUTLINE_COLOR, FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR);
		
		reverseColorMap = colorMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}

	/**
	 * Gets a color by specifying the id of the active file panel.
	 * 
	 * @param isActive
	 *            whether the id of the active or the inactive file panel should be returned
	 * @param activeColorId
	 *            the id of the color in the active file panel
	 * @return the id of the active or inactive panel depending on <code>isActive</code>
	 */
	int getIdByActive(boolean isActive, int activeColorId) {
		return isActive ? activeColorId : colorMap.get(activeColorId);
	}

	/**
	 * Gets a color by specifying the id of the inactive file panel.
	 * 
	 * @param isActive
	 *            whether the id of the active or the inactive file panel should be returned
	 * @param inactiveColorId
	 *            the id of the color in the inactive file panel
	 * @return the id of the active or inactive panel depending on <code>isActive</code>
	 */
	int getIdByInactive(boolean isActive, int inactiveColorId) {
		return isActive ? reverseColorMap.get(inactiveColorId) : inactiveColorId;
	}

	private void add(int active, int inactive) {
		colorMap.put(active, inactive);
	}

}