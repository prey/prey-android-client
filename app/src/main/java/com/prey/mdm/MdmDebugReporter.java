/*******************************************************************************
 * MdmDebugReporter — temporary helper to send arbitrary status events to the
 * Prey MDM server's open debug endpoint. Used during the AMAPI integration
 * bring-up when ADB / logcat is not available on the test device.
 *
 * Remove this class (and its callers) once the integration is verified.
 ******************************************************************************/
package com.prey.mdm;

import android.content.Context;

import com.prey.BuildConfig;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MdmDebugReporter {

    static String normalizeDebugUrl(String rawUrl) {
        if (rawUrl == null) {
            return "";
        }
        return rawUrl.trim();
    }

    static boolean isEnabledForUrl(String rawUrl) {
        return !normalizeDebugUrl(rawUrl).isEmpty();
    }

    public static void send(Context ctx, String event, Map<String, Object> details) {
        final String debugUrl = normalizeDebugUrl(BuildConfig.MDM_DEBUG_URL);
        if (!isEnabledForUrl(debugUrl)) {
            return;
        }
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("event", event);
                body.put("ts", System.currentTimeMillis());
                if (ctx != null) {
                    body.put("package", ctx.getPackageName());
                }
                if (details != null) {
                    for (Map.Entry<String, Object> e : details.entrySet()) {
                        body.put(e.getKey(), e.getValue());
                    }
                }
                URL url = new URL(debugUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Throwable ignore) {
                // Best-effort, never break the caller.
            }
        }).start();
    }

    public static void send(Context ctx, String event) {
        send(ctx, event, null);
    }
}
