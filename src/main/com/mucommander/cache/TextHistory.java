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

import java.io.*;
import java.util.*;

/**
 * Stores history of search requests for text search, file search etc.
 */
public class TextHistory {

    private static final int MAX_RECORDS = 250;

    public enum Type {
        TEXT_SEARCH("search-text.history"),
        HEX_DATA_SEARCH("search-hex.history"),
        FILE_NAME("search-files.history"),
        CALCULATOR("calculator.history");

        private final String fileName;
        private Type(String fileName) {
            this.fileName = fileName;
        }
    }
    private final Map<Type, List<String>> history = new HashMap<>();

    private static TextHistory instance;

    public static TextHistory getInstance() {
        if (instance == null) {
            instance = new TextHistory();
        }
        return instance;
    }


    public List<String> getList(Type type) {
        List<String> result = history.get(type);
        if (result == null) {
            try {
                result = load(getHistoryFile(type));
            } catch (IOException e) {
                e.printStackTrace();
                result = new ArrayList<>();
            }
            history.put(type, result);
        }
        return result;
    }

    public void add(Type type, String s, boolean save) {
        List<String> list = getList(type);
        boolean alreadyInList = list.contains(s);
        if (alreadyInList) {
            list.remove(s);
        }
        if (s.trim().isEmpty()) {
            return;
        }
        list.add(0, s);
        while (list.size() > MAX_RECORDS) {
            list.remove(0);
        }
        // save only if new record was added
        if (!alreadyInList && save) {
            save(type);
        }
    }


    public void save(Type type) {
        try {
            save(getHistoryFile(type), getList(type));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<String> load(AbstractFile file) throws IOException {
        BufferedReader reader = null;
        List<String> result = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            while ( (line = reader.readLine() ) != null) {
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("#")) {
                    continue;
                }
                result.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return result;
    }

    private void save(AbstractFile file, List<String> list) throws  IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(file.getOutputStream()));
            for (String s : list) {
                writer.write(s);
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
    private static synchronized AbstractFile getHistoryFile(Type type) throws IOException {
        return PlatformManager.getPreferencesFolder().getChild(type.fileName);
    }
}
