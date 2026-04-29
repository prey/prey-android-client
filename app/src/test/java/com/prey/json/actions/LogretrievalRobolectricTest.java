/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Robolectric tests for {@link Logretrieval}.
 *
 * <p>Logretrieval orchestrates three things: notify "started" → zip the log
 * file → upload the zip → notify "stopped" (or "failed"). The tests stub
 * {@link PreyWebServices} with a recording subclass so the action runs
 * end-to-end without making any HTTP calls, and so we can assert on which
 * notifications and which uploaded payload the action emitted.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LogretrievalRobolectricTest {

    private Context context;
    private RecordingPreyWebServices recordingWebServices;
    private File logFile;
    private File oldLogFile;
    private File cacheZipFile;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        recordingWebServices = new RecordingPreyWebServices();
        PreyWebServices.setInstanceForTests(recordingWebServices);

        // Drain anything still queued in the writer thread from previous tests
        // before we touch the file references — otherwise a stale write could
        // race with this test's setup.
        PreyLogger.flush();

        // Each test drives PreyLogger.logFile / oldLogFile explicitly. We
        // deliberately do NOT call PreyLogger.init(): production code calls
        // PreyLogger.d() inside Logretrieval.start() *before* checking the
        // log file's existence, so leaving logFile null lets us simulate the
        // "no log file" path without it being re-created behind our back.
        setPreyLoggerLogFile(null);
        setPreyLoggerOldLogFile(null);
        logFile = new File(context.getFilesDir(), "prey.log");
        oldLogFile = new File(context.getFilesDir(), "prey.log.1");
        logFile.getParentFile().mkdirs();
        if (logFile.exists() && !logFile.delete()) {
            throw new IOException("Could not clear pre-existing log file");
        }
        if (oldLogFile.exists() && !oldLogFile.delete()) {
            throw new IOException("Could not clear pre-existing rotated log file");
        }
        cacheZipFile = new File(context.getCacheDir(), "prey-log.zip");
        if (cacheZipFile.exists() && !cacheZipFile.delete()) {
            throw new IOException("Could not clear pre-existing zip file");
        }
    }

    @After
    public void tearDown() throws Exception {
        PreyWebServices.resetInstanceForTests();
        // Drain again so a queued write does not recreate a file the next
        // test has just deleted.
        PreyLogger.flush();
        if (logFile != null && logFile.exists()) {
            logFile.delete();
        }
        if (oldLogFile != null && oldLogFile.exists()) {
            oldLogFile.delete();
        }
        if (cacheZipFile != null && cacheZipFile.exists()) {
            cacheZipFile.delete();
        }
        setPreyLoggerLogFile(null);
        setPreyLoggerOldLogFile(null);
    }

    private static void setPreyLoggerLogFile(File file) throws Exception {
        Field logFileField = PreyLogger.class.getDeclaredField("logFile");
        logFileField.setAccessible(true);
        logFileField.set(null, file);
    }

    private static void setPreyLoggerOldLogFile(File file) throws Exception {
        Field oldLogFileField = PreyLogger.class.getDeclaredField("oldLogFile");
        oldLogFileField.setAccessible(true);
        oldLogFileField.set(null, file);
    }

    // =========================================================================
    // Happy path
    // =========================================================================

    @Test
    public void start_withLogFilePresentAndUploadOk_sendsStartedAndStoppedAndUploadsZippedLog()
            throws Exception {
        writeLog("hello-from-prey-logger");
        recordingWebServices.uploadResponseCode = HttpURLConnection.HTTP_OK;

        new Logretrieval().start(context, null, null);

        // Two notify calls: "started" (4-arg) then "stopped" (2-arg).
        assertEquals(2, recordingWebServices.notifyCalls.size());

        NotifyCall startedCall = recordingWebServices.notifyCalls.get(0);
        assertEquals("processed", startedCall.status);
        assertEquals("started", startedCall.params.get("status"));
        assertEquals("logretrieval", startedCall.params.get("target"));
        assertEquals("start", startedCall.params.get("command"));

        NotifyCall stoppedCall = recordingWebServices.notifyCalls.get(1);
        // The "stopped" notification uses the 2-arg overload that has neither
        // status nor correlationId.
        assertNull(stoppedCall.status);
        assertNull(stoppedCall.correlationId);
        assertEquals("stopped", stoppedCall.params.get("status"));
        assertEquals("logretrieval", stoppedCall.params.get("target"));

        // Upload should have happened exactly once with a valid zip.
        assertEquals(1, recordingWebServices.uploadCalls.size());
        UploadCall upload = recordingWebServices.uploadCalls.get(0);
        assertTrue(
                "Active segment must be present in the zip",
                upload.entries.containsKey("prey.log")
        );
        assertTrue(
                "Zip should contain the original log content",
                upload.entries.get("prey.log").contains("hello-from-prey-logger")
        );

        // The zip must be cleaned up in finally.
        assertFalse(
                "Logretrieval should delete the temporary zip from cache",
                cacheZipFile.exists()
        );
    }

    @Test
    public void start_withHttpCreatedResponse_isAlsoTreatedAsSuccess() throws Exception {
        writeLog("created-response-content");
        recordingWebServices.uploadResponseCode = HttpURLConnection.HTTP_CREATED;

        new Logretrieval().start(context, null, null);

        assertEquals(2, recordingWebServices.notifyCalls.size());
        assertEquals("started", recordingWebServices.notifyCalls.get(0).params.get("status"));
        assertEquals("stopped", recordingWebServices.notifyCalls.get(1).params.get("status"));
    }

    // =========================================================================
    // Failure paths
    // =========================================================================

    @Test
    public void start_withLogFileMissing_sendsStartedAndFailed_andDoesNotUpload() {
        // PreyLogger is initialized but no log content has been written, so the
        // file does not exist on disk.
        assertFalse(logFile.exists());

        new Logretrieval().start(context, null, null);

        assertEquals(2, recordingWebServices.notifyCalls.size());
        assertEquals("started", recordingWebServices.notifyCalls.get(0).params.get("status"));
        NotifyCall failedCall = recordingWebServices.notifyCalls.get(1);
        assertEquals("failed", failedCall.params.get("status"));
        assertEquals(
                "Failure reason should mention the missing log file",
                "prey.log not found",
                failedCall.params.get("reason")
        );
        assertEquals(
                "Upload must not be attempted when the source log is missing",
                0,
                recordingWebServices.uploadCalls.size()
        );
    }

    @Test
    public void start_withUploadHttpError_sendsFailedAndCleansUpZip() throws Exception {
        writeLog("doomed-upload");
        recordingWebServices.uploadResponseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;

        new Logretrieval().start(context, null, null);

        assertEquals(2, recordingWebServices.notifyCalls.size());
        NotifyCall failedCall = recordingWebServices.notifyCalls.get(1);
        assertEquals("failed", failedCall.params.get("status"));
        assertNotNull(failedCall.params.get("reason"));
        assertTrue(
                "Failure reason should expose the HTTP status code",
                failedCall.params.get("reason").contains("500")
        );
        assertFalse(
                "Zip must be deleted even when the upload fails",
                cacheZipFile.exists()
        );
    }

    @Test
    public void start_withUploadThrowing_sendsFailed_andCleansUpZip() throws Exception {
        writeLog("upload-throws");
        recordingWebServices.uploadShouldThrow = true;

        new Logretrieval().start(context, null, null);

        assertEquals(2, recordingWebServices.notifyCalls.size());
        assertEquals("failed", recordingWebServices.notifyCalls.get(1).params.get("status"));
        assertFalse(cacheZipFile.exists());
    }

    // =========================================================================
    // Parameter forwarding
    // =========================================================================

    @Test
    public void start_withMessageIdParameter_forwardsItAsCorrelationIdOnStartedNotification()
            throws Exception {
        writeLog("with-message-id");
        JSONObject params = new JSONObject();
        params.put(PreyConfig.MESSAGE_ID, "msg-42");

        new Logretrieval().start(context, null, params);

        NotifyCall startedCall = recordingWebServices.notifyCalls.get(0);
        assertEquals("msg-42", startedCall.correlationId);
    }

    @Test
    public void start_withMessageIdParameter_forwardsItOnFailedNotification() {
        // No log content written → start() will go through the failure path.
        assertFalse(logFile.exists());

        JSONObject params = new JSONObject();
        try {
            params.put(PreyConfig.MESSAGE_ID, "msg-fail");
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        new Logretrieval().start(context, null, params);

        NotifyCall failedCall = recordingWebServices.notifyCalls.get(1);
        assertEquals(
                "Failed notification should carry the original messageId so the panel can correlate it",
                "msg-fail",
                failedCall.correlationId
        );
    }

    @Test
    public void start_withJobIdParameter_includesDeviceJobIdReasonInStartedNotification()
            throws Exception {
        writeLog("with-job-id");
        JSONObject params = new JSONObject();
        params.put(PreyConfig.JOB_ID, "job-abc");

        new Logretrieval().start(context, null, params);

        NotifyCall startedCall = recordingWebServices.notifyCalls.get(0);
        assertEquals(
                "{\"device_job_id\":\"job-abc\"}",
                startedCall.params.get("reason")
        );
    }

    @Test
    public void start_withoutJobIdParameter_omitsReasonFromStartedNotification()
            throws Exception {
        writeLog("no-job-id");

        new Logretrieval().start(context, null, null);

        NotifyCall startedCall = recordingWebServices.notifyCalls.get(0);
        assertFalse(
                "When no jobId is provided the started notification should not include a reason",
                startedCall.params.containsKey("reason")
        );
    }

    @Test
    public void getMethod_delegatesToStart() throws Exception {
        writeLog("get-delegates-to-start");

        new Logretrieval().get(context, null, null);

        // get() is just a façade for start() — should produce a started + stopped pair.
        assertEquals(2, recordingWebServices.notifyCalls.size());
        assertEquals("started", recordingWebServices.notifyCalls.get(0).params.get("status"));
        assertEquals("stopped", recordingWebServices.notifyCalls.get(1).params.get("status"));
    }

    // =========================================================================
    // Rotated segment inclusion
    // =========================================================================

    @Test
    public void start_withRotatedSegmentPresent_includesBothSegmentsInZip() throws Exception {
        // Seed a rotated previous segment alongside the active one.
        try (FileWriter w = new FileWriter(oldLogFile)) {
            w.write("older-rotated-content");
        }
        setPreyLoggerOldLogFile(oldLogFile);
        writeLog("newest-content");

        new Logretrieval().start(context, null, null);

        assertEquals(1, recordingWebServices.uploadCalls.size());
        UploadCall upload = recordingWebServices.uploadCalls.get(0);
        assertTrue(
                "Active segment must be present in the zip",
                upload.entries.containsKey("prey.log")
        );
        assertTrue(
                "Rotated segment must be present so retrieval does not lose history",
                upload.entries.containsKey("prey.log.1")
        );
        assertTrue(
                "Active segment should contain the latest log content",
                upload.entries.get("prey.log").contains("newest-content")
        );
        assertEquals(
                "Rotated segment content should be preserved verbatim",
                "older-rotated-content",
                upload.entries.get("prey.log.1")
        );
    }

    @Test
    public void start_withOnlyRotatedSegmentPresent_stillUploadsThatSegment() throws Exception {
        // No active segment — only the rotated previous one. Logretrieval should
        // still surface what's available rather than treating it as "no log".
        try (FileWriter w = new FileWriter(oldLogFile)) {
            w.write("only-rotated-content");
        }
        setPreyLoggerOldLogFile(oldLogFile);
        // Active log file deliberately missing.
        setPreyLoggerLogFile(null);

        new Logretrieval().start(context, null, null);

        assertEquals(1, recordingWebServices.uploadCalls.size());
        UploadCall upload = recordingWebServices.uploadCalls.get(0);
        assertTrue(upload.entries.containsKey("prey.log.1"));
        assertEquals("only-rotated-content", upload.entries.get("prey.log.1"));
        assertEquals("stopped", recordingWebServices.notifyCalls.get(1).params.get("status"));
    }

    // =========================================================================
    // Private buildZip — exercised via reflection to verify the zip is well-formed
    // =========================================================================

    @Test
    public void buildZip_producesZipEntryNamedAfterSourceFile() throws Exception {
        File source = new File(context.getCacheDir(), "prey.log");
        try (FileWriter w = new FileWriter(source)) {
            w.write("zip-me-please");
        }
        try {
            File zip = invokeBuildZip(new Logretrieval(), source);
            try {
                assertTrue("buildZip should produce a real file", zip.exists());
                try (ZipFile zf = new ZipFile(zip)) {
                    ZipEntry entry = zf.getEntry("prey.log");
                    assertNotNull("Zip should contain a prey.log entry", entry);
                    assertEquals("zip-me-please", readEntry(zf, entry));
                }
            } finally {
                zip.delete();
            }
        } finally {
            source.delete();
        }
    }

    @Test
    public void buildZip_overwritesPreExistingZipInCacheDir() throws Exception {
        File source = new File(context.getCacheDir(), "prey.log");
        try (FileWriter w = new FileWriter(source)) {
            w.write("fresh-content");
        }
        // Pre-seed the target zip path with a non-zip payload to ensure
        // buildZip wipes it before writing.
        try (FileWriter w = new FileWriter(cacheZipFile)) {
            w.write("this is definitely not a zip");
        }
        try {
            File zip = invokeBuildZip(new Logretrieval(), source);
            try (ZipFile zf = new ZipFile(zip)) {
                assertNotNull(zf.getEntry("prey.log"));
            }
            zip.delete();
        } finally {
            source.delete();
        }
    }

    @SuppressWarnings("unchecked")
    private File invokeBuildZip(Logretrieval action, File... segments) throws Exception {
        Method buildZip = Logretrieval.class.getDeclaredMethod(
                "buildZip", Context.class, List.class);
        buildZip.setAccessible(true);
        List<File> list = new ArrayList<>();
        for (File f : segments) list.add(f);
        return (File) buildZip.invoke(action, context, list);
    }

    private static String readEntry(ZipFile zf, ZipEntry entry) throws IOException {
        try (InputStream is = zf.getInputStream(entry);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }
    }

    // =========================================================================
    // Helpers and fakes
    // =========================================================================

    private void writeLog(String content) throws Exception {
        try (FileWriter w = new FileWriter(logFile)) {
            w.write(content);
        }
        // Point PreyLogger at this file so Logretrieval.start() picks it up.
        // PreyLogger.d() inside start() will append additional entries, but
        // that's fine — the original content is what we assert on.
        setPreyLoggerLogFile(logFile);
    }

    /** A {@link PreyWebServices} that records calls instead of hitting the network. */
    private static class RecordingPreyWebServices extends PreyWebServices {

        final List<NotifyCall> notifyCalls = new ArrayList<>();
        final List<UploadCall> uploadCalls = new ArrayList<>();

        int uploadResponseCode = HttpURLConnection.HTTP_OK;
        boolean uploadShouldThrow = false;

        @Override
        public void sendNotifyActionResultPreyHttp(
                final Context ctx,
                final String status,
                final String correlationId,
                final Map<String, String> params) {
            // Deliberately synchronous: production spawns a Thread, but for
            // tests we want the call recorded inline so assertions are stable.
            notifyCalls.add(new NotifyCall(status, correlationId, copy(params)));
        }

        @Override
        public String sendNotifyActionResultPreyHttp(Context ctx, Map<String, String> params) {
            notifyCalls.add(new NotifyCall(null, null, copy(params)));
            return null;
        }

        @Override
        public int uploadLog(Context ctx, File file) throws PreyException {
            UploadCall call = new UploadCall();
            call.fileName = file == null ? null : file.getName();
            call.fileLength = file == null ? -1L : file.length();
            if (file != null && file.exists()) {
                try (ZipFile zf = new ZipFile(file)) {
                    java.util.Enumeration<? extends ZipEntry> entries = zf.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        try (InputStream is = zf.getInputStream(entry);
                             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                            byte[] buf = new byte[8192];
                            int n;
                            while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
                            call.entries.put(entry.getName(), out.toString("UTF-8"));
                        }
                    }
                } catch (IOException e) {
                    call.readError = e;
                }
            }
            uploadCalls.add(call);
            if (uploadShouldThrow) {
                throw new PreyException("simulated upload exception");
            }
            return uploadResponseCode;
        }

        private static Map<String, String> copy(Map<String, String> params) {
            return params == null ? new HashMap<>() : new HashMap<>(params);
        }
    }

    /** Snapshot of a single sendNotifyActionResultPreyHttp invocation. */
    private static class NotifyCall {
        final String status;
        final String correlationId;
        final Map<String, String> params;

        NotifyCall(String status, String correlationId, Map<String, String> params) {
            this.status = status;
            this.correlationId = correlationId;
            this.params = params;
        }
    }

    /** Snapshot of a single uploadLog invocation, including the unzipped payload. */
    private static class UploadCall {
        String fileName;
        long fileLength;
        final Map<String, String> entries = new HashMap<>();
        IOException readError;
    }
}
