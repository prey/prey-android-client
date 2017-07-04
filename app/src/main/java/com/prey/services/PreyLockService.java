/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class PreyLockService extends Service{

    private WindowManager windowManager;
    private View view;


    public PreyLockService(){

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        PreyLogger.i("PreyLockService onCreate");
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent,startId);
        final Context ctx=this;
        PreyLogger.i("PreyLockService onStart");

        final String unlock= PreyConfig.getPreyConfig(ctx).getUnlockPass();

        if(unlock!=null&&!"".equals(unlock)) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.lock_android7, null);

            Typeface regularMedium = Typeface.createFromAsset(getAssets(), "fonts/Regular/regular-medium.ttf");
            TextView textView1 = (TextView) view.findViewById(R.id.TextView_Lock_AccessDenied);
            textView1.setTypeface(regularMedium);
            Typeface regularBold = Typeface.createFromAsset(getAssets(), "fonts/Regular/regular-bold.ttf");
            EditText editText1 = (EditText) view.findViewById(R.id.EditText_Lock_Password);
            editText1.setTypeface(regularMedium);

            final EditText editText = (EditText) this.view.findViewById(R.id.EditText_Lock_Password);

            final Button btn_unlock = (Button) this.view.findViewById(R.id.Button_Lock_Unlock);
            btn_unlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String key = editText.getText().toString();
                        if (unlock.equals(key)) {
                            PreyConfig.getPreyConfig(ctx).deleteUnlockPass();
                            if (view != null) {
                                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                                wm.removeView(view);
                                view = null;
                                new Thread() {
                                    public void run() {
                                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "lock", "stopped"));
                                    }
                                }.start();
                            }
                            stopSelf();
                        } else {
                            editText.setText("");
                        }
                    } catch (Exception e) {
                    }
                }
            });
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            wm.addView(view, layoutParams);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        PreyLogger.i("PreyLockService onDestroy");
        if(view != null){
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(view);
            view = null;
        }
    }

}
