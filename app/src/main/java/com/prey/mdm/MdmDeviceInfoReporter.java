/*******************************************************************************
 * Created by Prey Inc.
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.mdm;

import android.content.Context;
import android.util.Base64;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.net.UtilConnection;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class MdmDeviceInfoReporter {
    private interface TransportFactory {
        Transport create(Context context);
    }

    public interface Transport {
        void postJson(String url, String user, String password, JSONObject body) throws Exception;
    }

    private static TransportFactory transportFactory = HttpTransport::new;

    private MdmDeviceInfoReporter() {
    }

    public static boolean isEnabled(String url, String setupKey, String deviceKey) {
        return url != null && !url.isEmpty()
                && setupKey != null && !setupKey.isEmpty()
                && deviceKey != null && !deviceKey.isEmpty();
    }

    public static void report(Context context, String setupKey, String deviceKey, String name) {
        new Thread(() -> reportNow(context, setupKey, deviceKey, name)).start();
    }

    public static void reportNow(Context context, String setupKey, String deviceKey, String name) {
        String url = buildUrl(context, deviceKey);
        if (!isEnabled(url, setupKey, deviceKey)) {
            return;
        }

        PreyConfig config = PreyConfig.getPreyConfig(context);
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("os", "Android");
            body.put("os_version", android.os.Build.VERSION.RELEASE);

            String serialNumber = config.getMdmSerialNumber();
            if (serialNumber != null && !serialNumber.isEmpty()) {
                body.put("serial_number", serialNumber);
            }

            String imei = config.getMdmImei();
            if (imei != null && !imei.isEmpty()) {
                body.put("imei", imei);
            }

            MdmDebugReporter.send(context, "mdm_device_info_send_start");
            transportFactory.create(context).postJson(url, setupKey, "x", body);
            MdmDebugReporter.send(context, "mdm_device_info_send_ok");
        } catch (Exception e) {
            PreyLogger.e("MdmDeviceInfoReporter error:" + e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            MdmDebugReporter.send(context, "mdm_device_info_send_error", err);
        }
    }

    static void setTransportForTests(Transport transport) {
        transportFactory = ignored -> transport;
    }

    static void resetTransportForTests() {
        transportFactory = HttpTransport::new;
    }

    private static String buildUrl(Context context, String deviceKey) {
        return PreyConfig.getPreyConfig(context).getPreyUrl()
                + FileConfigReader.getInstance(context).getApiV2()
                + "devices/"
                + deviceKey
                + "/mdm";
    }

    private static final class HttpTransport implements Transport {
        private final Context context;

        private HttpTransport(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public void postJson(String url, String user, String password, JSONObject body) throws Exception {
            String authorization = "Basic " + Base64.encodeToString(
                    (user + ":" + password).getBytes(StandardCharsets.UTF_8),
                    Base64.NO_WRAP
            );
            UtilConnection.connectionJson(
                    PreyConfig.getPreyConfig(context),
                    url,
                    UtilConnection.REQUEST_METHOD_POST,
                    body,
                    authorization
            );
        }
    }
}
