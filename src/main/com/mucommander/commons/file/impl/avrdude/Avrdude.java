/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
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

import com.mucommander.command.Command;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.process.*;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 21/04/16.
 */
public class Avrdude {

    enum Status {
        NONE,
        IN_PROGRESS,
        FINISHED
    }

    public enum Operation {
        READ_FLASH(false),
        READ_EEPROM(false),
        READ_SIGNATURE(false),
        READ_FUSES(false),
        READ_CALIBRATION(false),
        WRITE_FLASH(true),
        WRITE_EEPROM(true),
        WRITE_FUSES(true),
        WRITE_CALIBRATION(true);

        final boolean isWriteOperation;

        Operation(boolean isWriteOperation) {
            this.isWriteOperation = isWriteOperation;
        }
    }

    private volatile int progress;
    private int exitCode;
    private volatile Status status;
    private AbstractProcess process;
    private final InputStream inputStream;
    private final ByteArrayOutputStream fullOutputStream = new ByteArrayOutputStream();

    public Avrdude() {
        this.inputStream = null;
        status = Status.NONE;
    }

    public Avrdude(InputStream inputStream) {
        this.inputStream = inputStream;
        status = Status.NONE;
    }

    private static String buildCommandLine(AvrdudeConfiguration config, Operation operation, StreamType streamType) {
        String cmd;

        if (config.avrdudeLocation != null) {
            cmd = config.avrdudeLocation;
        } else {
            cmd = OsFamily.WINDOWS.isCurrent() ? "avrdude.exe" : "avrdude";
        }
        cmd += " -p " + config.deviceName;
        if (config.baudrate != null) {
            cmd += " -b " + config.baudrate;
        }
        if (config.bitclock != null) {
            cmd += " -B " + config.bitclock;
        }
        if (config.configFile != null) {
            cmd += " -C " + config.configFile;
        }
        if (config.programmer != null) {
            cmd += " -c " + config.programmer;
        }
        if (!config.flashAutoerase) {
            cmd += " -D";
        }
        if (config.ispCockDelay != null) {
            cmd += " -i" + config.ispCockDelay;
        }
        if (config.port != null) {
            cmd += " -P " + config.port;
        }
        if (config.overrideInvalidSignatureCheck) {
            cmd += " -F";
        }
        if (!config.verify) {
            cmd += " -V";
        }
        if (config.extendedParam != null) {
            cmd += " -x " + config.extendedParam;
        }
        AvrdudeDevice device = AvrdudeDevice.getDevice(config.deviceName);

        switch (operation) {
            case READ_FLASH:
                return cmd + " -u -U flash:r:-:" + streamType.getAvrdudeName();
            case READ_EEPROM:
                return cmd + " -u -U eeprom:r:-:" + streamType.getAvrdudeName();
            case READ_SIGNATURE:
                return cmd + " -u -U signature:r:-:" + streamType.getAvrdudeName();
            case READ_FUSES:
                cmd += " -u ";
                if (device.blockSizes.containsKey("efuse")) {
                    cmd += "-U efuse:-:" + streamType.getAvrdudeName();
                }
                if (device.blockSizes.containsKey("hfuse")) {
                    cmd += "-U hfuse:-:" + streamType.getAvrdudeName();
                }
                if (device.blockSizes.containsKey("lfuse")) {
                    cmd += "-U lfuse:-:" + streamType.getAvrdudeName();
                }
                return cmd;
            case READ_CALIBRATION:
                return cmd + " -u -U calibration:r:-:" + streamType.getAvrdudeName();
            case WRITE_FLASH:
                return cmd + " -u -U flash:w:-:" + streamType.getAvrdudeName();
            case WRITE_EEPROM:
                return cmd + " -u -U eeprom:w:-:" + streamType.getAvrdudeName();
            case WRITE_FUSES:
                // TODO
            case WRITE_CALIBRATION:
                return cmd + " -u -U calibration:w:-:" + streamType.getAvrdudeName();

        }
        throw new RuntimeException("unknown operation");
    }


    public void execute(AvrdudeConfiguration config, Operation operation, StreamType streamType) throws IOException, InterruptedException {
        String cmd = buildCommandLine(config, operation, streamType);

        ProcessListener processListener = new ProcessListener() {
            int operationCount;
            List<String> lines = new ArrayList<>();
            int nextLineIndex = 0;
            int progressCount = 0;

            @Override
            public void processDied(int returnValue) {
System.out.println("--------- ");
                for (String s : lines) {
                    System.out.println("@"+s);
                }
            }

            @Override
            public void processOutput(String output) {
                String[] outLines = StringUtils.splitByWholeSeparatorPreserveAllTokens(output, "\n");//output.split("\n");
                for (int i = 0; i < outLines.length; i++) {
                    String s = outLines[i];
                    if (i == 0 && lines.size() > 0) {
                        String old = lines.get(lines.size()-1);
                        lines.set(lines.size()-1, old + s);
                    } else {
                        lines.add(s);
                    }
                }
//if (output.equals("#")) System.out.println(">"+output + "  (" + progress + ")"); else
//System.out.println(">"+output);
                for (int i = nextLineIndex; i < lines.size(); i++) {
                    if (i >= lines.size()) {
                        break;
                    }
                    String s = lines.get(i);
                    if (s.contains("Reading |") || s.contains("Writing |")) {
                        operationCount++;
                        nextLineIndex = i + 1;
                    }
//System.out.println("?"+s + "    (" + operationCount + ")");

                }
                if (nextLineIndex < lines.size()-1) {
                    nextLineIndex = lines.size()-1;
                }
//                if (output.contains("Reading |") || output.contains("Writing |")) {
//                    operationCount++;
//                }
                if (operationCount > 0 && status == Status.NONE) {
                    status = Status.IN_PROGRESS;
                    progressCount = 0;
                    progress = 0;
                }
                if (status == Status.IN_PROGRESS && output.contains("#")) {
//System.out.println("\n?+ " + StringUtils.countMatches(output, "#"));
                    progressCount += StringUtils.countMatches(output, "#")*2;
                    if (progressCount > 100) {
                        progress = progressCount - 100;
                    }
                }
            }

            @Override
            public void processOutput(byte[] buffer, int offset, int length) {
                fullOutputStream.write(buffer, offset, length);
            }
        };

        String[] tokens = Command.getTokens(cmd);
        process = ProcessRunner.execute(tokens, null, processListener, null);
        if (inputStream != null) {
//Thread.sleep(1000);
            StreamUtils.copyStream(inputStream, process.getOutputStream());
            process.getOutputStream().close();
            inputStream.close();
        }

//        process.getOutputStream().write("!!!!!!!\n".getBytes());
        process.getOutputStream().close();
    }


    public void waitFor() throws IOException, InterruptedException {
        exitCode = process.waitFor();
        process.waitMonitoring();
        process.destroy();
        status = Status.FINISHED;
    }


    public String getHexOutput() {
        final String startTemplate = "writing output file \"<stdout>\"";
        String fullOutput = fullOutputStream.toString();
        int start = fullOutput.indexOf(startTemplate);
        if (start < 0) {
            return null;
        }
        start = fullOutput.indexOf(':', start + startTemplate.length());
        int finish = fullOutput.indexOf("avrdude done", start);
        if (finish > 0) {
            return fullOutput.substring(start-1, finish);
        }
        return null;//fullOutput.substring(start);
    }


    public int getProgress() {
        return progress;
    }

    public Status getStatus() {
        return status;
    }



    public static void main(String args[]) throws IOException, InterruptedException {
        AvrdudeConfiguration config = new AvrdudeConfiguration("m8", null, null, null, "usbasp", true, null, null, true, false, null,
                "/Users/trol/-avrdude/avrdude-6.3/avrdude");
        //OutputStream os = new FileOutputStream("/Users/trol/--------.bin");//System.out;

/*
        AvrDudeInputStream is = new AvrDudeInputStream(StreamType.HEX, config, Operation.READ_FLASH);
        new Thread() {
            int lastProgress = -1;
            @Override
            public void run() {
                while (true) {
                    int progress = is.getProgress();
                    if (progress != lastProgress) {
                        System.out.println("progress: " + progress);
                    }
                    lastProgress = progress;
                    if (progress == 100) {
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
        StreamUtils.copyStream(is, new FileOutputStream(new File("/Users/trol/--------.hex")));
*/


        AvrdudeOutputStream os = new AvrdudeOutputStream(StreamType.HEX, config, Operation.WRITE_FLASH);
        new Thread() {
            int lastProgress;
            @Override
            public void run() {
                while (true) {
                    int progress = os.getProgress();
                    if (progress != lastProgress) {
                        System.out.println("progress: " + progress);
                    }
                    lastProgress = progress;
                    if (progress == 100) {
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
        StreamUtils.copyStream(new FileInputStream(new File("/Users/trol/--------.hex")), os);
        os.close();



/*
        Avrdude avrdude = new Avrdude();
        avrdude.execute(config, Operation.READ_FLASH, StreamType.HEX);
        new Thread() {
            @Override
            public void run() {
                int progress = avrdude.progress;
                while (progress < 100) {
                    progress = avrdude.progress;
                    System.out.println("Progress: " + progress);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    avrdude.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("------------");
                System.out.println(avrdude.getHexOutput());
            }
        }.start();
*/
    }


}
