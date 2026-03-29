/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2021 Oleg Trifonov
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
package com.mucommander.updates;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.process.ExecutionFinishListener;
import com.mucommander.process.ExecutorUtils;
import ru.trolsoft.utils.FileUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SelfUpdateUtils {

    private static final String RESTARTER_LOCAL_PATH = "restarter";

    public static boolean isUpdaterAvailable() {
        return getRestarterJarPath() != null;
    }

    private static boolean prepareUpdater() {
        return extractRestarter() && checkRestarter();
    }

    private static String execRestarter(boolean waitComplete, String... args) {
System.out.println("restarter exec '" + (args != null && args.length > 0 ? args[0] : "") + "'");
        String jarPath = FileUtils.getJarPath();
        AbstractFile root = FileFactory.getFile(jarPath);
        AtomicBoolean done = new AtomicBoolean(false);
        StringBuffer outStr = new StringBuffer();
        ExecutionFinishListener finishListener = (exitCode, output) -> {
            done.set(true);
            outStr.append(output);
        };
        try {
            if (args == null || args.length == 0) {
                ExecutorUtils.executeAndGetOutput(jarPath + "/" + RESTARTER_LOCAL_PATH, root, finishListener);
            } else {
                String[] cmd = new String[args.length+1];
                cmd[0] = jarPath + "/" + RESTARTER_LOCAL_PATH;
                System.arraycopy(args, 0, cmd, 1, args.length);
                ExecutorUtils.executeAndGetOutput(cmd, root, finishListener);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        if (!waitComplete) {
            return null;
        }
        for (int i = 0; i < 20; i++) {
            if (done.get()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {}
        }
System.out.println("RESTARTER RESULT '" + outStr + "'");
        return outStr.toString();
    }


    public static boolean checkRestarter() {
        String out = execRestarter(true);
        return out != null && out.startsWith("restarter");
    }

    private static int getPid() {
        String out = execRestarter(true, "ppid");
        if (out == null) {
            return -1;
        }
        try {
            return Integer.parseInt(out.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getTcExecutionCommand() {
        if (!OsFamily.getCurrent().isUnixBased()) {
            return null;
        }
        int pid = getPid();
        String out = execRestarter(true, "ps -p " + pid + " -o args");
        if (out == null) {
            return out;
        }
        if (out.toLowerCase().startsWith("args")) {
            return out.substring(4).trim();
        }
        return out;
    }

    public static void updateAndRestart() {
        String cmd = getTcExecutionCommand();
        new Thread(() -> execRestarter(false, "update", cmd)).start();
    }


    private static String getRestarterJarPath() {
        switch (OsFamily.getCurrent()) {
            case MAC_OS_X:
                return "/bin/macos/restarter";
            case LINUX:
                return "/bin/linux/restarter";
        }
        return null;
    }


    public static boolean extractRestarter() {
        String jarPath = FileUtils.getJarPath();
        String outPath = jarPath + "/" + RESTARTER_LOCAL_PATH;
        try {
            FileUtils.copyFileFromJar(getRestarterJarPath(), outPath, true);
            AbstractFile file = FileFactory.getFile(outPath);
            if (file == null) {
                return false;
            }
            file.changePermissions(FilePermissions.DEFAULT_EXECUTABLE_PERMISSIONS);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
