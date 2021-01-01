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

package com.mucommander.ui.action;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.ui.action.impl.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ActionManager provides methods to retrieve {@link TcAction} instances and invoke them. It keeps track of all the
 * action instances it has created and allows them to be reused within a {@link MainFrame}.
 *
 * <p>MuAction subclasses should not be instantiated directly, <code>getActionInstance</code>
 * methods should be used instead. Using ActionManager to retrieve a MuAction ensures that only one instance
 * exists for a given {@link MainFrame}. This is particularly important because actions are stateful and can be used
 * in several components of a MainFrame at the same time; if an action's state changes, the change must be reflected
 * everywhere the action is used. It is also important for performance reasons: sharing one action throughout a
 * {@link com.mucommander.ui.main.MainFrame} saves some memory and also CPU cycles as some actions listen to particular events to change
 * their state accordingly.
 *
 * @see TcAction
 * @see ActionParameters
 * @see ActionKeymap
 * @author Maxence Bernard, Arik Hadas
 */
public class ActionManager {
	//private static final Logger LOGGER = LoggerFactory.getLogger(ActionManager.class);
	
    /* MuAction id -> factory map */
    //static final Map<String, ActionFactory> actionFactories = new HashMap<>();
    
    /** MainFrame -> MuAction map */
    private static WeakHashMap<MainFrame, Map<ActionParameters, ActionAndIdPair>> mainFrameActionsMap = new WeakHashMap<>();
    
    /** Pattern to resolve the action ID from action class path */
    private final static Pattern PATTERN = Pattern.compile(".*\\.(.*)?Action");

    public static void registerActions() {
    	registerAction(new AddBookmarkAction.Descriptor());
    	registerAction(new AddTabAction.Descriptor());
    	registerAction(new BatchRenameAction.Descriptor());
    	registerAction(new BringAllToFrontAction.Descriptor());
    	registerAction(new CalculateChecksumAction.Descriptor());
    	registerAction(new ChangeDateAction.Descriptor());
		registerAction(new ChangeReplicationAction.Descriptor());
    	registerAction(new ChangeLocationAction.Descriptor());
    	registerAction(new ChangePermissionsAction.Descriptor());
    	registerAction(new CheckForUpdatesAction.Descriptor());
    	registerAction(new CloneTabToOtherPanelAction.Descriptor());
    	registerAction(new CloseDuplicateTabsAction.Descriptor());
    	registerAction(new CloseOtherTabsAction.Descriptor());
    	registerAction(new CloseWindowAction.Descriptor());
    	registerAction(new CloseTabAction.Descriptor());
//    	registerAction(new CommandAction.Descriptor());
    	registerAction(new CompareFoldersAction.Descriptor());
		registerAction(new CompareFolderFilesAction.Descriptor());
    	registerAction(new ConnectToServerAction.Descriptor());
    	registerAction(new CopyAction.Descriptor());
    	registerAction(new CopyFileBaseNamesAction.Descriptor());
    	registerAction(new CopyFileNamesAction.Descriptor());
    	registerAction(new CopyFilePathsAction.Descriptor());
    	registerAction(new CopyFilesToClipboardAction.Descriptor());
    	registerAction(new FocusPreviousAction.Descriptor());
    	registerAction(new FocusNextAction.Descriptor());
    	registerAction(new DeleteAction.Descriptor());
    	registerAction(new DonateAction.Descriptor());
    	registerAction(new DuplicateTabAction.Descriptor());
    	registerAction(new EditAction.Descriptor());
    	registerAction(new EditBookmarksAction.Descriptor());
    	registerAction(new EditCredentialsAction.Descriptor());
    	registerAction(new EmailAction.Descriptor());
    	registerAction(new EmptyTrashAction.Descriptor());
    	registerAction(new ExploreBookmarksAction.Descriptor());
//    	registerAction(new GarbageCollectAction.Descriptor());
    	registerAction(new GoBackAction.Descriptor());
    	registerAction(new GoForwardAction.Descriptor());
    	registerAction(new GoToDocumentationAction.Descriptor());
    	registerAction(new GoToForumsAction.Descriptor());
    	registerAction(new GoToHomeAction.Descriptor());
    	registerAction(new GoToParentAction.Descriptor());
    	registerAction(new GoToParentInBothPanelsAction.Descriptor());
    	registerAction(new GoToParentInOtherPanelAction.Descriptor());
    	registerAction(new GoToRootAction.Descriptor());
    	registerAction(new GoToWebsiteAction.Descriptor());
    	registerAction(new InternalEditAction.Descriptor());
    	registerAction(new InternalViewAction.Descriptor());
    	registerAction(new InvertSelectionAction.Descriptor());
    	registerAction(new LocalCopyAction.Descriptor());
    	registerAction(new MarkAllAction.Descriptor());
    	registerAction(new MarkExtensionAction.Descriptor());
    	registerAction(new MarkGroupAction.Descriptor());
        registerAction(new MarkNextBlockAction.Descriptor());
    	registerAction(new MarkNextPageAction.Descriptor());
        registerAction(new MarkNextRowAction.Descriptor());
        registerAction(new MarkPreviousBlockAction.Descriptor());
    	registerAction(new MarkPreviousPageAction.Descriptor());
        registerAction(new MarkPreviousRowAction.Descriptor());
    	registerAction(new MarkSelectedFileAction.Descriptor());
    	registerAction(new MarkToFirstRowAction.Descriptor());
    	registerAction(new MarkToLastRowAction.Descriptor());
    	registerAction(new MaximizeWindowAction.Descriptor());
    	registerAction(new CombineFilesAction.Descriptor());
    	registerAction(new MinimizeWindowAction.Descriptor());
    	registerAction(new MkdirAction.Descriptor());
		registerAction(new MkfileAction.Descriptor());
    	registerAction(new MoveAction.Descriptor());
    	registerAction(new MoveTabToOtherPanelAction.Descriptor());
    	registerAction(new NewWindowAction.Descriptor());
    	registerAction(new NextTabAction.Descriptor());
    	registerAction(new OpenAction.Descriptor());
		registerAction(new OpenAsAction.Descriptor());
    	registerAction(new OpenInBothPanelsAction.Descriptor());
    	registerAction(new OpenInNewTabAction.Descriptor());
    	registerAction(new OpenInOtherPanelAction.Descriptor());
        registerAction(new OpenLeftInRightPanelAction.Descriptor());
        registerAction(new OpenRightInLeftPanelAction.Descriptor());
//    	registerAction(new OpenLocationAction.Descriptor());
    	registerAction(new OpenNativelyAction.Descriptor());
    	registerAction(new OpenTrashAction.Descriptor());
    	registerAction(new OpenURLInBrowserAction.Descriptor());
    	registerAction(new PackAction.Descriptor());
    	registerAction(new PasteClipboardFilesAction.Descriptor());
    	registerAction(new PermanentDeleteAction.Descriptor());
    	registerAction(new PopupLeftDriveButtonAction.Descriptor());
    	registerAction(new PopupRightDriveButtonAction.Descriptor());
    	registerAction(new PreviousTabAction.Descriptor());
    	registerAction(new QuitAction.Descriptor());
    	registerAction(new RecallNextWindowAction.Descriptor());
    	registerAction(new RecallPreviousWindowAction.Descriptor());
    	registerAction(new RecallWindow10Action.Descriptor());
    	registerAction(new RecallWindow1Action.Descriptor());
    	registerAction(new RecallWindow2Action.Descriptor());
    	registerAction(new RecallWindow3Action.Descriptor());
    	registerAction(new RecallWindow4Action.Descriptor());
    	registerAction(new RecallWindow5Action.Descriptor());
    	registerAction(new RecallWindow6Action.Descriptor());
    	registerAction(new RecallWindow7Action.Descriptor());
    	registerAction(new RecallWindow8Action.Descriptor());
    	registerAction(new RecallWindow9Action.Descriptor());
    	registerAction(new RecallWindowAction.Descriptor());
    	registerAction(new RefreshAction.Descriptor());
    	registerAction(new RenameAction.Descriptor());
    	registerAction(new ReportBugAction.Descriptor());
    	registerAction(new RevealInDesktopAction.Descriptor());
    	registerAction(new ReverseSortOrderAction.Descriptor());
    	registerAction(new RunCommandAction.Descriptor());
        registerAction(new SelectPreviousBlockAction.Descriptor());
        registerAction(new SelectPreviousPageAction.Descriptor());
        registerAction(new SelectPreviousRowAction.Descriptor());
        registerAction(new SelectNextBlockAction.Descriptor());
        registerAction(new SelectNextPageAction.Descriptor());
        registerAction(new SelectNextRowAction.Descriptor());
    	registerAction(new SelectFirstRowAction.Descriptor());
    	registerAction(new SelectLastRowAction.Descriptor());
		registerAction(new LeftArrowAction.Descriptor());
		registerAction(new RightArrowAction.Descriptor());
    	registerAction(new SetSameFolderAction.Descriptor());
    	registerAction(new SetTabTitleAction.Descriptor());
    	registerAction(new ShowAboutAction.Descriptor());
    	registerAction(new ShowBookmarksQLAction.Descriptor());
    	registerAction(new CustomizeCommandBarAction.Descriptor());
        registerAction(new ShowDebugConsoleAction.Descriptor());
        registerAction(new ShowFilePropertiesAction.Descriptor());
        registerAction(new ShowFilePopupMenuAction.Descriptor());
    	registerAction(new ShowKeyboardShortcutsAction.Descriptor());
    	registerAction(new ShowParentFoldersQLAction.Descriptor());
    	registerAction(new ShowPreferencesAction.Descriptor());
    	registerAction(new ShowRecentExecutedFilesQLAction.Descriptor());
    	registerAction(new ShowRecentLocationsQLAction.Descriptor());
    	registerAction(new ShowRootFoldersQLAction.Descriptor());
        registerAction(new ShowRecentViewedFilesQLAction.Descriptor());
        registerAction(new ShowRecentEditedFilesQLAction.Descriptor());
		registerAction(new ShowEditorBookmarksQLAction.Descriptor());
    	registerAction(new ShowServerConnectionsAction.Descriptor());
    	registerAction(new ShowTabsQLAction.Descriptor());
    	registerAction(new SortByDateAction.Descriptor());
    	registerAction(new SortByExtensionAction.Descriptor());
    	registerAction(new SortByGroupAction.Descriptor());
    	registerAction(new SortByNameAction.Descriptor());
    	registerAction(new SortByOwnerAction.Descriptor());
    	registerAction(new SortByPermissionsAction.Descriptor());
    	registerAction(new SortBySizeAction.Descriptor());
    	registerAction(new SplitEquallyAction.Descriptor());
    	registerAction(new SplitFileAction.Descriptor());
    	registerAction(new SplitHorizontallyAction.Descriptor());
    	registerAction(new SplitVerticallyAction.Descriptor());
    	registerAction(new StopAction.Descriptor());
		registerAction(new ToggleSinglePanelAction.Descriptor());
    	registerAction(new SwapFoldersAction.Descriptor());
    	registerAction(new SwitchActiveTableAction.Descriptor());
    	registerAction(new ToggleAutoSizeAction.Descriptor());
//    	registerAction(new ToggleColumnAction.Descriptor());
    	registerAction(new ToggleCommandBarAction.Descriptor());
    	registerAction(new ToggleDateColumnAction.Descriptor());
    	registerAction(new ToggleExtensionColumnAction.Descriptor());
    	registerAction(new ToggleGroupColumnAction.Descriptor());
    	registerAction(new ToggleHiddenFilesAction.Descriptor());
    	registerAction(new ToggleLockTabAction.Descriptor());
    	registerAction(new ToggleOwnerColumnAction.Descriptor());
    	registerAction(new TogglePermissionsColumnAction.Descriptor());
    	registerAction(new ToggleShowFoldersFirstAction.Descriptor());
    	registerAction(new ToggleFoldersAlwaysAlphabeticalAction.Descriptor());
    	registerAction(new ToggleSizeColumnAction.Descriptor());
    	registerAction(new ToggleStatusBarAction.Descriptor());
    	registerAction(new ToggleToolBarAction.Descriptor());
    	registerAction(new ToggleTreeAction.Descriptor());
    	registerAction(new UnmarkAllAction.Descriptor());
    	registerAction(new UnmarkGroupAction.Descriptor());
		registerAction(new MarkEmptyFilesAction.Descriptor());
    	registerAction(new UnpackAction.Descriptor());
    	registerAction(new ViewAction.Descriptor());
        registerAction(new ViewAsAction.Descriptor());
		registerAction(new EditAsAction.Descriptor());
        registerAction(new TerminalAction.Descriptor());
        registerAction(new FindFileAction.Descriptor());
        registerAction(new CalculatorAction.Descriptor());
        registerAction(new CreateSymlinkAction.Descriptor());
        registerAction(new LocateSymlinkAction.Descriptor());
        registerAction(new EditCommandsAction.Descriptor());
        registerAction(new TerminalPanelAction.Descriptor());
        registerAction(new ShowFoldersSizeAction.Descriptor());
		registerAction(new ToggleTableViewModeFullAction.Descriptor());
		registerAction(new ToggleTableViewModeCompactAction.Descriptor());
		registerAction(new ToggleTableViewModeShortAction.Descriptor());
		registerAction(new EjectDriveAction.Descriptor());
		registerAction(new CompareFilesAction.Descriptor());
		registerAction(new TogglePanelPreviewModeAction.Descriptor());
		registerAction(new TextEditorsListAction.Descriptor());
		registerAction(new UserMenuAction.Descriptor());
    }

	public static void registerCommandsActions() {
		// register "open with" commands as actions, to allow for keyboard shortcuts for them
		for (Command command : CommandManager.commands()) {
			if (command.getType() == CommandType.NORMAL_COMMAND) {
				ActionManager.registerAction(new CommandAction.Descriptor(command));
			}
		}
	}

    /**
     * Registration method for MuActions.
     * 
     * @param actionDescriptor - ActionDescriptor instance of the action.
     */
    private static void registerAction(ActionDescriptor actionDescriptor) {
    	ActionProperties.addActionDescriptor(actionDescriptor);
    }
    
    /**
     * Return all ids of the registered actions.
     * 
     * @return Enumeration of all registered actions' ids.
     */
    public static Iterator<String> getActionIds() {
		return ActionProperties.actionDescriptors.keySet().iterator();
    }
    
    /**
     * Return the id of MuAction in a given path.
     * 
     * @param actionClassPath - path to MuAction class.
     * @return String representing the id of the MuAction in the specified path. null is returned if the given path is invalid.
     */
    public static String extrapolateId(String actionClassPath) {
    	if (actionClassPath == null) {
			return null;
		}
    	
    	Matcher matcher = PATTERN.matcher(actionClassPath);
    	return matcher.matches() ? matcher.group(1) : actionClassPath;
    }
    
    /**
     * Checks whether an MuAction is registered.
     * 
     * @param actionId - id of MuAction.
     * @return true if an MuAction which is represented by the given id is registered, otherwise return false.
     */
    public static boolean isActionExist(String actionId) {    	
    	//return actionId != null && actionFactories.containsKey(actionId);
		return actionId != null && ActionProperties.actionDescriptors.containsKey(actionId);
    }

    /**
     * Convenience method that returns an instance of the action corresponding to the given <code>Command</code>,
     * and associated with the specified <code>MainFrame</code>. This method gets the ID of the relevant action,
     * passes it to {@link #getActionInstance(String, MainFrame)} and returns the {@link TcAction} instance.
     *
     * @param command the command that is invoked by the returned action
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given action ID and MainFrame, <code>null</code> if the
     * @see #getActionInstance(String, MainFrame)
     * action could not be found or could not be instantiated.
     */
    public static TcAction getActionInstance(Command command, MainFrame mainFrame) {
        return getActionInstance(new CommandAction.Descriptor(command).getId(), mainFrame);
    }

    /**
     * Convenience method that returns an instance of the action denoted by the given ID, and associated with the
     * specified <code>MainFrame</code>. This method creates an ActionParameters with no initial property, passes it to
     * {@link #getActionInstance(ActionParameters, MainFrame)} and returns the {@link TcAction} instance.
     *
     * @param actionId ID of the action to instantiate
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given action ID and MainFrame, <code>null</code> if the
     * @see #getActionInstance(ActionParameters, MainFrame)
     * action could not be found or could not be instantiated.
     */
    public static TcAction getActionInstance(String actionId, MainFrame mainFrame) {
        return getActionInstance(new ActionParameters(actionId), mainFrame);
    }

    public static Optional<TcAction> getActionInstance2(ActionParameters actionParameters, MainFrame mainFrame) {
    	return Optional.ofNullable(getActionInstance(actionParameters,mainFrame));
    }
    /**
     * Returns an instance of the MuAction class denoted by the given ActionParameters and for the
     * specified MainFrame. If an existing instance corresponding to the same ActionParameters and MainFrame is found,
     * it is simply returned.
     * If no matching instance could be found, a new instance is created, added to the internal action instances map
     * (for further use) and returned.
     * If the action denoted by the specified ActionParameters cannot be found or cannot be instantiated,
     * <code>null</code> is returned.
     *
     * @param actionParameters a descriptor of the action to instantiate with initial properties
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given ActionParameters and MainFrame, <code>null</code> if the
     * MuAction action denoted by the ActionParameters could not be found or could not be instantiated.
     */
    public static TcAction getActionInstance(ActionParameters actionParameters, MainFrame mainFrame) {
		Map<ActionParameters, ActionAndIdPair> mainFrameActions = mainFrameActionsMap.computeIfAbsent(mainFrame, k -> new Hashtable<>());

		// Looks for an existing MuAction instance used by the specified MainFrame
        if (mainFrameActions.containsKey(actionParameters)) {
        	return mainFrameActions.get(actionParameters).getAction();
        } else {
            String actionId = actionParameters.getActionId();

            // Looks for the action's factory
            //ActionFactory actionFactory = actionFactories.get(actionId);
			ActionFactory actionFactory = ActionProperties.actionDescriptors.get(actionId);
            if (actionFactory == null) {
//            	LOGGER.debug("couldn't initiate action: " + actionId + ", its factory wasn't found");
//            	return null;
            	throw new IllegalStateException("couldn't initiate action: " + actionId + ", its factory wasn't found");
            }

            Map<String,Object> properties = actionParameters.getInitProperties();
            // If no properties hashtable is specified in the action descriptor
            if (properties == null) {
            	properties = Collections.emptyMap();
            }
            // else clone the hashtable to ensure that it doesn't get modified by action instances.
            // Since cloning is an expensive operation, this is done only if the hashtable is not empty.
            else if(!properties.isEmpty()) {
                properties = new Hashtable<>(properties);
            }

            // Instantiate the MuAction class
            TcAction action = actionFactory.createAction(mainFrame, properties);
            mainFrameActions.put(actionParameters, new ActionAndIdPair(action, actionId));

            // If the action's label has not been set yet, use the action descriptor's
            if (action.getLabel() == null) {
                // Retrieve the standard label entry from the dictionary and use it as this action's label
                String label = ActionProperties.getActionLabel(actionId);
                
                // Append '...' to the label if this action invokes a dialog when performed
                if (action.getClass().isAnnotationPresent(InvokesDialog.class)) {
                    label += "...";
                }

                action.setLabel(label);

                // Looks for a standard label entry in the dictionary and if it is defined, use it as this action's tooltip
                String tooltip = ActionProperties.getActionTooltip(actionId);
                if (tooltip != null)
                    action.setToolTipText(tooltip);
            }
            
            // If the action's accelerators have not been set yet, use the ones from ActionKeymap
            if (action.getAccelerator() == null) {
                // Retrieve the standard accelerator (if any) and use it as this action's accelerator
                KeyStroke accelerator = ActionKeymap.getAccelerator(actionId);
                if (accelerator != null) {
                    action.setAccelerator(accelerator);
                }

                // Retrieve the standard alternate accelerator (if any) and use it as this action's alternate accelerator
                accelerator = ActionKeymap.getAlternateAccelerator(actionId);
                if (accelerator != null) {
                    action.setAlternateAccelerator(accelerator);
                }
            }
            
            // If the action's icon has not been set yet, use the action descriptor's
            if (action.getIcon() == null) {
                // Retrieve the standard icon image (if any) and use it as the action's icon
                ImageIcon icon = ActionProperties.getActionIcon(actionId);
                if (icon != null) {
                    action.setIcon(icon);
                }
            }
            
            return action;
        }
    }


    /**
     * Returns a ArrayList of all MuAction instances matching the specified action id.
     *
     * @param muActionId the MuAction id to compare instances against
     * @return  a ArrayList of all MuAction instances matching the specified action id
     */
    static List<TcAction> getActionInstances(String muActionId) {
        List<TcAction> actionInstances = new ArrayList<>();

        // Iterate on all MainFrame instances
        for (Map<ActionParameters, ActionAndIdPair> actionParametersActionAndIdPairHashtable : mainFrameActionsMap.values()) {
            // Iterate on all the MainFrame's actions and their ids pairs
            for (ActionAndIdPair actionAndIdPair : actionParametersActionAndIdPairHashtable.values()) {
                if (actionAndIdPair.getId().equals(muActionId)) {
                    // Found an action matching the specified class
                    actionInstances.add(actionAndIdPair.getAction());
                    // Jump to the next MainFrame
                    break;
                }
            }
        }

        return actionInstances;
    }

    /**
     * Convenience method that retrieves an instance of the action denoted by the given ID and associated
     * with the given {@link MainFrame} and calls {@link TcAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MuAction could not be found or could not be instantiated.
     *
     * @param actionId ID of the action to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise 
     */
    public static boolean performAction(String actionId, MainFrame mainFrame) {
        return performAction(new ActionParameters(actionId), mainFrame);
    }

    /**
     * Convenience method that retrieves an instance of the MuAction denoted by the given {@link ActionParameters}
     * and associated with the given {@link com.mucommander.ui.main.MainFrame} and calls {@link TcAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MuAction could not be found or could not be instantiated.
     *
     * @param actionParameters the ActionParameters of the action to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise
     */
    public static boolean performAction(ActionParameters actionParameters, MainFrame mainFrame) {
        TcAction action = getActionInstance(actionParameters, mainFrame);

        if (action == null) {
            return false;
        }

        action.performAction();

        return true;
    }
    
    /**
     *  Helper class to represent a pair of instance and id of MuAction.
     */
    private static class ActionAndIdPair {
    	private TcAction action;
    	private String id;
    	
    	ActionAndIdPair(TcAction action, String id) {
    		this.action = action;
    		this.id = id;
    	}
    	
    	public TcAction getAction() { return action; }
    	
    	public String getId() { return id; }
    }
}
