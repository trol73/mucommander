/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.viewer;

import com.mucommander.commons.HasProgress;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.EncodingDetector;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.statusbar.TaskWidget;

import javax.swing.SwingWorker;
import java.io.PushbackInputStream;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 07/07/16.
 */
public class FilePreloadWorker extends SwingWorker<Void, Void> {
    private final AbstractFile file;
    private final MainFrame mainFrame;
    private final TaskWidget taskWidget;
    private final Runnable onFinish;
    private volatile Throwable readException;
    private volatile int progress;
    private boolean taskWidgetAttached;


    FilePreloadWorker(AbstractFile file, MainFrame mainFrame, Runnable onFinish) {
        this.file = file;
        this.mainFrame = mainFrame;
        this.onFinish = onFinish;
        this.taskWidget = new TaskWidget();
        taskWidget.setText(file.getName());
    }

    @Override
    protected Void doInBackground() {
        try {
            publish();
            final PushbackInputStream is = file.getPushBackInputStream(EncodingDetector.MAX_RECOMMENDED_BYTE_SIZE);
            if (is instanceof HasProgress) {
                Thread progressThread = new Thread(() -> {
                    while (true) {
                        progress = ((HasProgress) is).getProgress();
                        publish();
                        if (progress >= 100 || readException != null || progress < 0) {
                            progress = -1;
                            publish();
                            break;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}
                    }
                });
                progressThread.setName("ProgressInputStreamThread");
                progressThread.start();

            }
            // read one byte to init reader (actual for avrdude-files, etc.)
            int b = is.read();
            if (b >= 0) {
                is.unread(b);
            }
        } catch (Throwable e) {
            readException = e;
        }
        return null;
    }

    @Override
    protected void process(List<Void> chunks) {
        if (!taskWidgetAttached) {
            mainFrame.getStatusBar().getTaskPanel().addTask(taskWidget);
            mainFrame.getStatusBar().revalidate();
            mainFrame.getStatusBar().repaint();
            taskWidgetAttached = true;
        }
        taskWidget.setProgress(progress);
    }

    @Override
    protected void done() {
        taskWidget.removeFromPanel();
        if (readException != null) {
            mainFrame.getStatusBar().setStatusInfo(Translator.get("text_viewer.open_file_error"));
        } else  if (onFinish != null) {
            onFinish.run();
        }
    }


}
