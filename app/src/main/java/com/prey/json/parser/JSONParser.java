/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;

public class JSONParser {

    JSONObject jObj;

    boolean error;

    private final static String COMMAND = "\"command\"";
    private final static String TARGET  = "\"target\"";

    public JSONParser() {
    }

    public List<JSONObject> getJSONFromUrl(Context ctx, String uri) {
        PreyLogger.d("getJSONFromUrl:" + uri);
        String sb=null;
        String json=null;
        try{
            PreyHttpResponse response=PreyRestHttpClient.getInstance(ctx).get(uri,null);
            try{sb=response.getResponseAsString();}catch(Exception e){PreyLogger.e("Error:"+e.getMessage(),e);}
            if (sb!=null)
                json = sb.trim();
        }catch(Exception e){
            PreyLogger.e("Error, causa:" + e.getMessage(), e);
            return null;
        }
        if(sb!=null) {
            PreyLogger.d("_______cmd________");
            PreyLogger.d(sb);
        }
        //json = "[{\"command\":\"history\",\"target\":\"call\",\"options\":{}}]";
        //json = "[{\"command\":\"history\",\"target\":\"sms\",\"options\":{}}]";
        //json = "[{\"command\":\"history\",\"target\":\"contact\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"system_install\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"server\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"ring\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"video\",\"options\":{}}]";
        //json = "[{\"command\":\"get\",\"target\":\"picture\",\"options\":{}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\",\"picture\",\"location\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"location\"]}}]";
        //json = "[{\"command\":\"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-36.42372147\",\"radius\": \"100\" }}]";
        //json = "[{\"command\":\"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.7193117,-32.7521112\",\"radius\": \"100\" }}]";
        //json = "[{\"command\":\"start\",\"target\": \"geofencing\",\"options\": {\"id\":\"id1\",\"origin\":\"-70.60713481,-33.42372147\",\"radius\":\"100\",\"type:\":\"in",\"expire":"-1" }}]";
        //json = "[{\"command\":\"stop\",\"target\": \"geofencing\",\"options\": {\"id\":\"id1\"}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}},{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"delay\": \"25\",\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}
        //json = "[{\"command\": \"get\",\"target\": \"location\",\"options\": {}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}]";
        //json = "[{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"interval\":\"2\"}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"],\"interval\":\"10\"}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"access_points_list\"],\"interval\":\"10\"}}]";
        //json = "[{\"command\":\"start\",\"target\":\"camouflage\",\"options\":null}]";
        //json = "[{\"command\":\"stop\",\"target\":\"camouflage\",\"options\":{\"interval\":\"2\"}}}]";
        //json = "[{\"target\":\"alert\",\"command\":\"start\",\"options\":{\"alert_message\":\"This device is stolen property. Please contact testforkhq@gmail.com to arrange its safe return.\"}},{\"target\":\"lock\",\"command\":\"start\",\"options\":{\"unlock_pass\":\"oso\"}},{\"command\":\"get\",\"target\":\"location\"},{\"target\":\"network\",\"command\":\"start\"},{\"target\":\"geo\",\"command\":\"start\"}]";
        //json = "[{\"command\":\"start\",\"target\":\"contacts_backup\" }]";
        //json = "[{\"command\":\"start\",\"target\":\"contacts_restore\" }]";
        //json = "[{\"command\":\"start\",\"target\":\"browser\" }]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"interval\":\"2\",\"exclude\":[\"picture\",false]}}]";
        //json = "[{\"command\":\"start\",\"target\": \"detach\",\"options\": {}}]";
        if ("[]".equals(json)) {
            return null;
        }
        if ("Invalid.".equals(json)) {
            return null;
        }
        return getJSONFromTxt(ctx, json);
    }

    /**
     * Parse a JSON string into a list of JSONObject instances.
     *
     * @param context The application context.
     * @param jsonString The JSON string to parse.
     * @return A list of JSONObject instances, or null if the input string is invalid.
     */
    public List<JSONObject> getJSONFromTxt(Context context, String jsonString) {
        // Check if the input string is null or empty
        if (jsonString == null || jsonString.isEmpty() || jsonString.equals("Invalid data received") || jsonString.equals("[null]")) {
            // If so, return null immediately
            return null;
        }
        // Create an empty list to store the parsed JSONObject instances
        List<JSONObject> jsonObjectList = new ArrayList<>();
        // Wrap the input string in a JSON object with a "prey" key
        String wrappedJsonString = "{\"prey\":" + jsonString + "}";
        try {
            // Create a JSONObject instance from the wrapped string
            JSONObject jsonObject = new JSONObject(wrappedJsonString);
            // Get the JSONArray instance associated with the "prey" key
            JSONArray jsonArray = jsonObject.getJSONArray("prey");
            // Iterate over the JSONArray and create a JSONObject instance for each element
            for (int i = 0; i < jsonArray.length(); i++) {
                // Get the current element as a string
                String jsonElement = jsonArray.get(i).toString();
                // Create a JSONObject instance from the string
                JSONObject commandObject = new JSONObject(jsonElement);
                // Add the JSONObject instance to the list
                jsonObjectList.add(commandObject);
            }
        } catch (Exception e) {
            // Log any errors that occur during parsing
            PreyLogger.e(String.format("Error parsing JSON:%s", e.getMessage()), e);
        }
        // Return the list of parsed JSONObject instances
        return jsonObjectList;
    }

    public List<JSONObject> getJSONFromTxt2(Context ctx, String json) {
        jObj = null;
        List<JSONObject> listaJson = new ArrayList<JSONObject>();
        List<String> listCommands = getListCommands(json);
        for (int i = 0; listCommands != null && i < listCommands.size(); i++) {
            String command = listCommands.get(i);
            try {
                jObj = new JSONObject(command);
                listaJson.add(jObj);
            } catch (JSONException e) {
                PreyLogger.e("JSON Parser, Error parsing data " + e.toString(), e);
            }
        }
        PreyLogger.d("json:" + json);
        // return JSON String
        return listaJson;
    }

    private List<String> getListCommands(String json) {
        if (json.indexOf("[{"+COMMAND)==0){
            return getListCommandsCmd(json);
        }else{
            return getListCommandsTarget(json);
        }
    }

    private List<String> getListCommandsTarget(String json) {
        json = json.replaceAll("nil", "{}");
        json = json.replaceAll("null", "{}");
        List<String> lista = new ArrayList<String>();
        int posicion = json.indexOf(TARGET);
        json = json.substring(posicion + 8);
        posicion = json.indexOf(TARGET);
        String command = "";
        while (posicion > 0) {
            command = json.substring(0, posicion);
            json = json.substring(posicion + 8);
            lista.add("{" + TARGET + cleanChar(command));
            posicion = json.indexOf("\"target\"");
        }
        lista.add("{" + TARGET + cleanChar(json));
        return lista;
    }

    private List<String> getListCommandsCmd(String json) {
        json = json.replaceAll("nil", "{}");
        json = json.replaceAll("null", "{}");
        List<String> lista = new ArrayList<String>();
        int posicion = json.indexOf(COMMAND);
        json = json.substring(posicion + 9);
        posicion = json.indexOf(COMMAND);
        String command = "";
        while (posicion > 0) {
            command = json.substring(0, posicion);
            json = json.substring(posicion + 9);
            lista.add("{" + COMMAND + cleanChar(command));
            posicion = json.indexOf("\"command\"");
        }
        lista.add("{" + COMMAND + cleanChar(json));
        return lista;
    }

    private String cleanChar(String json) {
        if (json != null) {
            json = json.trim();
            char c = json.charAt(json.length() - 1);
            while (c == '{' || c == ',' || c == ']') {
                json = json.substring(0, json.length() - 1);
                json = json.trim();
                c = json.charAt(json.length() - 1);
            }
        }
        return json;
    }
}