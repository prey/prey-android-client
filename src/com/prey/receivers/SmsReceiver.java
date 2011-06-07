package com.prey.receivers;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.prey.PreyConfig;
import com.prey.PreyController;
import com.prey.PreyLogger;
import com.prey.backwardcompatibility.CupcakeSupport;
import com.prey.sms.SMSSupport;

public class SmsReceiver extends BroadcastReceiver {

	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// PreyLogger.d("SMS Broadcast - Action received: " +
		// intent.getAction());
		if (intent.getAction() != null && intent.getAction().equals(ACTION)) {
			// ---get the SMS message passed in---
			Bundle bundle = intent.getExtras();

			// String str = "";
			if (bundle != null) {
				// ---retrieve the SMS message received---
				Object[] pdus = (Object[]) bundle.get("pdus");
				ArrayList<String> smsMessages = null;
				if (PreyConfig.getPreyConfig(context).isCupcake())
					smsMessages = CupcakeSupport.getSMSMessage(pdus);
				else
					smsMessages = SMSSupport.getSMSMessage(pdus);
				for (String sms : smsMessages) {
					executeActionsBasedOnSMSMessage(context, sms);
				}
				
			}
		}
	}

	private void executeActionsBasedOnSMSMessage(Context ctx, String SMSMessage) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		PreyLogger.i("SMS received: " + SMSMessage);
		boolean shouldPerform = SMSMessage.indexOf(preyConfig.getSmsToRun()) >= 0;
		boolean shouldStop = SMSMessage.indexOf(preyConfig.getSmsToStop()) >= 0;
		if (shouldPerform) {
			PreyLogger.i("SMS Match!, waking up Prey right now!");
			PreyController.startPrey(ctx);
		} else if (shouldStop) {
			PreyLogger.i("SMS Match!, stopping Prey!");
			PreyController.stopPrey(ctx);
		}
	}

}
