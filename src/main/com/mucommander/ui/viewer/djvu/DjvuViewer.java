package com.mucommander.ui.viewer.djvu;

import com.lizardtech.djvubean.DjVuBean;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.viewer.FileViewer;
import org.fife.ui.StatusBar;

import javax.swing.JScrollPane;
import java.io.IOException;

/**
 * Created by trol on 04/08/14.
 */
public class DjvuViewer extends FileViewer {

    private DjVuBean djvuBean;

    public DjvuViewer() {
        super();
        djvuBean = new DjVuBean();
        setComponentToPresent(new JScrollPane(djvuBean));
    }
    @Override
    protected void show(AbstractFile file) throws IOException {
        djvuBean.setURL(file.getURL().getJavaNetURL());
    }

    @Override
    protected StatusBar getStatusBar() {
        return null;
    }

    @Override
    protected void saveStateOnClose() {

    }

    @Override
    protected void restoreStateOnStartup() {

    }
}
