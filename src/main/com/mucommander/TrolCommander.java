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

import com.mucommander.auth.CredentialsManager;
import com.mucommander.bookmark.file.BookmarkProtocolProvider;
import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.icon.impl.SwingFileIconProvider;
import com.mucommander.commons.file.impl.ftp.FTPProtocolProvider;
import com.mucommander.commons.file.impl.smb.SMBProtocolProvider;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.profiler.Profiler;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.ui.action.ActionKeymapIO;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.startup.CheckVersionDialog;
import com.mucommander.ui.dialog.startup.InitialSetupDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.main.SplashScreen;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.main.frame.CommandLineMainFrameBuilder;
import com.mucommander.ui.main.frame.DefaultMainFramesBuilder;
import com.mucommander.ui.main.frame.MainFrameBuilder;
import com.mucommander.ui.main.toolbar.ToolBarIO;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.tools.ToolsEnvironment;
import com.mucommander.utils.MuLogging;
import com.mucommander.utils.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * trolCommander launcher.
 * <p>
 * This class is used to start muCommander. It will analyse command line
 * arguments, initialize the whole software and start the main window.
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Oleg Trifonov
 */
public class TrolCommander {
	private static Logger logger;

    // - Class fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    private static SplashScreen  splashScreen;
    /** Whether or not to display the splash screen. */
    private static boolean useSplash;
    /** true while the application is launching, false after it has finished launching */
    private static boolean isLaunching = true;
    /** Launch lock. */
    private static final Object LAUNCH_LOCK = new Object();


    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Prevents initialisation of the <code>Launcher</code>.
     */
    private TrolCommander() {}


    /**
     * This method can be called to wait until the application has been launched. The caller thread will be blocked
     * until the application has been launched.
     * This method will return immediately if the application has already been launched when it is called.
     */
    public static void waitUntilLaunched() {
        getLogger().debug("called, thread="+Thread.currentThread());
        synchronized(LAUNCH_LOCK) {
            while (isLaunching) {
                try {
                    getLogger().debug("waiting");
                    LAUNCH_LOCK.wait();
                } catch (InterruptedException e) {
                    // will loop
                }
            }
        }
    }




    /**
     * Prints the specified startup message.
     */
    private static void printStartupMessage(String message) {
        if (useSplash && splashScreen != null) {
            splashScreen.setLoadingMessage(message);
        }
        getLogger().trace(message);
    }


    // - Boot code --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Method used to migrate commands that used to be defined in the configuration but were moved to <code>commands.xml</code>.
     * @param useName     name of the <code>use custom command</code> configuration variable.
     * @param commandName name of the <code>custom command</code> configuration variable.
     */
    private static void migrateCommand(String useName, String commandName, String alias) {
        String command;

        if (MuConfigurations.getPreferences().getBooleanVariable(useName) && (command = MuConfigurations.getPreferences().getVariable(commandName)) != null) {
            CommandManager.registerCommand(new Command(alias, command, CommandType.SYSTEM_COMMAND));
            MuConfigurations.getPreferences().removeVariable(useName);
            MuConfigurations.getPreferences().removeVariable(commandName);
        }
    }

    /**
     * Checks whether a graphics environment is available and exit with an error otherwise.
     */
    private static void checkHeadless() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: no graphical environment detected.");
            System.exit(1);
        }
    }

    private abstract static class LauncherTask implements Callable<Void> {
        private final String name;
        protected final LauncherCmdHelper helper;
        private final LauncherTask[] depends;
        private final FutureTask<Void> task;

        LauncherTask(String name, LauncherCmdHelper helper, LauncherTask... depends) {
            this.name = "launcher." + name;
            this.helper = helper;
            this.depends = depends;
            this.task = new FutureTask<>(this);
        }

        @Override
        public final Void call() throws Exception {
            if (depends != null && depends.length > 0) {
                Profiler.start(name + ".depends");
                for (LauncherTask t : depends) {
                    t.task.get();
                }
                Profiler.stop(name + ".depends");
            }
            Profiler.start(name);
            try {
                run();
            } catch (Throwable e) {
                e.printStackTrace();
                helper.printFileError("Launcher getTask error for " + name + ": ", e);
            }
            Profiler.stop(name);
            onFinish();
            return null;
        }

        FutureTask<Void> getTask() {
            return task;
        }

        boolean isReadyForExecution() {
            if (depends == null || depends.length == 0) {
                return true;
            }
            for (LauncherTask dt : depends) {
                if (!dt.isDone()) {
                    return false;
                }
            }
            return true;
        }

        public boolean isDone() {
            return task.isDone();
        }

        void onFinish() {
        }

        @Override
        public String toString() {
            return name;
        }

        abstract void run() throws Exception;
    }

    private static class LoadConfigsTask extends LauncherTask {
        LoadConfigsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("configs", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Attempts to guess whether this is the first time muCommander is booted or not.
            //boolean isFirstBoot;
            //try {isFirstBoot = !MuConfigurations.isPreferencesFileExists();}
            //catch(IOException e) {isFirstBoot = true;}

            // Load snapshot data before loading configuration as until version 0.9 the snapshot properties
            // were stored as preferences so when loading such preferences they could overload snapshot properties
            try {
                MuConfigurations.loadSnapshot();
            } catch(Exception e) {
                helper.printFileError("Could not load snapshot", e);
            }
            // Configuration needs to be loaded before any sort of GUI creation is performed : under Mac OS X, if we're
            // to use the metal look, we need to know about it right about now.
            try {
                MuConfigurations.loadPreferences();
            } catch(Exception e) {
                helper.printFileError("Could not load configuration", e);
            }

            // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
            FileIcons.setScaleFactor(Math.max(1.0f, MuConfigurations.getPreferences().getVariable(MuPreference.TABLE_ICON_SCALE, MuPreferences.DEFAULT_TABLE_ICON_SCALE)));
            FileIcons.setSystemIconsPolicy(MuConfigurations.getPreferences().getVariable(MuPreference.USE_SYSTEM_FILE_ICONS, MuPreferences.DEFAULT_USE_SYSTEM_FILE_ICONS));
        }
    }

    private static class LoadActionsTask extends LauncherTask {
        LoadActionsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("register_actions", helper, depends);
        }

        @Override
        void run() throws Exception {
            ActionManager.registerActions();
        }
    }

    private static class InitBarsTask extends LauncherTask {
        InitBarsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("bars", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Loading actions shortcuts...");
            try {
                ActionKeymapIO.loadActionKeymap();
            } catch(Exception e) {
                e.printStackTrace();
                helper.printFileError("Could not load actions shortcuts", e);
            }

            // Loads the ToolBar's description file
            printStartupMessage("Loading toolbar description...");
            try {
                ToolBarIO.loadDescriptionFile();
            } catch(Exception e) {
                e.printStackTrace();
                helper.printFileError("Could not load toolbar description", e);
            }

            // Loads the CommandBar's description file
            printStartupMessage("Loading command bar description...");
            try {
                CommandBarIO.loadCommandBar();
            } catch(Exception e) {
                e.printStackTrace();
                helper.printFileError("Could not load commandbar description", e);
            }
        }
    }

    private static class LoadThemesTask extends LauncherTask {
        LoadThemesTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("theme", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Loading theme...");
            ThemeManager.loadCurrentTheme();
        }
    }

    private static class LoadIconsTask extends LauncherTask {
        LoadIconsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("file_icons", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Loading icons...");
            // Initialize the SwingFileIconProvider from the main thread, see method Javadoc for an explanation on why we do this now
            SwingFileIconProvider.forceInit();
        }
    }

    private static class EnableNotificationsTask extends LauncherTask {
        EnableNotificationsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("notifications", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Enable system notifications, only after MainFrame is created as SystemTrayNotifier needs to retrieve
            // a MainFrame instance
            if (MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS, MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS)) {
                printStartupMessage("Enabling system notifications...");
                if (com.mucommander.ui.notifier.AbstractNotifier.isAvailable())
                    com.mucommander.ui.notifier.AbstractNotifier.getNotifier().setEnabled(true);
            }

        }
    }

    private static class CreateWindowTask extends LauncherTask {
        CreateWindowTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("window", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Initializing window...");
            Profiler.start("launcher.create-window");
            WindowManager.createNewMainFrame(new CommandLineMainFrameBuilder(helper.getFolders())); // !!!!
            // If no initial path was specified, start a default main window.
            if (WindowManager.getCurrentMainFrame() == null) {
                MainFrameBuilder mainFrameBuilder = new DefaultMainFramesBuilder();
                WindowManager.createNewMainFrame(mainFrameBuilder);                                 // !!!!
            }
            Profiler.stop("launcher.create-window");
            Profiler.stop("loading");
            Profiler.print();
            Profiler.hide("launcher.");
        }
    }

    private static class LoadDictTask extends LauncherTask {
        LoadDictTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("dictionary", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Loading dictionary...");
            try {
                Translator.init();
            } catch(Exception e) {
                helper.printError("Could not load dictionary", e, true);
            }
        }
    }

    private static class StartBonjourTask extends LauncherTask {
        StartBonjourTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("bonjour", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Starting Bonjour services discovery...");
            com.mucommander.bonjour.BonjourDirectory.setActive(MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY, MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
        }
    }

    private static class InitDesktopTask extends LauncherTask {
        InitDesktopTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("desktop", helper, depends);
        }

        @Override
        void run() throws Exception {
            try {
                boolean install = !MuConfigurations.isPreferencesFileExists();
                com.mucommander.desktop.DesktopManager.init(install);
            } catch(Exception e) {
                helper.printError("Could not initialize desktop", e, true);
            }
        }
    }

    private static void initMacOsSupport() {
        // - MAC OS X specific init -----------------------------------
        // ------------------------------------------------------------
        // If trolCommander is running under Mac OS X (how lucky!), add some glue for the main menu bar and other OS X
        // specifics.
        if (OsFamily.MAC_OS_X.isCurrent()) {
            // Use reflection to create an OSXIntegration instance so that ClassLoader
            // doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
            try {
                Class<?> osxIntegrationClass = Class.forName("com.mucommander.ui.macosx.OSXIntegration");
                Constructor<?> constructor = osxIntegrationClass.getConstructor();
                constructor.newInstance();
            } catch(Exception e) {
                getLogger().debug("Exception thrown while initializing Mac OS X integration", e);
            }
        }

    }


    private static class StartTask extends LauncherTask {
        StartTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("start", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Ensure that a graphics environment is available, exit otherwise.
            checkHeadless();

            // - Logging configuration ------------------------------------
            // ------------------------------------------------------------
            MuLogging.configureLogging();

            initMacOsSupport();

            // - trolCommander boot -----------------------------------------
            // ------------------------------------------------------------
            // Adds all extensions to the classpath.
            try {
                Profiler.start("init-extensions-manager");
                ExtensionManager.init();
                Profiler.stop("init-extensions-manager");
                ExtensionManager.addExtensionsToClasspath();
            } catch(Exception e) {
                getLogger().debug("Failed to add extensions to the classpath", e);
            }

            // This the property is supposed to have the java.net package use the proxy defined in the system settings
            // to establish HTTP connections. This property is supported only under Java 1.5 and up.
            // Note that Mac OS X already uses the system HTTP proxy, with or without this property being set.
            System.setProperty("java.net.useSystemProxies", "true");

            //boolean showSetup = MuConfigurations.getPreferences().getVariable(MuPreference.THEME_TYPE) == null;
            // Traps VM shutdown
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        }
    }

    private static class ShowSplashTask extends LauncherTask {

        ShowSplashTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("show_splash", helper, depends);
        }

        @Override
        void run() throws Exception {
            useSplash = MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN);
            splashScreen = new SplashScreen(RuntimeConstants.VERSION, "Loading preferences...", useSplash);
        }
    }

    private static class DisposeSplashTask extends LauncherTask {
        DisposeSplashTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("dispose_splash", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Dispose splash screen.
            if (splashScreen != null) {
                splashScreen.dispose();
            }
        }
    }

    private static class ConfigureFsTask extends LauncherTask {
        ConfigureFsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("configure_fs", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Configure the SMB subsystem (backed by jCIFS) to maintain compatibility with SMB servers that don't support
            // NTLM v2 authentication such as Samba 3.0.x, which still is widely used and comes pre-installed on
            // Mac OS X Leopard.
            // Since jCIFS 1.3.0, the default is to use NTLM v2 authentication and extended security.
            SMBProtocolProvider.setLmCompatibility(MuConfigurations.getPreferences().getVariable(MuPreference.SMB_LM_COMPATIBILITY, MuPreferences.DEFAULT_SMB_LM_COMPATIBILITY));
            SMBProtocolProvider.setExtendedSecurity(MuConfigurations.getPreferences().getVariable(MuPreference.SMB_USE_EXTENDED_SECURITY, MuPreferences.DEFAULT_SMB_USE_EXTENDED_SECURITY));

            // Use the FTP configuration option that controls whether to force the display of hidden files, or leave it for
            // the servers to decide whether to show them.
            FTPProtocolProvider.setForceHiddenFilesListing(MuConfigurations.getPreferences().getVariable(MuPreference.LIST_HIDDEN_FILES, MuPreferences.DEFAULT_LIST_HIDDEN_FILES));

//            FileFactory.registerProtocolFile();
            // Use CredentialsManager for file URL authentication
            FileFactory.setDefaultAuthenticator(CredentialsManager.getAuthenticator());

            // Register the application-specific 'bookmark' protocol.
            FileFactory.registerProtocol(BookmarkProtocolProvider.BOOKMARK, new com.mucommander.bookmark.file.BookmarkProtocolProvider());
        }
    }

    private static class LoadCustomCommands extends LauncherTask {
        LoadCustomCommands(LauncherCmdHelper helper, LauncherTask... depends) {
            super("custom_commands", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Loads custom commands
            printStartupMessage("Loading file associations...");
            try {
                com.mucommander.command.CommandManager.loadCommands();
            } catch(Exception e) {
                helper.printFileError("Could not load custom commands", e);
            }

            // Migrates the custom editor and custom viewer if necessary.
            migrateCommand("viewer.use_custom", "viewer.custom_command", CommandManager.VIEWER_ALIAS);
            migrateCommand("editor.use_custom", "editor.custom_command", CommandManager.EDITOR_ALIAS);
            try {
                CommandManager.writeCommands();
            } catch(Exception e) {
                System.out.println("###############################");
                getLogger().debug("Caught exception", e);
                // There's really nothing we can do about this...
            }

            try {
                CommandManager.loadAssociations();
            } catch(Exception e) {
                helper.printFileError("Could not load custom associations", e);
            }

            ActionManager.registerCommandsActions();
        }
    }

    private static class LoadBookmarksTask extends LauncherTask {
        LoadBookmarksTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("load_bookmarks", helper, depends);
        }

        @Override
        void run() throws Exception {
            printStartupMessage("Loading bookmarks...");
            try {
                com.mucommander.bookmark.BookmarkManager.loadBookmarks();
            } catch(Exception e) {
                helper.printFileError("Could not load bookmarks", e);
            }
        }
    }

    private static class LoadCredentialsTask extends LauncherTask {
        private LoadCredentialsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("load_credentials", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Loads credentials
            printStartupMessage("Loading credentials...");
            try {
                CredentialsManager.loadCredentials();
            } catch(Exception e) {
                helper.printFileError("Could not load credentials", e);
            }
        }
    }

    private static class InitCustomDateFormatTask extends LauncherTask {
        InitCustomDateFormatTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("init_custom_dateformat", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Inits CustomDateFormat to make sure that its ConfigurationListener is added
            // before FileTable, so CustomDateFormat gets notified of date format changes first
            com.mucommander.utils.text.CustomDateFormat.init();
        }
    }

    private static class ShowSetupWindowTask extends LauncherTask {
        ShowSetupWindowTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("show_setup", helper, depends);
        }

        @Override
        void run() throws Exception {
            boolean showSetup = MuConfigurations.getPreferences().getVariable(MuPreference.THEME_TYPE) == null;
            if (showSetup) {
                new InitialSetupDialog(WindowManager.getCurrentMainFrame()).showDialog();
            }
        }
    }

    private static class LoadShellHistoryTask extends LauncherTask {
        LoadShellHistoryTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("load_shell", helper, depends);
        }

        @Override
        void run() throws Exception {
            // Loads shell history
            printStartupMessage("Loading shell history...");
            try {
                ShellHistoryManager.loadHistory();
            } catch(Exception e) {
                helper.printFileError("Could not load shell history", e);
            }
        }
    }


    private static class RegisterNetworkProtocolsTask extends LauncherTask {
        RegisterNetworkProtocolsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("protocols_network", helper, depends);
        }

        @Override
        void run() throws Exception {
            FileFactory.registerProtocolNetworks();
        }
    }


    private static class RegisterArchiveProtocolsTask extends LauncherTask {
        RegisterArchiveProtocolsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("protocols_archive", helper, depends);
        }

        @Override
        void run() throws Exception {
            FileFactory.registerProtocolArchives();
        }
    }


    private static class RegisterOtherProtocolsTask extends LauncherTask {
        RegisterOtherProtocolsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("protocols_other", helper, depends);
        }

        @Override
        void run() throws Exception {
            FileFactory.registerProtocolOthers();
        }
    }

    private static class LoadEnvironmentTask extends LauncherTask {

        LoadEnvironmentTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("load_env", helper, depends);
        }

        @Override
        void run() throws Exception {
            ToolsEnvironment.load();
        }
    }

    private static class PrepareGraphicsTask extends LauncherTask {

        PrepareGraphicsTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("prepare_graphics", helper, depends);
        }

        @Override
        void run() throws Exception {
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        }
    }


    private static class PrepareKeystrokeClassTask extends LauncherTask {

        PrepareKeystrokeClassTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("prepare_keystroke", helper, depends);
        }

        @Override
        void run() throws Exception {
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        }
    }


    private static class PrepareLoggerTask extends LauncherTask {

        PrepareLoggerTask(LauncherCmdHelper helper, LauncherTask... depends) {
            super("prepare_logger", helper, depends);
        }

        @Override
        void run() throws Exception {
            getLogger().info("Current OS family: {}", OsFamily.getCurrent());
        }
    }


    private static class LauncherExecutor extends ThreadPoolExecutor {
        private final Set<LauncherTask> runningTasks = new HashSet<>();
        private final int cores;

        LauncherExecutor(int cores) {
            super(cores, cores, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            this.cores = cores;
        }

        public boolean isFull() {
            if (runningTasks.size() < cores) {
                return  false;
            }
            for (LauncherTask t : runningTasks) {
                if (t.isDone()) {
                    runningTasks.remove(t);
                    break;
                }
            }
            return runningTasks.size() >= cores;
        }

        public int getRunningTasksCount() {
            return runningTasks.size();
        }

        public boolean execute(LauncherTask task, boolean force) {
            if (force || (runningTasks.size() < cores && task.isReadyForExecution())) {
                super.execute(task.getTask());
// System.out.println("Execute " + task + " " + task.depends.length + "  " + task.isReadyForExecution() + "      " + runningTasks);
                runningTasks.add(task);
                return true;
            }
            return false;
        }
    }
    /**
     * Main method used to startup muCommander.
     * @param args command line arguments.
     */
    public static void main(String args[]) {
        if (OsFamily.MAC_OS_X.isCurrent()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "trolCommander");
			// disable openGL in javaFX (used for HtmlViewer) as it cashes JVM under vmWare
			System.setProperty("prism.order", "sw");
        }

        Profiler.start("init");
        Profiler.start("loading");

        getLogger().info("Current OS family: {}", OsFamily.getCurrent());

        String lang = System.getProperty("user.language");
        String country = System.getProperty("user.country");
        if ("tr".equalsIgnoreCase(lang) || "tr".equalsIgnoreCase(country)) {
            throw new RuntimeException("Unsupported");
        }

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Processors: " + processors);
        //ExecutorService executor = Executors.newFixedThreadPool(processors < 2 ? 2 : processors);

        LauncherExecutor executor = new LauncherExecutor(processors <= 0 ? 1 : processors);
        try {
            // Initialises fields.
            LauncherCmdHelper helper = new LauncherCmdHelper(args, true, false);
            // Whether or not to ignore warnings when booting.
            helper.parseArgs();

            LauncherTask taskPrepareGraphics = new PrepareGraphicsTask(helper);
            LauncherTask taskPrepareKeystrokeClass = new PrepareKeystrokeClassTask(helper);
            LauncherTask taskPrepareLogger = new PrepareLoggerTask(helper);
            LauncherTask taskLoadConfigs = new LoadConfigsTask(helper);
            LauncherTask taskRegisterArchives = new RegisterArchiveProtocolsTask(helper);
            LauncherTask taskStart = new StartTask(helper, taskLoadConfigs, taskRegisterArchives);
            LauncherTask taskShowSplash = new ShowSplashTask(helper, taskLoadConfigs);
            LauncherTask taskLoadTheme = new LoadThemesTask(helper, taskShowSplash, taskPrepareGraphics);
            LauncherTask taskInitDesktop = new InitDesktopTask(helper, taskLoadConfigs);
            LauncherTask taskLoadDict = new LoadDictTask(helper, taskLoadConfigs);
            LauncherTask taskConfigureFs = new ConfigureFsTask(helper);
            LauncherTask taskLoadCustomCommands = new LoadCustomCommands(helper);
            LauncherTask taskLoadBookmarks = new LoadBookmarksTask(helper);
            LauncherTask taskLoadCredentials = new LoadCredentialsTask(helper);
            LauncherTask taskInitCustomDataFormat = new InitCustomDateFormatTask(helper, taskLoadConfigs);
            LauncherTask taskRegisterActions = new LoadActionsTask(helper, taskPrepareKeystrokeClass);
            LauncherTask taskLoadIcons = new LoadIconsTask(helper);
            LauncherTask taskInitBars = new InitBarsTask(helper, taskRegisterActions);
            LauncherTask taskStartBonjour = new StartBonjourTask(helper);
            LauncherTask enableNotificationsTask = new EnableNotificationsTask(helper, taskRegisterActions);
            LauncherTask taskCreateWindow = new CreateWindowTask(helper, taskStart, taskLoadTheme, taskShowSplash, taskInitBars, taskRegisterActions, taskLoadCustomCommands);
            LauncherTask taskShowSetupWindow = new ShowSetupWindowTask(helper, taskLoadConfigs);
            LauncherTask taskLoadShellHistory = new LoadShellHistoryTask(helper);
            LauncherTask taskDisposeSplash = new DisposeSplashTask(helper, taskShowSplash, taskCreateWindow);
            LauncherTask taskRegisterNetwork = new RegisterNetworkProtocolsTask(helper);
            LauncherTask taskRegisterOtherProtocols = new RegisterOtherProtocolsTask(helper);
            LauncherTask taskLoadEnvironment = new LoadEnvironmentTask(helper);

            List<LauncherTask> tasks = new LinkedList<>();
            tasks.add(taskPrepareLogger);
            tasks.add(taskPrepareGraphics);
            tasks.add(taskPrepareKeystrokeClass);
            tasks.add(taskRegisterActions);
            tasks.add(taskLoadConfigs);
            tasks.add(taskRegisterArchives);
            tasks.add(taskStart);
            tasks.add(taskLoadIcons);
            tasks.add(taskShowSplash);
            tasks.add(taskConfigureFs);
            tasks.add(taskLoadTheme);
            tasks.add(taskLoadDict);
            tasks.add(taskLoadCustomCommands);
            tasks.add(taskLoadBookmarks);
            tasks.add(taskLoadCredentials);
            tasks.add(taskLoadShellHistory);
            tasks.add(taskInitCustomDataFormat);
            tasks.add(taskStartBonjour);
            tasks.add(taskInitBars);
            tasks.add(taskCreateWindow);
            tasks.add(enableNotificationsTask);
            tasks.add(taskInitDesktop);
            tasks.add(taskDisposeSplash);
            tasks.add(taskShowSetupWindow);
            tasks.add(taskRegisterNetwork);
            tasks.add(taskRegisterOtherProtocols);
            tasks.add(taskLoadEnvironment);

            if (processors <= 1 ) {
                for (LauncherTask t : tasks) {
                    t.run();
                }
            } else {
                while (!tasks.isEmpty()) {
                    // execute tasks with ready dependencies
                    for (LauncherTask task : tasks) {
                        if (executor.execute(task, false) ) {
                            tasks.remove(task);
                            break;
                        }
                    }
                    // TODO
                    if (executor.isFull()) {
                        try {
                            Thread.sleep(5);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        boolean found = false;
                        for (LauncherTask task : tasks) {
                            if (executor.execute(task, false) ) {
                                tasks.remove(task);
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            continue;
                        }

                        LauncherTask t = tasks.get(0);
                        executor.execute(t, true);
                        tasks.remove(t);
                        try {
                            Thread.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            executor.shutdown();
            System.out.println("finished");
        } catch(Throwable t) {
            // Startup failed, dispose the splash screen
            if (splashScreen != null) {
                splashScreen.dispose();
            }

            getLogger().error("Startup failed", t);
            
            // Display an error dialog with a proper message and error details
            InformationDialog.showErrorDialog(null, null, Translator.get("startup_error"), null, t);

            // Quit the application
            WindowManager.quit();
        }

        executor.shutdown();
/*
        try {
            executor.awaitTermination(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
        // Done launching, wake up threads waiting for the application being launched.
        // Important: this must be done before disposing the splash screen, as this would otherwise create a deadlock
        // if the AWT event thread were waiting in #waitUntilLaunched .
        synchronized(LAUNCH_LOCK) {
            isLaunching = false;
            LAUNCH_LOCK.notifyAll();
        }

        // Check for newer version unless it was disabled
        if (MuConfigurations.getPreferences().getVariable(MuPreference.CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    VersionChecker versionChecker = VersionChecker.getInstance();
                    if (versionChecker.isNewVersionAvailable()) {
                        new CheckVersionDialog(WindowManager.getCurrentMainFrame(), versionChecker, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        Profiler.stop("init");

        //Profiler.print();
        //Profiler.hide("launcher.");
        //Profiler.printThreads();
        //Profiler.initThreads();
    }

    private static Logger getLogger() {
        if (logger == null) {
            Profiler.start("create-logger");
            logger = LoggerFactory.getLogger(TrolCommander.class);
            Profiler.stop("create-logger");
        }
        return logger;
    }


}
