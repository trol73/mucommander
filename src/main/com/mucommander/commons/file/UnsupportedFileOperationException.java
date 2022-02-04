package com.mucommander.commons.file;

import java.io.IOException;

/**
 * This exception can be thrown by certain {@link AbstractFile} methods, when the corresponding operation
 * is not available, either because the underlying file protocol does not support it, or because it is not
 * implemented. This exception may also be thrown by file operations that depend on another file operation that is
 * not supported.
 * <br>
 * Unlike <code>java.lang.UnsupportedOperationException</code>, this exception is <b>not</b> a
 * <code>RuntimeException</code> and must therefore be caught explicitly.
 *
 * <p>
 * This exception is to be thrown in a way that is independent of the actual file instance, and of I/O or
 * network conditions: an <code>AbstractFile</code> method that throws this exception once must throw it
 * always, for any file instance.
 *
 * @author Maxence Bernard
 * @see UnsupportedFileOperation
 * @see AbstractFile
 */
public class UnsupportedFileOperationException extends IOException {

    /** The {@link FileOperation} this exception refers to */
    private final FileOperation op;

    /**
     * Creates a new <code>UnsupportedFileOperationException</code> corresponding to the specified {@link FileOperation}.
     *
     * @param op the {@link FileOperation} this exception refers to.
     */
    public UnsupportedFileOperationException(FileOperation op) {
        super();

        this.op = op;
    }

    /**
     * Creates a new <code>UnsupportedFileOperationException</code> corresponding to the specified {@link FileOperation}
     * with a custom message.
     *
     * @param op the {@link FileOperation} this exception refers to.
     * @param message a message describing the exception cause.
     */
    public UnsupportedFileOperationException(FileOperation op, String message) {
        super(message);

        this.op = op;
    }

    /**
     * Returns the {@link FileOperation} this exception refers to.
     *
     * @return the {@link FileOperation} this exception refers to.
     */
    public FileOperation getFileOperation() {
        return op;
    }
}
