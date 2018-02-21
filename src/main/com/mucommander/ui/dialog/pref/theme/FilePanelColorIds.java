package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.ui.theme.ThemeData;

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
		add(ThemeData.FILE_TABLE_BACKGROUND_COLOR, ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR);
		add(ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR, ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR);
		add(ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR);
		add(ThemeData.FOLDER_FOREGROUND_COLOR, ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR, ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.FILE_FOREGROUND_COLOR, ThemeData.FILE_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.FILE_SELECTED_FOREGROUND_COLOR, ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.ARCHIVE_FOREGROUND_COLOR, ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR, ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.HIDDEN_FILE_FOREGROUND_COLOR, ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.SYMLINK_FOREGROUND_COLOR, ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR, ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.MARKED_FOREGROUND_COLOR, ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.MARKED_SELECTED_FOREGROUND_COLOR, ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.EXECUTABLE_FOREGROUND_COLOR, ThemeData.EXECUTABLE_INACTIVE_FOREGROUND_COLOR);
		add(ThemeData.EXECUTABLE_SELECTED_FOREGROUND_COLOR, ThemeData.EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR);
		add(ThemeData.FILE_TABLE_BORDER_COLOR, ThemeData.FILE_TABLE_INACTIVE_BORDER_COLOR);
		add(ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR, ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR);
		
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