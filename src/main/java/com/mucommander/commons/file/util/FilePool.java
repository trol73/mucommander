package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;
import org.apache.commons.collections4.map.ReferenceMap;

/**
 * This class allows {@link AbstractFile} instances to be pooled, so that existing file instances can be reused,
 * and to guarantee that only one instance of the same file may exist at any given time.
 * File keys are mapped onto {@link AbstractFile} instances. Any kind of Object may be used as the key,
 * but a sensible choice is to use the {@link AbstractFile#getURL() file's URL}.
 *
 * <p>Files are stored as {@link java.lang.ref.WeakReference weak references} so they can be garbage collected
 * when they are no longer hard-referenced.
 *
 * <p>This class uses the {@link ReferenceMap} class part of the <code>Apache Commons Collection</code> library.
 * All accesses to the underlying map is synchronized, making this class thread-safe.
 *
 * @author Maxence Bernard
 */
public class FilePool {

    /** The actual hash map */
    protected final ReferenceMap<Object, AbstractFile> hashMap = new ReferenceMap<>(ReferenceMap.ReferenceStrength.HARD, ReferenceMap.ReferenceStrength.WEAK);

    /**
     * Creates a new file pool.
     */
    public FilePool() {
    }

    /**
     * Adds a new key/file mapping to the pool. If a mapping with the same key exists, it is replaced and the previous
     * value returned.
     *
     * @param key the key that will later allow to retrieve the pooled file instance
     * @param value the file instance to add to the pool
     * @return returns the file instance previously mapped onto the given key, <code>null</code> if no
     * such mapping existed
     */
    public synchronized AbstractFile put(Object key, AbstractFile value) {
        return hashMap.put(key, value);
    }

    /**
     * Returns the {@link AbstractFile} instance mapped onto the given key if there is one,
     * <code>null</code> otherwise
     *
     * @param key key of the file instance to retrieve
     * @return the {@link AbstractFile} instance mapped onto the given key if there is one,
     * <code>null</code> otherwise
     */
    public synchronized AbstractFile get(Object key) {
        return hashMap.get(key);
    }

    /**
     * Returns <code>true</code> if this pool currently contains a key/file mapping where the given key is used as
     * the mapping's key.
     *
     * @param key key to lookup
     * @return <code>true</code> if this pool currently contains a key/file mapping where the given key is used as
     * the mapping's key.
     */
    public synchronized boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    /**
     * Returns <code>true</code> if this pool currently contains a key/file mapping where the given file is used as
     * the mapping's value.
     *
     * @param file file to lookup
     * @return <code>true</code> if this pool currently contains a key/file mapping where the given file is used as
     * the mapping's key.
     */
    public synchronized boolean containsValue(AbstractFile file) {
        return hashMap.containsValue(file);
    }

    /**
     * Removes all existing key/file mapping from this pool, leaving the pool in the same state as it was right after
     * its creation.
     */
    public synchronized void clear() {
        hashMap.clear();
    }

    /**
     * Returns the number of key/file mapping this pool currently contains.
     *
     * @return the number of key/file mapping this pool currently contains.
     */
    public synchronized int size() {
        return hashMap.size();
    }
}
