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

/**
 * @author Oleg Trifonov
 * Created on 23/03/16.
 */
public class AvrdudeConfiguration {
    /**
     * Required. Specify AVR device.
     */
    public final String deviceName;

    /**
     * Override RS-232 baud rate.
     */
    public final Integer baudrate;

    /**
     * Specify JTAG/STK500v2 bit clock period (us).
     */
    public final Integer bitclock;

    /**
     * Specify location of configuration file.
     */
    public final String configFile;

    /**
     * Specify programmer type.
     */
    public final String programmer;

    /**
     * Enable auto erase for flash memory
     */
    public final boolean flashAutoerase;

    /**
     * ISP Clock Delay [in microseconds]
     */
    public final Integer ispCockDelay;

    /**
     * Specify connection port.
     */
    public final String port;

    /**
     * Override invalid signature check.
     */
    public final boolean overrideInvalidSignatureCheck;

    public final boolean verify;

    /**
     * Pass extended_param to programmer.
     */
    public final String extendedParam;

    /**
     * Path to avrdude
     */
    public final String avrdudeLocation;


    public AvrdudeConfiguration(String deviceName, Integer baudrate, Integer bitclock, String configFile, String programmer,
                         boolean flashAutoerase, Integer ispCockDelay, String port, boolean overrideInvalidSignatureCheck,
                         boolean verify, String extendedParam, String avrdudeLocation) {
        this.deviceName = deviceName;
        this.baudrate = baudrate;
        this.bitclock = bitclock;
        this.configFile = configFile;
        this.programmer = programmer;
        this.flashAutoerase = flashAutoerase;
        this.ispCockDelay = ispCockDelay;
        this.port = port;
        this.overrideInvalidSignatureCheck = overrideInvalidSignatureCheck;
        this.verify = verify;
        this.extendedParam = extendedParam;
        this.avrdudeLocation = avrdudeLocation;
    }

    public AvrdudeConfiguration() {
        this.deviceName = "m8";
        this.baudrate = null;
        this.bitclock = null;
        this.configFile = null;
        this.programmer = "usbasp";
        this.flashAutoerase = true;
        this.ispCockDelay = null;
        this.port = null;
        this.overrideInvalidSignatureCheck = false;
        this.verify = true;
        this.extendedParam = null;
        this.avrdudeLocation = null;
    }

    public boolean isValid() {
        return deviceName != null && programmer != null && !deviceName.trim().isEmpty() && !programmer.trim().isEmpty();
    }
}
