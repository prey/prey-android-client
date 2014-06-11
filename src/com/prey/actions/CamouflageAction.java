package com.prey.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.camouflage.Camouflage;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.exceptions.PreyException;

public class CamouflageAction extends PreyAction {

	public static final String DATA_ID = "camouflage";
	public final String ID = "camouflage";

	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		return "";
	}

	@Override
	public boolean shouldNotify() {
		return false;
	}

	@Override
	public void execute(ActionJob actionJob, Context ctx) throws PreyException {
		PreyLogger.i("Ejecuting CamouflageAction Action");
		List<ActionResult> lista=null; 
		JSONObject parameters=null;
		Camouflage.hide(ctx, lista, parameters);
		PreyLogger.i("Ejecuting CamouflageAction Action[Finish]");
	}
	
	
	@Override
	public boolean isSyncAction() {
		return false;
	}

	@Override
	public int getPriority() {
		return CAMOUFLAGE_PRIORITY;
	}

 

}
