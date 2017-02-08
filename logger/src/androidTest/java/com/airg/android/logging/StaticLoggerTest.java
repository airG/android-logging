/*
 * ****************************************************************************
 *   Copyright  2017 airG Inc.                                                 *
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

import com.airg.android.logging.util.LogLine;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.EOFException;
import java.util.Locale;
import java.util.UUID;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mahramf.
 */
@SuppressWarnings("ThrowableInstanceNeverThrown") // there's gonna be a lot of exceptions not thrown
public class StaticLoggerTest {

    private static LogTestHelper helper;
    private final Locale locale = Logger.defaultLocale;

    final long MAX_WAIT_FOR_LINE = 5000;

    @BeforeClass
    public static void beforeStaticLoggerTest() throws Exception {
        helper = new LogTestHelper();
        assertTrue("Test helper not in the expected state", helper.start());
    }

    @AfterClass
    public static void afterStaticLoggerTest() throws Exception {
        assertEquals("Log capture errors were encountered during test", 0, helper.stop());
    }

    @Before
    public void clearHelper() throws Exception {
        helper.clear();
        assertTrue("Helper is not capturing log lines", helper.capturing());
    }

    @Test
    public void logDPlain() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String message = uuid + "Testing a plain string logged to debug";
        final String tag = Integer.toHexString(uuid.hashCode());
        Logger.d(tag, message);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(DEBUG));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch", message, log.message);
    }

    @Test
    public void logVPlain() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String message = uuid + "Logging a plain string to verbose";
        final String tag = Integer.toHexString(uuid.hashCode());
        Logger.v(tag, message);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(VERBOSE));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch", message, log.message);
    }

    @Test
    public void logIPlain() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String message = uuid + "This log message will ... inform ... you?";
        final String tag = Integer.toHexString(uuid.hashCode());
        Logger.i(tag, message);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(INFO));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch", message, log.message);
    }

    @Test
    public void logWPlain() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String message = uuid + "This is your last warning!";
        final String tag = Integer.toHexString(uuid.hashCode());
        Logger.w(tag, message);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(WARN));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch", message, log.message);
    }

    @Test
    public void logEPlain() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String message = uuid + "There must be an error in this...";
        final String tag = Integer.toHexString(uuid.hashCode());
        Logger.e(tag, message);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(ERROR));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch", message, log.message);
    }

    @Test
    public void logDFormat() throws Exception {
        final String uuid = UUID.randomUUID().toString();

        final String tag = Integer.toHexString(uuid.hashCode());

        final String format = "%s This is a formatted debug message %d";
        final long timeMillis = System.currentTimeMillis();
        final String expectedMsg = String.format(locale, format, uuid, timeMillis);
        Logger.d(tag, format, uuid, timeMillis);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(DEBUG));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch: " + line, expectedMsg, log.message);
    }

    @Test
    public void logVFormat() throws Exception {
        final String uuid = UUID.randomUUID().toString();

        final String tag = Integer.toHexString(uuid.hashCode());

        final String format = "%s - %f - This is a verbose formatted log entry";

        final float someFloat = 23.43543f;
        final String expectedMsg = String.format(locale, format, uuid, someFloat);
        Logger.v(tag, format, uuid, someFloat);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(VERBOSE));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch: " + line, expectedMsg, log.message);
    }

    @Test
    public void logIFormat() throws Exception {
        final String uuid = UUID.randomUUID().toString();

        final String tag = Integer.toHexString(uuid.hashCode());

        final String format = "%s This is a to inform you that you owe me $%02f. Pay up.";

        final float someFloat = 23.43543f;
        final String expectedMsg = String.format(locale, format, uuid, someFloat);
        Logger.i(tag, format, uuid, someFloat);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(INFO));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch: " + line, expectedMsg, log.message);
    }

    @Test
    public void logWFormat() throws Exception {
        final String uuid = UUID.randomUUID().toString();

        final String tag = Integer.toHexString(uuid.hashCode());

        final String format = "%s Warning: %e is a really large number";

        final double someNumber = 234340003252.443563543;
        final String expectedMsg = String.format(locale, format, uuid, someNumber);
        Logger.w(tag, format, uuid, someNumber);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(WARN));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch: " + line, expectedMsg, log.message);
    }

    @Test
    public void logEFormat() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final String format = "%s%d error blah blah blah";

        final int someNumber = 1243214;
        final String expectedMsg = String.format(locale, format, uuid, someNumber);
        Logger.e(tag, format, uuid, someNumber);

        final String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(ERROR));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertEquals("log message mismatch: " + line, expectedMsg, log.message);
    }

    @Test
    public void logWithBadFormatWontCrash() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String tag = Integer.toHexString(uuid.hashCode());
        String format = uuid + " some thing %s w00t";

        Logger.e(tag, format);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);
        LogLine log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(ERROR));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain markers: " + line,
                log.message.contains(uuid) && log.message.contains(format));

        // try another
        uuid = UUID.randomUUID().toString();
        tag = Integer.toHexString(uuid.hashCode());
        format = uuid + "%d is an int. don't give it a string";
        String invalidParam = "bob";
        Logger.d(tag, format, invalidParam);

        line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);
        log = new LogLine(line, tag);
        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(DEBUG));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain markers: " + line,
                log.message.contains(uuid) &&
                        log.message.contains(format) &&
                        log.message.contains(invalidParam));

        // one more
        uuid = UUID.randomUUID().toString();
        tag = Integer.toHexString(uuid.hashCode());
        format = uuid + "There's a %d and a %s that I forgot.";
        Logger.i(tag, format, 25);

        line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);
        log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(INFO));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain markers: " + line,
                log.message.contains(uuid) && log.message.contains(format));
    }

    @Test
    public void logEThrowable() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final Exception exception = new RuntimeException(uuid + " This is an exception");
        Logger.e(tag, exception);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        // skip the printed stack trace
        if (line.contains("System.err"))
            line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(ERROR));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain markers: " + line, log.message.contains(uuid));
        assertTrue("log message does not contain markers: " + line, log.message.contains(exception.getMessage()));
        assertTrue("log message does not contain markers: " + line,
                log.message.contains(exception.getClass().getName()));
    }

    @Test
    public void logEThrowableWithMessage() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final Exception exception = new IllegalStateException("This is illegal");
        final String message = uuid + " This is the extra message";
        Logger.e(tag, exception, message);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        // skip the printed stack trace
        if (line.contains("System.err"))
            line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(ERROR));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain uuid: " + line, log.message.contains(uuid));
        assertTrue("log message does not contain extra message: " + line, log.message.contains(message));
    }

    @Test
    public void logWThrowable() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final Exception exception = new RuntimeException(uuid + " I'm warning you!");
        Logger.w(tag, exception);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        // skip the printed stack trace
        if (line.contains("System.err"))
            line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(WARN));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain markers: " + line, log.message.contains(uuid));
        assertTrue("log message does not contain markers: " + line, log.message.contains(exception.getMessage()));
        assertTrue("log message does not contain markers: " + line,
                log.message.contains(exception.getClass().getName()));
    }

    @Test
    public void logWThrowableWithMessage() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final Exception exception = new IllegalStateException("This is illegal. I've warned you before.");
        final String message = uuid + " This is the extra warning";
        Logger.w(tag, exception, message);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        // skip the printed stack trace
        if (line.contains("System.err"))
            line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(WARN));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain uuid: " + line, log.message.contains(uuid));
        assertTrue("log message does not contain extra message: " + line, log.message.contains(message));
    }

    @Test
    public void logDThrowable() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final Exception exception = new IllegalArgumentException(uuid + " Debug this");
        Logger.d(tag, exception);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        // skip the printed stack trace
        if (line.contains("System.err"))
            line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(DEBUG));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain markers: " + line, log.message.contains(uuid));
        assertTrue("log message does not contain markers: " + line, log.message.contains(exception.getMessage()));
        assertTrue("log message does not contain markers: " + line,
                log.message.contains(exception.getClass().getName()));
    }

    @Test
    public void logDThrowableWithMessage() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String tag = Integer.toHexString(uuid.hashCode());
        final Exception exception = new IllegalStateException("Don't make me throw something at you!");
        final String message = uuid + " I've got great aim!";
        Logger.d(tag, exception, message);

        String line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        // skip the printed stack trace
        if (line.contains("System.err"))
            line = helper.waitForLineContaining(uuid, MAX_WAIT_FOR_LINE);

        assertNotNull("line = null", line);

        final LogLine log = new LogLine(line, tag);

        assertTrue("Unable to parse line: " + line, log.valid());
        assertTrue("Log level mismatch: " + line, log.isLevel(DEBUG));
        assertEquals("log tag mismatch: " + line, tag, log.tag);
        assertTrue("log message does not contain uuid: " + line, log.message.contains(uuid));
        assertTrue("log message does not contain extra message: " + line, log.message.contains(message));
    }

    @Test
    public void throwableFormatterIncludesMessage() throws Exception {
        final String message = "Exception message";
        final Exception exception = new EOFException("This is an eof");
        final String formatted = Logger.format(exception, message);

        assertFalse(TextUtils.isEmpty(formatted));
        assertTrue("formatted exception does not contain extra message", formatted.startsWith(message));
        assertTrue("formatted exception does not contain exception name", formatted.contains(exception.getClass().getName()));
        assertTrue("formatted exception does not contain exception message", formatted.contains(exception.getMessage()));
    }

    @Test
    public void throwableFormatterSkipsEmptyMessage() throws Exception {
        final Exception exception = new EOFException("This is an eof");
        final String formatted = Logger.format(exception, "");

        assertFalse(TextUtils.isEmpty(formatted));
        assertTrue("formatted exception does not contain exception name", formatted.startsWith(exception.getClass().getName()));
        assertTrue("formatted exception does not contain exception message", formatted.contains(exception.getMessage()));
    }

    @Test
    public void throwableFormatterSkipsNullMessage() throws Exception {
        final Exception exception = new EOFException("This is an eof");
        final String formatted = Logger.format(exception, null);

        assertFalse(TextUtils.isEmpty(formatted));
        assertTrue("formatted exception does not contain exception name", formatted.startsWith(exception.getClass().getName()));
        assertTrue("formatted exception does not contain exception message", formatted.contains(exception.getMessage()));
    }

    /*
    These don't need to be tested too rigorously as they just use platform methods to perform the formatting
     */
    @SuppressWarnings("RedundantStringFormatCall")
    @Test
    public void expandFormatsCorrectly() throws Exception {
        String format ="";
        assertEquals(String.format(locale, format), Logger.expand(format));

        format = "This is just a string. Nothing more.";
        assertEquals(String.format(locale, format), Logger.expand(format));

        format = "I've got an int (%d) and a letter (%c)";
        assertEquals(String.format(locale, format, 34, 'f'), Logger.expand(format, 34, 'f'));

        format = "This float is not truncated: %f, but this one is %3f";
        assertEquals(String.format(locale, format, 2342.345f, 333.1234453f),
                Logger.expand(format, 2342.345f, 333.1234453f));
    }

    // no asserts. This should just be able to run to end without throwing an exception
    @Test
    public void expandWontCrashOnBadFormat() throws Exception {
        Logger.expand(null, null);
        Logger.expand("", null);
        Logger.expand(null, 23, "bob");
        Logger.expand("%s %d", 23, "bob");
        Logger.expand("%d");
        Logger.expand("%d %s %f", "bob", "sally", "joe");
    }
}