package com.prey.json.actions;

/**
 * Created by oso on 24-08-15.
 */

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class PrivateIp  extends JsonAction{


    public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
        List<HttpDataService> listResult=super.report(ctx, list, parameters);
        return listResult;
    }

    public  List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Ejecuting PrivateIp Data.");
        List<HttpDataService> listResult=super.get(ctx, list, parameters);
        return listResult;
    }


    public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
        PreyPhone phone=new PreyPhone(ctx);
        HttpDataService data = new HttpDataService("private_ip");
        HashMap<String, String> parametersMap = new HashMap<String, String>();

        String privateIp=phone.getWifi().getIpAddress();
        parametersMap.put(privateIp,privateIp);
        PreyLogger.d("privateIp:"+privateIp);;

        data.setSingleData(privateIp);

        return data;
    }


}

