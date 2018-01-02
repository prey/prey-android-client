/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.sms;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class SMSParser {

    public static List<JSONObject> getJSONListFromText(String command, String phoneNumber) {
        List<JSONObject> jsonObjectList = null;
        List<String> listCommand = SMSUtil.getListCommand(command);
        try {
            if (listCommand != null) {
                JSONObject json = new JSONObject();
                json.put("command", "sms");
                json.put("target", listCommand.get(2));
                if (listCommand.size() == 3) {
                    json.put("options", null);
                } else {
                    JSONObject jsonParameter = new JSONObject();
                    StringBuilder parameter = new StringBuilder();
                    for (int i = 3; listCommand != null && i < listCommand.size(); i++) {
                        parameter.append(" ").append(listCommand.get(i).toLowerCase());
                    }
                    parameter = new StringBuilder(parameter.toString().trim());
                    jsonParameter.put("parameter", parameter.toString());
                    jsonParameter.put("phoneNumber", phoneNumber);
                    json.put("options", jsonParameter);
                }
                jsonObjectList = new ArrayList<JSONObject>();
                jsonObjectList.add(json);
            }
        } catch (JSONException e) {
        }
        return jsonObjectList;
    }

}