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

import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mahramf.
 */
class LogTestHelper implements LogCatcher.OnLogLinesListener {
    private static AtomicInteger errorCount = new AtomicInteger(0);

    private final Object catcherLock = new Object();

    private enum CatcherState {
        NONE, WAIT, STARTED, FINISHED
    }

    private final List<String> logLines;

    private final LogCatcher catcher;
    private volatile CatcherState catcherState;

    LogTestHelper() {
        catcher = new LogCatcher(true, true);
        catcherState = CatcherState.NONE;
        logLines = new ArrayList<>();
    }

    boolean dump () throws InterruptedException {
        if (0 != errorCount.get() || CatcherState.NONE != catcherState)
            return false;

        catcher.waitForClearEnd();
        catcher.dump(this);
        catcher.waitForCaptureEnd();
        return true;
    }

    boolean start() throws InterruptedException {
        if (0 != errorCount.get() || CatcherState.NONE != catcherState)
            return false;

        catcher.waitForClearEnd();

        synchronized (catcherLock) {
            catcherState = CatcherState.WAIT;

            catcher.startCapture(this);

            while (catcherState != CatcherState.STARTED)
                catcherLock.wait();
        }

        return true;
    }

    int stop() throws InterruptedException {
        // wait for catcher to stop
        synchronized (catcherLock) {
            catcher.endCapture();

            while (catcherState == CatcherState.WAIT || catcherState == CatcherState.STARTED)
                catcherLock.wait();

            catcherState = CatcherState.NONE;
        }

        final int errors = errorCount.get();
        errorCount.set(0);
        return errors;
    }

    void clear() {
        logLines.clear();
    }

    boolean capturing() {
        synchronized (catcherLock) {
            return catcherState == CatcherState.STARTED;
        }
    }

    void drop(final String line) {
        synchronized (logLines) {
            logLines.remove(line);
        }
    }

    @Override
    public void onLogLine(String logLine) {
        synchronized (logLines) {
            logLines.add(logLine);
            logLines.notifyAll();
        }
    }

    @Override
    public void onStart() {
        synchronized (catcherLock) {
            catcherState = CatcherState.STARTED;
            catcherLock.notifyAll();
        }
    }

    @Override
    public void onFinished() {
        synchronized (catcherLock) {
            catcherState = CatcherState.FINISHED;
            catcherLock.notifyAll();
        }
    }

    @Override
    public void onError(Throwable t) {
        errorCount.incrementAndGet();
    }

    @SuppressWarnings("unused")
    String waitForLineContaining(final String line, final long maxWait) throws InterruptedException {
        synchronized (logLines) {
            final long start = System.currentTimeMillis();
            // loop is broken when found
            while (true) {
                for (int i = logLines.size() - 1; i >= 0; i--) {
                    final String current = logLines.get(i);

                    if (current.contains(line)) {
                        logLines.remove(i);
                        return current;
                    }
                }

                if (System.currentTimeMillis() - start > maxWait) return null;
                logLines.wait();
            }
        }
    }
}
