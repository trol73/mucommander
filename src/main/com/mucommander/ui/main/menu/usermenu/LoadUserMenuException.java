package com.mucommander.ui.main.menu.usermenu;

import org.json.JSONException;

public class LoadUserMenuException extends Exception {

    private final int line, column;

    LoadUserMenuException(JSONException e) {
        super(extractMessage(e), e);
        this.line = parseJsonErrorLine(e);
        this.column = parseJsonErrorColumn(e);

    }

    private static String extractMessage(JSONException e) {
        String msg = e.getMessage();
        if (!msg.contains("[character ") || !msg.contains(" line ") || !msg.contains("]")) {
            return msg;
        }
        int pos = msg.lastIndexOf('[');
        if (pos < 0) {
            return msg;
        } else {
            String firstPart = msg.substring(0, pos-1);
            int posAt = firstPart.lastIndexOf("at ");
            return posAt < 0 ? firstPart : firstPart.substring(0, posAt);
        }
    }


    private int parseJsonErrorLine(JSONException e) {
        String msg = e.getMessage();
        int pos = msg.lastIndexOf("[character ");
        if (pos < 0) {
            return -1;
        }
        String parts[] = msg.substring(pos, msg.length()-1).split(" ");
        try {
            return Integer.parseInt(parts[3]);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private int parseJsonErrorColumn(JSONException e) {
        String msg = e.getMessage();
        int pos = msg.lastIndexOf("[character ");
        if (pos < 0) {
            return -1;
        }
        String parts[] = msg.substring(pos, msg.length()-1).split(" ");
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
