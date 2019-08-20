package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.Map;
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
            String messageId = null;
            try {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID);
                PreyLogger.d("messageId:"+messageId);
            } catch (Exception e) {
            }
            String reason = null;
            try {
                String jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                if(jobId!=null&&!"".equals(jobId)){
                    reason="{\"device_job_id\":\""+jobId+"\"}";
                }
            } catch (Exception e) {
            }
            new Thread(new EventManagerRunner(ctx, new Event(Event.DEVICE_STATUS))).start();
            Map<String,String> map=UtilJson.makeMapParam("start","ping","started",reason);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed",messageId,map);
            PreyLogger.d("messageId:"+messageId);
        }catch (Exception e){
            PreyLogger.e("error ping:"+e.getMessage(),e);
        }
        return null;
    }
}
