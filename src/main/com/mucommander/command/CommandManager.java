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

package com.mucommander.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.mucommander.commons.file.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.PermissionTypes;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.io.backup.BackupOutputStream;

/**
 * Manages custom commands and associations.
 * @author Nicolas Rinaudo
 */
public class CommandManager implements CommandBuilder {
	private static Logger logger;
	
    // - Built-in commands -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias for the system file opener. */
    public static final String FILE_OPENER_ALIAS           = "open";
    /** Alias for the system URL opener. */
    public static final String URL_OPENER_ALIAS            = "openURL";
    /** Alias for the system file manager. */
    public static final String FILE_MANAGER_ALIAS          = "openFM";
    /** Alias for the system executable file opener. */
    public static final String EXE_OPENER_ALIAS            = "openEXE";
    /** Alias for the default text viewer. */
    public static final String VIEWER_ALIAS                = "view";
    /** Alias for the default text editor. */ 
    public static final String EDITOR_ALIAS                = "edit";



    // - Self-open command -----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Alias of the 'init as executable' command. */
    private static final String  RUN_AS_EXECUTABLE_ALIAS   = "execute";
    /** Command used to init a file as an executable. */
    private static final Command RUN_AS_EXECUTABLE_COMMAND = new Command(RUN_AS_EXECUTABLE_ALIAS, "$f", CommandType.SYSTEM_COMMAND);



    // - Association definitions -----------------------------------------------
    // -------------------------------------------------------------------------
    /** System dependent file associations. */
    private static final List<CommandAssociation> systemAssociations = new ArrayList<>();
    /** All known file associations. */
    private static final List<CommandAssociation> associations = new ArrayList<>();
    /** Path to the custom association file, <code>null</code> if the default one should be used. */
    private static       AbstractFile             associationFile;
    /** Whether the associations were modified since the last time they were saved. */
    private static       boolean                  wereAssociationsModified;
    /** Default name of the association XML file. */
    private static final String                   DEFAULT_ASSOCIATION_FILE_NAME = "associations.xml";



    // - Commands definition ---------------------------------------------------
    // -------------------------------------------------------------------------

    /** Default name of the custom commands file. */
    private static final String DEFAULT_COMMANDS_FILE_NAME = "commands.xml";

    /** All known commands. */
    private static Map<String, List<Command>> commands = new HashMap<>();
    /** Path to the custom commands XML file, <code>null</code> if the default one should be used. */
    private static       AbstractFile         commandsFile;
    /** Whether the custom commands have been modified since the last time they were saved. */
    private static       boolean              wereCommandsModified;
    /** Default command used when no other command is found for a specific file type. */
    private static       Command              defaultCommand;


    /**
     * Prevents instances of CommandManager from being created.
     */
    private CommandManager() {}



    // - Command handling ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the tokens that compose the command that must be executed to open the specified file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling
     * <code>{@link #getTokensForFile(AbstractFile,boolean) getTokensForFile(}file, true)</code>.
     *
     * @param file file for which the opening command's tokens must be returned.
     * @return the tokens that compose the command that must be executed to open the specified file.
     */
    public static String[] getTokensForFile(AbstractFile file) {
        return getTokensForFile(file, true);
    }

    /**
     * Returns the tokens that compose the command that must be executed to open the specified file.
     * @param  file         file for which the opening command's tokens must be returned.
     * @param  allowDefault whether to use the default command if none was found to match the specified file.
     * @return              the tokens that compose the command that must be executed to open the specified file, <code>null</code> if not found.
     */
    public static String[] getTokensForFile(AbstractFile file, boolean allowDefault) {
        Command command = getCommandForFile(file, allowDefault);
        return command == null ? null : command.getTokens(file);
    }

    /**
     * Returns the command that must be executed to open the specified file.
     * <p>
     * This is a convenience method and is stricly equivalent to calling
     * <code>{@link #getCommandForFile(AbstractFile,boolean) getCommandForFile(}file, true)</code>.
     *
     * @param  file file for which the opening command must be returned.
     * @return      the command that must be executed to open the specified file.
     */
    public static Command getCommandForFile(AbstractFile file) {
        return getCommandForFile(file, true);
    }

    private static Command getCommandForFile(AbstractFile file, List<CommandAssociation> associations) {
        for (CommandAssociation association : associations) {
            if (association.accept(file))
                return association.getCommand();
        }
        return null;
    }

    /**
     * Returns the command that must be executed to open the specified file.
     * @param  file         file for which the opening command must be returned.
     * @param  allowDefault whether to use the default command if none was found to match the specified file.
     * @return              the command that must be executed to open the specified file, <code>null</code> if not found.
     */
    public static Command getCommandForFile(AbstractFile file, boolean allowDefault) {
        Command command  = getCommandForFile(file, associations);
        // Goes through all known associations and checks whether file matches any.
        if (command != null) {
            return command;
        }

        // Goes through all system associations and checks whether file matches any.
        command = getCommandForFile(file, systemAssociations);
        if (command != null) {
            return command;
        }

        // We haven't found a command explicitly associated with 'file',
        // but we might have a generic file opener.
        if (defaultCommand != null) {
            return defaultCommand;
        }

        // We don't have a generic file opener, return the 'self execute'
        // command if we're allowed.
        if (allowDefault) {
            return RUN_AS_EXECUTABLE_COMMAND;
        }
        return null;
    }

    /**
     * Returns a sorted collection of all registered commands.
     * @return a sorted collection of all registered commands.
     */
    public static Collection<Command> commands() {
        // Copy the registered commands to a new list
    	List<Command> list = new ArrayList<>();
        for (List<Command> lst : commands.values()) {
            list.addAll(lst);
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Returns the command associated with the specified alias.
     * @param  alias alias whose associated command should be returned.
     * @return       the command associated with the specified alias if found, <code>null</code> otherwise.
     */
    public static Command getCommandForAlias(String alias, AbstractFile file) {
        List<Command> list = commands.get(alias);
        if (list == null || list.isEmpty()) {
            return null;
        }
        // if we have command with specified filemask then return it
        for (Command cmd : list) {
            if (checkFileMask(cmd, file)) {
                return cmd;
            }
        }
        // else if we have command with empty filemask (default command) then return it
        for (Command cmd : list) {
            String fileMask = cmd.getFileMask();
            if (fileMask == null || fileMask.isEmpty()) {
                return cmd;
            }
        }
        return null;
    }

    /**
     *
     * @param cmd command
     * @param file file to check
     * @return true if file corresponds to command filemask
     */
    public static boolean checkFileMask(Command cmd, AbstractFile file) {
        String fileMask = cmd.getFileMask();
        if (fileMask == null || fileMask.isEmpty()) {
            return false;
        }
        String[] split = fileMask.split(",");
        for (String aSplit : split) {
            String mask = aSplit.trim().toLowerCase();
            if (mask.isEmpty()) {
                continue;
            }
            WildcardFileFilter filter = new WildcardFileFilter(mask);
            if (filter.accept(file)) {
                return true;
            }
        }

        return false;
    }

    private static void setDefaultCommand(Command command) {
        if (defaultCommand == null && command.getAlias().equals(FILE_OPENER_ALIAS)) {
        	getLogger().debug("Registering '" + command.getCommand() + "' as default command.");
            defaultCommand = command;
        }
    }

    private static void registerCommand(Command command, boolean mark)  {
        // Registers the command and marks command as having been modified.
        setDefaultCommand(command);

        getLogger().debug("Registering '" + command.getCommand() + "' as '" + command.getAlias() + "'");
        final String alias = command.getAlias();
        if (!commands.containsKey(alias)) {
            commands.put(alias, new ArrayList<>());
        }
        commands.get(alias).add(command);
        if (mark) {
            wereCommandsModified = true;
        }
//        Command oldCommand = commands.put(command.getAlias(), command);
//        if (mark && !command.equals(oldCommand)) {
//            wereCommandsModified = true;
//        }
    }

    public static void registerDefaultCommand(Command command) throws CommandException {
        registerCommand(command, false);
    }

    /**
     * Registers the specified command at the end of the command list.
     * @param  command          command to register.
     */
    public static void registerCommand(Command command) {
        registerCommand(command, true);
    }



    // - Associations handling -------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Registers the specified association.
     * @param  command          command to execute when the association is matched.
     * @param  filter           file filters that a file must match to be accepted by the association.
     * @throws CommandException if an error occurs.
     */
    public static void registerAssociation(String command, FileFilter filter) throws CommandException {
        associations.add(createAssociation(command, filter));
    }
    
    private static CommandAssociation createAssociation(String cmd, FileFilter filter) throws CommandException {
        Command command = getCommandForAlias(cmd, null);

        if (command == null) {
        	getLogger().debug("Failed to create association as '" + command + "' is not known.");
            throw new CommandException(cmd + " not found");
        }

        return new CommandAssociation(command, filter);
    }

    public static void registerDefaultAssociation(String command, FileFilter filter) throws CommandException {
        systemAssociations.add(createAssociation(command, filter));
    }



    // - Command builder code --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void addCommand(Command command) {
        registerCommand(command, false);
    }

    /**
     * Passes all known custom commands to the specified builder.
     * <p>
     * This method guarantees that the builder's {@link CommandBuilder#startBuilding() startBuilding()} and
     * {@link CommandBuilder#endBuilding() endBuilding()} methods will both be called even if an error occurs.
     * If that happens however, it is entirely possible that not all commands will be passed to
     * the builder.
     *
     * @param  builder          object that will receive commands list building messages.
     * @param type              if not null then build only commands with specified type
     * @throws CommandException if anything goes wrong.
     */
    public static void buildCommands(CommandBuilder builder, CommandType type) throws CommandException {
        builder.startBuilding();

        try {
        	// Goes through all the registered commands.
        	for (Command command : commands()) {
                if (type == null || command.getType() == type) {
                    builder.addCommand(command);
                }
            }
        } finally {
            builder.endBuilding();
        }
    }



    // - Associations building -------------------------------------------------
    // -------------------------------------------------------------------------
    private static void buildFilter(FileFilter filter, AssociationBuilder builder) throws CommandException {
        // Filter on the file type.
        if (filter instanceof AttributeFileFilter) {
            AttributeFileFilter attributeFilter = (AttributeFileFilter)filter;

            switch (attributeFilter.getAttribute()) {
            case HIDDEN:
                builder.setIsHidden(!attributeFilter.isInverted());
                break;

            case SYMLINK:
                builder.setIsSymlink(!attributeFilter.isInverted());
                break;
            }
        } else if (filter instanceof PermissionsFileFilter) {
            PermissionsFileFilter permissionFilter = (PermissionsFileFilter)filter;

            switch(permissionFilter.getPermission()) {
            case PermissionTypes.READ_PERMISSION:
                builder.setIsReadable(permissionFilter.getFilter());
                break;

            case PermissionTypes.WRITE_PERMISSION:
                builder.setIsWritable(permissionFilter.getFilter());
                break;

            case PermissionTypes.EXECUTE_PERMISSION:
                builder.setIsExecutable(permissionFilter.getFilter());
                break;
            }
        } else if (filter instanceof RegexpFilenameFilter) {
            RegexpFilenameFilter regexpFilter = (RegexpFilenameFilter)filter;

            builder.setMask(regexpFilter.getRegularExpression(), regexpFilter.isCaseSensitive());
        }
    }

    /**
     * Passes all known file associations to the specified builder.
     * <p>
     * This method guarantees that the builder's {@link AssociationBuilder#startBuilding() startBuilding()} and
     * {@link AssociationBuilder#endBuilding() endBuilding()} methods will both be called even if an error occurs.
     * If that happens however, it is entirely possible that not all associations will be passed to
     * the builder.
     *
     * @param  builder          object that will receive association list building messages.
     * @throws CommandException if anything goes wrong.
     */
    public static void buildAssociations(AssociationBuilder builder) throws CommandException {
        builder.startBuilding();

        // Goes through all the registered associations.
        try {
            for (CommandAssociation current : associations) {
                builder.startAssociation(current.getCommand().getAlias());

                FileFilter filter = current.getFilter();
                if (filter instanceof ChainedFileFilter) {
                    Iterator<FileFilter> filters = ((ChainedFileFilter)filter).getFileFilterIterator();
                    while (filters.hasNext()) {
                        buildFilter(filters.next(), builder);
                    }
                } else {
                    buildFilter(filter, builder);
                }

                builder.endAssociation();
            }
        } finally {
            builder.endBuilding();
        }
    }



    // - Associations reading/writing ------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the custom associations XML file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created custom
     * associations.
     * <p>
     * This method's return value can be modified through {@link #setAssociationFile(String)}.
     * If this wasn't called, the default path will be used: {@link #DEFAULT_ASSOCIATION_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     *
     * @return the path to the custom associations XML file.
     * @see    #setAssociationFile(String)
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     * @throws IOException if there was an error locating the default commands file.
     */
    public static AbstractFile getAssociationFile() throws IOException {
        if (associationFile == null) {
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_ASSOCIATION_FILE_NAME);
        }
        return associationFile;
    }

    /**
     * Sets the path to the custom associations file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setAssociationFile(FileFactory.getFile(file))</code>.
     *
     * @param  path                  path to the custom associations file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(String path) throws FileNotFoundException {
        AbstractFile file = FileFactory.getFile(path);
        if (file == null) {
            setAssociationFile(new File(path));
        } else {
            setAssociationFile(file);
        }
    }

    /**
     * Sets the path to the custom associations file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setAssociationFile(FileFactory.getFile(file.getAbsolutePath()))</code>.
     *
     * @param  file                  path to the custom associations file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(File file) throws FileNotFoundException {
        setAssociationFile(FileFactory.getFile(file.getAbsolutePath()));
    }

    /**
     * Sets the path to the custom associations file.
     * @param  file                  path to the custom associations file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getAssociationFile()
     * @see    #loadAssociations()
     * @see    #writeAssociations()
     */
    public static void setAssociationFile(AbstractFile file) throws FileNotFoundException {
        if (file.isBrowsable()) {
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        }

        associationFile = file;
    }

    /**
     * Loads the custom associations XML File.
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link BackupInputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     *
     * @throws IOException if an IO error occurs.
     * @throws CommandException thrown when errors occur while building custom commands
     * @see                #writeAssociations()
     * @see                #getAssociationFile()
     * @see                #setAssociationFile(String)
     */
    public static void loadAssociations() throws IOException, CommandException {
        AbstractFile file = getAssociationFile();
        getLogger().debug("Loading associations from file: " + file.getAbsolutePath());

        // Tries to load the associations file.
        // Associations are not considered to be modified by this. 
        //InputStream in = null;
        try (InputStream in = new BackupInputStream(file)) {
            AssociationReader.read(in, new AssociationFactory());
        } finally {
            wereAssociationsModified = false;
        }
    }

    /**,
     * Writes all registered associations to the custom associations file.
     * <p>
     * Data will be written to the path returned by {@link #getAssociationFile()}. Note, however,
     * that this method will not actually do anything if the association list hasn't been modified
     * since the last time it was saved.
     * <p>
     * The association files will be saved as a <i>backed-up file</i> (see {@link BackupOutputStream}).
     * Its format is described {@link AssociationsXmlConstants here}.
     *
     * @throws IOException      if an I/O error occurs.
     * @throws CommandException if an error occurs.
     * @see                     #loadAssociations()
     * @see                     #getAssociationFile()
     * @see                     #setAssociationFile(String)
     */
    public static void writeAssociations() throws CommandException, IOException {
        // Do not save the associations if they were not modified.
        if (!wereAssociationsModified) {
            getLogger().debug("Custom file associations not modified, skip saving.");
            return;
        }
        getLogger().debug("Writing associations to file: " + getAssociationFile());

        // Writes the associations.
        try (BackupOutputStream out = new BackupOutputStream(getAssociationFile())) {
            buildAssociations(new AssociationWriter(out));
            wereAssociationsModified = false;
        }
    }



    // - Commands reading/writing ----------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the custom commands XML file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created custom
     * commands.
     * <p>
     * This method's return value can be modified through {@link #setCommandFile(String)}.
     * If this wasn't called, the default path will be used: {@link #DEFAULT_COMMANDS_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     *
     * @return the path to the custom commands XML file.
     * @see    #setCommandFile(String)
     * @see    #loadCommands()
     * @see    #writeCommands()
     * @throws IOException if there was some error locating the default commands file.
     */
    public static AbstractFile getCommandFile() throws IOException {
        if (commandsFile == null) {
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_COMMANDS_FILE_NAME);
        }
        return commandsFile;
    }

    /**
     * Sets the path to the custom commands file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setCommandFile(FileFactory.getFile(file));</code>.
     *
     * @param  path                  path to the custom commands file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(String path) throws FileNotFoundException {
        AbstractFile file = FileFactory.getFile(path);

        if (file == null) {
            setCommandFile(new File(path));
        } else {
            setCommandFile(file);
        }
    }
        

    /**
     * Sets the path to the custom commands file.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setCommandFile(FileFactory.getFile(file.getAbsolutePath()));</code>.
     *
     * @param  file                  path to the custom commands file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(File file) throws FileNotFoundException {setCommandFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the custom commands file.
     * @param  file                  path to the custom commands file.
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     * @see    #getCommandFile()
     * @see    #loadCommands()
     * @see    #writeCommands()
     */
    public static void setCommandFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a command file.
        if (file.isBrowsable()) {
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        }

        commandsFile = file;
    }

    /**
     * Writes Normal registered commands to the custom commands file.
     * <p>
     * Data will be written to the path returned by {@link #getCommandFile()}. Note, however,
     * that this method will not actually do anything if the command list hasn't been modified
     * since the last time it was saved.
     * <p>
     * The command files will be saved as a <i>backed-up file</i> (see {@link BackupOutputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     *
     * @throws IOException      if an I/O error occurs.
     * @throws CommandException if an error occurs.
     * @see                     #loadCommands()
     * @see                     #getCommandFile()
     * @see                     #setCommandFile(String)
     */
    public static void writeCommands() throws IOException, CommandException {
        // Only saves the command if they were modified since the last time they were written.
        if (!wereCommandsModified) {
            getLogger().debug("Custom commands not modified, skip saving.");
            return;
        }
        getLogger().debug("Writing custom commands to file: " + getCommandFile());

        // Writes the commands.
        try (BackupOutputStream out = new BackupOutputStream(getCommandFile())) {
            buildCommands(new CommandWriter(out), CommandType.NORMAL_COMMAND);
            wereCommandsModified = false;
        }
    }

    /**
     * Loads the custom commands XML File.
     * <p>
     * The command files will be loaded as a <i>backed-up file</i> (see {@link BackupInputStream}).
     * Its format is described {@link CommandsXmlConstants here}.
     *
     * @throws IOException if an I/O error occurs.
     * @see                #writeCommands()
     * @see                #getCommandFile()
     * @see                #setCommandFile(String)
     */
    public static void loadCommands() throws IOException, CommandException {
        AbstractFile file = getCommandFile();
        getLogger().debug("Loading custom commands from: " + file.getAbsolutePath());

        // Tries to load the commands file.
        // Commands are not considered to be modified by this.
        try (InputStream in = new BackupInputStream(file)) {
            CommandReader.read(in, new CommandManager());
        } finally {
            wereCommandsModified = false;
        }
    }

    /*
    private static void registerDefaultCommand(String alias, String command, String display) {
        if(getCommandForAlias(alias) == null) {
            if(command != null) {
                //                try {registerCommand(CommandParser.getCommand(alias, command, Command.SYSTEM_COMMAND, display));}
                try {registerCommand(new Command(alias, command, Command.SYSTEM_COMMAND, display));}
                catch(Exception e) {AppLogger.fine("Failed to register " + command + ": " + e.getMessage());}
            }
        }
    }
    */


    // - Unused methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void startBuilding() {}

    /**
     * This method is public as an implementation side effect and must not be called directly.
     */
    public void endBuilding() {}


    public static List<Command> getCommands(String type) {
        if (!commands.containsKey(type)) {
            commands.put(type, new ArrayList<>());
        }
        return commands.get(type);
    }


    /**
     * Removes all user defined commands with Normal type.
     * Called before updating list in editor dialog
     */
    public static void removeAllNormalCommands() {
        for (String type : commands.keySet()) {
            List<Command> list = commands.get(type);
            list.removeIf(cmd -> cmd.getType() == CommandType.NORMAL_COMMAND);
        }
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(CommandManager.class);
        }
        return logger;
    }
}
