/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.mdm;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.net.HttpURLConnection;

public class MdmSetupPrerequisites {
    private static final long POLL_INTERVAL_MS = 500L;
    private static final long DEFAULT_TIMEOUT_MS = 30000L;

    public interface LocationUploader {
        boolean upload(Context context);
    }

    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    public interface Clock {
        long now();
    }

    private final LocationUploader locationUploader;
    private final Sleeper sleeper;
    private final Clock clock;

    public MdmSetupPrerequisites() {
        this(new DefaultLocationUploader(), Thread::sleep, System::currentTimeMillis);
    }

    MdmSetupPrerequisites(LocationUploader locationUploader, Sleeper sleeper, Clock clock) {
        this.locationUploader = locationUploader;
        this.sleeper = sleeper;
        this.clock = clock;
    }

    public boolean waitUntilReady(Context context) {
        return waitUntilReady(context, DEFAULT_TIMEOUT_MS);
    }

    public boolean waitUntilReady(Context context, long timeoutMs) {
        long deadline = clock.now() + timeoutMs;
        maybeUploadLocation(context);
        while (clock.now() <= deadline) {
            if (isReady(context)) {
                return true;
            }
            maybeUploadLocation(context);
            try {
                sleeper.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return isReady(context);
    }

    public boolean isReady(Context context) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        String notificationId = preyConfig.getNotificationId();
        return notificationId != null
                && !notificationId.isEmpty()
                && preyConfig.isMdmSetupLocationSent();
    }

    private void maybeUploadLocation(Context context) {
        if (!PreyConfig.getPreyConfig(context).isMdmSetupLocationSent()) {
            locationUploader.upload(context);
        }
    }

    private static class DefaultLocationUploader implements LocationUploader {
        @Override
        public boolean upload(Context context) {
            try {
                PreyLocation location = LocationUtil.getLocation(context, null, false);
                if (location == null || location.getLat() == 0 || location.getLng() == 0) {
                    return false;
                }
                JSONObject json = new JSONObject();
                json.put("lat", location.getLat());
                json.put("lng", location.getLng());
                json.put("accuracy", Math.round(location.getAccuracy()));
                json.put("method", location.getMethod());
                PreyHttpResponse response = PreyWebServices.getInstance().sendLocation(context, json);
                if (response != null && response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    PreyConfig.getPreyConfig(context).setLocation(location);
                    PreyConfig.getPreyConfig(context).setMdmSetupLocationSent(true);
                    return true;
                }
            } catch (Exception e) {
                PreyLogger.e("Error sending MDM setup location", e);
            }
            return false;
        }
    }
}
