/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

public class DeviceReadyActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    public void onBackPressed() {
        Intent intent = null;
        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancel(PreyConfig.TAG,PreyConfig.NOTIFY_ANDROID_6);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.device_ready);
        PreyLogger.d("onCreate of DeviceReadyActivity");
        final Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        final Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");
        TextView textView3_1=(TextView)findViewById(R.id.textView3_1);
        TextView textView3_2=(TextView)findViewById(R.id.textView3_2);
        TextView textView4_1=(TextView)findViewById(R.id.textView4_1);
        TextView textView4_2=(TextView)findViewById(R.id.textView4_2);
        textView3_1.setTypeface(magdacleanmonoRegular);
        textView3_2.setTypeface(titilliumWebBold);
        textView4_1.setTypeface(magdacleanmonoRegular);
        textView4_2.setTypeface(titilliumWebBold);
        LinearLayout linearLayout1=(LinearLayout)findViewById(R.id.linearLayout1);
        linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                intent = new Intent(getApplication(), PanelWebActivity.class);
                startActivity(intent);
                finish();
            }
        });
        LinearLayout linearLayout2=(LinearLayout)findViewById(R.id.linearLayout2);
        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                intent = new Intent(getApplication(), PreyConfigurationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}