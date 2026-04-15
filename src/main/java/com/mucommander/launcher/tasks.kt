package com.mucommander.launcher

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import com.mucommander.RuntimeConstants
import com.mucommander.TrolCommander
import com.mucommander.auth.CredentialsManager
import com.mucommander.bonjour.BonjourDirectory
import com.mucommander.bookmark.BookmarkManager
import com.mucommander.bookmark.file.BookmarkProtocolProvider
import com.mucommander.command.CommandManager
import com.mucommander.commons.file.FileFactory
import com.mucommander.commons.file.icon.impl.SwingFileIconProvider
import com.mucommander.commons.file.impl.ftp.FTPProtocolProvider
import com.mucommander.commons.file.impl.smb.SMBProtocolProvider
import com.mucommander.commons.runtime.OsFamily
import com.mucommander.commons.runtime.OsVersion
import com.mucommander.conf.TcConfigurations
import com.mucommander.conf.TcPreference
import com.mucommander.conf.TcPreferences
import com.mucommander.desktop.DesktopManager
import com.mucommander.extension.ExtensionManager
import com.mucommander.ui.PreloadedJFrame
import com.mucommander.profiler.Profiler
import com.mucommander.shell.ShellHistoryManager
import com.mucommander.ui.action.ActionKeymapIO
import com.mucommander.ui.action.ActionManager
import com.mucommander.ui.dialog.about.AboutDialog
import com.mucommander.ui.dialog.pref.general.GeneralPreferencesDialog
import com.mucommander.ui.dialog.startup.InitialSetupDialog
import com.mucommander.ui.icon.FileIcons
import com.mucommander.ui.main.SplashScreen
import com.mucommander.ui.main.WindowManager
import com.mucommander.ui.main.commandbar.CommandBarIO
import com.mucommander.ui.main.frame.CommandLineMainFrameBuilder
import com.mucommander.ui.main.frame.DefaultMainFramesBuilder
import com.mucommander.ui.main.toolbar.ToolBarIO
import com.mucommander.ui.notifier.AbstractNotifier
import com.mucommander.ui.theme.ThemeManager
import com.mucommander.ui.tools.ToolsEnvironment
import com.mucommander.utils.MuLogging
import com.mucommander.utils.text.CustomDateFormat
import com.mucommander.utils.text.Translator
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.awt.desktop.AboutEvent
import java.awt.event.KeyEvent
import java.lang.reflect.Constructor
import java.util.*
import javax.swing.KeyStroke
import javax.swing.UIManager
import kotlin.math.max
import kotlin.system.exitProcess


private var splashScreen: SplashScreen? = null

fun prepareLauncherTasks(helper: LauncherCmdHelper): List<LauncherTask> {
    val prepareLoggerTask = LauncherTask("prepare_logger") {
        LoggerFactory.getLogger(TrolCommander::class.java)
    }
    val prepareGraphicsTask = LauncherTask("prepare_graphics") {
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
    }
    val loadPreferencesTask = LauncherTask("load_preferences") {
        TcConfigurations.getPreferences()
    }
    val installFlatLightLafTask = LauncherTask("install_flat_light_laf") {
        FlatLightLaf.installLafInfo()
    }
    val installFlatDarculaLafTask = LauncherTask("install_flat_dracula_laf") {
        FlatDarculaLaf.installLafInfo()
    }
    val installFlatDarkLafTask = LauncherTask("install_flat_dark_laf") {
        FlatDarkLaf.installLafInfo()
    }
    val installFlatIntelliJLafTask = LauncherTask("install_flat_intellij_laf") {
        FlatIntelliJLaf.installLafInfo()
    }
    val installFlatMacLightLafTask = LauncherTask("install_flat_maclight_laf") {
        FlatMacLightLaf.installLafInfo()
    }
    val installFlatMacDarkLafTask = LauncherTask("install_flat_macdark_laf") {
        FlatMacDarkLaf.installLafInfo()
    }
    val installVaquaLafTask = LauncherTask("install_aqua_lf") {
        if (OsFamily.getCurrent() == OsFamily.MAC_OS_X && OsVersion.MAC_OS_X_10_13.isCurrentLower()) {
            val aquaLaf = org.violetlib.aqua.AquaLookAndFeel()
            UIManager.installLookAndFeel(UIManager.LookAndFeelInfo(aquaLaf.getName(), aquaLaf.javaClass.getName()))
        }
    }
    val prepareKeystrokeClassTask = LauncherTask("prepare_keystroke") {
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK)
    }
    val loadConfigsTask = LauncherTask("config") {
        loadConfigs(helper)
    }
    val loadEnvTask = LauncherTask("load_env") {
        ToolsEnvironment.load()
    }
    val loadBookmarksTask = LauncherTask("load_bookmarks") {
        BookmarkManager.loadBookmarks()
    }
    val loadIconsTask = LauncherTask("load_file_icons") {
        // Initialize the SwingFileIconProvider from the main thread, see method Javadoc for an explanation on why we do this now
        SwingFileIconProvider.forceInit()
    }
    val registerArchiveProtocolsTask = LauncherTask("register_archive_protocols") {
        FileFactory.registerProtocolArchives()
    }
    val registerNetworkProtocolsTask = LauncherTask("register_network_protocols") {
        FileFactory.registerProtocolNetworks()
    }
    val registerOtherProtocolsTask = LauncherTask("register_other_protocols") {
        FileFactory.registerProtocolOthers()
    }
    val loadCredentialsTask = LauncherTask("load_credentials") {
        CredentialsManager.loadCredentials()
    }
    val loadCustomCommandsTask = LauncherTask("load_custom_commands") {
        loadCustomCommands(helper)
    }
    val loadShellHistoryTask = LauncherTask("load_shell_history") {
        ShellHistoryManager.loadHistory()
    }
    val configureFileSystemTask = LauncherTask("configure_files") {
        configureFileSystems()
    }
    val initMacOsSupportTask = LauncherTask("init_macos_support") {
        initMacOsSupport(helper)
    }
    val startBonjourTask = LauncherTask("start_bonjour") {
        BonjourDirectory.setActive(isBonjourEnabled())
    }
    val preloadedFramesTask = LauncherTask("preloaded_frames") {
        PreloadedJFrame.init();
    }


    val startTask = LauncherTask("start") {
        start(helper)
    }.depends(initMacOsSupportTask)
    val initCustomDateFormatTask = LauncherTask("init_custom_date_format") {
        CustomDateFormat.init()
    }.depends(loadConfigsTask)
    val loadDictionaryTask = LauncherTask("load_dictionary") {
        Translator.init()
    }.depends(loadConfigsTask)
    val showSplashTask = LauncherTask("show_splash") {
        if (isShowSplash()) {
            splashScreen = SplashScreen(RuntimeConstants.VERSION, "Loading preferences...")
        }
    }.depends(loadPreferencesTask, loadConfigsTask)
    val loadThemeTask = LauncherTask("load_theme") {
        ThemeManager.loadCurrentTheme()
    }.depends(showSplashTask, prepareGraphicsTask)
    val registerActionsTask = LauncherTask("register_actions") {
        ActionManager.registerActions()
    }.depends(prepareKeystrokeClassTask, loadDictionaryTask)
    val initDesktopTask = LauncherTask("init_desktop") {
        initDesktop()
    }.depends(loadConfigsTask)
    val initBarsTask = LauncherTask("init_bars") {
        initBarFiles()
    }.depends(registerActionsTask)
    val enableNotificationsTask = LauncherTask("enable_notifications") {
        enableNotifications()
    }.depends(registerActionsTask)
    val createMainWindowTask = LauncherTask("create_main_window") {
        createMainWindow(helper)
    }.depends(loadThemeTask, showSplashTask, initDesktopTask, registerActionsTask, loadCustomCommandsTask)
    val disposeSplashTask = LauncherTask("dispose_splash") {
        splashScreen?.dispose()
        splashScreen = null
    }.depends(showSplashTask, createMainWindowTask)
    val showSetupWindowTask = LauncherTask("show_setup_window") {
        val showSetup = TcConfigurations.getPreferences().getVariable(TcPreference.THEME_TYPE) == null
        if (showSetup) {
            InitialSetupDialog(WindowManager.getCurrentMainFrame().jFrame).showDialog()
        }
    }.depends(loadConfigsTask)

    return LinkedList(
        listOf(
            prepareLoggerTask,
            prepareGraphicsTask,
            loadPreferencesTask,
            installFlatLightLafTask,
            preloadedFramesTask,

            installFlatDarculaLafTask,
            installFlatDarkLafTask,
            installFlatIntelliJLafTask,
            installFlatMacLightLafTask,
            installFlatMacDarkLafTask,
            installVaquaLafTask,
            prepareKeystrokeClassTask,
            initMacOsSupportTask,
        registerActionsTask,
            loadConfigsTask,
            startTask,
            loadIconsTask,
            showSplashTask,
            configureFileSystemTask,
            loadThemeTask,
        loadDictionaryTask,
            loadCustomCommandsTask,
            loadBookmarksTask,
            loadCredentialsTask,
            loadShellHistoryTask,
            initCustomDateFormatTask,
            startBonjourTask,
            initBarsTask,
        createMainWindowTask,
            enableNotificationsTask,
            initDesktopTask,


            registerArchiveProtocolsTask,
            registerNetworkProtocolsTask,
            registerOtherProtocolsTask,
            disposeSplashTask,

            showSetupWindowTask,
            loadEnvTask,
        ))

//        tasks.add(taskDisposeSplash);
//        tasks.add(taskShowSetupWindow);
//        tasks.add(taskLoadEnvironment);

}

private fun initDesktop() {
    try {
        val install = !TcConfigurations.isPreferencesFileExists()
        DesktopManager.init(install)
    } catch (e: Exception) {
        System.err.println("Could not initialize desktop")
        e.printStackTrace()
        exitProcess(1)
    }
}

fun start(helper: LauncherCmdHelper) {
    // Checks whether a graphics environment is available and exit with an error otherwise.
    if (GraphicsEnvironment.isHeadless()) {
        System.err.println("Error: no graphical environment detected.")
        exitProcess(1)
    }
    try {
        MuLogging.configureLogging()
    } catch (e: Exception) {
        helper.printFileError("Configure logging error", e)
    }

    // Adds all extensions to the classpath.
    try {
        Profiler.start("init-extensions-manager")
        ExtensionManager.init()
        Profiler.stop("init-extensions-manager")
        ExtensionManager.addExtensionsToClasspath()
    } catch (e: Exception) {
        helper.printFileError("Failed to add extensions to the classpath", e)
    }

    // This the property is supposed to have the java.net package use the proxy defined in the system settings
    // to establish HTTP connections. This property is supported only under Java 1.5 and up.
    // Note that Mac OS X already uses the system HTTP proxy, with or without this property being set.
    System.setProperty("java.net.useSystemProxies", "true")

    //boolean showSetup = MuConfigurations.getPreferences().getVariable(MuPreference.THEME_TYPE) == null;
    // Traps VM shutdown
    Runtime.getRuntime().addShutdownHook(ShutdownHook())
}


private fun createMainWindow(helper: LauncherCmdHelper) {
    println("Initializing window...")
    Profiler.start("launcher.create-window")
    WindowManager.createNewMainFrame(CommandLineMainFrameBuilder(helper.getFolders()))

    // If no initial path was specified, start a default main window.
    if (WindowManager.getCurrentMainFrame() == null) {
        val mainFrameBuilder = DefaultMainFramesBuilder()
        WindowManager.createNewMainFrame(mainFrameBuilder)
    }

    Profiler.stop("launcher.create-window")
    Profiler.stop("loading")
    Profiler.print()
    Profiler.hide("launcher.")
}

fun initMacOsSupport(helper: LauncherCmdHelper) {
    // If trolCommander is running under Mac OS X (how lucky!), add some glue for the main menu bar and other OS X specifics.
    if (OsFamily.MAC_OS_X.isCurrent) {
        // Use reflection to create an OSXIntegration instance so that ClassLoader
        // doesn't throw an NoClassDefFoundException under platforms other than Mac OS X
        try {
            val osxIntegrationClass = Class.forName("com.mucommander.ui.macosx.OSXIntegration")
            val constructor: Constructor<*> = osxIntegrationClass.getConstructor()
            constructor.newInstance()
        } catch (e: java.lang.Exception) {
            helper.printFileError("Exception thrown while initializing Mac OS X integration", e)
        }
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler { _: AboutEvent? -> AboutDialog((WindowManager.getCurrentMainFrame())).showDialog() }
        }
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setAboutHandler {
                _: AboutEvent? -> GeneralPreferencesDialog.getDialog().showDialog()
            }
        }
    }
}


private fun loadConfigs(helper: LauncherCmdHelper) {
    // Attempts to guess whether this is the first time trolCommander is booted or not.
    //boolean isFirstBoot;
    //try {isFirstBoot = !MuConfigurations.isPreferencesFileExists();}
    //catch(IOException e) {isFirstBoot = true;}

    // Load snapshot data before loading configuration as until version 0.9 the snapshot properties
    // were stored as preferences so when loading such preferences they could overload snapshot properties
    try {
        TcConfigurations.loadSnapshot()
    } catch (e: Exception) {
        helper.printFileError("Could not load snapshot", e)
    }

    // Configuration needs to be loaded before any sort of GUI creation is performed
    // under Mac OS X, if we're to use the metal look, we need to know about it right about now.
    try {
        TcConfigurations.loadPreferences()
    } catch (e: Exception) {
        helper.printFileError("Could not load configuration", e)
    }

    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    FileIcons.setScaleFactor(
        max(1.0f, TcConfigurations.getPreferences().getVariable(TcPreference.TABLE_ICON_SCALE, TcPreferences.DEFAULT_TABLE_ICON_SCALE))
    )
    FileIcons.setSystemIconsPolicy(
        TcConfigurations.getPreferences().getVariable(TcPreference.USE_SYSTEM_FILE_ICONS, TcPreferences.DEFAULT_USE_SYSTEM_FILE_ICONS)
    )
}

private fun loadCustomCommands(helper: LauncherCmdHelper) {
    try {
        CommandManager.loadCommands()
    } catch (e: java.lang.Exception) {
        helper.printFileError("Could not load custom commands", e)
    }
    // Migrates the custom editor and custom viewer if necessary.
    TrolCommander.migrateCommand("viewer.use_custom", "viewer.custom_command", CommandManager.VIEWER_ALIAS)
    TrolCommander.migrateCommand("editor.use_custom", "editor.custom_command", CommandManager.EDITOR_ALIAS)
    try {
        CommandManager.writeCommands()
    } catch (e: java.lang.Exception) {
        helper.printFileError("Caught exception", e)
        // There's really nothing we can do about this...
    }

    try {
        CommandManager.loadAssociations()
    } catch (e: java.lang.Exception) {
        helper.printFileError("Could not load custom associations", e)
    }

    ActionManager.registerCommandsActions()
}


private fun configureFileSystems() {
    // Configure the SMB subsystem (backed by jCIFS) to maintain compatibility with SMB servers that don't support
    // NTLM v2 authentication such as Samba 3.0.x, which still is widely used and comes pre-installed on
    // Mac OS X Leopard.
    // Since jCIFS 1.3.0, the default is to use NTLM v2 authentication and extended security.
    SMBProtocolProvider.setSmbLmCompatibility(isSmbLmCompatibilityEnabled())
    SMBProtocolProvider.setExtendedSecurity(isSmbExtendedSecurityEnabled())


    // Use the FTP configuration option that controls whether to force the display of hidden files, or leave it for
    // the servers to decide whether to show them.
    FTPProtocolProvider.setForceHiddenFilesListing(isListHiddenFiles())


    //            FileFactory.registerProtocolFile();
    // Use CredentialsManager for file URL authentication
    FileFactory.setDefaultAuthenticator(CredentialsManager.getAuthenticator())


    // Register the application-specific 'bookmark' protocol.
    FileFactory.registerProtocol(BookmarkProtocolProvider.BOOKMARK, BookmarkProtocolProvider())
}

private fun initBarFiles() {
    println("Loading actions shortcuts...")
    try {
        ActionKeymapIO.loadActionKeymap()
    } catch (e: Exception) {
        printError("Could not load actions shortcuts", e)
    }
    println("Loading toolbar description...")
    try {
        ToolBarIO.loadDescriptionFile()
    } catch (e: Exception) {
        printError("Could not load toolbar description", e)
    }
    println("Loading command bar description...")
    try {
        CommandBarIO.loadCommandBar()
    } catch (e: Exception) {
        printError("Could not load commandbar description", e)
    }
}

private fun enableNotifications() {
    // Enable system notifications, only after MainFrame is created as SystemTrayNotifier needs to retrieve a MainFrame instance
    if (isNotificationsEnabled()) {
        println("Enabling system notifications...")
        if (AbstractNotifier.isAvailable()) {
            AbstractNotifier.getNotifier().setEnabled(true)
        }
    }
}

private fun printError(msg: String, e: Throwable) {
    println(msg)
    e.printStackTrace()
}

private fun isListHiddenFiles() =
    TcConfigurations.getPreferences().getVariable(TcPreference.LIST_HIDDEN_FILES, TcPreferences.DEFAULT_LIST_HIDDEN_FILES)


private fun isSmbExtendedSecurityEnabled() =
    TcConfigurations.getPreferences().getVariable(TcPreference.SMB_USE_EXTENDED_SECURITY, TcPreferences.DEFAULT_SMB_USE_EXTENDED_SECURITY)

private fun isSmbLmCompatibilityEnabled() =
    TcConfigurations.getPreferences().getVariable(TcPreference.SMB_LM_COMPATIBILITY, TcPreferences.DEFAULT_SMB_LM_COMPATIBILITY)

private fun isBonjourEnabled() =
    TcConfigurations.getPreferences().getVariable(TcPreference.ENABLE_BONJOUR_DISCOVERY, TcPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY)

private fun isShowSplash() =
    TcConfigurations.getPreferences().getVariable(TcPreference.SHOW_SPLASH_SCREEN, TcPreferences.DEFAULT_SHOW_SPLASH_SCREEN)

private fun isNotificationsEnabled() =
    TcConfigurations.getPreferences().getVariable(TcPreference.ENABLE_SYSTEM_NOTIFICATIONS, TcPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS)
