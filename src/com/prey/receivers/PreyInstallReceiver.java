package com.prey.receivers;

import java.util.Date;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.accounts.UserEmail;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract.CommonDataKinds.Email;

public class PreyInstallReceiver extends BroadcastReceiver {

	private Context ctx;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		this.ctx = ctx;
		 
		try {
			PreyConfig preyConfig=PreyConfig.getPreyConfig(ctx);

			// esta registrado
			if (isThisDeviceAlreadyRegisteredWithPrey()) {
				PreyLogger.i("No hacer nada");

			} else {
				// no tiempo tiempo seteado
				if (hasLastExecution()) {
					// seteo la ultima ejecucion
					setLastExecutionTime();
				} else {
					// a pasado 10 minutos de la ultima ejecucion
					if (lastRunXMinuteAgo()) {
						// envio registro GCM y mail y m
						String notificationId = preyConfig.getNotificationId();
						if (notificationId==null){
							preyConfig.registerC2dm();
						}else{
							String email = UserEmail.getEmail(ctx);
							int statusCode=PreyWebServices.getInstance().registerNewDeviceNotificationId(ctx, notificationId, email);
							if (200!=statusCode){
								preyConfig.setActiveTour(false);
								preyConfig.setActiveWizard(false);
							}
						}
					}
				}

				PreyLogger.i("User Present new user:" + intent.getAction());
			}
		} catch (Exception e) {

		}
		 
	}

	protected PreyConfig getPreyConfig() {
		return PreyConfig.getPreyConfig(ctx);
	}

	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return getPreyConfig().isThisDeviceAlreadyRegisteredWithPrey(false);
	}

	private boolean hasLastExecution() {
		return getPreyConfig().getLastExecutionTime() == Long.MIN_VALUE;
	}

	private void setLastExecutionTime() {
		getPreyConfig().setLastExecutionTime(new Date().getTime());
	}

	private boolean lastRunXMinuteAgo() {
		long lastExecutionTime = getPreyConfig().getLastExecutionTime();
		long xMinutes = new Date().getTime() - (10 * 60 * 1000);
		long diff = lastExecutionTime - xMinutes;
		PreyLogger.i("lastExecutionTime:" + lastExecutionTime + " xMinutes:" + xMinutes + " diff:" + diff);
	//	return lastExecutionTime > xMinutes;
		
		return true;
	}
}
