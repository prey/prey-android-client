package com.prey.actions.autoconnect;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;


public class AutoconnectConfig {


    private static AutoconnectConfig cachedInstance = null;
    private Context ctx;


    private AutoconnectConfig(Context ctx) {
        this.ctx = ctx;
    }

    public boolean autoconnect = false;

    public static synchronized AutoconnectConfig getAutoconnectConfig(Context ctx) {
        if (cachedInstance == null) {
            synchronized (AutoconnectConfig.class) {
                if (cachedInstance == null)
                    cachedInstance = new AutoconnectConfig(ctx);
            }
        }
        return cachedInstance;
    }


}
