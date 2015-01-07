package com.prey.beta.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionsController;
import com.prey.beta.services.PreyBetaRunnerService;
import com.prey.exceptions.PreyException;
import com.prey.json.parser.JSONParser;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;
import com.prey.managers.PreyWifiManager;
import com.prey.net.NetworkUtils;
import com.prey.net.PreyWebServices;

public class PreyBetaActionsRunner implements Runnable {

	private Context ctx;
	private String body;
	private String version;
	private String cmd;

	private final int MAXIMUM_ATTEMPTS = 5;

	public PreyBetaActionsRunner(Context context, String cmd) {
		this.ctx = context;
		this.body = "";
		this.version = "";
		this.cmd = cmd;
	}

	public PreyBetaActionsRunner(Context context, String body, String version) {
		this.ctx = context;
		this.body = body;
		this.version = version;
	}

	public void run() {
		execute();
	}

	public void execute() {
		boolean wifiOpened = false;
		boolean mobilOpened = false;
		if (PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey(true)) {
			PreyTelephonyManager preyTelephony = PreyTelephonyManager.getInstance(ctx);
			PreyConnectivityManager preyConnectivity = PreyConnectivityManager.getInstance(ctx);
			boolean connection = false;
			try {
				List<JSONObject> jsonObject = null;
				int attempt = 0;
				while (!connection && attempt < MAXIMUM_ATTEMPTS) {
					connection = preyTelephony.isDataConnectivityEnabled() || preyConnectivity.isConnected();
					if (!connection) {
						PreyLogger.d("Phone doesn't have internet connection now. Waiting 5 secs for it");

						if (!PreyWifiManager.getInstance(ctx).isWifiEnabled()) {
							PreyWifiManager.getInstance(ctx).setWifiEnabled(true);
							wifiOpened = true;
						}
						if (!NetworkUtils.getNetworkUtils(ctx).isMobileDataEnabled()) {
							NetworkUtils.getNetworkUtils(ctx).enableMobileData(true);
							mobilOpened = true;
						}

						Thread.sleep(5000);
					}
					attempt = attempt + 1;
				}
				if (connection) {
					try {
						if (cmd == null || "".equals(cmd)) {
							jsonObject = PreyBetaActionsRunner.getInstructions(ctx);
						} else {
							jsonObject = getInstructionsNewThread(ctx, cmd);
						}
					} catch (Exception e) {
					}
					PreyLogger.d("version:" + version + " body:" + body);
					if (jsonObject == null || jsonObject.size() == 0) {
						PreyLogger.d("nothing");
					} else {
						PreyLogger.d("runInstructions");
						runInstructions(jsonObject);
					}
				}
			} catch (Exception e) {
				PreyLogger.e("Error, because:" + e.getMessage(), e);
			}
			PreyLogger.d("Prey execution has finished!!");
			if (wifiOpened) {
				PreyWifiManager.getInstance(ctx).setWifiEnabled(false);
			}
			if (mobilOpened) {
				NetworkUtils.getNetworkUtils(ctx).enableMobileData(false);
			}
		}
		ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
	}

	private static List<JSONObject> getInstructionsNewThread(Context ctx, String cmd) throws PreyException {
		List<JSONObject> jsonObject = new JSONParser().getJSONFromTxt(ctx, "[" + cmd + "]");
		final Context context = ctx;
		new Thread(new Runnable() {
			public void run() {
				try {
					PreyLogger.d("_________New Thread");
					PreyBetaActionsRunner.getInstructions(context);
				} catch (PreyException e) {
				}
			}
		}).start();
		return jsonObject;
	}

	private static List<JSONObject> getInstructions(Context ctx) throws PreyException {
		PreyLogger.d("______________________________");
		PreyLogger.d("_______getInstructions________");
		List<JSONObject> jsonObject = null;
		try {
			jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(ctx);
		} catch (PreyException e) {
			PreyLogger.e("Exception getting device's xml instruction set", e);
			throw e;
		}
		return jsonObject;
	}

	private List<HttpDataService> runInstructions(List<JSONObject> jsonObject) throws PreyException {
		List<HttpDataService> listData = null;
		listData = ActionsController.getInstance(ctx).runActionJson(ctx, jsonObject);
		return listData;
	}

}
