/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.mdm;

import android.content.Context;

import com.prey.PreyConfig;

public class MdmSetupPrerequisites {
    private static final long POLL_INTERVAL_MS = 500L;
    private static final long DEFAULT_TIMEOUT_MS = 30000L;

    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    public interface Clock {
        long now();
    }

    private final Sleeper sleeper;
    private final Clock clock;

    public MdmSetupPrerequisites() {
        this(Thread::sleep, System::currentTimeMillis);
    }

    MdmSetupPrerequisites(Sleeper sleeper, Clock clock) {
        this.sleeper = sleeper;
        this.clock = clock;
    }

    public boolean waitUntilReady(Context context) {
        return waitUntilReady(context, DEFAULT_TIMEOUT_MS);
    }

    public boolean waitUntilReady(Context context, long timeoutMs) {
        long deadline = clock.now() + timeoutMs;
        while (clock.now() <= deadline) {
            if (isReady(context)) {
                return true;
            }
            try {
                sleeper.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return isReady(context);
    }

    public boolean isReady(Context context) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        String notificationId = preyConfig.getNotificationId();
        return notificationId != null && !notificationId.isEmpty();
    }
}
