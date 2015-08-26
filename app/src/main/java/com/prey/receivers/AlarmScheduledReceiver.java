package com.prey.receivers;

/**
 * Created by oso on 25-08-15.
 */
import com.prey.beta.services.PreyBetaRunnerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmScheduledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, PreyBetaRunnerService.class);
        context.startService(intent2);
    }

}
