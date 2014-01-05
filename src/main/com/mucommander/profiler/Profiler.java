package com.mucommander.profiler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by trol on 01/01/14.
 */
public class Profiler {
    public static final boolean ENABLED = false;

    private static Map<String, Long> times = new HashMap<String, Long>();
    private static String lastSectionName;


    public static final void start(String name) {
        if (!ENABLED) {
            return;
        }
        lastSectionName = name;
        synchronized (times) {
            times.put(name, System.currentTimeMillis());
        }
    }

    public static final void stop(String name) {
        if (!ENABLED) {
            return;
        }
        synchronized (times) {
            long t = times.get(name);
            times.put(name, System.currentTimeMillis() - t);
        }
    }

    public static final void stop() {
        if (!ENABLED) {
            return;
        }
        stop(lastSectionName);
    }

    public static final void print() {
        if (!ENABLED) {
            return;
        }
        synchronized (times) {
            TreeMap<String, Long> sortedMap = new TreeMap<String, Long>(new ValueComparator(times));
            sortedMap.putAll(times);
            for (String name : sortedMap.keySet()) {
                System.out.println(withSpaces(name, 40) + "\t" + times.get(name));
            }
        }
    }

    private static String withSpaces(String name, int len) {
        while (name.length() < len) {
            name += " ";
        }
        return name;
    }

    static class ValueComparator implements Comparator<String> {

        Map<String, Long> base;
        public ValueComparator(Map<String, Long> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
