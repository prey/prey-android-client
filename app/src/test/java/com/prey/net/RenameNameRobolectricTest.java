/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyName;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PreyWebServices#renameName(Context, String, String, String, int)}.
 *
 * Spins up two {@link MockWebServer} instances acting as primary and fallback
 * hosts, then verifies the orchestration: success on primary, fallback on
 * primary failure (4xx, 5xx, timeout, connection refused), local device-name
 * persistence, and total time bounding when the primary times out.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class RenameNameRobolectricTest {

    private static final int TEST_TIMEOUT_MS = 500;
    private static final String DEVICE_KEY = "test-device-key";

    private Context context;
    private PreyConfig preyConfig;
    private PreyWebServices webServices;

    private MockWebServer primaryServer;
    private MockWebServer fallbackServer;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        // Reset PreyConfig singleton so we get a fresh instance bound to the test context.
        Field cached = PreyConfig.class.getDeclaredField("cachedInstance");
        cached.setAccessible(true);
        cached.set(null, null);
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setApiKey("test-api-key");
        preyConfig.setDeviceId(DEVICE_KEY);
        preyConfig.setDeviceName("");

        webServices = PreyWebServices.getInstance();

        primaryServer = new MockWebServer();
        fallbackServer = new MockWebServer();
        primaryServer.start();
        fallbackServer.start();
    }

    @After
    public void tearDown() throws IOException {
        primaryServer.shutdown();
        fallbackServer.shutdown();
    }

    // =========================================================================
    // Primary host succeeds
    // =========================================================================

    @Test
    public void givenPrimaryReturns200_thenSucceedsAndFallbackNotHit() {
        primaryServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        fallbackServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        PreyName result = webServices.renameName(
                context, "NewName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
        assertEquals(1, primaryServer.getRequestCount());
        assertEquals(0, fallbackServer.getRequestCount());
    }

    @Test
    public void givenPrimaryReturns200_thenDeviceNameIsSavedLocally() {
        primaryServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        webServices.renameName(
                context, "MyLaptop", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertEquals("MyLaptop", preyConfig.getDeviceName());
    }

    // =========================================================================
    // Primary fails → fallback used
    // =========================================================================

    @Test
    public void givenPrimaryReturns422_thenFallsBackToFallbackHost() {
        primaryServer.enqueue(new MockResponse().setResponseCode(422).setBody("{\"error\":\"bad\"}"));
        fallbackServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        PreyName result = webServices.renameName(
                context, "NewName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
        assertEquals(1, primaryServer.getRequestCount());
        assertEquals(1, fallbackServer.getRequestCount());
        assertEquals("NewName", preyConfig.getDeviceName());
    }

    @Test
    public void givenPrimaryReturns500_thenFallsBackToFallbackHost() {
        primaryServer.enqueue(new MockResponse().setResponseCode(500).setBody("boom"));
        fallbackServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        PreyName result = webServices.renameName(
                context, "NewName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
        assertEquals(1, fallbackServer.getRequestCount());
    }

    @Test
    public void givenPrimaryTimesOut_thenFallsBackToFallbackHost() {
        // Primary holds the response longer than the test timeout.
        primaryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .setHeadersDelay(TEST_TIMEOUT_MS * 4L, TimeUnit.MILLISECONDS));
        fallbackServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        long start = System.currentTimeMillis();
        PreyName result = webServices.renameName(
                context, "NewName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
        assertEquals(1, fallbackServer.getRequestCount());
        // Bound: primary timeout (~500ms) + fast fallback. Should never wait the full 2s primary delay.
        assertTrue("renameName took too long: " + elapsed + "ms", elapsed < TEST_TIMEOUT_MS * 4);
    }

    @Test
    public void givenPrimaryConnectionRefused_thenFallsBackToFallbackHost() {
        // Use a port that is reserved/unbound so the connect attempt fails fast.
        fallbackServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        String unreachablePrimary = "http://127.0.0.1:1/";

        PreyName result = webServices.renameName(
                context, "NewName", unreachablePrimary, baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
        assertEquals(1, fallbackServer.getRequestCount());
    }

    // =========================================================================
    // Both hosts fail
    // =========================================================================

    @Test
    public void givenPrimaryAndFallbackBothReturnError_thenCodeIsNotOk() {
        primaryServer.enqueue(new MockResponse().setResponseCode(500).setBody("boom"));
        fallbackServer.enqueue(new MockResponse().setResponseCode(500).setBody("boom"));

        PreyName result = webServices.renameName(
                context, "NewName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertNotEquals(HttpURLConnection.HTTP_OK, result.getCode());
        assertEquals(1, primaryServer.getRequestCount());
        assertEquals(1, fallbackServer.getRequestCount());
    }

    @Test
    public void givenBothHostsFail_thenDeviceNameIsNotChanged() {
        preyConfig.setDeviceName("OriginalName");
        primaryServer.enqueue(new MockResponse().setResponseCode(500).setBody("boom"));
        fallbackServer.enqueue(new MockResponse().setResponseCode(422).setBody("bad"));

        webServices.renameName(
                context, "AttemptedName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertEquals("OriginalName", preyConfig.getDeviceName());
    }

    @Test
    public void givenBothHostsTimeOut_thenCodeIsNotOk() {
        primaryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .setHeadersDelay(TEST_TIMEOUT_MS * 4L, TimeUnit.MILLISECONDS));
        fallbackServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .setHeadersDelay(TEST_TIMEOUT_MS * 4L, TimeUnit.MILLISECONDS));

        PreyName result = webServices.renameName(
                context, "NewName", baseUrl(primaryServer), baseUrl(fallbackServer), TEST_TIMEOUT_MS);

        assertNotEquals(HttpURLConnection.HTTP_OK, result.getCode());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** PreyWebServices appends "devices/<key>.json" to (baseUrl + apiV2). */
    private String baseUrl(MockWebServer server) {
        // server.url("/") returns "http://127.0.0.1:PORT/"; renameName then concatenates
        // FileConfigReader.getApiV2() ("api/v2/") and the device-key path.
        return server.url("/").toString();
    }
}
