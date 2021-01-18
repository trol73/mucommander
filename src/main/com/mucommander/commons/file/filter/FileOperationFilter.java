package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;

/**
 * <code>OperationFileFilter</code> matches files which support a specified {@link FileOperation file operation}.
 *
 * <p>Only one file operation can be matched at a time. To match several file operations, combine them using a
 * {@link com.mucommander.commons.file.filter.ChainedFileFilter}.
 *
 * @see FileOperation
 * @author Maxence Bernard
 */
public class FileOperationFilter extends AbstractFileFilter {

    /** The file operation to match */
    private FileOperation op;


    public FileOperationFilter(FileOperation op) {
        this(op, false);
    }

    public FileOperationFilter(FileOperation op, boolean inverted) {
        super(inverted);
        setFileOperation(op);
    }

    /**
     * Returns the file operation this filter matches.
     *
     * @return the file operation this filter matches.
     */
    public FileOperation getFileOperation() {
        return op;
    }

    /**
     * Sets the file operation this filter matches, replacing the previous operation.
     *
     * @param op the file operation this filter matches.
     */
    public void setFileOperation(FileOperation op) {
        this.op = op;
    }


    @Override
    public boolean accept(AbstractFile file) {
        return file.isFileOperationSupported(op);
    }
}
