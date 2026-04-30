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

    private Dialog popup;

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
        popup = alertBuilder.create();
        popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        popup.show();
        try {
            registerReceiver(close_prey_receiver, new IntentFilter(CheckPasswordHtmlActivity.CLOSE_PREY));
            registerReceiver(popup_prey_receiver, new IntentFilter(POPUP_PREY + "_" + notificationId));
        } catch (Exception e) {
            PreyLogger.d(String.format("Error receiver:%s", e.getMessage()));
        }
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

    @Override
    protected void onDestroy() {
        // Dismiss the dialog before the Activity window is torn down. Without
        // this, the receiver-driven finish() path destroys the Activity while
        // the dialog is still attached, and Android logs WindowLeaked. The
        // dismiss listener (which calls finish()) is unhooked first to avoid
        // bouncing through finish() while we're already on the destroy path.
        if (popup != null) {
            popup.setOnDismissListener(null);
            if (popup.isShowing()) {
                try {
                    popup.dismiss();
                } catch (IllegalArgumentException ignored) {
                    // Window already detached — nothing to clean up.
                }
            }
            popup = null;
        }
        // Each unregister is wrapped independently: the matching registerReceiver
        // calls in onCreate are guarded with their own try/catch, so one may have
        // succeeded while the other failed. Sharing a try would let the first
        // failure short-circuit the second unregister and leak that receiver.
        try {
            unregisterReceiver(close_prey_receiver);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            unregisterReceiver(popup_prey_receiver);
        } catch (IllegalArgumentException ignored) {
        }
        super.onDestroy();
    }

}