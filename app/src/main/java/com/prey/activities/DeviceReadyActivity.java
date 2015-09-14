/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;

public class DeviceReadyActivity extends Activity {


    @Override
    public void onResume() {
        PreyLogger.i("onResume of DeviceReadyActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of DeviceReadyActivity");
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setContentView(R.layout.device_ready);
        PreyLogger.i("onCreate of DeviceReadyActivity");

        String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();
        if (deviceKey != null && deviceKey != "")
            PreyConfig.getPreyConfig(this).registerC2dm();

        LinearLayout linearLayout1=(LinearLayout)findViewById(R.id.linearLayout1);
        linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = PreyConfig.getPreyConfig(getApplication()).getPreyPanelUrl();
                    Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(browserIntent);
                    finish();
                } catch (Exception e) {
                    PreyLogger.i("error:"+e.getMessage());
                }
            }
        });

        LinearLayout linearLayout2=(LinearLayout)findViewById(R.id.linearLayout2);
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
                    intent = new Intent(getApplication(), CheckPasswordActivity.class);
                } else {
                    intent = new Intent(getApplication(), PreyConfigurationActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });

        LinearLayout linearLayout3 = (LinearLayout) findViewById(R.id.linearLayout3);

        TextView textView4=(TextView) findViewById(R.id.textView4);




        if(PreyConfig.getPreyConfig(getApplication()).getProtectTour()) {
            linearLayout3.setVisibility(View.GONE);
            textView4.setVisibility(View.VISIBLE);

            textView4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = PreyConfig.getPreyConfig(getApplication()).getPreyUninstallUrl();

                    Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(browserIntent);

                    finish();
                }
            });
        }else{

            linearLayout3.setVisibility(View.VISIBLE);
            textView4.setVisibility(View.GONE);
            try {

                linearLayout3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplication(), TourActivity.class);
                        Bundle b = new Bundle();
                        b.putInt("id", 1);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    }
                });
            }catch (Exception e){

            }
        }
    }

    public void onBackPressed() {
        super.onResume();
    }
}
