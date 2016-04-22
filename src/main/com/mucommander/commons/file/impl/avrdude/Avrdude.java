/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.avrdude;

import com.mucommander.process.ExecutorUtils;

import java.io.IOException;

/**
 * @author Oleg Trifonov
 * Created on 21/04/16.
 */
public class Avrdude {
    // read to stdout
    // Tip: Use “-” as the file name, to write to stdout. Example:
    // avrdude -c usbasp -p m16 -U lfuse:r:-:h -U hfuse:r:-:h -U efuse:r:-:h

    private static void execute(AvrdudeConfiguration config, String command) throws IOException, InterruptedException {
        String cmd = "ls /Users/trol";
        ExecutorUtils.executeAndGetOutput(cmd, null, (exitCode, output) -> {
            System.out.println(exitCode + ": " + output);
        });
    }


    public static void main(String args[]) throws IOException, InterruptedException {
        execute(null, "");
    }
}
