/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

public class PopUpAlertActivity extends PreyActivity {

    private static final int SHOW_POPUP = 0;
    private String message = null;
    private int notificationId = 0;

    public static final String POPUP_PREY = "popup_prey";
    private final BroadcastReceiver close_prey_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreyLogger.d("PopUpAlertActivity close_prey_receiver finish");
            finish();
        }
    };

    private final BroadcastReceiver popup_prey_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreyLogger.d("PopUpAlertActivity popup_prey_receiver finish");
            PreyConfig.getPreyConfig(context).setNoficationPopupId(0);
            finish();
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            this.message = bundle.getString("alert_message");
            this.notificationId = bundle.getInt("notificationId");
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PopUpAlertActivity.this);
        alertBuilder.setTitle(R.string.popup_alert_title);
        alertBuilder.setMessage(this.message);
        alertBuilder.setCancelable(true);
        alertBuilder.setNeutralButton(R.string.close_alert, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancel(notificationId);
                finish();
            }
        });
        Dialog popup = alertBuilder.create();
        popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        popup.show();
        registerReceiver(close_prey_receiver, new IntentFilter(CheckPasswordHtmlActivity.CLOSE_PREY));
        registerReceiver(popup_prey_receiver, new IntentFilter(POPUP_PREY + "_" + notificationId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        int noficationPopupId = PreyConfig.getPreyConfig(this).getNoficationPopupId();
        PreyLogger.d("PopUpAlertActivity onResume noficationPopupId:" + noficationPopupId);
        if (noficationPopupId == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }

}

