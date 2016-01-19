/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
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
package com.mucommander.adb;


import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.shell.Shell;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 25/12/15.
 */
public class AdbUtils {

    private static Map<String, String> lastDeviceNames;

    /**
     * Get list of connected ADB devices
     * @return null if adb doesn't found
     */
    public static List<String> getDevices()  {
        try {
            JadbConnection connection = new JadbConnection();
            List<JadbDevice> devices = connection.getDevices();
            List<String> names = new ArrayList<>();
            for (JadbDevice device : devices) {
                names.add(device.getSerial());
            }
            return names;
        } catch (JadbException | IOException e) {
//            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param serial the device serial number
     *
     * @return device name (or null if unknown)
     */
    public static String getDeviceName(String serial) {
        if (lastDeviceNames == null || !lastDeviceNames.containsKey(serial)) {
            lastDeviceNames = getDeviceNames();
        }
        return lastDeviceNames.get(serial);
    }


    /**
     *
     * @return serial to name
     */
    public static Map<String, String> getDeviceNames() {
        final Map<String, String> result = new HashMap<>();
        try {
            AbstractProcess process = Shell.execute("adb devices -l", null, new ProcessListener() {
                @Override
                public void processDied(int returnValue) {
                }

                @Override
                public void processOutput(String output) {
                    synchronized (result) {
                        String lines[] = output.split("\\r?\\n");
                        for (String s : lines) {
                            String vals[] = s.split("\\s+");
                            for (String val : vals) {
                                if (val.startsWith("model:")) {
                                    String serial = vals[0];
                                    String name = val.substring(6); // "model:"
                                    name = name.replace('_', ' ');
                                    result.put(serial, name);
                                }
                            }
                        }
                    }
                }

                @Override
                public void processOutput(byte[] buffer, int offset, int length) {
                }
            });
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (result) {
            return result;
        }
    }
}
