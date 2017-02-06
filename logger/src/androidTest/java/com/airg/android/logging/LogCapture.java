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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mahramf.
 */

final class LogCapture implements Closeable {

    private AtomicBoolean clearInProgress = new AtomicBoolean(false);

    LogCapture() {
        this(false);
    }

    LogCapture(final boolean clear) {
        if (clear)
            clearLog();

        startCapture ();
    }

    private List<String> getLog() {
        try {
            logger = new ProcessBuilder().command("logcat").start();
        } catch (IOException ignore) {
            // oh well
        }
    }

    private void clearLog() {
        if (!clearInProgress.compareAndSet(false, true))
            throw IllegalStateException ("Clear already in progress");

        try {
            new ProcessBuilder().command("logcat", "-c").start().waitFor();
        } catch (IOException ignore) {
            // oh well
        }
    }

    @Override
    public void close() throws IOException {

    }
}
