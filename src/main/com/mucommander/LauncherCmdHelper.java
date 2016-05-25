/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.main.toolbar.ToolBarIO;

/**
 * Helper class for launcher command line
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Oleg Trifonov
 */
public class LauncherCmdHelper {
	private static Logger logger;

    /**
     * Whether or not to display verbose error messages.
     */
    private boolean verbose;

    /**
     * Whether or not to ignore warnings when booting.
     */
    private boolean fatalWarnings;

    /**
     * Index in the command line arguments.
     */
    private int index;

    private final String args[];


    public LauncherCmdHelper(String[] args, boolean verbose, boolean fatalWarnings) {
        this.args = args;
        this.verbose = verbose;
        this.fatalWarnings = fatalWarnings;
    }


    // - Commandline handling methods -------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Prints muCommander's command line usage and exits.
     */
    private static void printUsage() {
        System.out.println("Usage: mucommander [options] [folders]");
        System.out.println("Options:");

        // Allows users to tweak how file associations are loaded / saved.
        System.out.println(" -a FILE, --assoc FILE             Load associations from FILE.");

        // Allows users to tweak how bookmarks are loaded / saved.
        System.out.println(" -b FILE, --bookmarks FILE         Load bookmarks from FILE.");

        // Allows users to tweak how configuration is loaded / saved.
        System.out.println(" -c FILE, --configuration FILE     Load configuration from FILE");

        // Allows users to tweak how command bar configuration is loaded / saved.
        System.out.println(" -C FILE, --commandbar FILE        Load command bar from FILE.");

        // Allows users to change the extensions folder.
        System.out.println(" -e FOLDER, --extensions FOLDER    Load extensions from FOLDER.");

        // Allows users to tweak how custom commands are loaded / saved.
        System.out.println(" -f FILE, --commands FILE          Load custom commands from FILE.");

        // Ignore warnings.
        System.out.println(" -index, --ignore-warnings             Do not fail on warnings (default).");

        // Allows users to tweak how keymaps are loaded.
        System.out.println(" -k FILE, --keymap FILE            Load keymap from FILE");

        // Allows users to change the preferences folder.
        System.out.println(" -p FOLDER, --preferences FOLDER   Store configuration files in FOLDER");

        // muCommander will not print verbose error messages.
        System.out.println(" -S, --silent                      Do not print verbose error messages");

        // Allows users to tweak how shell history is loaded / saved.
        System.out.println(" -s FILE, --shell-history FILE     Load shell history from FILE");

        // Allows users to tweak how toolbar configuration are loaded.
        System.out.println(" -t FILE, --toolbar FILE           Load toolbar from FILE");

        // Allows users to tweak how credentials are loaded.
        System.out.println(" -u FILE, --credentials FILE       Load credentials from FILE");

        // Text commands.
        System.out.println(" -h, --help                        Print the help text and exit");
        System.out.println(" -v, --version                     Print the version and exit");

        // muCommander will print verbose boot error messages.
        System.out.println(" -V, --verbose                     Print verbose error messages (default)");

        // Pedantic mode.
        System.out.println(" -w, --fail-on-warnings            Quits when a warning is encountered during");
        System.out.println("                                   the boot process.");
        System.exit(0);
    }

    /**
     * Prints muCommander's version to stdout and exits.
     */
    private static void printVersion() {
        System.out.println(RuntimeConstants.APP_STRING);
        System.out.print("Copyright (C) ");
        System.out.print(RuntimeConstants.COPYRIGHT);
        System.out.println(" Maxence Bernard");
        System.out.println("This is free software, distributed under the terms of the GNU General Public License.");
        System.exit(0);
    }


    public void parseArgs() {
        // - Command line parsing -------------------------------------
        // ------------------------------------------------------------
        label:
        for (index = 0; index < args.length; index++) {
            // Print version.
            switch (args[index]) {
                case "-v":
                case "--version":
                    printVersion();
                    break;

                // Print help.
                case "-h":
                case "--help":
                    printUsage();
                    break;

                // Associations handling.
                case "-a":
                case "--assoc":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        com.mucommander.command.CommandManager.setAssociationFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set association files", e, fatalWarnings);
                    }
                    break;

                // Custom commands handling.
                case "-f":
                case "--commands":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        com.mucommander.command.CommandManager.setCommandFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set commands file", e, fatalWarnings);
                    }
                    break;

                // Bookmarks handling.
                case "-b":
                case "--bookmarks":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        com.mucommander.bookmark.BookmarkManager.setBookmarksFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set bookmarks file", e, fatalWarnings);
                    }
                    break;

                // Configuration handling.
                case "-c":
                case "--configuration":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        MuConfigurations.setPreferencesFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set configuration file", e, fatalWarnings);
                    }
                    break;

                // Shell history.
                case "-s":
                case "--shell-history":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        ShellHistoryManager.setHistoryFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set shell history file", e, fatalWarnings);
                    }
                    break;

                // Keymap file.
                case "-k":
                case "--keymap":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        com.mucommander.ui.action.ActionKeymapIO.setActionsFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set keymap file", e, fatalWarnings);
                    }
                    break;

                // Toolbar file.
                case "-t":
                case "--toolbar":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        ToolBarIO.setDescriptionFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set keymap file", e, fatalWarnings);
                    }
                    break;

                // Commandbar file.
                case "-C":
                case "--commandbar":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        CommandBarIO.setDescriptionFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set commandbar description file", e, fatalWarnings);
                    }
                    break;

                // Credentials file.
                case "-U":
                case "--credentials":
                    if (index >= args.length - 1)
                        printError("Missing FILE parameter to " + args[index], null, true);
                    try {
                        com.mucommander.auth.CredentialsManager.setCredentialsFile(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set credentials file", e, fatalWarnings);
                    }
                    break;

                // Preference folder.
                case "-p":
                case "--preferences":
                    if (index >= args.length - 1)
                        printError("Missing FOLDER parameter to " + args[index], null, true);
                    try {
                        PlatformManager.setPreferencesFolder(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set preferences folder", e, fatalWarnings);
                    }
                    break;

                // Extensions folder.
                case "-e":
                case "--extensions":
                    if (index >= args.length - 1)
                        printError("Missing FOLDER parameter to " + args[index], null, true);
                    try {
                        ExtensionManager.setExtensionsFolder(args[++index]);
                    } catch (Exception e) {
                        printError("Could not set extensions folder", e, fatalWarnings);
                    }
                    break;

                // Ignore warnings.
                case "-index":
                case "--ignore-warnings":
                    fatalWarnings = false;
                    break;

                // Fail on warnings.
                case "-w":
                case "--fail-on-warnings":
                    fatalWarnings = true;
                    break;

                // Silent mode.
                case "-S":
                case "--silent":
                    verbose = false;
                    break;

                // Verbose mode.
                case "-V":
                case "--verbose":
                    verbose = true;
                    break;

                // Illegal argument.
                default:
                    break label;
            }
        }
    }

    /**
     * Prints an error message.
     */
    private static void printError(String msg, boolean quit) {
        if (quit) {
        	getLogger().error(msg);
            System.err.println("See mucommander --help for more information.");
            System.exit(1);
        } else{
        	getLogger().warn(msg);
        }
    }

    /**
     * Prints a configuration file specific error message.
     */
    void printFileError(String msg, Throwable exception, boolean quit) {
        StringBuilder error;

        error = createErrorMessage(msg, exception, quit);
        if (!quit)
            error.append(". Using default values.");

        printError(error.toString(), quit);
    }

    public void printFileError(String msg, Throwable exception) {
        printFileError(msg, exception, fatalWarnings);
    }

    /**
     * Prints the specified error message to stderr.
     * @param msg       error message to print to stder.
     * @param quit      whether or not to quit after printing the error message.
     * @param exception exception that triggered the error (for verbose output).
     */
    public void printError(String msg, Exception exception, boolean quit) {
        printError(createErrorMessage(msg, exception, quit).toString(), quit);
    }

    /**
     * Creates an error message.
     */
    private StringBuilder createErrorMessage(String msg, Throwable exception, boolean quit) {
        StringBuilder error;

        error = new StringBuilder();
        if(quit)
            error.append("Warning: ");
        error.append(msg);
        if(verbose && (exception != null)) {
            error.append(": ");
            error.append(exception.getMessage());
        }

        return error;
    }

    public String[] getFolders() {
        String[] folders = new String[args.length - index];
        System.arraycopy(args, index, folders, 0, folders.length);
        return folders;
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(TrolCommander.class);
        }
        return logger;
    }

}
