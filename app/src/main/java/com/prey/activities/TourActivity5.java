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
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.R;

public class TourActivity5 extends Activity {

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        setContentView(R.layout.tour5);


        Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");


        TextView textView1 = (TextView) findViewById(R.id.textView1);
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        Button buttonTour = (Button) findViewById(R.id.buttontour01);
        textView1.setTypeface(titilliumWebBold);
        textView2.setTypeface(titilliumWebRegular);
        buttonTour.setTypeface(titilliumWebBold);


        buttonTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;

                intent = new Intent(getApplicationContext(), TourActivity6.class);


                startActivity(intent);
                finish();
            }
        });


        try {
            ImageView close = (ImageView) findViewById(R.id.close_tour1);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreyConfig.getPreyConfig(getApplication()).setProtectTour(true);
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                }


            });
        } catch (Exception e) {

        }


    }

    public void onBackPressed() {
        Intent intent = null;

        intent = new Intent(getApplicationContext(), TourActivity4.class);


        startActivity(intent);
        finish();
    }
}
