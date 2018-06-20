/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.bookmark;

import com.mucommander.PlatformManager;
import com.mucommander.bookmark.file.BookmarkProtocolProvider;
import com.mucommander.commons.collections.AlteredVector;
import com.mucommander.commons.collections.VectorChangeListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.io.backup.BackupOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class manages the bookmark list and its parsing and storage as an XML file.
 * <p>
 * It monitors any changes made to the bookmarks and when changes are made, fires change events to registered
 * listeners.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class BookmarkManager implements VectorChangeListener {

    /**
     * Default bookmarks file name
     */
    private static final String DEFAULT_BOOKMARKS_FILE_NAME = "bookmarks.xml";

    /**
     * Bookmark instances
     */
    private static final AlteredVector<Bookmark> BOOKMARKS = new AlteredVector<>();

    /**
     * Contains all registered bookmark listeners, stored as weak references
     */
    private static final Map<BookmarkListener, ?> LISTENERS = new WeakHashMap<>();

    /**
     * create a singleton instance, needs to be referenced so that it's not garbage collected (AlteredVector
     * stores VectorChangeListener as weak references)
     */
    private static final BookmarkManager SINGLETON = new BookmarkManager();

    /**
     * Value of bookmark's name that make the bookmark treated as a separator
     */
    public static final String BOOKMARKS_SEPARATOR = "-";

    /**
     * Whether we're currently loading the bookmarks or not.
     */
    private static boolean isLoading = false;

    /**
     * Bookmarks file location
     */
    private static AbstractFile bookmarksFile;

    /**
     * Specifies whether bookmark events should be fired when a change to the bookmarks is detected
     */
    private static boolean fireEvents = true;

    /**
     * True when changes were made after the bookmarks file was last saved
     */
    private static boolean saveNeeded;

    /**
     * Last bookmark change timestamp
     */
    private static long lastBookmarkChangeTime;

    /**
     * Last event pause timestamp
     */
    private static long lastEventPauseTime;

    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    static {
        // Listen to changes made to the bookmarks vector
        BOOKMARKS.addVectorChangeListener(SINGLETON);
    }

    /**
     * Prevents instanciation of <code>BookmarkManager</code>.
     */
    private BookmarkManager() {
    }

    // - Bookmark building -----------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Passes messages about all known bookmarks to the specified builder.
     *
     * @param builder where to send bookmark building messages.
     * @throws BookmarkException if an error occurs.
     */
    private static synchronized void buildBookmarks(BookmarkBuilder builder) throws BookmarkException {
        builder.startBookmarks();
        for (Bookmark bookmark : BOOKMARKS) {
            builder.addBookmark(bookmark.getName(), bookmark.getLocation());
        }
        builder.endBookmarks();
    }

    // - Bookmark file access --------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Returns the path to the bookmark file.
     * <p>
     * If it hasn't been changed through a call to {@link #setBookmarksFile(String)},
     * this method will return the default, system dependant bookmarks file.
     *
     * @return the path to the bookmark file.
     * @throws IOException if there was a problem locating the default bookmarks file.
     * @see #setBookmarksFile(String)
     */
    private static synchronized AbstractFile getBookmarksFile() throws IOException {
        if (bookmarksFile == null) {
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_BOOKMARKS_FILE_NAME);
        }
        return bookmarksFile;
    }

    /**
     * Sets the path to the bookmarks file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setBookmarksFile(FileFactory.getFile(file))</code>.
     *
     * @param path path to the bookmarks file
     * @throws FileNotFoundException if <code>path</code> is not accessible.
     * @see #getBookmarksFile()
     */
    public static void setBookmarksFile(String path) throws FileNotFoundException {
        AbstractFile file = FileFactory.getFile(path);

        if (file == null) {
            setBookmarksFile(new File(path));
        } else {
            setBookmarksFile(file);
        }
    }

    /**
     * Sets the path to the bookmarks file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setBookmarksFile(FileFactory.getFile(file.getAbsolutePath()))</code>.
     *
     * @param file path to the bookmarks file
     * @throws FileNotFoundException if <code>path</code> is not accessible.
     * @see #getBookmarksFile()
     */
    private static void setBookmarksFile(File file) throws FileNotFoundException {
        setBookmarksFile(FileFactory.getFile(file.getAbsolutePath()));
    }

    /**
     * Sets the path to the bookmarks file.
     *
     * @param file path to the bookmarks file
     * @throws FileNotFoundException if <code>path</code> is not accessible.
     * @see #getBookmarksFile()
     */

    private static synchronized void setBookmarksFile(AbstractFile file) throws FileNotFoundException {
        if (file.isBrowsable()) {
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        }
        bookmarksFile = file;
    }

    // - Bookmarks loading -----------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Loads all available bookmarks.
     *
     * @throws Exception if an error occurs.
     */
    public static synchronized void loadBookmarks() throws Exception {
        // Parse the bookmarks file
        isLoading = true;
        try (InputStream in = new BackupInputStream(getBookmarksFile())) {
            readBookmarks(in, new Loader());
            isLoading = false;
        } catch (IOException e) {
            isLoading = false;
            throw e;
        }
    }

    /**
     * Reads bookmarks from the specified <code>InputStream</code>.
     *
     * @param in where to read bookmarks from.
     * @throws Exception if an error occurs.
     */
    public static void readBookmarks(InputStream in) throws Exception {
        readBookmarks(in, new Loader());
    }

    /**
     * Reads bookmarks from the specified <code>InputStream</code> and passes messages to the specified {@link BookmarkBuilder}.
     *
     * @param in      where to read bookmarks from.
     * @param builder where to send builing messages to.
     * @throws Exception if an error occurs.
     */
    public static synchronized void readBookmarks(InputStream in, BookmarkBuilder builder) throws Exception {
        new BookmarkParser().parse(in, builder);
    }

    // - Bookmarks writing -----------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link BookmarkBuilder} that will write all building messages as XML to the specified output stream.
     *
     * @param out where to write the bookmarks' XML content.
     * @return a {@link BookmarkBuilder} that will write all building messages as XML to the specified output stream.
     * @throws IOException if an IO related error occurs.
     */
    public static BookmarkBuilder getBookmarkWriter(OutputStream out) throws IOException {
        return new BookmarkWriter(out);
    }

    /**
     * Writes all known bookmarks to the bookmark {@link #getBookmarksFile() file}.
     *
     * @param forceWrite if false, the bookmarks file will be written only if changes were made to bookmarks since
     *                   last write, if true the file will always be written
     * @throws IOException       if an I/O error occurs.
     * @throws BookmarkException if an error occurs.
     */
    public static synchronized void writeBookmarks(boolean forceWrite) throws IOException, BookmarkException {
        // Write bookmarks file only if changes were made to the bookmarks since last write, or if write is forced.
        if (!forceWrite && !saveNeeded) {
            return;
        }
        try (OutputStream out = new BackupOutputStream(getBookmarksFile())) {
            buildBookmarks(getBookmarkWriter(out));
            saveNeeded = false;
        }
    }

    // - Bookmarks access ------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Returns an {@link AlteredVector} that contains all bookmarks.
     * <p>
     * <p>Important: the returned Vector should not directly be used to
     * add or remove bookmarks, doing so won't trigger any event to registered bookmark listeners.
     * However, it is safe to modify bookmarks individually, events will be properly fired.
     *
     * @return an {@link AlteredVector} that contains all bookmarks.
     */
    public static synchronized AlteredVector<Bookmark> getBookmarks() {
        return BOOKMARKS;
    }

    /**
     * Deletes the specified bookmark.
     *
     * @param bookmark bookmark to delete from the list.
     */
    public static synchronized void removeBookmark(Bookmark bookmark) {
        BOOKMARKS.remove(bookmark);
    }

    /**
     * Convenience method that looks for a Bookmark with the given name (case ignored) and returns it,
     * or null if none was found. If several bookmarks have the given name, the first one is returned.
     *
     * @param name the bookmark's name
     * @return a Bookmark instance with the given name, null if none was found
     */
    public static synchronized Bookmark getBookmark(String name) {
        for (Bookmark b : BOOKMARKS) {
            if (b.getName().equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Convenience method that adds a bookmark to the bookmark list.
     *
     * @param b the Bookmark instance to add to the bookmark list.
     */
    public static synchronized void addBookmark(Bookmark b) {
        BOOKMARKS.add(b);
    }

    /**
     * Check if a given URL represents a bookmark.
     *
     * @param fileURL the URL to examine
     * @return true if the given URL represents a bookmark, false otherwise
     */
    public static boolean isBookmark(FileURL fileURL) {
        return fileURL != null && BookmarkProtocolProvider.BOOKMARK.equals(fileURL.getScheme());
    }

    /**
     * Check if a given file represents a bookmark.
     *
     * @param file the URL to examine
     * @return true if the given file represents a bookmark, false otherwise
     */
    public static boolean isBookmark(AbstractFile file) {
        return file != null && BookmarkProtocolProvider.BOOKMARK.equals(file.getURL().getScheme());
    }

    // - Listeners -------------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Adds the specified BookmarkListener to the list of registered listeners.
     * <p>
     * <p>Listeners are stored as weak references so {@link #removeBookmarkListener(BookmarkListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.
     *
     * @param listener the BookmarkListener to add to the list of registered listeners.
     * @see #removeBookmarkListener(BookmarkListener)
     */
    public static void addBookmarkListener(BookmarkListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.put(listener, null);
        }
    }

    /**
     * Removes the specified BookmarkListener from the list of registered listeners.
     *
     * @param listener the BookmarkListener to remove from the list of registered listeners.
     * @see #addBookmarkListener(BookmarkListener)
     */
    private static void removeBookmarkListener(BookmarkListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener);
        }
    }

    /**
     * Notifies all the registered bookmark listeners of a bookmark change. This can be :
     * <ul>
     * <li>A new bookmark which has just been added
     * <li>An existing bookmark which has been modified
     * <li>An existing bookmark which has been removed
     * </ul>
     */
    static void fireBookmarksChanged() {
        // Bookmarks file will need to be saved
        if (!isLoading) {
            saveNeeded = true;
        }

        lastBookmarkChangeTime = System.currentTimeMillis();

        // Do not fire event if events are currently disabled
        if (!fireEvents) {
            return;
        }

        synchronized (LISTENERS) {
            // Iterate on all listeners
            for (BookmarkListener listener : LISTENERS.keySet()) {
                listener.bookmarksChanged();
            }
        }
    }

    /**
     * Specifies whether bookmark events should be fired when a change in the bookmarks is detected. This allows
     * to temporarily suspend events firing when a lot of them are made, for example when editing the bookmarks list.
     * <p>
     * <p>If true is specified, any subsequent calls to fireBookmarksChanged will be ignored, until this method is
     * called again with false.
     *
     * @param b whether to fire events.
     */
    public static synchronized void setFireEvents(boolean b) {
        if (b) {
            // Fire a bookmarks changed event if bookmarks were modified during event pause
            if (!fireEvents && lastBookmarkChangeTime >= lastEventPauseTime) {
                fireEvents = true;
                fireBookmarksChanged();
            }
        } else {
            // Remember pause start time
            if (fireEvents) {
                fireEvents = false;
                lastEventPauseTime = System.currentTimeMillis();
            }
        }
    }

    /////////////////////////////////////////
    // VectorChangeListener implementation //
    /////////////////////////////////////////
    @Override
    public void elementsAdded(int startIndex, int nbAdded) {
        fireBookmarksChanged();
    }

    @Override
    public void elementsRemoved(int startIndex, int nbRemoved) {
        fireBookmarksChanged();
    }

    @Override
    public void elementChanged(int index) {
        fireBookmarksChanged();
    }

    // - Bookmark loading ------------------------------------------------------
    // -------------------------------------------------------------------------
    private static class Loader implements BookmarkBuilder {
        public void startBookmarks() {
        }

        public void endBookmarks() {
        }

        public void addBookmark(String name, String location) {
            BookmarkManager.addBookmark(new Bookmark(name, location));
        }
    }

}
