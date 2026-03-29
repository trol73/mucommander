package com.mucommander.utils;

import java.text.DecimalFormat;

/**
 * Created by snouhaud on 25/05/15.
 */
public class Convert {
    private static final String[] UNITS = new String[] { "B", "kB", "MB", "GB", "TB" };

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
    }
}
