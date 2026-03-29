/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
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

import com.mucommander.commons.file.util.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 13/04/16.
 */
public class AvrdudeDevice {
    private static final String AVRDUDE_RESOURCE_NAME = "/avr/avrdude.csv";

    public final String id;
    public final String name;
    public final int signature;
    public final Map<String, Integer> blockSizes;

    private static WeakReference<Map<String, AvrdudeDevice>> devices;

    private AvrdudeDevice(String id, String name, int signature, Map<String, Integer> blockSizes) {
        this.id = id;
        this.name = name;
        this.signature = signature;
        this.blockSizes = blockSizes;
    }

    public static AvrdudeDevice getDevice(String id) {
        return getDevices().get(id);
    }


    private static Map<String, AvrdudeDevice> getDevices() {
        Map<String, AvrdudeDevice> map = devices != null ? devices.get() : null;
        if (map == null) {
            map = load();
            devices = new WeakReference<>(map);
        }
        return map;
    }


    private static Map<String, AvrdudeDevice> load() {
        Map<String, AvrdudeDevice> result = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ResourceLoader.getResourceAsStream(AVRDUDE_RESOURCE_NAME)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                String id = parts[0];
                String name = parts[1];
                int signature = Integer.parseInt(parts[2].substring(2), 16);
                Map<String, Integer> blocks = new HashMap<>();
                for (int blockIndex = 3; blockIndex < parts.length; blockIndex++) {
                    String block = parts[blockIndex];
                    int nameEndIndex = block.indexOf('[');
                    String blockName = block.substring(0, nameEndIndex);
                    int length = Integer.parseInt(block.substring(nameEndIndex+1, block.length()-1));
                    blocks.put(blockName, length);
                }
                result.put(id, new AvrdudeDevice(id, name, signature, blocks));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
