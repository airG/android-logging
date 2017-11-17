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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Allows for capturing the android system log.
 * <p>
 * <b>Notes:</b>
 * <ul>
 * <li>This is an experimental class and most definitely full of bugs. Approach with caution!</li>
 * <li>If you have any services in your android manifest that run in their own process, filtering the log by pid will only capture the log output from the process that instantiates the <code>LogCatcher</code> instance. If you need to capture the output from <i>all</i> your processes, you will have to instantiate one LogCatcher instance per process and capture the logs separately.</li>
 * <li>The capture and clear tasks run asynchronously (as you can tell from the callbacks). There is a noticeable delay between when you write to log and when it actually appears in the log. If certain log entries <i>MUST</i> be captured, you'd want to hold off on ending a capture session until everything has been 'seen' in {@link OnLogLinesListener#onLogLine(String)}. If it's not there, it's not captured yet.</li>
 * </ul>
 * <p>
 *
 * @see <a href="https://developer.android.com/studio/command-line/logcat.html">https://developer.android.com/studio/command-line/logcat.html</a>.
 */
@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public final class LogCatcher {

    private static final String LOGCAT_EXEC = "logcat";
    private static final String ARG_DUMP = "-d";
    private static final String ARG_CLEAR = "-c";
    private static final String ARG_PID = "--pid";

    private static final int PID_NONE = -1;

    private final Executor taskExecutor;
    private final Executor callbackExecutor;
    private final int pid;

    private final Object lock = new Object();
    private volatile boolean clearing = false;
    private volatile boolean capturing = false;

    private volatile Process captureProcess = null;
    private volatile LogReader captureTask = null;

    /**
     * Constructor. Allows the caller to specify whether to clear the log, the process id, and an executor for background tasks.
     *
     * @param clear     specify <code>true</code> to clear the log, or <code>false</code> to leave the log untouched.
     * @param processId Specify a process id on which to filter the log.
     * @param executor  An {@link Executor} on which to execute background tasks on. If <code>null</code> is provided, the default background executor is obtained via {@link Executors#newSingleThreadExecutor()}
     */
    public LogCatcher(final boolean clear, final int processId, @Nullable final Executor executor) {
        pid = processId;
        taskExecutor = Executors.newFixedThreadPool(2);
        callbackExecutor = null == executor
                ? Executors.newSingleThreadExecutor()
                : executor;

        if (clear)
            clear();
    }

    /**
     * Constructor. Allows the caller to specify whether to clear the log, collect only self logs, and an executor for background tasks.
     *
     * @param clear    specify <code>true</code> to clear the log, or <code>false</code> to leave the log untouched.
     * @param self     specify <code>true</code> to capture only the log lines from your own application, or <code>false</code> to capture the entire log.
     * @param executor An {@link Executor} on which to execute background tasks on
     */
    public LogCatcher(final boolean clear, final boolean self, final Executor executor) {
        this(clear, self ? android.os.Process.myPid() : PID_NONE, executor);
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
     *
     * @param clear specify <code>true</code> to clear the log, or <code>false</code> to leave the log untouched.
     */
    public LogCatcher(final boolean clear) {
        this(clear, false, null);
    }

    /**
     * Capture the current log lines. This method produces the equivalent of <code>adb logcat -d</code>
     *
     * @param listener a listener to receive log lines on the same thread that is executing the capture (e.g. your provided executor. If you didn't provide one, this is a background thread).
     */
    public void dump(final OnLogLinesListener listener) {
        synchronized (lock) {
            if (capturing)
                throw new IllegalStateException("Capture already in progress");

            DEBUG("dump: waiting for clear to finish");
            waitForClearEnd();

            DEBUG("dump: dumping...");
            capturing = true;
            taskExecutor.execute(new LogReader(listener, true));
        }
    }

    /**
     * Starts to capture the log lines until {@link #endCapture()} is called.
     *
     * @param listener A listener to receive log lines as they are logged.
     */
    public void startCapture(final OnLogLinesListener listener) {
        synchronized (lock) {
            if (capturing)
                throw new IllegalStateException("Capture already in progress");

            waitForClearEnd();
            capturing = true;

            DEBUG("capture: capturing...");
            captureTask = new LogReader(listener, false);
            taskExecutor.execute(captureTask);
        }
    }

    /**
     * Stop capturing log output
     */
    public void endCapture() {
        synchronized (lock) {
            if (null == captureTask)
                throw new IllegalStateException("Not capturing");

            DEBUG("capture: stopping...");
            captureTask.stop();
        }
    }

    /**
     * Best effort log eraser. Blocks calling thread. No instance needed.
     */
    public static void clearLog() {
        DEBUG("clear");
        new LogEraser().run();
    }

    /**
     * Best effort log eraser.
     */
    public void clear() {
        synchronized (lock) {
            if (clearing)
                throw new IllegalStateException("Clear already in progress");

            waitForCaptureEnd();
            clearing = true;

            DEBUG("clear: clearing...");
            taskExecutor.execute(new LogEraser() {
                @Override
                protected void onComplete(int exitCode) {
                    synchronized (lock) {
                        clearing = false;
                        DEBUG("clear: complete");
                        lock.notifyAll();
                    }
                }

                @Override
                protected void onError(Exception e) {
                    synchronized (lock) {
                        clearing = false;
                        DEBUG("clear: failed");
                        lock.notifyAll();
                    }
                }
            });
        }
    }

    void waitForClearEnd() {
        synchronized (lock) {
            DEBUG("waiting for clear to finish");

            while (clearing) {
                try {
                    lock.wait();
                } catch (InterruptedException ignore) {
                    return;
                }
            }
        }
    }

    void waitForCaptureEnd() {
        synchronized (lock) {
            DEBUG("waiting for current capture task to finish");

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

        private final ProxyOnLogLinesListener listener;
        private final boolean dump;

        private final AtomicBoolean stop = new AtomicBoolean(false);

        private LogReader(final OnLogLinesListener logLinesListener, final boolean dumpOnly) {
            listener = new ProxyOnLogLinesListener(callbackExecutor, logLinesListener);
            dump = dumpOnly;
        }

        private LogReader(final OnLogLinesListener logLinesListener) {
            this(logLinesListener, true);
        }

        @Override
        public void run() {
            final List<String> commandline = new ArrayList<>();
            commandline.add(LOGCAT_EXEC);

            if (pid != PID_NONE)
                Collections.addAll(commandline, ARG_PID, String.valueOf(pid));

            if (dump)
                commandline.add(ARG_DUMP);

            DEBUG("reader: starting logcat process with params: %s", commandline.toString());

            try {
                captureProcess = new ProcessBuilder(commandline).start();
                DEBUG("reader: Started logcat.");

                listener.onStart();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final BufferedReader logcat = new BufferedReader(new InputStreamReader(captureProcess.getInputStream()));

                            String line;

                            while ((line = logcat.readLine()) != null) {

                                final String finalLine = line;
                                callbackExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onLogLine(finalLine);
                                    }
                                });

                                if (stop.get()) {
                                    DEBUG("reader: Stop requested. killing logcat.");
                                    captureProcess.destroy();
                                    break;
                                }
                            }

                            DEBUG("reader: No more lines.");
                            logcat.close();
                        } catch (final IOException e) {
                            callbackExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onError(e);
                                }
                            });
                        }

                        callbackExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFinished();
                            }
                        });
                    }
                }).start();

                final int exitCode = captureProcess.waitFor();
                DEBUG("reader: logcat process finished with %d", exitCode);
            } catch (Exception e) {
                if (Thread.interrupted()) {
                    captureProcess.destroy();
                    listener.onFinished();
                } else listener.onError(e);
            } finally {
                synchronized (lock) {
                    captureProcess = null;
                    capturing = false;
                    DEBUG("reader: complete");
                    lock.notifyAll();
                }
            }
        }

        public void stop() {
            synchronized (lock) {
                stop.set(true);
            }
        }
    }

    private static class LogEraser implements Runnable {
        @Override
        public final void run() {
            try {
                DEBUG("eraser: Started '%s %s'", LOGCAT_EXEC, ARG_CLEAR);
                onComplete(new ProcessBuilder().command(LOGCAT_EXEC, ARG_CLEAR).start().waitFor());
                DEBUG("eraser: '%s %s' finished", LOGCAT_EXEC, ARG_CLEAR);
            } catch (Exception e) {
                e.printStackTrace();
                // oh well
                onError(e);
            }
        }

        protected void onError(final Exception e) {
            // nothing by default
        }

        protected void onComplete(final int exitCode) {
            // nothing by default
        }
    }

    /**
     * receive log lines and updates on the capture state
     */
    public interface OnLogLinesListener {
        /**
         * A new line was read from the log
         *
         * @param logLine log line
         */
        void onLogLine(final String logLine);

        /**
         * Capture started (vie either {@link LogCatcher#dump(OnLogLinesListener)} or {@link LogCatcher#startCapture(OnLogLinesListener)}). This method indicates that the <code>logcat</code> process was successfully executed and the <code>LogCatcher</code> instance is now reading from the log stream.
         */
        void onStart();

        /**
         * Capture finished.
         * <ul>
         * <li>If the capture was started via {@link LogCatcher#dump(OnLogLinesListener)}, this method indicates that the end of stream was reached.</li>
         * <li>If the capture was started via {@link LogCatcher#startCapture(OnLogLinesListener)}, this method indicates that {@link LogCatcher#endCapture()} was called.</li>
         * </ul>
         */
        void onFinished();

        /**
         * Error encountered
         *
         * @param t caught error either during the execution of the <code>logcat</code> command or while trying to read from the log stream.
         */
        void onError(final Throwable t);
    }

    private static class ProxyOnLogLinesListener implements OnLogLinesListener {
        private final Executor executor;
        private final OnLogLinesListener delegate;

        private ProxyOnLogLinesListener(final Executor e, final OnLogLinesListener d) {
            executor = e;
            delegate = d;
        }

        @Override
        public void onLogLine(final String logLine) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    delegate.onLogLine(logLine);
                }
            });
        }

        @Override
        public void onStart() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    delegate.onStart();
                }
            });
        }

        @Override
        public void onFinished() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    delegate.onFinished();
                }
            });
        }

        @Override
        public void onError(final Throwable t) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    delegate.onError(t);
                }
            });
        }
    }

    private static void DEBUG(final String fmt, final Object... args) {
        if (!BuildConfig.DEBUG) return;
        Logger.d("LOG:CATCHER", fmt, args);
    }
}
