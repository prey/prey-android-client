package com.prey.managers;

import android.content.Context;
import android.net.ConnectivityManager;

public class PreyConnectivityManager {

	private ConnectivityManager connectivity = null;
	private static PreyConnectivityManager _instance = null;

	private PreyConnectivityManager(Context ctx) {
		connectivity = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public static PreyConnectivityManager getInstance(Context ctx) {
		if (_instance == null)
			_instance = new PreyConnectivityManager(ctx);
		return _instance;
	}

	public boolean isConnected() {

		if (connectivity.getActiveNetworkInfo() != null)
			return connectivity.getActiveNetworkInfo().isConnected();
		return false;
	}

	public boolean isAvailable() {
		if (connectivity.getActiveNetworkInfo() != null)
			return connectivity.getActiveNetworkInfo().isAvailable();
		return false;
	}

	public boolean isConnectedOrConnecting() {
		if (connectivity.getActiveNetworkInfo() != null)
			return connectivity.getActiveNetworkInfo().isConnectedOrConnecting();
		return false;
	}

	public boolean isFailover() {
		if (connectivity.getActiveNetworkInfo() != null)
			return connectivity.getActiveNetworkInfo().isFailover();
		return false;
	}

	public boolean isRoaming() {
		if (connectivity.getActiveNetworkInfo() != null)
			return connectivity.getActiveNetworkInfo().isRoaming();
		return false;
	}
}
