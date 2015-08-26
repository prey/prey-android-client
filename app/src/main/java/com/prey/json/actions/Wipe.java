package com.prey.json.actions;

/**
 * Created by oso on 24-08-15.
 */

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.wipe.WipeThread;

public class Wipe {

    public void sms(Context ctx,List<ActionResult> lista,JSONObject parameters){
        execute(ctx, lista, parameters);
    }

    public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
        execute(ctx, lista, parameters);
    }

    public void execute(Context ctx,List<ActionResult> lista,JSONObject parameters){
        boolean wipe=false;
        boolean deleteSD=false;
        try {
            String sd=parameters.getString("parameter");
            PreyLogger.i("sd:"+sd);
            if(sd!=null&&"sd".equals(sd)){
                wipe=false;
                deleteSD=true;
            }
        }catch(Exception e){

        }
        try {
            String factoryReset=parameters.getString("factory_reset");
            PreyLogger.i("factoryReset:"+factoryReset);
            if("on".equals(factoryReset)||"y".equals(factoryReset)||"true".equals(factoryReset)){
                wipe=true;
            }
            if("off".equals(factoryReset)||"n".equals(factoryReset)||"false".equals(factoryReset)){
                wipe=false;
            }
        }catch(Exception e){

        }
        try {
            String wipeSim=parameters.getString("wipe_sim");
            PreyLogger.i("wipeSim:"+wipeSim);
            if("on".equals(wipeSim)||"y".equals(wipeSim)||"true".equals(wipeSim)){
                deleteSD=true;
            }
            if("off".equals(wipeSim)||"n".equals(wipeSim)||"false".equals(wipeSim)){
                deleteSD=false;
            }
        }catch(Exception e){

        }
        PreyLogger.i("wipe:"+wipe+" deleteSD:"+deleteSD);
        new WipeThread(ctx,wipe, deleteSD).start();
    }
}

