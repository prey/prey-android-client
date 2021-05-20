/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.events.Event;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import java.net.HttpURLConnection;

public class EventThread extends Thread {

    private JSONObject jsonObjectStatus;

    private Event event;
    private Context ctx;
    private String eventGeo;

    public EventThread(Context ctx, Event event, JSONObject jsonObjectStatus) {
        this.ctx = ctx;
        this.event = event;
        this.jsonObjectStatus = jsonObjectStatus;
        this.eventGeo =null;
    }

    public EventThread(Context ctx, Event event, JSONObject jsonObjectStatus,String eventGeo) {
        this.ctx = ctx;
        this.event = event;
        this.jsonObjectStatus = jsonObjectStatus;
        this.eventGeo = eventGeo;
    }

    public void run() {
        try {
            boolean valida = EventControl.getInstance().valida(jsonObjectStatus);
            PreyLogger.d("EVENT valida:" + valida + " eventName:" + event.getName());
            if (valida) {
                PreyHttpResponse preyHttpResponse =PreyWebServices.getInstance().sendPreyHttpEvent(ctx, event, jsonObjectStatus);
                if(preyHttpResponse!=null){
                    if(preyHttpResponse.getStatusCode()==HttpURLConnection.HTTP_OK && eventGeo!=null) {
                        PreyLogger.d("EVENT sendPreyHttpEvent eventName:" + eventGeo);
                    }
                }
            }
        } catch (Exception e) {
            PreyLogger.e("EVENT Error EventThread:" + e.getMessage(),e);
        }
    }

}