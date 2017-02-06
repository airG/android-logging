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

package com.airg.android.logging.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.airg.android.logging.LogCatcher;
import com.airg.android.logging.Logger;
import com.airg.android.logging.TaggedLogger;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mahram Z. Foadi on 2016-09-21.
 *
 * @author Mahram Z. Foadi
 */

public class CoolActivity
        extends Activity {
    private final TaggedLogger LOG = Logger.tag("COOLACTIVITY");

    @BindView(android.R.id.button1)
    Button button;
    @BindView(R.id.local_capture)
    Button localCapture;
    @BindView(R.id.global_capture)
    Button globalCapture;
    @BindView(R.id.logcat)
    EditText logcat;
    @BindView(R.id.clear_log)
    CheckBox clear;
    @BindView(R.id.line_counter)
    TextView lineCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cool);
        ButterKnife.bind(this);
    }

    private void onTaskFinished() {
        LOG.d("Task finished");
        button.setEnabled(true);
    }

    @OnClick(android.R.id.button1)
    public void onClick() {
        button.setEnabled(false);
        LOG.d("Starting up a new intercooler instance.");
        new Thread(new InterCooler(this, 10000)).start();
    }

    @OnClick(R.id.local_capture)
    public void startLocalCapture() {
        disableLogCapButtons();
        logcat.setText(null);

        new LogCatcher(clear.isChecked(), true).getLogLines(new LogcatListener());
    }

    @OnClick(R.id.global_capture)
    public void startGlobalCapture() {
        disableLogCapButtons();
        logcat.setText(null);

        new LogCatcher(clear.isChecked(), false).getLogLines(new LogcatListener());
    }

    private void enableLogCapButtons() {
        globalCapture.setEnabled(true);
        localCapture.setEnabled(true);
    }

    private void disableLogCapButtons() {
        globalCapture.setEnabled(false);
        localCapture.setEnabled(false);
    }

    private class LogcatListener implements LogCatcher.OnLogLinesListener {

        int lines = 0;

        @Override
        public void onLogLine(final String logLine) {
            lines++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appendLog(logLine);
                }
            });
        }

        private void appendLog(String logLine) {
            logcat.append(logLine + '\n');
            lineCounter.setText(getString(R.string.n_lines, lines));
        }

        @Override
        public void onFinished() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enableLogCapButtons();
                }
            });
        }

        @Override
        public void onError(final Throwable t) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appendLog(t.getMessage());
                }
            });
        }
    }

    private static class InterCooler
            implements Runnable {
        private final long delay;
        private final WeakReference<CoolActivity> hostActivity;

        private InterCooler(final CoolActivity host, final long d) {
            delay = d;
            hostActivity = new WeakReference<>(host);
        }

        @Override
        public void run() {
            Logger.i("INTERCOOLER", "Napping for %dms", delay);
            try {
                Thread.sleep(delay);
                Logger.i("INTERCOOLER", "Slept for %dms", delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.i("INTERCOOLER", "How dare you interrupt %s?", getClass().getSimpleName());
            }

            final CoolActivity activity = hostActivity.get();
            if (null == activity) {
                Logger.w("INTERCOOLER", "Activity is gone. Not notifying.");
                return;
            }

            Logger.w("INTERCOOLER", "Notifying Activity.");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.onTaskFinished();
                }
            });
        }
    }
}
