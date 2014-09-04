package com.mucommander.ui.viewer.djvu;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.text.Translator;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFactory;
import com.mucommander.ui.viewer.WarnUserException;
import com.mucommander.ui.viewer.pdf.PdfViewer;

/**
 * Created by trol on 04/08/14.
 */
public class DjvuFactory implements ViewerFactory {

    public final static ExtensionFilenameFilter DJVU_FILTER = new ExtensionFilenameFilter(new String[]{".djvu", ".djv"});

    static {
        DJVU_FILTER.setCaseSensitive(false);
    }

    @Override
    public boolean canViewFile(AbstractFile file) throws WarnUserException {
        return !file.isDirectory() && DJVU_FILTER.accept(file);
    }

    @Override
    public FileViewer createFileViewer() {
        return new DjvuViewer();
    }

    @Override
    public String getName() {
        return Translator.get("viewer_type.pdf");
    }
}