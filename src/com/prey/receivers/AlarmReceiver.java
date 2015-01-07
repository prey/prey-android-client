package com.prey.receivers;

import com.prey.actions.report.ReportService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    	Intent intent2 = new Intent(context, ReportService.class);
    	context.startService(intent2);
    }

}