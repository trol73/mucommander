package com.mucommander.commons.file.util;

/**
 * Contains constants used by {@link com.mucommander.commons.file.util.FileMonitor}.
 *
 * @author Maxence Bernard
 */
public interface FileMonitorConstants {

    /** File date attribute, as returned by {@link com.mucommander.commons.file.AbstractFile#getLastModifiedDate()}. */
    int DATE_ATTRIBUTE = 1;

    /** File size attribute, as returned by {@link com.mucommander.commons.file.AbstractFile#getSize()}. */
    int SIZE_ATTRIBUTE = 2;

    /** File permissions attribute, as returned by {@link com.mucommander.commons.file.AbstractFile#getPermissions()}. */
    int PERMISSIONS_ATTRIBUTE = 4;

    /** File 'is directory' attribute, as returned by {@link com.mucommander.commons.file.AbstractFile#isDirectory()}. */
    int IS_DIRECTORY_ATTRIBUTE = 8;

    /** File 'exists' attribute, as returned by {@link com.mucommander.commons.file.AbstractFile#exists()}. */
    int EXISTS_ATTRIBUTE = 16;

    /** Default attribute set: {@link #DATE_ATTRIBUTE}. */
    int DEFAULT_ATTRIBUTES = DATE_ATTRIBUTE;

    /** Designates all attributes. */
    int ALL_ATTRIBUTES = DATE_ATTRIBUTE|SIZE_ATTRIBUTE|PERMISSIONS_ATTRIBUTE|IS_DIRECTORY_ATTRIBUTE|EXISTS_ATTRIBUTE;

    /** Default poll period in milliseconds. */
    long DEFAULT_POLL_PERIOD = 10000;
}
