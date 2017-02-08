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
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.airg.android.logging.util.LogTestUtils.containsLogLineThatContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mahramf.
 */
@RunWith(AndroidJUnit4.class)
public class LogCatcherDumpTest {

    private final String TAG = "DUMP";

    @Before
    public void clearLog() {
        LogCatcher.clearLog();
        Log.d(TAG, "Log cleared");
    }

    @Test
    public void dumpSelfLogsOnly() throws Exception {
        dumpAndVerify(true);
    }

    @Test
    public void dumpAllLogs() throws Exception {
        dumpAndVerify(false);
    }

    private void dumpAndVerify(final boolean self) throws InterruptedException {
        final String dExpected = "This is expected log line 1";
        final String vExpected = "and this is line 2";
        final String iExpected = "What is this? iOS?";
        final String wExpected = "line 3, right?";
        final String eExpected = "WRONG!";

        Log.d(TAG, dExpected);
        Log.v(TAG, vExpected);
        Log.i(TAG, iExpected);
        Log.w(TAG, wExpected);
        Log.e(TAG, eExpected);

        final AtomicBoolean lock = new AtomicBoolean(false);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final List<String> lines = new ArrayList<>();

        LogCatcher.OnLogLinesListener listener = new LogCatcher.OnLogLinesListener() {
            @Override
            public void onLogLine(final String logLine) {
                synchronized (lock) {
                    lines.add(logLine);
                }
            }

            @Override
            public void onStart() {
                // meh
            }

            @Override
            public void onFinished() {
                synchronized (lock) {
                    lock.set(true);
                    lock.notifyAll();
                }
            }

            @Override
            public void onError(Throwable t) {
                errorCount.incrementAndGet();
                onFinished();
            }
        };

        new LogCatcher(false, self).dump(listener);

        // now wait for it to finish

        synchronized (lock) {
            while (!lock.get()) {
                lock.wait();
            }
        }

        final String unExpected = "This one came later";
        Log.d(TAG, unExpected);

        final int errors = errorCount.get();
        assertEquals(errors + " errors encountered", 0, errors);

        // we logged 5 lines so there should be at least that many lines
        final int numLines = lines.size();
        assertTrue("Too few lines: " + numLines, numLines >= 5);

        assertTrue("Missing expected entry", containsLogLineThatContains(lines, dExpected));
        assertTrue("Missing expected entry", containsLogLineThatContains(lines, vExpected));
        assertTrue("Missing expected entry", containsLogLineThatContains(lines, iExpected));
        assertTrue("Missing expected entry", containsLogLineThatContains(lines, wExpected));
        assertTrue("Missing expected entry", containsLogLineThatContains(lines, eExpected));
        assertFalse("Unexpected entry", containsLogLineThatContains(lines, unExpected));
    }
}