/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2023 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.logger;

import android.content.Context;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.events.Event;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.Map;

public class LoggerController {

    private Context ctx;
    private static LoggerController INSTANCE;

    public static LoggerController getInstance(Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = new LoggerController(ctx);
        }
        return INSTANCE;
    }

    private final String CMD = "CMD";
    private final String ACTION = "ACTION";
    private final String DATA = "DATA";
    private final String REPORT = "REPORT";
    private final String UPLOAD = "UPLOAD";
    private final String GEOFENCING = "GEOFENCING";
    private final String EVENTS = "EVENTS";
    private final String TREE = "TREE";

    private int maxLogger;

    private LoggerController(Context context) {
        ctx = context;
        try {
            maxLogger = FileConfigReader.getInstance(context).getLoggerMax();
        } catch (Exception e) {
            maxLogger = 500;
        }
    }

    private static int sequence = 1;

    public synchronized static int getSequence(Context context) {
        sequence++;
        PreyConfig.getPreyConfig(context).setLoggerId(sequence);
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * Method to register logger
     *
     * @param type
     * @param txt
     */
    public void addLogger(String type, String txt) {
        int loggerID = getSequence(ctx);
        LoggerDto dto = new LoggerDto();
        dto.setType(type);
        dto.setTxt(txt);
        dto.setLoggerId(loggerID);
        dto.setTime(new Date().toGMTString());
        LoggerDatasource datasource = new LoggerDatasource(ctx);
        datasource.createLogger(dto);
        datasource.deleteMinorsLogger(loggerID - maxLogger);
        PreyConfig.getPreyConfig(ctx).setLoggerId(loggerID);
        PreyLogger.d(String.format("logger dto:%s", dto.toString()));
    }

    public void addComands(String commands) {
        addLogger(CMD, commands);
    }

    public void addData(JSONObject json) {
        addLogger(DATA, json.toString());
    }

    public void addTree(JSONObject json) {
        addLogger(TREE, json.toString());
    }

    public void addEvents(Event event, JSONObject json) {
        JSONObject newJson = new JSONObject();
        try {
            newJson.put("name", event.getName());
            newJson.put("info", event.getInfo());
            newJson.put("status", json);
        } catch (Exception e) {
            PreyLogger.e(String.format("error addEvents:%s", e.getMessage()), e);
        }
        addLogger(EVENTS, newJson.toString());
    }

    public void addReport(JSONObject json) {
        addLogger(REPORT, json.toString());

    }

    public void addGeofencing(String json) {
        addLogger(GEOFENCING, json.toString());
    }

    public void addUpload(File file, int responseCode) {
        JSONObject newJson = new JSONObject();
        try {
            newJson.put("name", file.getName());
            newJson.put("length", file.length());
            newJson.put("responseCode", responseCode);
        } catch (Exception e) {
            PreyLogger.e(String.format("error addUpload:%s", e.getMessage()), e);
        }
        addLogger(UPLOAD, newJson.toString());
    }

    public void addActionResult(String status, Map<String, String> params) {
        JSONObject json = new JSONObject();
        try {
            json.put("command", params.get("command"));
            json.put("target", params.get("target"));
            json.put("status", params.get("status"));
            if (params.containsKey("reason")) {
                String reason = params.get("reason");
                try {
                    JSONObject jsonReason = new JSONObject(reason);
                    json.put("reason", jsonReason);
                } catch (Exception e) {
                    json.put("reason", reason);
                }
            }
            addLogger(ACTION, json.toString());
        } catch (Exception e) {
            PreyLogger.e(String.format("error addActionResult:%s", e.getMessage()), e);
        }
    }

    public void deleteAllLogger() {
        LoggerDatasource datasource = new LoggerDatasource(ctx);
        datasource.deleteAllLogger();
    }

}