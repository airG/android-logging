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
import android.view.View;
import android.widget.Button;

import com.airg.android.logging.Logger;
import com.airg.android.logging.TaggedLogger;

import java.lang.ref.WeakReference;

/**
 Created by Mahram Z. Foadi on 2016-09-21.

 @author Mahram Z. Foadi */

public class CoolActivity
  extends Activity
  implements View.OnClickListener {
    private final TaggedLogger LOG = Logger.tag ("COOLACTIVITY");
    private Button button;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_cool);

        button = (Button) findViewById (android.R.id.button1);
        button.setOnClickListener (this);
    }

    private void onTaskFinished () {
        LOG.d ("Task finished");
        button.setEnabled (true);
    }

    @Override
    public void onClick (View v) {
        button.setEnabled (false);
        LOG.d ("Starting up a new intercooler instance.");
        new Thread (new InterCooler (this, 10000)).start ();
    }

    private static class InterCooler
      implements Runnable {
        private final long                        delay;
        private final WeakReference<CoolActivity> hostActivity;

        private InterCooler (final CoolActivity host, final long d) {
            delay = d;
            hostActivity = new WeakReference<> (host);
        }

        @Override
        public void run () {
            Logger.i ("INTERCOOLER", "Napping for %dms", delay);
            try {
                Thread.sleep (delay);
                Logger.i ("INTERCOOLER", "Slept for %dms", delay);
            } catch (InterruptedException e) {
                e.printStackTrace ();
                Logger.i ("INTERCOOLER", "How dare you interrupt %s?", getClass ().getSimpleName ());
            }

            final CoolActivity activity = hostActivity.get ();
            if (null == activity) {
                Logger.w ("INTERCOOLER", "Activity is gone. Not notifying.");
                return;
            }

            Logger.w ("INTERCOOLER", "Notifying Activity.");
            activity.runOnUiThread (new Runnable () {
                @Override
                public void run () {
                    activity.onTaskFinished ();
                }
            });
        }
    }
}
