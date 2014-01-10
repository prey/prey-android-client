package com.prey.actions.wipe;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.content.Context;

public class WipeThread extends Thread {

        private Context ctx;
        private boolean wipe;
        private boolean deleteSD;
        
        public WipeThread(Context ctx,boolean wipe,boolean deleteSD) {
                this.ctx = ctx;
                this.deleteSD = deleteSD;
                this.wipe = wipe;
        }
        
        public void run() {
                PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","started"));
                
                
                
                try{
                        if(deleteSD){
                                WipeUtil.deleteSD();
                        }
                }catch(Exception e){
                }
                try{
                        if (wipe&&preyConfig.isFroyoOrAbove()){
                                PreyLogger.d("Wiping the device!!");
                                FroyoSupport.getInstance(ctx).wipe();
                        }
                }catch(Exception e){
                        PreyLogger.e("Error Wipe1:"+e.getMessage(), e);
                }
                
                try{
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","stopped"));
                }catch(Exception e){
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","failed",e.getMessage()));
                        PreyLogger.e("Error Wipe2:"+e.getMessage(), e);
                }
        }

}
