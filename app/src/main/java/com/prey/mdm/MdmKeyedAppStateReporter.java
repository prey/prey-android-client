package com.prey.mdm;

import android.content.Context;

import androidx.enterprise.feedback.KeyedAppState;
import androidx.enterprise.feedback.KeyedAppStatesCallback;
import androidx.enterprise.feedback.KeyedAppStatesReporter;

import com.prey.PreyLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MdmKeyedAppStateReporter {
    public static final String DEVICE_KEY_STATE_KEY = "prey_device_key";
    private static final String DEVICE_KEY_STATE_MESSAGE = "Prey device key assigned";
    private static Factory factory = MdmKeyedAppStateReporter::new;
    private static DebugSink debugSink = MdmDebugReporter::send;

    private final ReporterClient reporter;

    public interface Factory {
        MdmKeyedAppStateReporter create(Context context);
    }

    interface DebugSink {
        void send(Context context, String event, Map<String, Object> details);
    }

    interface ReporterClient {
        void setStatesImmediate(java.util.Collection<KeyedAppState> states, KeyedAppStatesCallback callback);
    }

    public MdmKeyedAppStateReporter(Context context) {
        this((states, callback) -> KeyedAppStatesReporter.create(context.getApplicationContext())
                .setStatesImmediate(states, callback));
    }

    public MdmKeyedAppStateReporter(KeyedAppStatesReporter reporter) {
        this(reporter::setStatesImmediate);
    }

    MdmKeyedAppStateReporter(ReporterClient reporter) {
        this.reporter = reporter;
    }

    public void reportDeviceKey(String deviceKey) {
        if (deviceKey == null || deviceKey.length() == 0) {
            return;
        }
        reporter.setStatesImmediate(
                Collections.singleton(buildDeviceKeyState(deviceKey)),
                (state, throwable) -> onReportResult(deviceKey, state, throwable)
        );
    }

    public static void reportDeviceKey(Context context, String deviceKey) {
        sendDebug(context, "reportDeviceKey_enter", null);
        try {
            factory.create(context).reportDeviceKey(deviceKey);
            sendDebug(context, "reportDeviceKey_ok", deviceKeyDetails(deviceKey));
        } catch (RuntimeException e) {
            PreyLogger.e("Error reporting keyed app state", e);
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("error", e.getClass().getName() + ": " + e.getMessage());
            sendDebug(context, "reportDeviceKey_error", err);
        }
    }

    private void onReportResult(String deviceKey, int status, Throwable throwable) {
        try {
            Map<String, Object> details = deviceKeyDetails(deviceKey);
            details.put("status", status);
            if (throwable != null) {
                details.put("error", throwable.getClass().getName() + ": " + throwable.getMessage());
                try {
                    PreyLogger.e("Error reporting keyed app state callback", throwable);
                } catch (RuntimeException ignored) {
                    // Best-effort debug path; never break the enrollment flow.
                }
            }
            sendDebug(null, "reportDeviceKey_callback", details);
        } catch (RuntimeException ignored) {
            // The callback is diagnostic only; suppress local logging failures.
        }
    }

    private static Map<String, Object> deviceKeyDetails(String deviceKey) {
        Map<String, Object> details = new HashMap<>();
        details.put("device_key_length", deviceKey == null ? 0 : deviceKey.length());
        return details;
    }

    private static void sendDebug(Context context, String event, Map<String, Object> details) {
        debugSink.send(context, event, details);
    }

    public static void setFactoryForTests(Factory testFactory) {
        factory = testFactory;
    }

    public static void resetFactoryForTests() {
        factory = MdmKeyedAppStateReporter::new;
    }

    static void setDebugSinkForTests(DebugSink testDebugSink) {
        debugSink = testDebugSink;
    }

    static void resetDebugSinkForTests() {
        debugSink = MdmDebugReporter::send;
    }

    static KeyedAppState buildDeviceKeyState(String deviceKey) {
        return KeyedAppState.builder()
                .setKey(DEVICE_KEY_STATE_KEY)
                .setSeverity(KeyedAppState.SEVERITY_INFO)
                .setMessage(DEVICE_KEY_STATE_MESSAGE)
                .setData(deviceKey)
                .build();
    }
}
