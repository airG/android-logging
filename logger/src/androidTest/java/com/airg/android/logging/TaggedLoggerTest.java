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

import android.support.test.runner.AndroidJUnit4;

import com.airg.android.logging.util.LogLine;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.UUID;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mahramf.
 */
@RunWith(AndroidJUnit4.class)
public class TaggedLoggerTest {
    final long MAX_WAIT_FOR_LINE = 5000;
    private final Locale locale = Logger.defaultLocale;

    private static LogTestHelper helper;

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

        final TaggedLogger LOG = Logger.tag(tag);

        LOG.d(message);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.v(message);

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
        final TaggedLogger LOG = Logger.tag(tag);

        LOG.i(message);

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
        final TaggedLogger LOG = Logger.tag(tag);

        LOG.w(message);

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
        final TaggedLogger LOG = Logger.tag(tag);

        LOG.e(message);

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
        final TaggedLogger LOG = Logger.tag(tag);

        LOG.d(format, uuid, timeMillis);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.v(format, uuid, someFloat);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.i(format, uuid, someFloat);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.w(format, uuid, someNumber);

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
        final TaggedLogger LOG = Logger.tag(tag);

        LOG.e(format, uuid, someNumber);

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

        TaggedLogger LOG = Logger.tag(tag);
        LOG.e(format);

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
        LOG = Logger.tag(tag);
        LOG.d(format, invalidParam);

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
        LOG = Logger.tag(tag);
        LOG.i(format, 25);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.e(exception);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.e(exception, message);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.w(exception);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.w(exception, message);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.d(exception);

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

        final TaggedLogger LOG = Logger.tag(tag);
        LOG.d(exception, message);

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
}