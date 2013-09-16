/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.prey.PreyLogger;

import com.prey.sim.SIMCheckingThread;
import com.prey.twilio.TwilioRunner;

public class PreyBootService extends Service {

	// This is the object that receives interactions from clients
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		PreyBootService getService() {
			return PreyBootService.this;
		}
	}

	@Override
	public void onCreate() {
		PreyLogger.d("Prey Boot Service Started!");
		SIMCheckingThread simChecking= new SIMCheckingThread(getApplicationContext(),this);
		new Thread(simChecking).start();
		TwilioRunner twilio=new TwilioRunner(getApplicationContext());
		new Thread(twilio).start();
	}


	@Override
	public void onDestroy() {
		// locationListenerThread.stop();
		PreyLogger.d("Boot Service has been stopped");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	/*
	class SIMCheckingThread implements  Runnable {
		
		public void run() {
			PreyLogger.d("SIM checking thread has started");
			PreyConfig preyConfig = PreyConfig.getPreyConfig(PreyBootService.this);
			if (preyConfig.isThisDeviceAlreadyRegisteredWithPrey(false)){
				try {
					boolean isSIMReady = false;
					final PreyTelephonyManager telephony= PreyTelephonyManager.getInstance(PreyBootService.this); 
					if (!telephony.isSimStateAbsent()){
						while (!isSIMReady) {
							isSIMReady = telephony.isSimStateReady();
							if (isSIMReady) {
								PreyLogger.d("SIM is ready to be checked now. Checking...");
								if (preyConfig.isSimChanged()) {
									PreyLogger.d("Starting prey right now since SIM was replaced!");
									String destSMS = preyConfig.getDestinationSmsNumber();
									if (PhoneNumberUtils.isWellFormedSmsAddress(destSMS)) {
										String mail = PreyConfig.getPreyConfig(PreyBootService.this).getEmail();
										String message = getString(R.string.sms_to_send_text, mail);
										if (PreyConfig.getPreyConfig(PreyBootService.this).isCupcake())
											CupcakeSupport.sendSMS(destSMS,message);
										else
											try {
												SMSSupport.sendSMS(destSMS,message);
											} catch (SMSNotSendException e) {
												PreyLogger.i("There was an error sending the SIM replaced SMS alert");
											}
										
									}
									PreyController.startPrey(getApplicationContext());
								}
							} else {
								PreyLogger.d("SIM not ready. Waiting 5 secs before check again.");
								Thread.sleep(5000);
							}
						}
					} else
						PreyLogger.d("SIM absent. Can't check if changed");
	
				} catch (InterruptedException e) {
					PreyLogger.e("Can't wait for SIM Ready state. Cancelling SIM Change check", e);
				}
				preyConfig.registerC2dm();
	//			if (preyConfig.showLockScreen())
	//				PreyBootService.this.startService(new Intent(PreyBootService.this, LockMonitorService.class));
				stopSelf();
			}
		}
	}
	 */
	
}

