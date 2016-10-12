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

import lombok.Getter;

import static com.airg.android.logging.Logger.debug;
import static com.airg.android.logging.Logger.error;
import static com.airg.android.logging.Logger.expand;
import static com.airg.android.logging.Logger.format;
import static com.airg.android.logging.Logger.logThrowable;
import static com.airg.android.logging.Logger.warn;

/**
 Allows you to attach a tag to a log instance so that you can set the tag once and forget about it.
*/

@SuppressWarnings({"WeakerAccess", "unused"})
public class TaggedLogger {
    @Getter private final String tag;

    TaggedLogger (final String logtag) {
        tag = logtag;
    }

    // ********** Exception Logging **********//

    /**
     Log a {@link java.lang.Throwable} as error

     @param throwable
     {@link java.lang.Throwable} to log
     */
    public void e (final Throwable throwable) {
        e (throwable, null);
    }

    /**
     Log a {@link java.lang.Throwable} as error with a message

     @param throwable
     {@link java.lang.Throwable} to log
     @param msg
     message to log
     */
    public void e (final Throwable throwable, final String msg) {
        logThrowable (throwable);
        error (tag, format (throwable, msg));
    }

    /**
     Log a {@link java.lang.Throwable} as warning

     @param throwable
     {@link java.lang.Throwable} to log
     */
    public void w (final Throwable throwable) {
        w (throwable, null);
    }

    /**
     Log a {@link java.lang.Throwable} as warning with a message

     @param throwable
     {@link java.lang.Throwable} to log
     @param msg
     message to log
     */
    public void w (final Throwable throwable, final String msg) {
        logThrowable (throwable);
        warn (tag, format (throwable, msg));
    }

    /**
     Log a {@link java.lang.Throwable} as debug

     @param throwable
     {@link java.lang.Throwable} to log
     */
    public void d (final Throwable throwable) {
        d (null, throwable);
    }

    /**
     Log a {@link java.lang.Throwable} as debug with a message

     @param throwable
     {@link java.lang.Throwable} to log
     @param msg
     message to log
     */
    public void d (final Throwable throwable, final String msg) {
        logThrowable (throwable);
        debug (tag, format (throwable, msg));
    }

    // ********** Errors Logging **********//

    /**
     Log a formatted error message
     @param fmt message format
     @param args message format arguments
     */
    public void e (final String fmt, final Object... args) {
        error (tag, expand (fmt, args));
    }

    /**
     Log an error message
     @param str message to log
     */
    public void e (final String str) {
        error (tag, str);
    }

    // ********** Warnings Logging **********//

    /**
     Log a formatted warning message
     @param fmt message format
     @param args message format arguments
     */
    public void w (final String fmt, final Object... args) {
        warn (tag, expand (fmt, args));
    }

    /**
     Log a warning message
     @param str message to log
     */
    public void w (final String str) {
        warn (tag, str);
    }

    // ********** Info Logging **********//

    /**
     Log a formatted information message
     @param fmt message format
     @param args message format arguments
     */
    public void i (final String fmt, final Object... args) {
        Logger.info (tag, expand (fmt, args));
    }

    /**
     Log an information message
     @param str message to log
     */
    public void i (final String str) {
        Logger.info (tag, str);
    }

    // ********** Debug Logging **********//

    /**
     Log a formatted debug message
     @param fmt message format
     @param args message format arguments
     */
    public void d (final String fmt, final Object... args) {
        debug (tag, expand (fmt, args));
    }

    /**
     Log a debug message
     @param str message to log
     */
    public void d (final String str) {
        debug (tag, str);
    }

    // ********** Verbose Logging **********//

    /**
     Log a formatted verbose message
     @param fmt message format
     @param args message format arguments
     */
    public void v (final String fmt, final Object... args) {
        Logger.verbose (tag, expand (fmt, args));
    }

    /**
     Log a verbose message
     @param str message to log
     */
    public void v (final String str) {
        Logger.verbose (tag, str);
    }
}
