package com.prey.sim;

 
import android.content.Context;
 
import android.telephony.PhoneNumberUtils;

import com.prey.PreyConfig;
import com.prey.PreyController;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.sms.SMSSupport;
import com.prey.backwardcompatibility.CupcakeSupport;
import com.prey.exceptions.SMSNotSendException;
import com.prey.json.UtilJson;
import com.prey.managers.PreyTelephonyManager;
import com.prey.net.PreyWebServices;
import com.prey.services.PreyBootService;

public class SIMCheckingThread implements Runnable {

	private Context ctx = null;
	private PreyBootService preyBootService;

	public SIMCheckingThread(Context ctx, PreyBootService preyBootService) {
		this.ctx = ctx;
		this.preyBootService = preyBootService;
	}

	public void run() {
		execute();
	}
	public void execute() {
		
		

		PreyLogger.d("SIM checking thread has started");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		preyConfig.registerC2dm();
		if (preyConfig.isThisDeviceAlreadyRegisteredWithPrey()) {
			try {
				boolean isSIMReady = false;
				int countSIMReady=12;
				final PreyTelephonyManager telephony = PreyTelephonyManager.getInstance(ctx);
				if (!telephony.isSimStateAbsent()) {
					while (!isSIMReady) {
						isSIMReady = telephony.isSimStateReady();
						if (isSIMReady) {
							PreyLogger.d("SIM is ready to be checked now. Checking...");
							if (preyConfig.isSimChanged()) {
								PreyLogger.d("Starting prey right now since SIM was replaced!");
								String destSMS = preyConfig.getDestinationSmsNumber();
								if (PhoneNumberUtils.isWellFormedSmsAddress(destSMS)) {
									String mail = PreyConfig.getPreyConfig(ctx).getEmail();
									String message = ctx.getString(R.string.sms_to_send_text, mail);
									if (PreyConfig.getPreyConfig(ctx).isCupcake()){
										CupcakeSupport.sendSMS(destSMS, message);
										PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "sim_send_sms", "started"));
									}else
										try {
											SMSSupport.sendSMS(destSMS, message);
											PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","sim_send_sms", "started"));
										} catch (SMSNotSendException e) {
											PreyLogger.i("There was an error sending the SIM replaced SMS alert");
										}

								}
								PreyController.startPrey(ctx);
							}
						} else {
							PreyLogger.d("SIM not ready. Waiting 5 secs before check again.");
							Thread.sleep(5000);
						}
						if (countSIMReady>5){
							break;
						}
						countSIMReady++;
					}
				} else
					PreyLogger.d("SIM absent. Can't check if changed");

			} catch (InterruptedException e) {
				PreyLogger.e("Can't wait for SIM Ready state. Cancelling SIM Change check", e);
			}
			
			// if (preyConfig.showLockScreen())
			// PreyBootService.this.startService(new
			// Intent(PreyBootService.this, LockMonitorService.class));
			if (preyBootService!=null)
				preyBootService.stopSelf();
		}
	}
}
