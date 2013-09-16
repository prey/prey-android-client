package com.prey.json.actions;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;

import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.server.PreyHttp;

public class Server extends JsonAction {

	PreyHttp httpServer = null;

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		return null;
	}

	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {

		try {
			
			PreyPhone phone = new PreyPhone(ctx);

			String publicIp = phone.getIPAddress();
			PreyLogger.i("publicIp:"+publicIp);
			
			httpServer = new PreyHttp(8080, Environment.getExternalStorageDirectory());
		} catch (IOException e) {
			PreyLogger.e("causa:"+e.getMessage(),e);
		}

	}

	public void stop(Context ctx, List<ActionResult> lista, JSONObject options) {
		if (httpServer != null) {
			httpServer.stop();
		}
	}
}
