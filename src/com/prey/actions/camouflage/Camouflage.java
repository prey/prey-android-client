package com.prey.actions.camouflage;

import java.util.List;

import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class Camouflage {

	public static void hide(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		PreyLogger.i("hide start");
		PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "camouflage", "started"));
        PreyConfig.getPreyConfig(ctx).setCamouflageSet(true);

        ComponentName componentToDisabled = new ComponentName("com.prey", "com.prey.activities.LoginActivity");
        PackageManager pm = ctx.getPackageManager();
        pm.setComponentEnabledSetting(componentToDisabled, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    	PreyLogger.i("hide stop");
	}

	public static void unhide(Context ctx, List<ActionResult> lista, JSONObject options) {
		PreyLogger.i("unhide start");
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "camouflage", "stopped"));
        PreyConfig.getPreyConfig(ctx).setCamouflageSet(false);

        ComponentName componentToEnabled = new ComponentName("com.prey", "com.prey.activities.LoginActivity");
        PackageManager pm = ctx.getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(componentToEnabled, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    	PreyLogger.i("unhide stop");
	}
}
