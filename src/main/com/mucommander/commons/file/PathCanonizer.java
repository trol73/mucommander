package com.mucommander.commons.file;

/**
 * PathCanonizer is an interface that defines a single {@link #canonize(String)} method that returns the canonical
 * representation of a given path. This interface is used by {@link SchemeParser} implementations to perform
 * scheme-specific path canonization, independently of the actual URL parsing.
 *
 * @see DefaultSchemeParser
 * @author Maxence Bernard
 */
public interface PathCanonizer {

    /**
     * Returns a canonical representation of the given path.
     *
     * @param path path to canonize
     * @return a canonical representation of the given path.
     */
    String canonize(String path);
}
