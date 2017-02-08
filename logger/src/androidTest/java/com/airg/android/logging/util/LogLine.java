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

package com.airg.android.logging.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

/**
 * Created by mahramf.
 */

public final class LogLine {
    private static final int GROUP_LEVEL = 1;
    private static final int GROUP_TAG = 2;
    private static final int GROUP_MSG = 3;

    public final String tag;
    public final String message;
    private final String level;

    public LogLine(final String line, final String expectedTag) {
        final Matcher matcher = Pattern.compile("^.*\\s*([DIVEW])\\s*(" + expectedTag + ")\\s*:\\s*(.*)$")
                .matcher(line);

        if (!matcher.matches()) {
            tag = null;
            level = null;
            message = null;
        } else {
            tag = matcher.group(GROUP_TAG).trim();
            level = matcher.group(GROUP_LEVEL).trim();
            message = matcher.group(GROUP_MSG).trim();
        }
    }

    public boolean valid() {
        return null != tag && null != level && null != message;
    }

    public boolean isLevel(final int l) {
        return level.equals(logLovel(l));
    }

    private static String logLovel(final int level) {
        switch (level) {
            case DEBUG:
                return "D";
            case WARN:
                return "W";
            case ERROR:
                return "E";
            case VERBOSE:
                return "V";
            case INFO:
                return "I";
            default:
                return null;
        }
    }
}
