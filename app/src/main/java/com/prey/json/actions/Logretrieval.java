/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Logretrieval {

    private static final String ZIP_FILE_NAME = "prey-log.zip";

    // Signature is fixed: ClassUtil.execute resolves actions reflectively by
    // (Context, List<ActionResult>, JSONObject). Removing `list` would make
    // getMethod() throw NoSuchMethodException, which ClassUtil swallows
    // silently — the action would simply never run.
    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        start(ctx, list, parameters);
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        String reason = null;
        try {
            String jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID);
            if (jobId != null && !jobId.isEmpty()) {
                reason = "{\"device_job_id\":\"" + jobId + "\"}";
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        File zipFile = null;
        try {
            PreyLogger.d("Logretrieval started");
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    ctx, messageId,
                    UtilJson.makeMapParam("start", "logretrieval", "started", reason));

            // Drain any in-flight log entries so the zip captures the
            // most recent activity, including the "started" line above.
            PreyLogger.flush();

            List<File> segments = collectSegments();
            if (segments.isEmpty()) {
                throw new Exception("prey.log not found");
            }
            zipFile = buildZip(ctx, segments);
            int responseCode = PreyWebServices.getInstance().uploadLog(ctx, zipFile);
            PreyLogger.d(String.format("Logretrieval responseCode uploadLog:%d", responseCode));
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new Exception("upload failed: " + responseCode);
            }
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    ctx, UtilJson.makeMapParam("start", "logretrieval", "stopped", reason));
            PreyLogger.d("Logretrieval stopped");
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    ctx, messageId,
                    UtilJson.makeMapParam("start", "logretrieval", "failed", e.getMessage()));
            PreyLogger.d(String.format("Logretrieval failed:%s", e.getMessage()));
        } finally {
            if (zipFile != null) zipFile.delete();
        }
    }

    /**
     * Collects the existing log segments to include in the upload, in
     * chronological order: the rotated previous segment first (if any),
     * followed by the active segment.
     */
    private List<File> collectSegments() {
        List<File> segments = new ArrayList<>(2);
        File previous = PreyLogger.getOldLogFile();
        if (previous != null && previous.exists()) {
            segments.add(previous);
        }
        File active = PreyLogger.getLogFile();
        if (active != null && active.exists()) {
            segments.add(active);
        }
        return segments;
    }

    private File buildZip(Context ctx, List<File> segments) throws Exception {
        File zipFile = new File(ctx.getCacheDir(), ZIP_FILE_NAME);
        if (zipFile.exists()) zipFile.delete();
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            byte[] buffer = new byte[8192];
            for (File segment : segments) {
                zos.putNextEntry(new ZipEntry(segment.getName()));
                try (FileInputStream fis = new FileInputStream(segment)) {
                    int n;
                    while ((n = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, n);
                    }
                }
                zos.closeEntry();
            }
        }
        return zipFile;
    }
}
