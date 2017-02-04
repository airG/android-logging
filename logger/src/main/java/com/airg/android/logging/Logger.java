/*
 * ****************************************************************************
 *   Copyright  2016 airG Inc.                                                 *
 *                                                                             *
 *   Licensed under the Apache License, Version 2.0 (the "License");           *
 *   you may not use this file except in compliance with the License.          *
 *   You may obtain a copy of the License at                                   *
 *                                                                             *
 *       http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                             *
 *   Unless required by applicable law or agreed to in writing, software       *
 *   distributed under the License is distributed on an "AS IS" BASIS,         *
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *   See the License for the specific language governing permissions and       *
 *   limitations under the License.                                            *
 * ***************************************************************************
 */

package com.airg.android.logging;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import lombok.Setter;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

/**
 * Logging utility class that allows for formatted logging.
 */

@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
// static class ... we don't want instances
public final class Logger {
    private static final String TAG = "LOG";

    @Setter
    private static int rootLevel;

    private Logger() {
        // no instance
    }

    /**
     * Create an tagged logger instance
     *
     * @param tag log tag
     * @return a TaggedLog instance of the logger
     */
    public static TaggedLogger tag(final String tag) {
        return new TaggedLogger(tag);
    }

    // ********** Exception Logging **********//

    /**
     * Log a {@link java.lang.Throwable} as error
     *
     * @param tag       log tag
     * @param throwable {@link java.lang.Throwable} to log
     */
    public static void e(final String tag, final Throwable throwable) {
        e(tag, throwable, null);
    }

    /**
     * Log a {@link java.lang.Throwable} with message as error
     *
     * @param tag       log tag
     * @param throwable {@link java.lang.Throwable} to log
     * @param msg       message to log
     */
    public static void e(final String tag, final Throwable throwable, final String msg) {
        logThrowable(throwable);
        error(tag, format(throwable, msg));
    }

    /**
     * Log a {@link java.lang.Throwable} as warning
     *
     * @param tag       log tag
     * @param throwable {@link java.lang.Throwable} to log
     */
    public static void w(final String tag, final Throwable throwable) {
        w(tag, throwable, null);
    }

    /**
     * Log a {@link java.lang.Throwable} with message as warning
     *
     * @param tag       log tag
     * @param throwable {@link java.lang.Throwable} to log
     * @param msg       message to log
     */
    public static void w(final String tag, final Throwable throwable, final String msg) {
        logThrowable(throwable);
        warn(tag, format(throwable, msg));
    }

    /**
     * Log a {@link java.lang.Throwable} as debug trace
     *
     * @param tag       log tag
     * @param throwable {@link java.lang.Throwable} to log
     */
    public static void d(final String tag, final Throwable throwable) {
        d(tag, throwable, null);
    }

    /**
     * Log a {@link java.lang.Throwable} with message as debug trace
     *
     * @param tag       log tag
     * @param throwable {@link java.lang.Throwable} to log
     * @param msg       message to log
     */
    public static void d(final String tag, final Throwable throwable, final String msg) {
        logThrowable(throwable);
        debug(tag, format(throwable, msg));
    }

    // ********** Errors Logging **********//

    /**
     * Log a formatted error message
     *
     * @param tag  log tag
     * @param fmt  message format
     * @param args message format arguments
     */
    public static void e(final String tag,
                         final String fmt,
                         final Object... args) {
        error(tag, expand(fmt, args));
    }

    /**
     * Log an error message
     *
     * @param tag log tag
     * @param str log message
     */
    public static void e(final String tag, final String str) {
        error(tag, str);
    }

    // ********** Warnings Logging **********//

    /**
     * Log a formatted warning message
     *
     * @param tag  log tag
     * @param fmt  message format
     * @param args message format arguments
     */
    public static void w(final String tag,
                         final String fmt,
                         final Object... args) {
        warn(tag, expand(fmt, args));
    }

    /**
     * Log a warning message
     *
     * @param tag log tag
     * @param str log message
     */
    public static void w(final String tag, final String str) {
        warn(tag, str);
    }

    // ********** Info Logging **********//

    /**
     * Log a formatted information message
     *
     * @param tag  log tag
     * @param fmt  message format
     * @param args message format arguments
     */
    public static void i(final String tag,
                         final String fmt,
                         final Object... args) {
        info(tag, expand(fmt, args));
    }

    /**
     * Log an information message
     *
     * @param tag log tag
     * @param str log message
     */
    public static void i(final String tag, final String str) {
        info(tag, str);
    }

    // ********** Debug Logging **********//

    /**
     * Log a formatted debug message
     *
     * @param tag  log tag
     * @param fmt  message format
     * @param args message format arguments
     */
    public static void d(final String tag,
                         final String fmt,
                         final Object... args) {
        debug(tag, expand(fmt, args));
    }

    /**
     * Log a debug message
     *
     * @param tag log tag
     * @param str log message
     */
    public static void d(final String tag, final String str) {
        debug(tag, str);
    }

    // ********** Verbose Logging **********//

    /**
     * Log a formatted verbose message
     *
     * @param tag  log tag
     * @param fmt  message format
     * @param args message format arguments
     */
    public static void v(final String tag,
                         final String fmt,
                         final Object... args) {
        verbose(tag, expand(fmt, args));
    }

    /**
     * Log a verbose message
     *
     * @param tag log tag
     * @param str log message
     */
    public static void v(final String tag, final String str) {
        verbose(tag, str);
    }

    /**
     * Expand a formatted message and arguments to a string
     *
     * @param fmt  message format
     * @param args message format arguments
     * @return formatted message
     */
    static String expand(final String fmt, final Object... args) {
        try {
            return TextUtils.isEmpty(fmt)
                    ? Arrays.toString(args)
                    : null == args || args.length == 0
                    ? fmt
                    : String.format(Locale.ENGLISH, fmt, args);
        } catch (Exception e) {
            d(TAG, e, "Log format failed");
            return Arrays.toString(args);
        }
    }

    /**
     * Formats a {@link java.lang.Throwable} and an optional message into a string including the
     * Throwable's stack trace.
     *
     * @param t   throwable
     * @param msg message to log
     * @return formatted message and stack trace
     */
    public static String format(final Throwable t, final String msg) {
        return msg + '\n' + Log.getStackTraceString(t);
    }

    // ********** Privates, Helpers, and Utility methods **********//

    static void logThrowable(final Throwable throwable) {
        if (null == throwable)
            return;

        throwable.printStackTrace();
    }

    static void debug(final String tag, final String str) {
        logAs(DEBUG, tag, str);
    }

    static void verbose(final String tag, final String str) {
        logAs(VERBOSE, tag, str);
    }

    static void info(final String tag, final String str) {
        logAs(INFO, tag, str);
    }

    static void error(final String tag, final String str) {
        logAs(ERROR, tag, str);
    }

    static void warn(final String tag, final String str) {
        logAs(WARN, tag, str);
    }

    private static void logAs(final int level, final String tag, final String msg) {
        if (level < rootLevel)
            return;

        final String loggableMessage = null == msg ? "<null>" : msg;

        switch (level) {
            case DEBUG:
                android.util.Log.d(tag, loggableMessage);
                break;
            case WARN:
                android.util.Log.w(tag, loggableMessage);
                break;
            case ERROR:
                android.util.Log.e(tag, loggableMessage);
                break;
            case VERBOSE:
                android.util.Log.v(tag, loggableMessage);
                break;
            case INFO:
                // log unknowns as info
            default:
                android.util.Log.i(tag, loggableMessage);
                break;
        }
    }
}
