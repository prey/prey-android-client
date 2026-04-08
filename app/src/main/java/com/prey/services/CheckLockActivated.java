/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.activities.LockScreenActivity;
import com.prey.json.actions.Lock;

public class CheckLockActivated extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        new Thread() {
            public void run() {
                boolean run = true;
                while (run) {
                    String unlockPass = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
                    if (unlockPass == null || "".equals(unlockPass)) {
                        run = false;
                        stopSelf();
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                        // Re-launch LockScreenActivity if it's not in foreground
                        Intent lockIntent = new Intent(ctx, LockScreenActivity.class);
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ctx.startActivity(lockIntent);
                    } catch (Exception e) {
                        PreyLogger.e("CheckLockActivated Error:" + e.getMessage(), e);
                    }
                }
            }
        }.start();
    }

}