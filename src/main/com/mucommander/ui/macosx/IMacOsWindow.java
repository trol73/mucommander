package com.mucommander.ui.macosx;

import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;

import javax.swing.*;

public interface IMacOsWindow extends RootPaneContainer {

    default void initLookAndFeel() {
        if (OsVersion.MAC_OS_X_10_4.isCurrentOrLower() || OsVersion.MAC_OS_X_10_13.isCurrentOrHigher()) {
            getRootPane().putClientProperty("apple.awt.brushMetalLook",
                    MuConfigurations.getPreferences().getVariable(MuPreference.USE_BRUSHED_METAL, MuPreferences.DEFAULT_USE_BRUSHED_METAL));
        }
    }

}
