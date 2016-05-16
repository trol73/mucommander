/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.tools;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Oleg Trifonov
 * Created on 13/05/16.
 */
public class ToolsEnvironment {
    private static final Map<String, String> customEnvironment = new HashMap<>();

    public static void load() throws IOException {
        AbstractFile configPath = PlatformManager.getPreferencesFolder().getChild("env.properties");
        if (configPath != null && configPath.exists()) {
            try (InputStream is = configPath.getInputStream()) {
                Properties properties = new Properties();
                properties.load(is);
                for (Object key: properties.keySet()) {
                    String name = key.toString();
                    customEnvironment.put(name, properties.getProperty(name));
                }
            }
        }
    }


    public static Map<String, String> getCustomEnvironment() {
        return customEnvironment;
    }

    public static String getEnv(String name) {
        String result = customEnvironment.get(name);
        if (result != null) {
            return result;
        }
        return System.getenv(name);
    }
}
