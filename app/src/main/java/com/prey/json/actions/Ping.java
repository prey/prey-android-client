package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
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

    @Override
    public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try{
            String messageId=parameters.getString(PreyConfig.MESSAGE_ID);
            String reason = null;
            try {
                String jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                if(jobId!=null&&!"".equals(jobId)){
                    reason="{\"device_job_id\":\""+jobId+"\"}";
                }
            } catch (Exception e) {
            }

            Map<String,String> map=UtilJson.makeMapParam("start","ping","started",reason);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed",messageId,map);
            PreyLogger.i("messageId:"+messageId);
        }catch (Exception e){
            PreyLogger.i("error ping:"+e.getMessage());
        }
        return null;
    }
}
