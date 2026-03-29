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

package com.mucommander.job;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.updates.SelfUpdateUtils;
import com.mucommander.utils.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This job self-updates the trolCommander with a new JAR file that is fetched from a specified remote file.
 */
public class SelfUpdateJob extends CopyJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(SelfUpdateJob.class);

	private static final String TEMP_JAR_FILENAME = "trolcommander-new.jar";



    public SelfUpdateJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile remoteJarFile) {
        super(progressDialog, mainFrame, new FileSet(remoteJarFile.getParent(), remoteJarFile),
                getTempJarFolder(), TEMP_JAR_FILENAME, CopyJob.Mode.DOWNLOAD, FileCollisionDialog.OVERWRITE_ACTION);
    }


    private static AbstractFile getApplicationJarFile() {
        return ResourceLoader.getRootPackageAsFile(SelfUpdateJob.class);
    }

    private static AbstractFile getTempJarFolder() {
        return getApplicationJarFile().getParent();
    }



//    @Override
//    protected void jobStarted() {
//        super.jobStarted();
//        try {
////            // Loads all classes from the JAR file before the new JAR file is installed.
////            // This will ensure that the shutdown sequence, which invokes so not-yet-loaded classes goes down smoothly.
////            loadClassRecurse(destJar);
////            loadingClasses = false;
//        } catch(Exception e) {
//            LOGGER.debug("Caught exception", e);
//            // TODO: display an error message
//            interrupt();
//        }
//    }

    @Override
    protected void jobCompleted() {
        System.out.println("JOB COMPLETED");
        System.out.println(SelfUpdateUtils.extractRestarter());
        System.out.println(SelfUpdateUtils.checkRestarter());
        System.out.println("SelfUpdateUtils.updateAndRestart()");
        SelfUpdateUtils.updateAndRestart();
        System.out.println("WindowManager.quit()");
        WindowManager.quit();
        System.out.println("System.exit(0)");
        System.exit(0);
        //System.out.println(SelfUpdateUtils.getTcExecutionCommand());

//        try {
//            // Mac OS X
//            if (OsFamily.MAC_OS_X.isCurrent() && executeOnMacOsX()) {
//                return;
//            } else if (executeDefault()) {
//                return;
//            }
//
//            // No platform-specific launcher found, launch the Jar directly
//            ProcessRunner.execute(new String[]{"java", "-jar", destJar.getAbsolutePath()});
//        } catch(IOException e) {
//            LOGGER.debug("Caught exception", e);
//            // TODO: we might want to do something about this
//        } finally {
//            WindowManager.quit();
//        }
    }



//    @Override
//    protected boolean processFile(AbstractFile file, Object recurseParams) {
//        if (!super.processFile(file, recurseParams)) {
//            return false;
//        }
//    }

    @Override
    public String getStatusString() {
        return Translator.get("version_dialog.preparing_for_update");
    }

}
