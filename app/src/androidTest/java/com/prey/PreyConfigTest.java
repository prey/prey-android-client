/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;

import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.FakeWebServices;

import org.junit.Before;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test suite for the {@link PreyConfig} class.
 * <p>
 * This class contains unit tests that verify the behavior of the {@code PreyConfig} singleton,
 * specifically focusing on how its internal state changes in response to network operations.
 * It uses mock web services to simulate different HTTP response scenarios (e.g., success, failure)
 * and asserts that the configuration flags are updated correctly.
 */
public class PreyConfigTest {

    private SimpleDateFormat sdf;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    /**
     * Verifies that the {@code sendCompilation} flag in {@link PreyConfig} remains {@code true}
     * when a data-sending operation fails due to an HTTP error.
     *
     * <p><b>Scenario:</b> An operation is triggered to send compiled data to the server, and the
     * {@code sendCompilation} flag is initially set to {@code true}.
     *
     * <p><b>Test Steps:</b>
     * <ol>
     *     <li>The {@code sendCompilation} flag is explicitly set to {@code true}.</li>
     *     <li>A mock {@link PreyWebServices} is configured to simulate an HTTP server error
     *         (e.g., 500 Internal Server Error) upon receiving a data request.</li>
     *     <li>The {@code sendPreyHttpData} method is called to attempt sending the data.</li>
     *     <li>The test asserts that the {@code sendCompilation} flag is still {@code true} after
     *         the failed network attempt.</li>
     * </ol>
     *
     * <p><b>Expected Outcome:</b> The flag should not be reset to {@code false} on failure, ensuring
     * that the application will retry sending the compiled data in a subsequent operation.
     */
    @Test
    public void givenHttpError_whenSendingData_thenSendCompilationFlagRemainsTrue() {
        // Arrange: Set up the configuration and mock the web service response
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.sendCompilation(true);// Set the initial state

        // Create a mock HTTP response that simulates a server error
        PreyHttpResponse errorHttpResponse = new PreyHttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error");

        // Set up a mock web service to return the error response
        FakeWebServices mockWebServices = new FakeWebServices();
        mockWebServices.setPreyHttpResponse(errorHttpResponse);
        preyConfig.setWebServices(mockWebServices);

        // Prepare the data to be sent (even if its content isn't critical for this test)
        ArrayList<HttpDataService> dataToSend = createLocationDataPayload();

        // Act: Perform the action being tested - sending data which will result in an error
        preyConfig.getWebServices().sendPreyHttpData(context, dataToSend);

        // Assert: Verify the expected outcome
        assertTrue("The sendCompilation flag should remain true after a network error", preyConfig.isSendCompilation());
    }

    /**
     * Verifies that the {@code sendCompilation} flag in {@link PreyConfig} is reset to {@code false}
     * after data is successfully sent to the server.
     *
     * <p>This test simulates a scenario where the flag is initially {@code true}, indicating that a
     * data compilation needs to be sent. It then mocks a successful HTTP response (HTTP OK) from the
     * web service. After the data sending operation is performed, it asserts that the
     * {@code sendCompilation} flag has been correctly updated to {@code false}, confirming that the
     * system acknowledges the successful transmission and will not attempt to resend the compilation
     * unnecessarily.</p>
     */
    @Test
    public void whenDataIsSentSuccessfully_thenSendCompilationFlagIsReset() {
        // Arrange: Set up the configuration and mock the web service response
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.sendCompilation(true);// Set the initial state

        // Create a mock HTTP response that simulates a server ok
        PreyHttpResponse okHttpResponse = new PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK");

        // Set up a mock web service to return the error response
        FakeWebServices mockWebServices = new FakeWebServices();
        mockWebServices.setPreyHttpResponse(okHttpResponse);
        preyConfig.setWebServices(mockWebServices);

        // Prepare the data to be sent (even if its content isn't critical for this test)
        ArrayList<HttpDataService> dataToSend = createLocationDataPayload();

        // Act: Perform the action being tested - sending data which will result in an ok
        preyConfig.getWebServices().sendPreyHttpData(context, dataToSend);

        // Assert: Verify the expected outcome
        assertFalse("The sendCompilation flag should be false after a successful send", preyConfig.isSendCompilation());
    }


    /**
     * Creates a list of HttpDataService objects containing location data ready to be sent.
     *
     * @return An ArrayList containing the HttpDataService object. Returns an empty list if the input location is null.
     */
    public ArrayList<HttpDataService> createLocationDataPayload() {
        HttpDataService data = new HttpDataService("location");
        data.setList(true);
        HashMap<String, String> parametersMap = new HashMap<String, String>();
        PreyLocation lastLocation = null;
        try {
            lastLocation = new PreyLocation(0, 0, 0f, 0, sdf.parse("2023-09-03T18:29:56.000Z").getTime(), "native");
        } catch (Exception e) {
            PreyLogger.e("Failed to parse location date", e);
        }
        parametersMap.put(LocationUtil.LAT, Double.toString(lastLocation.getLat()));
        parametersMap.put(LocationUtil.LNG, Double.toString(lastLocation.getLng()));
        parametersMap.put(LocationUtil.ACC, Float.toString(Math.round(lastLocation.getAccuracy())));
        parametersMap.put(LocationUtil.METHOD, lastLocation.getMethod());
        data.addDataListAll(parametersMap);
        ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
        dataToBeSent.add(data);
        return dataToBeSent;
    }

}