package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by oso on 04-01-16.
 */
public class Signalflare extends JsonAction {

    public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Ejecuting Signalflare Data.");
        List<HttpDataService> listResult=super.get(ctx, list, parameters);
        return listResult;
    }


    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters){
        HttpDataService data = LocationUtil.dataLocation(ctx);
        data.setKey("signal_flare");
        return data;
    }
}
