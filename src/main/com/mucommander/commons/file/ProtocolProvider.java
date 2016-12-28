package com.mucommander.commons.file;

import java.io.IOException;

/**
 * This interface allows {@link FileFactory} to instantiate {@link AbstractFile} implementations.
 * <p>
 * For {@link AbstractFile} implementations to be automatically instantiated by {@link FileFactory}, this interface
 * needs to be implemented and an instance registered with {@link FileFactory} and binded to a protocol identifier.
 * </p>
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see FileFactory
 * @see AbstractFile
 */
public interface ProtocolProvider {
    /**
     * Creates a new instance of <code>AbstractFile</code> that matches the specified URL.
     * @param url URL to map as an <code>AbstractFile</code>.
     * @param instantiationParams file implementation-specific parameters used for instantiating the
     * {@link AbstractFile} implementation. Those parameters are used when creating file instances within
     * the AbstractFile implementation.
     * @return a new instance of <code>AbstractFile</code> that matches the specified URL.
     * @throws IOException if an error occurs.
     */
    AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException;
}
