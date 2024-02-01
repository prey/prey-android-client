/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.logger.LoggerController;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Tree {

    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        String reason = null;
        try {
            String jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID);
            PreyLogger.d(String.format("jobId:%s", jobId));
            if (jobId != null && !"".equals(jobId)) {
                reason = "{\"device_job_id\":\"" + jobId + "\"}";
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        try{
            PreyLogger.d("Tree started");
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId, UtilJson.makeMapParam("get", "tree", "started",reason));
            int depth = 1;
            try {
                depth=Integer.parseInt(parameters.getString("depth"));
            }catch(Exception e){
            }
            String path = parameters.getString("path");
            if ("sdcard".equals(path)){
                path="/";
            }
            String    pathBase = Environment.getExternalStorageDirectory().toString();
            File dir = new File(pathBase+path);
            JSONArray array = getFilesRecursiveJSON(pathBase, dir, depth-1);
            JSONObject jsonTree = new JSONObject();
            jsonTree.put("tree", array);
            boolean canAccessStorage = PreyPermission.canAccessStorage(ctx);
            try {
                JSONObject info = new JSONObject();
                info.put("storage", canAccessStorage);
                jsonTree.put("permissions", info);
            } catch (Exception e) {
                PreyLogger.e(String.format("error:%s", e.getMessage()), e);
            }
            LoggerController.getInstance(ctx).addTree(jsonTree);
            PreyHttpResponse response=PreyWebServices.getInstance().sendTree(ctx, jsonTree);
            PreyLogger.d(String.format("Tree stopped response:%d", response.getStatusCode()));
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get", "tree", "stopped",reason));
            PreyLogger.d("Tree stopped");
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId, UtilJson.makeMapParam("get", "tree", "failed", e.getMessage()));
            PreyLogger.e(String.format("Tree failed:%s", e.getMessage()), e);
        }
    }

    /**
     * Method to get a list of files
     *
     * @param pathBase
     * @param folder
     * @param depth
     * @return jsonArray
     */
    private JSONArray getFilesRecursiveJSON(String pathBase, File folder, int depth) {
        depth = 0;
        int length = 0;
        File[] arrayFile = null;
        try {
            arrayFile = folder.listFiles();
            length = arrayFile.length;
        } catch (Exception e) {
        }
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; folder != null && arrayFile != null && i < length; i++) {
                File child = arrayFile[i];
                String parent = child.getParent().replace(pathBase, "");
                JSONObject json = new JSONObject();
                int size = 0;
                try {
                    size = child.listFiles().length;
                } catch (Exception e) {
                }
                if (child.isDirectory() && size > 0) {
                    json.put("name", child.getName());
                    json.put("path", parent + "/" + child.getName());
                    JSONArray listChildren = new JSONArray();
                    if (depth > 0) {
                        listChildren = getFilesRecursiveJSON(pathBase, child, depth - 1);
                        json.put("children", listChildren);
                    }
                    json.put("isFile", false);
                    array.put(json);
                }
                if (child.isFile()) {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(child.getName());
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    json.put("name", child.getName());
                    json.put("path", parent + "/" + child.getName());
                    json.put("mimetype", mime.getMimeTypeFromExtension(extension));
                    json.put("size", child.length());
                    json.put("isFile", true);
                    json.put("hidden", false);
                    array.put(json);
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error getFilesRecursiveJSON:" + e.getMessage(), e);
        }
        return array;
    }
}