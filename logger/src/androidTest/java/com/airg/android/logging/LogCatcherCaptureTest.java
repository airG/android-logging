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
public class LogCatcherCaptureTest {
    private final String TAG = "CAPTURE";

    @Test
    public void captureSelfLogsOnly() throws Exception {
        captureAndVerify(true);
    }

    @Test
    public void captureAllLogs() throws Exception {
        captureAndVerify(false);
    }

    private void captureAndVerify(final boolean self) throws InterruptedException {
        final String dExpected = "This is expected log line 1";
        final String vExpected = "and this is line 2";
        final String iExpected = "What is this? iOS?";
        final String wExpected = "line 3, right?";
        final String eExpected = "WRONG!";
        final String unExpected = "This one came later";

        final AtomicBoolean lock = new AtomicBoolean(false);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final List<String> lines = new ArrayList<>();

        final LogCatcher logCatcher = new LogCatcher(true, self);
        logCatcher.waitForClearEnd();

        LogCatcher.OnLogLinesListener listener = new LogCatcher.OnLogLinesListener() {
            @Override
            public void onLogLine(final String logLine) {
                synchronized (lines) {
                    lines.add(logLine);
                    lines.notifyAll();
                }
            }

            @Override
            public void onStart() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignore) {

                        }

                        Log.d(TAG, dExpected);
                        waitFor(dExpected);

                        Log.v(TAG, vExpected);
                        waitFor(vExpected);

                        Log.i(TAG, iExpected);
                        waitFor(iExpected);

                        Log.w(TAG, wExpected);
                        waitFor(wExpected);

                        Log.e(TAG, eExpected);
                        waitFor(eExpected);

                        logCatcher.endCapture();
                    }
                }).start();
            }

            private void waitFor(final String line) {
                synchronized (lines) {
                    while (!containsLogLineThatContains(lines, line)) {
                        try {
                            lines.wait();
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
            }

            @Override
            public void onFinished() {
                synchronized (lock) {
                    lock.set(true);
                    lock.notifyAll();
                }

                Log.d(TAG, unExpected);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                errorCount.incrementAndGet();
                onFinished();
            }
        };

        logCatcher.startCapture(listener);

        // now wait for it to finish

        synchronized (lock) {
            while (!lock.get()) {
                lock.wait();
            }
        }

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