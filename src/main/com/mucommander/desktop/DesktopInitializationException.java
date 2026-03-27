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

package com.mucommander.desktop;

/**
 * Encapsulates errors that occur at {@link DesktopAdapter} initialization time.
 * <p>
 * This class can contain basic error information from either the <code>com.mucommander.desktop</code> API
 * or the application. Application writers can subclass it to provide additional functionality.
 * <p>
 * If the application needs to pass through other types of exceptions, it must wrap them in a
 * <code>DesktopInitializationException</code> or an exception derived from it.
 *
 * @author Nicolas Rinaudo
 */
public class DesktopInitializationException extends Exception {
    /**
     * Creates a new desktop initialization exception.
     * @param message the error message.
     */
    public DesktopInitializationException(String message) {super(message);}


    /**
     * Creates a new desktop initialization exception wrapping an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, and its message will
     * become the default message for the <code>DesktopInitializationException</code>.
     *
     * @param cause the exception to be wrapped in a <code>DesktopInitializationException</code>.
     */
    public DesktopInitializationException(Throwable cause) {super(cause);}

    /**
     * Creates a new desktop initialization exception from an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, but the new exception will have its own message.
     *
     * @param message the detail message.
     * @param cause   the exception to be wrapped in a <code>DesktopInitializationException</code>.
     */
    public DesktopInitializationException(String message, Throwable cause) {super(message, cause);}
}
