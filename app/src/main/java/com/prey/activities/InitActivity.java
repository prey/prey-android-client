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
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.prey.PreyLogger;
import com.prey.R;

public class InitActivity extends Activity {


    @Override
    public void onResume() {
        PreyLogger.d("onResume of InitActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of InitActivity");
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
        this.setContentView(R.layout.init);
        PreyLogger.i("onCreate of MenuActivity");

        Button buttonActivate = (Button) findViewById(R.id.buttonActivate);
        buttonActivate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");




        TextView textView1=(TextView)findViewById(R.id.textView1);
        TextView textView2=(TextView)findViewById(R.id.textView2);
        TextView textView3=(TextView)findViewById(R.id.textView3);
        TextView textView4=(TextView)findViewById(R.id.textView4);
        TextView textView5=(TextView)findViewById(R.id.textView5);
        textView1.setTypeface(magdacleanmonoRegular);
        textView2.setTypeface(magdacleanmonoRegular);
        textView3.setTypeface(titilliumWebRegular);
        textView4.setTypeface(titilliumWebBold);
        textView5.setTypeface(titilliumWebRegular);

        buttonActivate.setTypeface(titilliumWebBold);

    }



}
