package com.mucommander.ui.macosx;

import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;


import javax.swing.*;


public interface IMacOsWindow extends RootPaneContainer {

    default void initLookAndFeel() {
        if (OsVersion.MAC_OS_X_10_4.isCurrentOrLower() || OsVersion.MAC_OS_X_10_13.isCurrentOrHigher()) {
            getRootPane().putClientProperty("apple.awt.brushMetalLook",
                    TcConfigurations.getPreferences().getVariable(TcPreference.USE_BRUSHED_METAL, TcPreferences.DEFAULT_USE_BRUSHED_METAL));
        }

    }
}
