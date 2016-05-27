/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
package com.mucommander.tools;

import com.mucommander.commons.file.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;

/**
 * @author Oleg Trifonov
 * Created on 27/05/16.
 */
public class AvrAssemblerCommandsHelper {

    private static final String AVR_COMMANDS_FILE = "/avr/avr_commands.properties";
    private static WeakReference<Properties> propertiesRef;

    public static String getCommandDescription(String mnemonic) {
        if (mnemonic == null) {
            return null;
        }
        if (propertiesRef == null || propertiesRef.get() == null) {
            propertiesRef = new WeakReference<>(loadProperties());
        }
        return propertiesRef.get().getProperty(mnemonic.toUpperCase());
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream is = ResourceLoader.getResourceAsStream(AVR_COMMANDS_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
