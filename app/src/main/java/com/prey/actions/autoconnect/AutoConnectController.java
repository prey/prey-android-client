package com.prey.actions.autoconnect;

import android.content.Context;
import android.os.Build;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.services.AutoconnectJobService;

public class AutoConnectController {


    private static AutoConnectController INSTANCE;

    public static AutoConnectController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoConnectController();
        }
        return INSTANCE;
    }


    public void initJob(Context ctx) {
        try{
            boolean isAutoconnect=PreyConfig.getPreyConfig(ctx).getAutoConnect();
            PreyLogger.d("AUTO AutoConnectController initJob:"+isAutoconnect);
            if (isAutoconnect) {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    AutoconnectJobService.schedule(ctx);
                }
            }
        } catch (Exception e) {
            PreyLogger.e("AUTO error:" + e.getMessage(), e);
        }
    }
}
