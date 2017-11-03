/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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
package com.mucommander.ui.viewer.text;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.io.bom.BOMInputStream;
import com.mucommander.ui.viewer.text.utils.CodeFormatException;
import com.mucommander.ui.viewer.text.utils.CodeFormatter;
import com.mucommander.utils.text.Translator;

import java.io.IOException;
import java.io.PushbackInputStream;

public class TextEditorUtils {

    static FileType detectFileFormat(AbstractFile file) {
        byte bytes[] = BufferPool.getByteArray(256);
        int readBytes;
        try {
            PushbackInputStream is = file.getPushBackInputStream(256);
            BOMInputStream bomIs = new BOMInputStream(is);
            readBytes = StreamUtils.readUpTo(bomIs, bytes);
            is.unread(bytes, 0, readBytes);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                file.closePushbackInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return FileType.NONE;
        }
        if (readBytes < 5) {
            return FileType.NONE;
        }

        String str = new String(bytes, 0, readBytes).trim().toLowerCase();
        if (str.startsWith("<?xml")) {
            return FileType.XML;
        } else if (str.startsWith("<?php")) {
            return FileType.PHP;
        } else if (str.startsWith("#!/usr/bin/python") | str.startsWith("#! /usr/bin/python")) {
            return FileType.PYTHON;
        } else if (str.startsWith("#!/bin/bash") || str.startsWith("#!/bin/sh") || str.startsWith("#!/usr/bin/env bash") || str.startsWith("#! /bin/bash") || str.startsWith("#! /bin/sh")) {
            return FileType.UNIX_SHELL;
        } else if (str.startsWith("<!DOCTYPE html")) {
            return FileType.HTML;
        } else if (str.startsWith("#!/usr/bin/ruby") || str.startsWith("#!/usr/bin/env ruby")) {
            return FileType.RUBY;
        }
        return FileType.NONE;
    }


    static void formatTextArea(TextArea textArea) throws CodeFormatException {
        String src = textArea.getText();
        String formatted = null;

        switch (textArea.getFileType()) {
            case XML:
                formatted = CodeFormatter.formatXml(src);
                break;
            case JSON:
                formatted = CodeFormatter.formatJson(src);
                break;
        }
        if (formatted != null) {
            textArea.setText(formatted);
        }

    }

    static void formatCode(TextEditorImpl textEditor) {
        try {
            formatTextArea(textEditor.getTextArea());
            textEditor.setStatusMessage("");
        } catch (CodeFormatException e) {
            if (e.getLine() > 0 ) {
                if (e.getRow() > 0) {
                    textEditor.getTextArea().gotoLine(e.getLine(), e.getRow());
                } else {
                    textEditor.getTextArea().gotoLine(e.getLine());
                }
            }
            textEditor.setStatusMessage(e.getLocalizedMessage());
        } catch (Exception e) {
            textEditor.setStatusMessage(Translator.get("error"));
        }
    }

}
