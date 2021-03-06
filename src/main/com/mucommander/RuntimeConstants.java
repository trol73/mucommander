/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2020 Oleg Trifonov
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

package com.mucommander;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mucommander.commons.file.util.ResourceLoader;

/**
 * Defines various generic trolCommander constants.
 * @author Nicolas Rinaudo
 */
public class RuntimeConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConstants.class);
	
    /** Path to the themes directory. */
    public static final String THEMES_PATH     = "/themes";
    /** Path to the viewer/editor themes directory. */
    public static final String TEXT_SYNTAX_THEMES_PATH = "/themes/editor";
    /** Path to the trolCommander license file. */
    public static final String LICENSE         = "/license.txt";
    /** Default trolCommander theme. */
    public static final String DEFAULT_THEME   = "Native";
    public static final boolean DISPLAY_4K = is4KDisplay();


    /** Homepage URL. */
    public static final String HOMEPAGE_URL       = "http://trolsoft.ru/en/soft/trolcommander";

    public static final String DEFAULT_VERSION_URL = "http://trolsoft.ru/content/soft/trolcommander/version.xml";
    /** URL at which to download the latest version description. */
    public static final String VERSION_URL;
    /** URL of the trolCommander forums. */
    public static final String FORUMS_URL         = HOMEPAGE_URL + "/forums/";
    /** URL at which to see the donation information. */
    public static final String DONATION_URL       = HOMEPAGE_URL + "/#donate";
    /** Bug tracker URL. */
    public static final String BUG_REPOSITORY_URL = "https://github.com/trol73/mucommander/issues"; //HOMEPAGE_URL + "/bugs/";
    /** Documentation URL. */
    public static final String DOCUMENTATION_URL  = HOMEPAGE_URL;// + "/documentation/";



    /**
     * Release date to use in case the JAR file doesn't contain the information.
     * This is guaranteed to trigger a software update - the JAR file is corrupt, so we might as well get the latest
     * version.
     */
    private static final String DEFAULT_RELEASE_DATE = "20020101";
    /** Current trolCommander version (<code>MAJOR.MINOR.DEV</code>). */
    public  static final String VERSION;
    /** Date at which the build was generated (<code>YYYYMMDD</code>). */
    public  static final String BUILD_DATE;
    /** Time at which the build was generated (<code>HHMM</code>). */
    public  static final String BUILD_TIME;
    /** Copyright information (<code>YYYY-YYYY</code>). */
    public  static final String COPYRIGHT;
    /** String describing the software (<code>trolCommander vMAJOR.MINOR.DEV</code>). */
    public  static final String APP_STRING;
    /** String describing the trolCommander build number. */
    public static final String BUILD_NUMBER;
    /** YYYYMMDDHHmm   */
    public static final String BUILD_CODE;

    

    static {
        Attributes attributes = getManifestAttributes();

        if (attributes == null) {   // No MANIFEST.MF found, use default values.
            VERSION = "?";
            COPYRIGHT    = "2013-" + Calendar.getInstance().get(Calendar.YEAR);
            // We use a date that we are sure is later than the latest version to trigger the version checker.
            // After all, the JAR appears to be corrupt and should be upgraded.
            BUILD_DATE = DEFAULT_RELEASE_DATE;
            BUILD_TIME = null;
            VERSION_URL  = DEFAULT_VERSION_URL;
            BUILD_NUMBER = "?";
            BUILD_CODE = null;
        } else {    // A MANIFEST.MF file was found, extract data from it.
            VERSION = getAttribute(attributes, "Specification-Version");
            BUILD_DATE = getAttribute(attributes, "Build-Date");
            BUILD_TIME = getAttribute(attributes, "Build-Time");
            VERSION_URL = getAttribute(attributes, "Build-URL");
            BUILD_NUMBER = getAttribute(attributes, "Implementation-Version");
            // Protection against corrupt manifest files.
            COPYRIGHT = BUILD_DATE.length() > 4 ? BUILD_DATE.substring(0, 4) : DEFAULT_RELEASE_DATE;
            BUILD_CODE = BUILD_DATE + BUILD_TIME;
        }
        APP_STRING = "trolCommander v" + VERSION;
    }

    @Nullable
    private static Attributes getManifestAttributes() {
        try (InputStream in = ResourceLoader.getResourceAsStream("META-INF/MANIFEST.MF", ResourceLoader.getDefaultClassLoader(), ResourceLoader.getRootPackageAsFile(RuntimeConstants.class))) {
            if (in != null) {
                Manifest manifest = new Manifest();
                manifest.read(in);
                return manifest.getMainAttributes();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to read MANIFEST.MF, default values will be used", e);
        }
        return null;
    }

    /**
     * Extract the requested attribute value.
     * @param  attributes attributes from which to extract the requested value.
     * @param  name       name of the attribute to retrieve.
     * @return            the requested attribute value.
     */
    private static String getAttribute(Attributes attributes, String name) {
        String buffer = attributes.getValue(name);
        return buffer == null ? "?" : buffer;
    }

    private static boolean is4KDisplay() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.width*screenSize.height > 3500*3500;
    }

}
