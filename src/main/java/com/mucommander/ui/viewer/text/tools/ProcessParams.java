package com.mucommander.ui.viewer.text.tools;

import com.mucommander.commons.file.AbstractFile;

public class ProcessParams {

    public final AbstractFile folder;
    public final String command;

    ProcessParams(AbstractFile folder, String command) {
        this.folder = folder;
        this.command = command;
    }
}
