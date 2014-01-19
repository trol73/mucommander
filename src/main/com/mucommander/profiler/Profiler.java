package com.mucommander.profiler;

import java.util.*;

/**
 * Created by trol on 01/01/14.
 */
public class Profiler {
    public static final boolean ENABLED = true;

    private static final Map<String, Long> timesStart = new HashMap<>();
    private static final Map<String, Long> timesDuration = new HashMap<>();
    private static final Map<String, Integer> callCount = new HashMap<>();
    private static final Set<String> hiddenGroups = new HashSet<>();

    private static String lastSectionName;

    public static long getTime() {
//        return System.nanoTime();
        return System.currentTimeMillis();
    }

    public static void start(String name) {
        if (!ENABLED) {
            return;
        }
        lastSectionName = name;
        synchronized (timesStart) {
            timesStart.put(name, getTime());
        }
        synchronized (callCount) {
            Integer cnt = callCount.get(name);
            if (cnt == null) {
                cnt = 1;
            } else {
                cnt++;
            }
            callCount.put(name, cnt);
        }
    }

    public static void stop(String name) {
        if (!ENABLED) {
            return;
        }
        long endTime = getTime();
        long startTime;
        synchronized (timesStart) {
            startTime = timesStart.get(name);
        }
        long duration = endTime - startTime;
        synchronized (timesDuration) {
            Long sum = timesDuration.get(name);
            if (sum == null) {
                sum = duration;
            } else {
                sum += duration;
            }
            timesDuration.put(name, sum);
        }
    }

    public static void stop() {
        if (!ENABLED) {
            return;
        }
        stop(lastSectionName);
    }

    public static void print() {
        if (!ENABLED) {
            return;
        }
        synchronized (timesDuration) {
            TreeMap<String, Long> sortedMap = new TreeMap<String, Long>(new ValueComparator(timesDuration));
            sortedMap.putAll(timesDuration);
            System.out.println(withSpaces("Name", 40) + "\t" + withSpaces("Total", 10) + "\t" + withSpaces("Count", 7) + "\t" + "Average");
            System.out.println(withSpaces("-----------", 40) + "\t" + withSpaces("--------", 10) + "\t" + withSpaces("-------", 7) + "\t" + "----------");
            for (String name : sortedMap.keySet()) {
                boolean isHidden = false;
                for (String hiddenName : hiddenGroups) {
                    if (name.contains(hiddenName)) {
                        isHidden = true;
                        break;
                    }
                }
                if (isHidden) {
                    continue;
                }
                long duration = timesDuration.get(name);
                int count = callCount.get(name);
                long avgDuration = duration/count;
                System.out.println(withSpaces(name, 40) + "\t" + withSpaces(Long.toString(duration), 10) + "\t" + withSpaces(Integer.toString(count), 7) + "\t" + avgDuration);
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

    public static void hide(String name) {
        hiddenGroups.add(name);
    }


    public static void unhide(String name) {
        hiddenGroups.remove(name);
    }




}
