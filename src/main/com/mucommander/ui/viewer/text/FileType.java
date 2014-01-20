/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2013-2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.viewer.text;

import com.mucommander.commons.file.AbstractFile;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import java.io.File;

/**
 * Created by trol on 04/01/14.
 */
public enum FileType {
    NONE("None", SyntaxConstants.SYNTAX_STYLE_NONE),
    ACTIONSCRIPT("ActionScript", SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT, "*.as"),
    ASSEMBLER_X86("Assembler x86", SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86, "*.asm,*.s"),
    BBCODE("BBCode", SyntaxConstants.SYNTAX_STYLE_BBCODE),
    C("C", SyntaxConstants.SYNTAX_STYLE_C, "*.c"),
    CLOJURE("Clojure", SyntaxConstants.SYNTAX_STYLE_CLOJURE, "*.clj"),
    CPP("C++", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS, "*.cpp,*.cc"),
    CSHARP("C#", SyntaxConstants.SYNTAX_STYLE_CSHARP, "*.cs"),
    CSS("CSS", SyntaxConstants.SYNTAX_STYLE_CSS, "*.css"),
    DELPHI("Delphi", SyntaxConstants.SYNTAX_STYLE_DELPHI, "*.pas,*.dpr"),
    DTD("DTD", SyntaxConstants.SYNTAX_STYLE_DTD, "*.dtd"),
    FORTRAN("Fortran", SyntaxConstants.SYNTAX_STYLE_FORTRAN, "*.f,*.for,*.ftn,*.i"),
    GROOVY("Groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY,"*.groovy,*.gvy,*.gy,*.gsh,*.gradle"),
    HTACCESS("htaccess", SyntaxConstants.SYNTAX_STYLE_HTACCESS, ".htaccess"),
    HTML("HTML", SyntaxConstants.SYNTAX_STYLE_HTML, "*.html,*.htm"),
    JAVA("Java", SyntaxConstants.SYNTAX_STYLE_JAVA, "*.java"),
    JAVASCRIPT("JavaScript", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, "*.js"),
    JSON("Json", SyntaxConstants.SYNTAX_STYLE_JSON, "*.json"),
    JSP("JSP", SyntaxConstants.SYNTAX_STYLE_JSP, "*.jsp"),
    LATEX("Latex", SyntaxConstants.SYNTAX_STYLE_LATEX, "*.tex"),
    LISP("Lisp", SyntaxConstants.SYNTAX_STYLE_LISP, "*.lisp,*.lsp"),
    LUA("Lua", SyntaxConstants.SYNTAX_STYLE_LUA, "*.lua"),
    MAKEFILE("Makefile", SyntaxConstants.SYNTAX_STYLE_MAKEFILE, "Makefile"),
    MXML("MXML", SyntaxConstants.SYNTAX_STYLE_MXML, "*.mxml"),
    NSIS("Nsis", SyntaxConstants.SYNTAX_STYLE_NSIS, "*.nsi"),
    PERL("Perl", SyntaxConstants.SYNTAX_STYLE_PERL, "*.pl"),
    PHP("PHP", SyntaxConstants.SYNTAX_STYLE_PHP, "*.php"),
    PROPERTIES_FILE("Properties file", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE, "*.properties"),
    PYTHON("Python", SyntaxConstants.SYNTAX_STYLE_PYTHON, "*.py"),
    RUBY("Ruby", SyntaxConstants.SYNTAX_STYLE_RUBY, "*.rb"),
    SAS("SAS", SyntaxConstants.SYNTAX_STYLE_SAS, "*.sas"),
    SCALA("Scala", SyntaxConstants.SYNTAX_STYLE_SCALA, "*.scala"),
    SQL("SQL", SyntaxConstants.SYNTAX_STYLE_SQL, "*.sql"),
    TCL("TCL", SyntaxConstants.SYNTAX_STYLE_TCL, "*.tcl"),
    UNIX_SHELL("Unix Shell", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL, "*.sh"),
    VISUAL_BASIC("VisualBasic", SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC, "*.bat,*.vb"),
    WINDOWS_BATCH("Windows bath", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH, "*.bat,*.cmd"),
    XML("XML", SyntaxConstants.SYNTAX_STYLE_XML, "*.xml,Info.plist,*.jnlp");


    private final String contentType;
    private final String fileMasks;
    private final WildcardFileFilter fileFilters[];
    private final String name;

    FileType(String name, String contentType, String fileMasks) {
        this.name = name;
        this.contentType = contentType;
        this.fileMasks = fileMasks;
        this.fileFilters = buildFileFilters(fileMasks);
    }


    FileType(String name, String contentType) {
        this(name, contentType, null);
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public static FileType getFileType(AbstractFile file) {
        return getFileType(file.toString());
    }

    public static FileType getFileType(String fileName) {
        File f = new File(fileName);
        for (FileType ft : FileType.values()) {
            if (ft.checkFile(f))  {
                return ft;
            }
        }
        return NONE;
    }

    public boolean checkFile(File file) {
        for (WildcardFileFilter filter : fileFilters) {
            if (filter.accept(file)) {
                return true;
            }
        }
        return false;
    }


    private static WildcardFileFilter[] buildFileFilters(String fileMasks) {
        if (fileMasks == null) {
            return new WildcardFileFilter[0];
        }
        String masks[] = fileMasks.split(",");
        WildcardFileFilter[] result = new WildcardFileFilter[masks.length];
        for (int i = 0; i < masks.length; i++) {
            result[i] = new WildcardFileFilter(masks[i]);
        }
        return result;
    }

    public static FileType getByName(String name) {
        for (FileType fileType : FileType.values()) {
            if (fileType.getName().equals(name)) {
                return fileType;
            }
        }
        return null;
    }


    public static FileType getByContentType(String contentType) {
        for (FileType fileType : FileType.values()) {
            if (fileType.getContentType().equals(contentType)) {
                return fileType;
            }
        }
        return null;
    }

}
