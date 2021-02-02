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
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.R;
import com.prey.activities.CloseActivity;
import com.prey.receivers.PreyDisablePowerOptionsReceiver;

import java.util.Calendar;
import java.util.Date;

public class PreySecureService extends Service{

    private WindowManager windowManager;
    private View view;
    Button button_close=null;
    Button button_Super_Lock_Unlock=null;
    TextView textViewPin=null;
    EditText editTextPin =null;

    public PreySecureService(){
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        PreyLogger.d("PreySecureService onCreate");
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent,startId);
        final Context ctx=this;
        PreyLogger.d("PreySecureService onStart");
        boolean canDrawOverlays= PreyPermission.canDrawOverlays(ctx);
        if(!canDrawOverlays){
            stopSelf();
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.super_lock, null);
        editTextPin = (EditText) view.findViewById(R.id.editTextPin);
        textViewPin= (TextView) view.findViewById(R.id.textViewPin);
        button_Super_Lock_Unlock= (Button)view.findViewById(R.id.button_Super_Lock_Unlock);
        button_close= (Button)view.findViewById(R.id.button_close);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(view != null){
                    WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                    if(wm != null) {
                        wm.removeView(view);
                    }
                    view = null;
                }
                stopSelf();
            }
        });
        Typeface regularBold= Typeface.createFromAsset(getAssets(), "fonts/Regular/regular-bold.otf");
        Typeface regularBook = Typeface.createFromAsset(getAssets(), "fonts/Regular/regular-book.otf");
        editTextPin.setTypeface(regularBold);
        textViewPin.setTypeface(regularBook);
        button_Super_Lock_Unlock.setTypeface(regularBook);
        button_close.setTypeface(regularBook);
        final String pinNumber= PreyConfig.getPreyConfig(ctx).getPinNumber();
        if(pinNumber!=null&&!"".equals(pinNumber)) {
            try {
                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                intentClose.putExtra(PreyDisablePowerOptionsReceiver.stringExtra, PreyDisablePowerOptionsReceiver.stringExtra);
                this.sendBroadcast(intentClose);
            }catch (Exception e){}
            editTextPin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    editTextPin.setBackgroundColor(Color.WHITE);
                }
                @Override
                public void afterTextChanged(Editable s) {
                    editTextPin.setBackgroundColor(Color.WHITE);
                }
            });
            editTextPin.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        String pin=editTextPin.getText().toString();
                        if(pin!=null ){
                            String pinNumber=PreyConfig.getPreyConfig(getApplicationContext()).getPinNumber();
                            PreyLogger.d("pinNumber:"+pinNumber+" pin:"+pin );
                            if(pinNumber.equals(pin)){
                                PreyConfig.getPreyConfig(getApplicationContext()).setOpenSecureService(false);
                                PreyConfig.getPreyConfig(getApplicationContext()).setCounterOff(0);
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(new Date().getTime());
                                cal.add(Calendar.MINUTE, 1);
                                PreyConfig.getPreyConfig(getApplicationContext()).setTimeSecureLock(cal.getTimeInMillis());
                                stopSelf();
                                Intent intent = new Intent(ctx, CloseActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                ctx.startActivity(intent);
                            }else{
                                PreyLogger.d("error"  );
                                editTextPin.setBackgroundColor(Color.RED);
                            }
                        }
                    }
                    return false;
                }
            });
            button_Super_Lock_Unlock.setOnClickListener(new ButtonPinOnClickListener());
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                if (Settings.canDrawOverlays(this)) {
                    if(wm != null) {
                        try{
                            wm.addView(view, layoutParams);
                            PreyConfig.getPreyConfig(this).setOpenSecureService(true);
                        }catch (Exception e){
                            PreyLogger.e(e.getMessage(),e);
                        }
                    }
                }
            }
            try {
                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                intentClose.putExtra(PreyDisablePowerOptionsReceiver.stringExtra, PreyDisablePowerOptionsReceiver.stringExtra);
                this.sendBroadcast(intentClose);
            }catch (Exception e){}
        }else{
            if(view != null){
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                if(wm != null) {
                    wm.removeView(view);
                }
                view = null;
            }
            PreyConfig.getPreyConfig(this).setOpenSecureService(false);
            stopSelf();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        PreyLogger.d("PreySecureService onDestroy");
        PreyConfig.getPreyConfig(this).setOpenSecureService(false);
        if(view != null){
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            if(wm != null) {
                try{wm.removeView(view);}catch (Exception e){}
            }
            view = null;
        }
    }

    public class ButtonPinOnClickListener implements View.OnClickListener {
        @Override public void onClick(View v) {
            String pin=editTextPin.getText().toString();
            if(pin!=null ){
                String pinNumber=PreyConfig.getPreyConfig(getApplicationContext()).getPinNumber();
                PreyLogger.d("pinNumber:"+pinNumber+" pin:"+pin );
                if(pinNumber.equals(pin)){
                    PreyConfig.getPreyConfig(getApplicationContext()).setOpenSecureService(false);
                    PreyConfig.getPreyConfig(getApplicationContext()).setCounterOff(0);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(new Date().getTime());
                    cal.add(Calendar.MINUTE, 1);
                    PreyConfig.getPreyConfig(getApplicationContext()).setTimeSecureLock(cal.getTimeInMillis());
                    stopSelf();
                    Intent intent = new Intent(getApplicationContext(), CloseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }else{
                    PreyLogger.d("error"  );
                    editTextPin.setBackgroundColor(Color.RED);
                }
            }
        }
    }

}