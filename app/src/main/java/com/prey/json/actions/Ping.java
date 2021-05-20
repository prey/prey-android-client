package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.json.JsonAction;

import org.json.JSONObject;

import java.util.List;

public class Ping extends JsonAction {

    @Override
    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        get(ctx,list,parameters);
    }

    @Override
    public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try{
            Event eventStatus=new Event(Event.DEVICE_STATUS,parameters.toString());
            eventStatus.setAlwaysSend(true);
            new Thread(new EventManagerRunner(ctx, eventStatus)).start();
        }catch (Exception e){
            PreyLogger.e("error ping:"+e.getMessage(),e);
        }
        return null;
    }

}