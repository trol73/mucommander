/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
package com.mucommander.tools;

import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessRunner;

/**
 * @author Oleg Trifonov
 * Created on 09/09/15.
 */
public class AdbTool extends ExternalTool {

    @Override
    boolean detect() {
        try {
            AbstractProcess process = ProcessRunner.execute("adb devices");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Throwable t) {
            return false;
        }
    }



}
