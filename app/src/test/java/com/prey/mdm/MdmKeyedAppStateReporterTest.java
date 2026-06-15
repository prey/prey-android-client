package com.prey.mdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.enterprise.feedback.FakeKeyedAppStatesReporter;
import androidx.enterprise.feedback.KeyedAppState;
import androidx.enterprise.feedback.KeyedAppStatesCallback;

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MdmKeyedAppStateReporterTest {

    @After
    public void tearDown() {
        MdmKeyedAppStateReporter.resetDebugSinkForTests();
    }

    @Test
    public void reportDeviceKey_sendsImmediateDeviceKeyState() {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter sut = new MdmKeyedAppStateReporter(reporter);

        sut.reportDeviceKey("dvc-123");

        assertEquals(1, reporter.getNumberOfUploads());
        KeyedAppState state = reporter.getUploadedKeyedAppStatesByKey().get(MdmKeyedAppStateReporter.DEVICE_KEY_STATE_KEY);
        assertNotNull(state);
        assertEquals("dvc-123", state.getData());
        assertEquals(KeyedAppState.SEVERITY_INFO, state.getSeverity());
        assertEquals("Prey device key assigned", state.getMessage());
    }

    @Test
    public void reportDeviceKey_withEmptyDeviceKey_doesNotUpload() {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter sut = new MdmKeyedAppStateReporter(reporter);

        sut.reportDeviceKey("");

        assertEquals(0, reporter.getNumberOfUploads());
    }

    @Test
    public void reportDeviceKey_withNullDeviceKey_doesNotUpload() {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter sut = new MdmKeyedAppStateReporter(reporter);

        sut.reportDeviceKey(null);

        assertEquals(0, reporter.getNumberOfUploads());
    }

    @Test
    public void reportDeviceKey_staticReportsCallbackSuccessToDebugSink() {
        RecordingDebugSink debugSink = new RecordingDebugSink();
        MdmKeyedAppStateReporter.setDebugSinkForTests(debugSink);
        MdmKeyedAppStateReporter.setFactoryForTests(ctx -> new MdmKeyedAppStateReporter(
                new CallbackCapturingReporter(KeyedAppStatesCallback.STATUS_SUCCESS, null)
        ));
        try {
            MdmKeyedAppStateReporter.reportDeviceKey(null, "dvc-123");

            assertTrue(debugSink.hasEvent("reportDeviceKey_enter"));
            assertTrue(debugSink.hasEvent("reportDeviceKey_ok"));
            Map<String, Object> details = debugSink.getDetails("reportDeviceKey_callback");
            assertNotNull(details);
            assertEquals(KeyedAppStatesCallback.STATUS_SUCCESS, details.get("status"));
            assertEquals(7, details.get("device_key_length"));
            assertNull(details.get("error"));
        } finally {
            MdmKeyedAppStateReporter.resetFactoryForTests();
        }
    }

    @Test
    public void reportDeviceKey_staticReportsCallbackFailureToDebugSink() {
        RecordingDebugSink debugSink = new RecordingDebugSink();
        MdmKeyedAppStateReporter.setDebugSinkForTests(debugSink);
        MdmKeyedAppStateReporter.setFactoryForTests(ctx -> new MdmKeyedAppStateReporter(
                new CallbackCapturingReporter(
                        KeyedAppStatesCallback.STATUS_UNKNOWN_ERROR,
                        new IllegalStateException("boom")
                )
        ));
        try {
            MdmKeyedAppStateReporter.reportDeviceKey(null, "dvc-123");

            Map<String, Object> details = debugSink.getDetails("reportDeviceKey_callback");
            assertNotNull(details);
            assertEquals(KeyedAppStatesCallback.STATUS_UNKNOWN_ERROR, details.get("status"));
            assertEquals("java.lang.IllegalStateException: boom", details.get("error"));
        } finally {
            MdmKeyedAppStateReporter.resetFactoryForTests();
        }
    }

    private static final class RecordingDebugSink implements MdmKeyedAppStateReporter.DebugSink {
        private final Map<String, Map<String, Object>> events = new HashMap<>();

        @Override
        public void send(Context context, String event, Map<String, Object> details) {
            events.put(event, details == null ? null : new HashMap<>(details));
        }

        boolean hasEvent(String event) {
            return events.containsKey(event);
        }

        Map<String, Object> getDetails(String event) {
            return events.get(event);
        }
    }

    private static final class CallbackCapturingReporter implements MdmKeyedAppStateReporter.ReporterClient {
        private final int status;
        private final Throwable throwable;

        CallbackCapturingReporter(int status, Throwable throwable) {
            this.status = status;
            this.throwable = throwable;
        }

        @Override
        public void setStatesImmediate(java.util.Collection<KeyedAppState> states, KeyedAppStatesCallback callback) {
            callback.onResult(status, throwable);
        }
    }
}
