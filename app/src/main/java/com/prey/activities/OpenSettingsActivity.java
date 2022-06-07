/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2022 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

import com.prey.R;
import com.prey.events.factories.EventFactory;

public class OpenSettingsActivity extends Activity {

    /**
     * Activity that checks if it should hide the notification or
     * should open the settings to grant permissions
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash_batch);
        boolean verifyNotification = EventFactory.verifyNotification(this);
        if (verifyNotification) {
            NotificationManager manager = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);
            manager.cancel(EventFactory.NOTIFICATION_ID);
        } else {
            Intent intentSetting = new Intent();
            intentSetting.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intentSetting.setData(uri);
            intentSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(intentSetting);
        }
        finish();
    }

}
