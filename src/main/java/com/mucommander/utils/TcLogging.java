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

package com.mucommander.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.*;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;
import com.mucommander.ui.dialog.debug.DebugConsoleAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class manages logging issues within trolCommander
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class TcLogging {

    /**
     * Levels of log printings
     */
    public enum LogLevel {
        OFF,
        ERROR,
        WARNING,
        INFO,
        CONFIG,
        DEBUG,
        FINER,
        TRACE;

        /**
         * This method maps logback levels to trolCommander log levels
         *
         * @param logbackLevel logback log level
         * @return <code>LogLevel</code> corresponding to the given logback log level
         */
        public static LogLevel valueOf(Level logbackLevel) {
            return switch (logbackLevel.toInt()) {
                case Level.OFF_INT -> LogLevel.OFF;
                case Level.ERROR_INT -> LogLevel.ERROR;
                case Level.WARN_INT -> LogLevel.WARNING;
                case Level.INFO_INT -> LogLevel.INFO;
                case Level.DEBUG_INT -> LogLevel.DEBUG;
                case Level.TRACE_INT -> LogLevel.TRACE;
                default -> LogLevel.OFF;
            };
        }

        /**
         * This method maps trolCommander log levels to logback levels
         *
         * @return logback level corresponding to this <code>LogLevel</code>
         */
        public Level toLogbackLevel() {
            return switch (this) {
                case ERROR -> Level.ERROR;
                case WARNING -> Level.WARN;
                case INFO, CONFIG -> Level.INFO;
                case DEBUG, FINER -> Level.DEBUG;
                case TRACE -> Level.TRACE;
                default -> Level.OFF;
            };
        }
    }

    /**
     * Appender that writes log printings to the standard console
     */
    private static ConsoleAppender<ILoggingEvent> consoleAppender;

    /**
     * Appender that writes log printings to the debug console dialog
     */
    private static DebugConsoleAppender debugConsoleAppender;

    /**
     * Sets the level of all muCommander loggers.
     *
     * @param level the new log level
     */
    private static void updateLogLevel(LogLevel level) {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(level.toLogbackLevel());
    }

    /**
     * Returns the log level, in mucommander terms, that match the level of a given logback logging event
     *
     * @param loggingEvent logback logging event
     * @return log level, in mucommander terms, that match the level of the given logback logging event
     */
    public static LogLevel getLevel(ILoggingEvent loggingEvent) {
        return LogLevel.valueOf(loggingEvent.getLevel());
    }

    /**
     * Returns the current log level used by all <code>org.slf4j</code> loggers.
     *
     * @return the current log level used by all <code>org.slf4j</code> loggers.
     */
    public static LogLevel getLogLevel() {
        return LogLevel.valueOf(TcConfigurations.getPreferences().getVariable(TcPreference.LOG_LEVEL, TcPreferences.DEFAULT_LOG_LEVEL));
    }

    /**
     * Sets the new log level to be used by all <code>org.slf4j</code> loggers, and persists it in the
     * application preferences.
     *
     * @param level the new log level to be used by all <code>org.slf4j</code> loggers.
     */
    public static void setLogLevel(LogLevel level) {
        TcConfigurations.getPreferences().setVariable(TcPreference.LOG_LEVEL, level.toString());
        updateLogLevel(level);
    }

    public static DebugConsoleAppender getDebugConsoleAppender() {
        return debugConsoleAppender;
    }

    public static ConsoleAppender<ILoggingEvent> getConsoleAppender() {
        return consoleAppender;
    }

    public static void configureLogging() {
        // Get root logger
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        // we are not interested in auto-configuration
        LoggerContext loggerContext = rootLogger.getLoggerContext();
        loggerContext.reset();

        // Remove default appenders
        rootLogger.detachAndStopAllAppenders();

        // and add custom
        consoleAppender = createConsoleAppender(loggerContext, new CustomLoggingLayout(OsFamily.getCurrent().isUnixBased()));
        debugConsoleAppender = createDebugConsoleAppender(loggerContext, new CustomLoggingLayout(false));

        if (!"false".equals(System.getProperty("log.stdout"))) {
            rootLogger.addAppender(consoleAppender);
        }
        rootLogger.addAppender(debugConsoleAppender);

        // Set the log level to the value defined in the configuration
        updateLogLevel(getLogLevel());
    }

    private static ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext loggerContext, Layout<ILoggingEvent> layout) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();

        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(loggerContext);
        encoder.setLayout(layout);
        encoder.start();

        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        return consoleAppender;
    }

    private static DebugConsoleAppender createDebugConsoleAppender(LoggerContext loggerContext, Layout<ILoggingEvent> layout) {
        DebugConsoleAppender debugConsoleAppender = new DebugConsoleAppender(layout);

        debugConsoleAppender.setContext(loggerContext);
        debugConsoleAppender.start();

        return debugConsoleAppender;
    }

    private static class CustomLoggingLayout extends LayoutBase<ILoggingEvent> {
        private final boolean colored;

        CustomLoggingLayout(boolean colored) {
            this.colored = colored;
        }

        private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        private static final String RESET = "\u001B[0m";
        private static final String RED = "\u001B[31m";
        private static final String YELLOW = "\u001B[33m";
        private static final String GREEN = "\u001B[32m";
        private static final String MAGENTA = "\u001B[35m";
        private static final String CYAN = "\u001B[36m";
        private static final String BLUE = "\u001B[34m";

        private static final String BOLD_RED = "\u001B[1;31m";
        private static final String BOLD_YELLOW = "\u001B[1;33m";
        private static final String BOLD_GREEN = "\u001B[1;32m";
        private static final String BOLD_WHITE = "\u001B[1;37m";
        private static final String BOLD_CYAN = "\u001B[1;36m";
        private static final String BOLD_BLUE = "\u001B[1;34m";

        private static final String HI_RED = "\u001B[0;91m";
        private static final String HI_GREEN = "\u001B[0;92m";
        private static final String HI_BLUE = "\u001B[0;94m";
        private static final String HI_WHITE = "\u001B[0;97m";

        private static final String BOLD_HI_WHITE = "\u001B[1;97m";


        public String doLayout(ILoggingEvent event) {
            StackTraceElement stackTraceElement = event.getCallerData()[0];

            StringBuilder sb = new StringBuilder(128);
            sb.append("[");
            if (colored) sb.append(HI_BLUE);
            sb.append(SIMPLE_DATE_FORMAT.format(new Date(event.getTimeStamp())));
            if (colored) sb.append(RESET);
            sb.append("] ");
            if (colored) sb.append(getColorForLevel(getLevel(event)));
            sb.append(getLevel(event));
            if (colored) {
                while (sb.length() < 50) {
                    sb.append(" ");
                }
            }

            sb.append(" ");
            if (colored) sb.append(MAGENTA);
            sb.append(stackTraceElement.getFileName());
            if (colored) sb.append(RESET);
            sb.append(":");
            if (colored) sb.append(HI_WHITE);
            sb.append(stackTraceElement.getLineNumber());
            if (colored) sb.append(RESET);
            sb.append("#");
            if (colored) sb.append(CYAN);
            sb.append(stackTraceElement.getMethodName());
            sb.append(" ");
            if (colored) {
                while (sb.length() < 130) {
                    sb.append(" ");
                }
            }
            if (colored) sb.append(BOLD_WHITE);
            sb.append(event.getFormattedMessage());
            if (colored) sb.append(RESET);
            sb.append(CoreConstants.LINE_SEPARATOR);

            if (event.getThrowableProxy() != null) {
                if (colored) {
                    sb.append(HI_RED);
                }
                convertThrowable(sb, event.getThrowableProxy(), "");
                if (colored) {
                    sb.append(RESET);
                }
            }
            return sb.toString();
        }

        private String getColorForLevel(LogLevel level) {
            return switch (level) {
                case ERROR -> BOLD_RED;
                case WARNING -> BOLD_YELLOW;
                case INFO, CONFIG -> BOLD_GREEN;
                case DEBUG -> BOLD_CYAN;
                case FINER, TRACE -> BOLD_CYAN;
                default -> "";
            };
        }

        private void convertThrowable(StringBuilder sb, ch.qos.logback.classic.spi.IThrowableProxy throwableProxy, String prefix) {
            sb.append(prefix);
            sb.append(throwableProxy.getClassName());
            sb.append(": ");
            sb.append(throwableProxy.getMessage());
            sb.append(CoreConstants.LINE_SEPARATOR);

            StackTraceElementProxy[] stackTraceElementProxies = throwableProxy.getStackTraceElementProxyArray();
            if (stackTraceElementProxies != null) {
                for (StackTraceElementProxy step : stackTraceElementProxies) {
                    sb.append(prefix);
                    sb.append("\t");
                    String sstep = step.toString();
                    if (colored) {
                        sstep = sstep.replace("(", BOLD_BLUE +"(" + MAGENTA).replace(")", BOLD_BLUE + ")" + HI_RED);
                    }
                    sb.append(sstep);
                    sb.append(CoreConstants.LINE_SEPARATOR);
                }
            }

            ch.qos.logback.classic.spi.IThrowableProxy[] suppressed = throwableProxy.getSuppressed();
            if (suppressed != null) {
                for (ch.qos.logback.classic.spi.IThrowableProxy suppressedThrowable : suppressed) {
                    sb.append(prefix);
                    sb.append("\t... suppressed exception(s):");
                    sb.append(CoreConstants.LINE_SEPARATOR);
                    convertThrowable(sb, suppressedThrowable, prefix + "\t");
                }
            }

            ch.qos.logback.classic.spi.IThrowableProxy cause = throwableProxy.getCause();
            if (cause != null) {
                sb.append(prefix);
                sb.append("\t... caused by:");
                sb.append(CoreConstants.LINE_SEPARATOR);
                convertThrowable(sb, cause, prefix);
            }
        }
    }
}
