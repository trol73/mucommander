package com.mucommander.ui.main.menu.usermenu;

import java.util.List;

public class UserMenuItem {

    public enum ConsoleType {
        SHOW,
        NONE,
        HIDE,
        APPEND;

        public static ConsoleType fromStr(String s) {
            for (ConsoleType c : values()) {
                if (c.name().equalsIgnoreCase(s)) {
                    return c;
                }
            }
            return HIDE;
        }
    }

    public static class Command {
        public final List<List<String>> commandsList;
        public final String singleCommand;

        public Command(String singleCommand) {
            this.singleCommand = singleCommand;
            this.commandsList = null;
        }

        public Command(List<List<String>> commandsList) {
            this.singleCommand = null;
            this.commandsList = commandsList;
        }

        public boolean isSingle() {
            return singleCommand != null;
        }
    }

    public final Command command;
    public final ConsoleType console;

    UserMenuItem(Command command, ConsoleType console) {
        this.command = command;
        this.console = console;
    }

}
