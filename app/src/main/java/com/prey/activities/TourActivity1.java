/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class TourActivity1 extends Activity {

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        setContentView(R.layout.tour1);


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

                intent = new Intent(getApplicationContext(), TourActivity2.class);

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
                    Intent intent = new Intent(getApplicationContext(), CheckPasswordHtmlActivity.class);
                    startActivity(intent);
                    finish();
                }


            });
        } catch (Exception e) {

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new TourInitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new TourInitTask().execute();


    }

    public void onBackPressed() {
        Intent intent = null;

        intent = new Intent(getApplicationContext(), CheckPasswordHtmlActivity.class);

        startActivity(intent);
        finish();
    }

    private class TourInitTask extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... data) {
            try {

                PreyWebServices.getInstance().sendEvent(getApplication(),PreyConfig.ANDROID_TOUR_SCREEN);
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

        }
    }
}
