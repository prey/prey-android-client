package com.prey.actions;

/**
 * Created by oso on 24-08-15.
 */

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.camouflage.Camouflage;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.exceptions.PreyException;

public class UnCamouflageAction extends PreyAction {

    public static final String DATA_ID = "uncamouflage";
    public final String ID = "uncamouflage";

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
        PreyLogger.i("Ejecuting UnCamouflageAction Action");
        List<ActionResult> lista=null;
        JSONObject parameters=null;
        Camouflage.unhide(ctx, lista, parameters);
        PreyLogger.i("Ejecuting UnCamouflageAction Action[Finish]");
    }


    @Override
    public boolean isSyncAction() {
        return false;
    }

    @Override
    public int getPriority() {
        return UN_CAMOUFLAGE_PRIORITY;
    }



}

