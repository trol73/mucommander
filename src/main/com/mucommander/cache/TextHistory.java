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
package com.mucommander.cache;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Oleg Trifonov
 *
 * Stores history of search requests for text search, file search etc.
 */
public class TextHistory {

    private static final int MAX_RECORDS = 500;

    public enum Type {
        TEXT_SEARCH("search-text.history"),
        HEX_DATA_SEARCH("search-hex.history"),
        FILE_NAME("search-files.history"),
        CALCULATOR("calculator.history");

        private final String fileName;
        Type(String fileName) {
            this.fileName = fileName;
        }
    }
    private final Map<Type, LinkedList<String>> history = new HashMap<>();

    private static WeakReference<TextHistory> instance;

    public static TextHistory getInstance() {
        TextHistory textHistory = instance != null ? instance.get() : null;
        if (textHistory == null) {
            textHistory = new TextHistory();
            instance = new WeakReference<>(textHistory);
        }
        return textHistory;
    }


    public LinkedList<String> getList(Type type) {
        LinkedList<String> result = history.get(type);
        if (result == null) {
            try {
                result = load(getHistoryFile(type));
            } catch (IOException e) {
                e.printStackTrace();
                result = new LinkedList<>();
            }
            history.put(type, result);
        }
        return result;
    }

    public void add(Type type, String s, boolean save) {
        LinkedList<String> list = getList(type);
        int index = list.indexOf(s);

        if (index >= 0) {
            list.remove(index);
            // remove other elements if they exists
            while (list.remove(s)) {
                //
            }
        }
        if (s.trim().isEmpty()) {
            return;
        }
        list.addFirst(s);
        while (list.size() > MAX_RECORDS) {
            list.removeLast();
        }
        // save only if new record was added
        if (index != 0 && save) {
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


    private LinkedList<String> load(AbstractFile file) throws IOException {
        LinkedList<String> result = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF8"))) {
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
        }
        return result;
    }

    private void save(AbstractFile file, List<String> list) throws  IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(), "UTF8"))) {
            for (String s : list) {
                writer.write(s);
                writer.write('\n');
            }
        }
    }

    /**
     * Returns the path to the history file.
     * <p>
     * Will return the default, system dependant bookmarks file.
     *
     * @return             the path to the bookmark file.
     * @throws java.io.IOException if there was a problem locating the default history file.
     */
    private static synchronized AbstractFile getHistoryFile(Type type) throws IOException {
        return PlatformManager.getPreferencesFolder().getChild(type.fileName);
    }


    public void clear() {
        history.clear();
        instance = null;
    }
}
