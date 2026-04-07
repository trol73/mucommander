/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2020 Oleg Trifonov
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

package com.mucommander;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.launcher.LauncherCmdHelper;
import com.mucommander.launcher.LauncherExecutor;
import com.mucommander.launcher.LauncherTask;
import com.mucommander.profiler.Profiler;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.startup.CheckVersionDialog;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.updates.VersionChecker;
import com.mucommander.utils.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.util.*;
import java.util.List;

import static com.mucommander.launcher.TasksKt.prepareLauncherTasks;

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

    /** true while the application is launching, false after it has finished launching */
    private static boolean isLaunching = true;
    /** Launch lock. */
    private static final Object LAUNCH_LOCK = new Object();


    /**
     * Prevents initialization of the <code>Launcher</code>.
     */
    private TrolCommander() {}


    /**
     * This method can be called to wait until the application has been launched. The caller thread will be blocked
     * until the application has been launched.
     * This method will return immediately if the application has already been launched when it is called.
     */
    public static void waitUntilLaunched() {
        getLogger().debug("called, thread {}", Thread.currentThread());
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

    // - Boot code --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Method used to migrate commands that used to be defined in the configuration but were moved to <code>commands.xml</code>.
     * @param useName     name of the <code>use custom command</code> configuration variable.
     * @param commandName name of the <code>custom command</code> configuration variable.
     */
    public static void migrateCommand(String useName, String commandName, String alias) {
        String command;

        if (TcConfigurations.getPreferences().getBooleanVariable(useName) && (command = TcConfigurations.getPreferences().getVariable(commandName)) != null) {
            CommandManager.registerCommand(new Command(alias, command, CommandType.SYSTEM_COMMAND));
            TcConfigurations.getPreferences().removeVariable(useName);
            TcConfigurations.getPreferences().removeVariable(commandName);
        }
    }


    /**
     * Main method used to startup muCommander.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        if (OsFamily.MAC_OS_X.isCurrent()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "trolCommander");
			// disable openGL in javaFX (used for HtmlViewer) as it cashes JVM under vmWare
			System.setProperty("prism.order", "sw");
        }

        Profiler.start("init");
        Profiler.start("loading");

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Current OS family: " + OsFamily.getCurrent());
        System.out.println("Processors: " + processors);

        try (LauncherExecutor executor = new LauncherExecutor(processors <= 0 ? 1 : processors)) {
            LauncherCmdHelper helper = new LauncherCmdHelper(args, true, false);
            // Whether, or not to ignore warnings when booting.
            helper.parseArgs();

            List<LauncherTask> tasks = prepareLauncherTasks(helper);
            if (processors <= 1) {
                for (LauncherTask t : tasks) {
                    t.call();
                }
            } else {
                while (!executor.isFull()) {
                    executor.executeFirst(tasks);
                }
                while (!tasks.isEmpty()) {
                    executor.executeFirst(tasks);
                    if (executor.isFull()) {
                        try {
                            Thread.sleep(1);
                        } catch (Exception ignore) {}
                    } else {
                        if (executor.executeFirst(tasks)) {
                            continue;
                        }
                        LauncherTask t = tasks.getFirst();
                        executor.execute(t, true);
                        tasks.remove(t);
                    }
                }
            }
//            executor.shutdown();
            System.out.println("finished");
        } catch(Throwable t) {
            // Startup failed, dispose the splash screen
//            if (splashScreen != null) {
//                splashScreen.dispose();
//            }

            getLogger().error("Startup failed", t);
            
            // Display an error dialog with a proper message and error details
            InformationDialog.showErrorDialog(null, null, Translator.get("startup_error"), null, t);

            // Quit the application
            WindowManager.quit();
        }


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
        if (TcConfigurations.getPreferences().getVariable(TcPreference.CHECK_FOR_UPDATE, TcPreferences.DEFAULT_CHECK_FOR_UPDATE)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    VersionChecker versionChecker = VersionChecker.getInstance();
                    if (versionChecker != null && versionChecker.isNewVersionAvailable()) {
                        new CheckVersionDialog(WindowManager.getCurrentMainFrame(), versionChecker, false);
                    }
                } catch (Exception e) {
                    getLogger().error("Check version error", e);
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
            logger = LoggerFactory.getLogger(TrolCommander.class);
        }
        return logger;
    }
}
