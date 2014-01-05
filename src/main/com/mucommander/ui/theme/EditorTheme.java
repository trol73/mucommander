package com.mucommander.ui.theme;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.Theme;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by trol on 04/01/14.
 */
public class EditorTheme {
    private org.fife.ui.rsyntaxtextarea.Theme theme;

    private EditorTheme() {
    }

    private EditorTheme(org.fife.ui.rsyntaxtextarea.Theme theme) {
        this.theme = theme;
    }

    public EditorTheme(RSyntaxTextArea textArea) {
        theme = new Theme(textArea);
    }
    public static EditorTheme load(InputStream in) throws IOException {
        return new EditorTheme(org.fife.ui.rsyntaxtextarea.Theme.load(in));
    }

    public static EditorTheme load(InputStream in, Font baseFont) throws IOException {
        return new EditorTheme(org.fife.ui.rsyntaxtextarea.Theme.load(in, baseFont));
    }

    public void apply(RSyntaxTextArea textArea) {
        theme.apply(textArea);
    }

    public void save(OutputStream out) throws IOException {
        theme.save(out);
    }

}
