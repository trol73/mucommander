/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.ui.main.table;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.WildcardFileFilter;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferencesAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Trifonov
 */
public class FileGroupResolver {

    private static class ResolverRecord {
        WildcardFileFilter filter;
        int group;

        public ResolverRecord(int group, String mask) {
            this.group = group;
            this.filter = new WildcardFileFilter(mask);
        }
    }

    private Map<String, Integer> extensionsMap = new HashMap<>();
    private List<ResolverRecord> filtersList = new ArrayList<>();

    private static FileGroupResolver instance;

    private FileGroupResolver() {

    }

    public static FileGroupResolver getInstance() {
        if (instance == null) {
            instance = new FileGroupResolver();
            instance.init();
        }
        return instance;
    }

    /**
     * Reads and parse configuration
     */
    public void init() {
        extensionsMap.clear();
        filtersList.clear();

        MuPreferencesAPI prefs = MuConfigurations.getPreferences();
        for (int group = 0; group < 10; group++) {
            String masks = prefs.getVariable(MuPreference.values()[MuPreference.FILE_GROUP_1_MASK.ordinal() + group]);
            if (masks == null) {
                continue;
            }
            String[] split = masks.split(",");
            for (String aSplit : split) {
                String mask = aSplit.trim().toLowerCase();
                addMask(mask, group);
            }
        }
    }

    private void addMask(String mask, int group) {
        if (mask.startsWith("*.")) {
            String ext = mask.substring(2);
            if (ext.contains("*") || ext.contains("?")) {
                filtersList.add(new ResolverRecord(group, mask));
            } else {
                extensionsMap.put(ext, group);
            }
        } else {
            filtersList.add(new ResolverRecord(group, mask));
        }
    }


    /**
     * Returns the number of group for specified filename or -1s
     * @param file file
     * @return group number (0..9) or -1
     */
    public int resolve(AbstractFile file) {
        if (file.isDirectory() || file.isSymlink()) {
            return -1;
        }
        String ext = file.getExtension();
        if (ext == null) {
            ext = "";
        } else {
            ext = ext.toLowerCase();
        }
        Integer group = extensionsMap.get(ext);
        if (group != null) {
            return group;
        }
        for (ResolverRecord rec : filtersList) {
            if (rec.filter.accept(file)) {
                return rec.group;
            }
        }
        return -1;
    }
}
