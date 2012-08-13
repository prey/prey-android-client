/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionsController;
import com.prey.actions.parser.ResponseParser;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;
import com.prey.services.PreyRunnerService;

public class ActionsRunnner {

	protected boolean running = false;

	Thread myActionsRunnerThread = null;

	public ActionsRunnner() {

	}

	public void run(Context ctx) {
		this.myActionsRunnerThread = new Thread(new ActionsRunner(ctx));
		this.myActionsRunnerThread.start();

	}

	class ActionsRunner implements Runnable {
		private ReportActionResponse preyControlStatus;
		private Context ctx;
		private PreyConfig preyConfig = null;

		public ActionsRunner(Context context) {
			this.ctx = context;
		}

		public void run() {
			preyConfig = PreyConfig.getPreyConfig(ctx);
			if (preyConfig.isThisDeviceAlreadyRegisteredWithPrey(true)){
				PreyExecutionWaitNotify waitNotify = new PreyExecutionWaitNotify();
				
				if (preyConfig.isRunOnce()){
					try {
						preyConfig.setRunOnce(false);
						preyConfig.setMissing(true);
						PreyWebServices.getInstance().setMissing(ctx, true);
						boolean isMissing = getInstructionsAndRun(waitNotify, true);
						PreyLogger.d("Prey is set to run once. Waiting for the report to be sent (if any), then finishing");
						if (isMissing) //Have to wait for the report being sent.
							waitNotify.doWait();
					} catch (PreyException e) {
						PreyLogger.e("Error while running once: ",e);
					}
				} 
				else {
					boolean isMissing = true;
					preyConfig.setMissing(isMissing);
					PreyWebServices.getInstance().setMissing(ctx, isMissing);
					while (isMissing) {
						try {
							isMissing = getInstructionsAndRun(waitNotify, false);
							preyConfig.setMissing(isMissing);
							if (isMissing){
								PreyRunnerService.interval = preyControlStatus.getDelay();
								PreyRunnerService.pausedAt = System.currentTimeMillis();
								PreyLogger.d( "Now waiting [" + preyControlStatus.getDelay() + "] minutes before next execution");
								Thread.sleep(preyControlStatus.getDelay() * PreyConfig.DELAY_MULTIPLIER);
							} else
								PreyLogger.d( "!! Device not marked as missing anymore. Stopping interval execution.");
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						} catch (PreyException e) {
							//PreyWebServices.getInstance().setMissing(ctx, false);
							//preyConfig.setMissing(false);
							//break;
							PreyLogger.e("Error while running on interval: ",e);
						}
					}
					
				}
			
				ctx.stopService(new Intent(ctx, LocationService.class));
				ctx.stopService(new Intent(ctx, PreyRunnerService.class));
				//PreyConfig.getPreyConfig(ctx).setShowLockScreen(false);
				PreyLogger.d("Prey execution has finished!!");
			}
		}
		
		private boolean getInstructionsAndRun(PreyExecutionWaitNotify waitNotify, boolean runIfNotMissing) throws PreyException{
			ArrayList<PreyAction> actions = null;
			String actionsToExecute = null;
			try {
				actionsToExecute = PreyWebServices.getInstance().getActionsToPerform(ctx);
				preyControlStatus = ResponseParser.parseResponse(actionsToExecute);
				boolean isMissing = preyControlStatus.isMissing();
				PreyConfig.getPreyConfig(ctx).setMissing(isMissing);
				if (runIfNotMissing || (!runIfNotMissing && isMissing)){
					actions = PreyAction.getActionsFromPreyControlStatus(preyControlStatus);
					preyConfig.unlockIfLockActionIsntEnabled(actions);
					ActionsController.getInstance(ctx).stopUnselectedModules(actions);
					ActionsController.getInstance(ctx).runActionGroup(actions,waitNotify,isMissing);
				}
				return isMissing;
			} catch (PreyException e) {
				PreyLogger.e("Exception getting device's xml instruction set", e);
				throw e;
			}			
		}
		
		/*
		private void notifyUser(PreyAction actionExecuted) {
			String notificationTitle = this.ctx.getText(R.string.notification_title).toString();
			NotificationManager nm = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.prey_status_bar_icon, notificationTitle, System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;

			// In this version we redirect user to prey application
			// ------------
			// Set the info for the views that show in the notification panel.
			// String url =
			// PreyWebServices.getInstance().getDeviceWebControlPanelUrl(this.ctx);
			// Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
			// notificationIntent.setData(Uri.parse(url));

			Intent preyMainActivity = new Intent(this.ctx, WelcomeActivity.class);

			String notificationToShow = actionExecuted.textToNotifyUserOnEachReport(ctx);
			PendingIntent contentIntent = PendingIntent.getActivity(this.ctx, 0, preyMainActivity, 0);
			notification.contentIntent = contentIntent;
			notification.setLatestEventInfo(this.ctx, ctx.getText(R.string.notification_title), notificationToShow, contentIntent);

			// Send the notification.
			// We use a layout id because it is a unique number. We use it later
			// to cancel.
			nm.notify(R.string.preyForAndroid_name, notification);

		}
		*/

	}

}
