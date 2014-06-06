/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * Copyright (C) 2014 Oleg Trifonov
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
package com.mucommander.cache;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;

import java.awt.Window;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores windows sizes and positions
 */
public class WindowsStorage {

    private static final String STORAGE_FILE_NAME = "windows.list";

    private static WindowsStorage instance;

    private Map<String, Record> records;

    public static class Record {
        public final int left, top, width, height;

        public Record(String s) {
            String[] val = s.split(",");
            this.left = Integer.parseInt(val[0].trim());
            this.top = Integer.parseInt(val[1].trim());
            this.width = Integer.parseInt(val[2].trim());
            this.height = Integer.parseInt(val[3].trim());
        }

        public Record(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !(obj instanceof Record)) {
                return false;
            }
            Record rec = (Record)obj;
            return left == rec.left && top == rec.top && width == rec.width && height == rec.height;
        }

        @Override
        public String toString() {
            return new StringBuilder().append(left).append(',').append(top).append(',').append(width).append(',').append(height).toString();
        }

        public void apply(Window window) {
            window.setLocation(left, top);
            window.setSize(width, height);
        }
    }

    public static WindowsStorage getInstance() {
        if (instance == null) {
            instance = new WindowsStorage();
        }
        return instance;
    }

    public Record get(String key) {
        return getRecords().get(key);
    }

    public Record get(Window window, String suffix) {
        String key = getKey(window, suffix);
        return getRecords().get(key);
    }

    public Record get(Window frame) {
        return get(frame, null);
    }

    public void put(String key, Record rec) {
        Record prev = getRecords().put(key, rec);
        if (prev == null || !prev.equals(rec)) {
            try {
                save(getHistoryFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void put(Window window) {
        put(window, null);
    }

    public void put(Window window, String suffix) {
        Record rec = new Record(window.getLocation().x, window.getLocation().y, window.getWidth(), window.getHeight());
        String key = getKey(window, suffix);
        put(key, rec);
    }

    public boolean init(Window window, String suffix) {
        String key = getKey(window, suffix);
        Record rec = getRecords().get(key);
        if (rec != null) {
            rec.apply(window);
            return true;
        }
        return false;

    }


    private String getKey(Window window, String suffix) {
        Class c = window.getClass();
        String key = c.getCanonicalName();
        if (key == null) {
            key = c.getPackage().getName() + '.' + c.getName();
        }
        if (suffix != null) {
            key += '#' + suffix;
        }
        return key;
    }

    public boolean init(Window window) {
        return init(window, null);
    }


    private Map<String, Record> getRecords() {
        if (records == null) {
            records = new HashMap<>();
            try {
                load(getHistoryFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return records;
    }


    private void load(AbstractFile file) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            while ( (line = reader.readLine() ) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int index = line.indexOf('=');
                if (index < 0) {
                    continue;
                }
                String key = line.substring(0, index);
                String val = line.substring(index + 1);
                try {
                    records.put(key, new Record(val));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void save(AbstractFile file) throws  IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(file.getOutputStream()));
            for (String key : records.keySet()) {
                if (key == null) {
                    continue;
                }
                writer.write(key);
                writer.write('=');
                writer.write(records.get(key).toString());
                writer.write('\n');
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Returns the path to the history file.
     * <p>
     * Will return the default, system dependant bookmarks file.
     * </p>
     * @return             the path to the bookmark file.
     * @throws java.io.IOException if there was a problem locating the default history file.
     */
    private static synchronized AbstractFile getHistoryFile() throws IOException {
        return PlatformManager.getPreferencesFolder().getChild(STORAGE_FILE_NAME);
    }

}
