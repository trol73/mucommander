package com.mucommander;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.main.toolbar.ToolBarIO;

/**
 * Created by trol on 03/01/14.
 */
public class LauncherCmdHelper {

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
        for(index = 0; index < args.length; index++) {
            // Print version.
            if(args[index].equals("-v") || args[index].equals("--version"))
                printVersion();

                // Print help.
            else if(args[index].equals("-h") || args[index].equals("--help"))
                printUsage();

                // Associations handling.
            else if(args[index].equals("-a") || args[index].equals("--assoc")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {com.mucommander.command.CommandManager.setAssociationFile(args[++index]);}
                catch(Exception e) {printError("Could not set association files", e, fatalWarnings);}
            }

            // Custom commands handling.
            else if(args[index].equals("-f") || args[index].equals("--commands")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {com.mucommander.command.CommandManager.setCommandFile(args[++index]);}
                catch(Exception e) {printError("Could not set commands file", e, fatalWarnings);}
            }

            // Bookmarks handling.
            else if(args[index].equals("-b") || args[index].equals("--bookmarks")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {com.mucommander.bookmark.BookmarkManager.setBookmarksFile(args[++index]);}
                catch(Exception e) {printError("Could not set bookmarks file", e, fatalWarnings);}
            }

            // Configuration handling.
            else if(args[index].equals("-c") || args[index].equals("--configuration")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {
                    MuConfigurations.setPreferencesFile(args[++index]);}
                catch(Exception e) {printError("Could not set configuration file", e, fatalWarnings);}
            }

            // Shell history.
            else if(args[index].equals("-s") || args[index].equals("--shell-history")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {
                    ShellHistoryManager.setHistoryFile(args[++index]);}
                catch(Exception e) {printError("Could not set shell history file", e, fatalWarnings);}
            }

            // Keymap file.
            else if(args[index].equals("-k") || args[index].equals("--keymap")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {com.mucommander.ui.action.ActionKeymapIO.setActionsFile(args[++index]);}
                catch(Exception e) {printError("Could not set keymap file", e, fatalWarnings);}
            }

            // Toolbar file.
            else if(args[index].equals("-t") || args[index].equals("--toolbar")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {
                    ToolBarIO.setDescriptionFile(args[++index]);}
                catch(Exception e) {printError("Could not set keymap file", e, fatalWarnings);}
            }

            // Commandbar file.
            else if(args[index].equals("-C") || args[index].equals("--commandbar")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {
                    CommandBarIO.setDescriptionFile(args[++index]);}
                catch(Exception e) {printError("Could not set commandbar description file", e, fatalWarnings);}
            }

            // Credentials file.
            else if(args[index].equals("-U") || args[index].equals("--credentials")) {
                if(index >= args.length - 1)
                    printError("Missing FILE parameter to " + args[index], null, true);
                try {com.mucommander.auth.CredentialsManager.setCredentialsFile(args[++index]);}
                catch(Exception e) {printError("Could not set credentials file", e, fatalWarnings);}
            }

            // Preference folder.
            else if((args[index].equals("-p") || args[index].equals("--preferences"))) {
                if(index >= args.length - 1)
                    printError("Missing FOLDER parameter to " + args[index], null, true);
                try {PlatformManager.setPreferencesFolder(args[++index]);}
                catch(Exception e) {printError("Could not set preferences folder", e, fatalWarnings);}
            }

            // Extensions folder.
            else if((args[index].equals("-e") || args[index].equals("--extensions"))) {
                if(index >= args.length - 1)
                    printError("Missing FOLDER parameter to " + args[index], null, true);
                try {
                    ExtensionManager.setExtensionsFolder(args[++index]);}
                catch(Exception e) {printError("Could not set extensions folder", e, fatalWarnings);}
            }

            // Ignore warnings.
            else if(args[index].equals("-index") || args[index].equals("--ignore-warnings"))
                fatalWarnings = false;

                // Fail on warnings.
            else if(args[index].equals("-w") || args[index].equals("--fail-on-warnings"))
                fatalWarnings = true;

                // Silent mode.
            else if(args[index].equals("-S") || args[index].equals("--silent"))
                verbose = false;

                // Verbose mode.
            else if(args[index].equals("-V") || args[index].equals("--verbose"))
                verbose = true;

                // Illegal argument.
            else
                break;
        }
    }

    /**
     * Prints an error message.
     */
    private static void printError(String msg, boolean quit) {
        System.err.println(msg);
        if(quit) {
            System.err.println("See mucommander --help for more information.");
            System.exit(1);
        }
    }

    /**
     * Prints a configuration file specific error message.
     */
    void printFileError(String msg, Throwable exception, boolean quit) {
        StringBuilder error;

        error = createErrorMessage(msg, exception, quit);
        if(!quit)
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

}
