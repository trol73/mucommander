/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.avrdude;


import java.io.*;
import java.util.Properties;

/**
 * @author Oleg Trifonov
 * Created on 23/03/16.
 */
public class AvrConfigFileUtils {

    private static final String KEY_DEVICE_NAME = "avr_device_name";
    private static final String KEY_PROGRAMMER = "programmer";
    private static final String KEY_VERIFY = "verify";
    private static final String KEY_PORT = "port";
    private static final String KEY_BAUDRATE = "baudrate";
    private static final String KEY_BITCLOCK = "bitclock";
    private static final String KEY_CONFIG_FILE = "config_file";
    private static final String KEY_AUTOERASE = "autoerase";
    private static final String KEY_ISP_CLOCK_DELAY = "isp_clock_delay";
    private static final String KEY_OVERRIDE_INVALID_SIGNATURE_CHECK = "override_invalid_signature_check";
    private static final String KEY_EXTENDED_PARAM = "extended_param";
    private static final String KEY_AVRDUDE_PATH = "avrdude_path";




    public static AvrdudeConfiguration load(String filePath) throws IOException {
        Properties properties = new Properties();
        Reader reader = new BufferedReader(new FileReader(filePath));
        properties.load(reader);
        AvrdudeConfiguration result = load(properties);
        reader.close();
        return result;
    }


    public static AvrdudeConfiguration load(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        AvrdudeConfiguration result = load(properties);
        is.close();
        return result;
    }


    private static AvrdudeConfiguration load(Properties properties) {
        String deviceName = properties.getProperty(KEY_DEVICE_NAME);
        Integer baudrate = getPropertyInt(properties, KEY_BAUDRATE);
        Integer bitclock = getPropertyInt(properties, KEY_BITCLOCK);
        String configFile = properties.getProperty(KEY_CONFIG_FILE);
        String programmer = properties.getProperty(KEY_PROGRAMMER);
        Boolean flashAutoerase = getPropertyBool(properties, KEY_AUTOERASE);
        Integer ispCockDelay = getPropertyInt(properties, KEY_ISP_CLOCK_DELAY);
        String port = properties.getProperty(KEY_PORT);
        Boolean overrideInvalidSignatureCheck = getPropertyBool(properties, KEY_OVERRIDE_INVALID_SIGNATURE_CHECK);
        Boolean verify = getPropertyBool(properties, KEY_VERIFY);
        String extendedParam = properties.getProperty(KEY_EXTENDED_PARAM);
        String avrdudeLocation = properties.getProperty(KEY_AVRDUDE_PATH);

        return new AvrdudeConfiguration(deviceName, baudrate, bitclock, configFile, programmer,
                flashAutoerase, ispCockDelay, port, overrideInvalidSignatureCheck, verify, extendedParam, avrdudeLocation);
    }




    private static Integer getPropertyInt(Properties properties, String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (Throwable t) {
            return null;
        }
    }


    private static Boolean getPropertyBool(Properties properties, String key) {
        String val = properties.getProperty(key);
        if (val == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val)) {
            return true;
        } else if ("false".equalsIgnoreCase(val) || "no".equalsIgnoreCase(val)) {
            return false;
        }
        return null;
    }


    public static void save(AvrdudeConfiguration config, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(build(config));
        writer.close();
    }


    private static String build(AvrdudeConfiguration config) {
        StringBuilder sb = new StringBuilder();

        sb.append("### trolCommander avrdude configuration\n\n");

        sb.append("# Required. Specify AVR device. (etc. \"m8\", \"m32\", \"t13\")\n");
        addRequiredParam(sb, KEY_DEVICE_NAME, config.deviceName);

        sb.append("# Specify programmer type. (etc \"usbasp\")\n");
        addRequiredParam(sb, KEY_PROGRAMMER, config.programmer);

        sb.append("# Verify after write\n");
        addRequiredParam(sb, KEY_VERIFY, config.verify);

        sb.append("# Specify connection port\n");
        addOptionalParam(sb, KEY_PORT, config.port);

        sb.append("# RS-232 baud rate\n");
        addOptionalParam(sb, KEY_BAUDRATE, config.baudrate);

        sb.append("# JTAG/STK500v2 bit clock period (us)\n");
        addOptionalParam(sb, KEY_BITCLOCK, config.bitclock);

        sb.append("# Specify location of configuration file\n");
        addOptionalParam(sb, KEY_CONFIG_FILE, config.configFile);

        sb.append("# Enable auto erase for flash memory\n");
        addRequiredParam(sb, KEY_AUTOERASE, config.flashAutoerase);

        sb.append("# ISP Clock Delay [in microseconds]\n");
        addOptionalParam(sb, KEY_ISP_CLOCK_DELAY, config.ispCockDelay);

        sb.append("# Override invalid signature check.\n");
        addRequiredParam(sb, KEY_OVERRIDE_INVALID_SIGNATURE_CHECK, config.overrideInvalidSignatureCheck);

        sb.append("# Pass extended_param to programmer.\n");
        addOptionalParam(sb, KEY_EXTENDED_PARAM, config.extendedParam);

        sb.append("# Path to avrdude\n");
        addOptionalParam(sb, KEY_AVRDUDE_PATH, config.avrdudeLocation);

        return sb.toString();
    }


    private static void addRequiredParam(StringBuilder sb, String name, Object value) {
        sb.append(name);
        sb.append(" = ");
        sb.append(value);
        sb.append("\n\n");
    }

    private static void addOptionalParam(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append(name);
            sb.append(" = ");
            sb.append(value);
        } else {
            sb.append("# ");
            sb.append(name);
            sb.append(" = ");
        }
        sb.append("\n\n");
    }



}
