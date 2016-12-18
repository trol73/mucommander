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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.profiler;

import java.util.*;

/**
 * Created on 01/01/14.
 */
public class Profiler {
    public static final boolean ENABLED = true;

    private static final Map<String, Long> timesStart = new HashMap<>();
    private static final Map<String, Long> timesDuration = new HashMap<>();
    private static final Map<String, Integer> callCount = new HashMap<>();
    private static final Set<String> hiddenGroups = new HashSet<>();

    private static String lastSectionName;
    private static List<String> initThreads = new ArrayList<>();

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
            TreeMap<String, Long> sortedMap = new TreeMap<>(new ValueComparator(timesDuration));
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
                String avgDuration = String.valueOf(duration/count);
                if (avgDuration.equals("0") && duration != 0) {
                    avgDuration = String.valueOf(1.0*duration/count);
                }
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


/*
    public static void printThreads() {
        System.out.println("---------------------------");
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            String s = thread2str(thread);
            if (initThreads.contains(s)) {
                continue;
            }
            System.out.println(s);
        }
        System.out.println("---------------------------");
    }

    public static void initThreads() {
        if (initThreads.size() > 0) {
            return;
        }
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            initThreads.add(thread2str(thread));
        }
    }

    private static String thread2str(Thread thread) {
        return thread.getId() + ":" + thread.getName() + ":" + thread.getState();
    }
*/
}
