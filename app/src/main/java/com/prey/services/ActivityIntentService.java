/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import java.util.ArrayList;
import java.lang.reflect.Type;


import android.content.Context;
import com.google.gson.Gson;
import android.content.Intent;
import android.app.IntentService;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.content.res.Resources;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;

import org.json.JSONObject;

//Extend IntentService//
public class ActivityIntentService extends IntentService {

    //Call the super IntentService constructor with the name for the worker thread//
    public ActivityIntentService() {
        super(PreyConfig.TAG);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        PreyLogger.d("ActivityIntentService onCreate");
    }
//Define an onHandleIntent() method, which will be called whenever an activity detection update is available//

    @Override
    protected void onHandleIntent(Intent intent) {
    }
    /*
//Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {

//If data is available, then extract the ActivityRecognitionResult from the Intent//
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

//Get an array of DetectedActivity objects//
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            String out2="";
            for (DetectedActivity activity : detectedActivities) {
                if(activity.getConfidence()>=70) {
                    out2 += " " + getActivityString(getApplicationContext(), activity.getType()) + " " + activity.getConfidence() + "%";

                    try {
                        JSONObject info = new JSONObject();
                        info.put("activity_type", getActivityString(getApplicationContext(), activity.getType()));
                        info.put("activity_confidence", activity.getConfidence());
                        Event event= new Event(Event.ACTIVITY_UPDATES, info.toString());
                        new EventManagerRunner(getApplicationContext(), event).run(); ;
                    }catch (Exception e){}
                }
            }

            final String out=out2;
                PreyLogger.d(out);


            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(out!=null&& !"".equals(out)) {
                        Toast.makeText(getApplicationContext(), out, Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }
//Convert the code for the detected activity type, into the corresponding string//

    static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.vehicle);
            default:
                return resources.getString(R.string.unknown_activity);
        }
    }
    static final int[] POSSIBLE_ACTIVITIES = {

            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };
    static String detectedActivitiesToJson(ArrayList<DetectedActivity> detectedActivitiesList) {
        Type type = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        return new Gson().toJson(detectedActivitiesList, type);
    }
    static ArrayList<DetectedActivity> detectedActivitiesFromJson(String jsonArray) {
        Type listType = new TypeToken<ArrayList<DetectedActivity>>(){}.getType();
        ArrayList<DetectedActivity> detectedActivities = new Gson().fromJson(jsonArray, listType);
        if (detectedActivities == null) {
            detectedActivities = new ArrayList<>();
        }
        return detectedActivities;
    }*/
}