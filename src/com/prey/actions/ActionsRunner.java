package com.prey.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionsController;
import com.prey.exceptions.PreyException;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;
import com.prey.services.PreyRunnerService;

public class ActionsRunner implements Runnable {

	private Context ctx;
	private PreyConfig preyConfig = null;

	public ActionsRunner(Context context) {
		this.ctx = context;
		
	}

	public void run() {
		execute();
	}
	public List<HttpDataService> execute() {
		List<HttpDataService> listData=null;
		
		preyConfig = PreyConfig.getPreyConfig(ctx);
	 	if (preyConfig.isThisDeviceAlreadyRegisteredWithPrey(true)){
	 		PreyTelephonyManager preyTelephony = PreyTelephonyManager.getInstance(ctx);
			PreyConnectivityManager preyConnectivity = PreyConnectivityManager.getInstance(ctx);
	 		boolean connection=false;
	 		try {
	 			while(!connection){
	 				connection= preyTelephony.isDataConnectivityEnabled() || preyConnectivity.isConnected();
	 				if(!connection){
						PreyLogger.d("Phone doesn't have internet connection now. Waiting 10 secs for it");
						Thread.sleep(10000);
					}
	 			}
	 			listData=getInstructionsJsonAndRun(ctx);
			} catch (Exception e) {
				PreyLogger.e("Error, because:"+e.getMessage(),e );
			}
			ctx.stopService(new Intent(ctx, LocationService.class));
			ctx.stopService(new Intent(ctx, PreyRunnerService.class));
			PreyLogger.d("Prey execution has finished!!");
	 	}
	 	return listData;
	}
	
	private List<HttpDataService> getInstructionsJsonAndRun(Context ctx) throws PreyException {
		List<HttpDataService> listData=null;
		List<JSONObject> jsonObject = null;
		try {
			jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(ctx);			
			listData = ActionsController.getInstance(ctx).runActionJson(ctx,jsonObject);			 
		} catch (PreyException e) {
			PreyLogger.e("Exception getting device's xml instruction set", e);
			throw e;
		}
		return listData;
	}
 
	/*
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
	}*/
	
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
 