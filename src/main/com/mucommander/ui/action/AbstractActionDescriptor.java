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

import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * AbstractActionDescriptor is an abstract class which implements ActionDescriptor interface.
 * this class implements the following methods which are common to all action descriptors:
 * {@link ActionDescriptor#getLabel()}
 * {@link ActionDescriptor#getIcon()}
 * {@link ActionDescriptor#getTooltip()}
 * 
 * @author Arik Hadas
 */
public abstract class AbstractActionDescriptor implements ActionDescriptor {
    protected static final int CTRL_OR_META_DOWN_MASK = OsFamily.MAC_OS_X.isCurrent() ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;

//        static int count;
//    public AbstractActionDescriptor() {
//        count++;
//        if (count % 10 == 0) {
//            System.out.println(count);
//            if (count == 200) {
//                new Exception().printStackTrace();
//            }
//        }
//    }
	
	//////////////////////////////////
	//// ActionDescriptor methods ////
	//////////////////////////////////
	
    public String getLabel() {
        String label = getStandardLabel();
        return label != null ? label : getLabelKey();
    }

    public ImageIcon getIcon() {
        return getStandardIcon(getId());
    }
    
    public String getTooltip() {
        return getStandardTooltip(getId());
    }
    
    public String getDescription() {
    	String tooltip = getTooltip();
    	return tooltip == null ? getLabel() : tooltip;
    }
    
    /**
     * Returns the dictionary key for action's label, using the following standard naming convention:
     * <pre>
     *      action_id.label
     * </pre>
     * where <code>action_id</code> is a String identification of the action, as returned by <code>getId()</code>.
     *
     * @return the standard dictionary key for the action's label
     */
    public String getLabelKey() {
		return getId()+".label";
	}

    /**
     * Implements {@link ActionDescriptor#isParameterized()} by returning <code>false</code>, which suits most actions.
     * This method can be overridden to change this behavior.
     *
     * @return <code>false</code>
     */
    public boolean isParameterized() {
        return false;
    }


    /////////////////////////
    //// Private methods ////
    /////////////////////////
    
    /**
     * Queries {@link Translator} for a label corresponding to the action using the standard naming convention.
     * Returns the label or <code>null</code> if no corresponding entry was found in the dictionary.
     *
     * @return the standard label corresponding to the MuAction, <code>null</code> if none was found
     */
    private String getStandardLabel() {
    	String labelKey = getLabelKey();
        if (!Translator.hasValue(labelKey, true))
            return null;

        return Translator.get(labelKey);
    }
    
    /**
     * Queries {@link IconManager} for an image icon corresponding to the specified action using standard icon path
     * conventions. Returns the image icon, <code>null</code> if none was found.
     *
     * @param actionId a String identification of MuAction
     * @return the standard icon image corresponding to the specified MuAction, <code>null</code> if none was found
     */
    protected static ImageIcon getStandardIcon(String actionId) {
        // Look for an icon image file with the /action/<action id>.png path and use it if it exists
        String iconPath = getStandardIconPath(actionId);
        return ResourceLoader.getResourceAsURL(iconPath) == null ? null : IconManager.getIcon(iconPath);
    }
    
    /**
     * Returns the standard path to the icon image for the specified {@link MuAction} id. 
     * The returned path is relative to the application's JAR file.
     *
     * @param actionId a String identification of MuAction
     * @return the standard path to the icon image corresponding to the specified MuAction
     */
    private static String getStandardIconPath(String actionId) {
        return IconManager.IconSet.ACTION.getFolder() + actionId + ".png";
    }
    
    /**
     * Queries {@link Translator} for a tooltip corresponding to the specified action using standard naming conventions.
     * Returns the tooltip or <code>null</code> if no corresponding entry was found in the dictionary.
     *
     * @param actionId a String identification of MuAction
     * @return the standard tooltip corresponding to the specified MuAction, <code>null</code> if none was found
     */
    private static String getStandardTooltip(String actionId) {
        String tooltipKey = getStandardTooltipKey(actionId);
        if (!Translator.hasValue(tooltipKey, true))
            return null;

        return Translator.get(tooltipKey);
    }
    
    /**
     * Returns the dictionary key for the specified action's tooltip, using the following standard naming convention:
     * <pre>
     *      action_id.tooltip
     * </pre>
     * where <code>action_id</code> is a String identification of the action, as returned by <code>getId()</code>.
     *
     * @param actionId a String identification of MuAction
     * @return the standard dictionary key for the specified action's tooltip
     */
    private static String getStandardTooltipKey(String actionId) {
        return actionId+".tooltip";
    }
}
