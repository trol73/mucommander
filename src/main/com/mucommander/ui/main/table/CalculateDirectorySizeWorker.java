package com.mucommander.ui.main.table;

import com.mucommander.commons.file.AbstractFile;

import javax.swing.SwingWorker;
import java.io.IOException;
import java.util.List;

/**
 * Created by trol on 09/01/14.
 */
public class CalculateDirectorySizeWorker extends SwingWorker<Long, Long> {
    /** Refresh rate in milliseconds  */
    private static final long REFRESH_RATE_MS = 300;

    private final FileTableModel fileTableModel;
    private final AbstractFile path;
    private final FileTable table;
    private long size;
    private long lastRefreshTime;

    public CalculateDirectorySizeWorker(FileTableModel fileTableModel, FileTable table, AbstractFile path) {
        this.fileTableModel = fileTableModel;
        this.table = table;
        this.path = path;
    }

    @Override
    protected Long doInBackground() throws Exception {
        size = 0;
        try {
            calcDirectorySize(path);
        } catch (Exception e) {
            e.printStackTrace();
            size = -1;
        }
        return size;
    }

    @Override
    protected void done() {
        fileTableModel.addProcessedDirectory(path, table, size, true);
        fileTableModel.fillCellCache();
        table.repaint();
    }

    @Override
    protected void process(List<Long> chunks) {
        fileTableModel.addProcessedDirectory(path, table, size, false);
        fileTableModel.fillCellCache();
        table.repaint();
        table.updateSelectedFilesStatusbar();
    }

    private void calcDirectorySize(AbstractFile path) throws IOException {
        if (isCancelled()) {
            return;
        }
        long tm = System.currentTimeMillis();
        if (tm - lastRefreshTime > REFRESH_RATE_MS) {
            lastRefreshTime = tm;
            publish(size);
        }
        if (path.isSymlink() && path != this.path) {
            return;
        }
        AbstractFile[] childs;
        try {
            childs = path.ls();
        } catch (IOException e) {
            return;
        }
        for (AbstractFile f : childs) {
            if (isCancelled()) {
                return;
            }
            if (f.isDirectory()) {
                calcDirectorySize(f);
            } else if (!f.isSymlink()) {
                size += f.getSize();
            }
        }

    }

}
