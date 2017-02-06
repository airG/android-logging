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

import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Allows for capturing the android system log.
 * <p>
 * <b>Note:</b> If you have any services in your android manifest that run in their own process, filtering the log by pid will only capture the log output from the process that instantiates the <code>LogCatcher</code> instance. If you need to capture the output from <i>all</i> your processes, you will have to instantiate one LogCatcher instance per process and capture the logs separately.
 * <p>
 * Full <code>logcat</code> reference: https://developer.android.com/studio/command-line/logcat.html
 */
@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public final class LogCatcher {

    private static final String LOGCAT = "logcat";
    private static final String ARG_DUMP = "-d";
    private static final String ARG_CLEAR = "-c";
    private static final String ARG_PID = "--pid";

    private static final int PID_NONE = -1;

    private final Executor executor;
    private final int pid;

    private final Object lock = new Object();
    private volatile boolean clearing = false;
    private volatile boolean capturing = false;

    /**
     * Constructor. Allows the caller to specify whether to clear the log, the process id, and an executor for background tasks.
     *
     * @param clear     specify <code>true</code> to clear the log, or <code>false</code> to leave the log untouched.
     * @param processId Specify a process id on which to filter the log.
     * @param ex        An executor on which to execute background tasks on
     */
    public LogCatcher(final boolean clear, final int processId, @Nullable final Executor ex) {
        pid = processId;

        executor = null == ex
                ? Executors.newSingleThreadExecutor()
                : ex;

        if (clear)
            clearLog();
    }

    /**
     * Constructor. Allows the caller to specify whether to clear the log, collect only self logs, and an executor for background tasks.
     *
     * @param clear specify <code>true</code> to clear the log, or <code>false</code> to leave the log untouched.
     * @param self  specify <code>true</code> to capture only the log lines from your own application, or <code>false</code> to capture the entire log.
     * @param ex    An executor on which to execute background tasks on
     */
    public LogCatcher(final boolean clear, final boolean self, final Executor ex) {
        this(clear, self ? android.os.Process.myPid() : PID_NONE, ex);
    }

    /**
     * Constructor. Allows the caller to specify whether to clear the log and whether to collect global or only self logs.
     *
     * @param clear specify <code>true</code> to clear the log, or <code>false</code> to leave the log untouched.
     * @param self  specify <code>true</code> to capture only the log lines from your own application, or <code>false</code> to capture the entire log.
     */
    public LogCatcher(final boolean clear, final boolean self) {
        this(clear, self, null);
    }

    /**
     * Default constructor. Will not clear the log, captures globally, and runs tasks on a default background executor.
     */
    public LogCatcher() {
        this(false, false, null);
    }

    /**
     * Constructor. Allows the caller to choose whether or not to clear the log, captures globally, and runs tasks on a default background executor.
     */
    public LogCatcher(final boolean clear) {
        this(clear, false, null);
    }

    /**
     * Capture the current log lines. This method produces the equivalent of <code>adb logcat -d</code>
     *
     * @param listener a listener to receive log lines on the same thread that is executing the capture (e.g. your provided executor. If you didn't provide one, this is a background thread).
     */
    public void getLogLines(final OnLogLinesListener listener) {
        synchronized (lock) {
            if (capturing)
                throw new IllegalStateException("Capture already in progress");

            waitForClearEnd();
            capturing = true;
        }

        executor.execute(new LogReader(listener));
    }

    /**
     * Best effort log eraser.
     */
    public void clearLog() {
        synchronized (lock) {
            if (clearing)
                throw new IllegalStateException("Clear already in progress");

            waitForCaptureEnd();
            clearing = true;
        }

        executor.execute(new LogEraser());
    }

    private void waitForClearEnd() {
        synchronized (lock) {
            while (clearing) {
                try {
                    lock.wait();
                } catch (InterruptedException ignore) {
                    return;
                }
            }
        }
    }

    private void waitForCaptureEnd() {
        synchronized (lock) {
            while (capturing) {
                try {
                    lock.wait();
                } catch (InterruptedException ignore) {
                    return;
                }
            }
        }
    }

    private class LogReader implements Runnable {

        final OnLogLinesListener listener;

        private LogReader(final OnLogLinesListener logLinesListener) {
            listener = logLinesListener;
        }

        @Override
        public void run() {
            final String[] commandline = pid == PID_NONE
                    ? new String[]{LOGCAT, ARG_DUMP}
                    : new String[]{LOGCAT, ARG_DUMP, ARG_PID, String.valueOf(pid)};

            try {
                final BufferedReader logcat = new BufferedReader(
                        new InputStreamReader(
                                new ProcessBuilder(commandline).start().getInputStream()
                        ));

                String line;

                while ((line = logcat.readLine()) != null)
                    listener.onLogLine(line);

                logcat.close();
                listener.onFinished();
            } catch (Exception e) {
                listener.onError(e);
            }
        }
    }

    private class LogEraser implements Runnable {
        @Override
        public void run() {
            try {
                new ProcessBuilder().command(LOGCAT, ARG_CLEAR).start().waitFor();
            } catch (Exception ignore) {
                // oh well
            } finally {
                synchronized (lock) {
                    clearing = false;
                    lock.notifyAll();
                }
            }
        }
    }

    public interface OnLogLinesListener {
        void onLogLine(final String logLine);

        void onFinished();

        void onError(final Throwable t);
    }
}
