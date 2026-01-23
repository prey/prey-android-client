/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.actions.HttpDataService;
import com.prey.exceptions.PreyException;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Mock implementation of {@link WebServices} for testing purposes.
 * <p>
 * This class provides a way to simulate network responses without making actual HTTP requests.
 * It allows setting predefined responses for various web service calls, enabling isolated
 * and predictable testing of components that rely on {@code WebServices}.
 *
 * @see WebServices
 */
public class FakeWebServices implements WebServices {

    private PreyHttpResponse preyHttpResponse;
    private JSONObject status;

    /**
     * Sets the status JSON object to be returned by {@link #getStatus(Context)}.
     * This is used for testing purposes to simulate different responses from the server.
     *
     * @param status The JSON object representing the desired status.
     */
    public void setStatus(JSONObject status) {
        this.status = status;
    }

    /**
     * Sets the mock {@link PreyHttpResponse} to be returned by web service calls.
     * This is used for testing purposes to simulate different server responses.
     *
     * @param preyHttpResponse The {@link PreyHttpResponse} object to be used in tests.
     */
    public void setPreyHttpResponse(PreyHttpResponse preyHttpResponse) {
        this.preyHttpResponse = preyHttpResponse;
    }

    /**
     * Returns a predefined status {@link JSONObject}.
     * <p>
     * This mock implementation retrieves a previously set JSON object,
     * simulating a response from the server's status endpoint. The object
     * to be returned can be configured using the {@link #setStatus(JSONObject)} method.
     *
     * @param ctx The application context (unused in this mock implementation).
     * @return The predefined status {@link JSONObject}.
     * @throws PreyException In a real implementation, this would be thrown on network or server errors.
     *                       This mock version does not throw it but includes it for signature compliance.
     * @see WebServices#getStatus(Context)
     */
    @Override
    public JSONObject getStatus(Context ctx) throws PreyException {
        return status;
    }

    /**
     * Simulates sending data to the Prey server and returns a predefined response.
     * <p>
     * In this mock implementation, it does not actually send any data over the network. Instead, it
     * inspects the status code of the preset {@link PreyHttpResponse}. If the status code is
     * {@link HttpURLConnection#HTTP_OK}, it simulates a successful data compilation by updating
     * the {@link PreyConfig}. It then returns the predefined {@link PreyHttpResponse} set via
     * {@link #setPreyHttpResponse(PreyHttpResponse)}.
     *
     * @param ctx        The application context (unused in this mock implementation).
     * @param dataToSend A list of {@link HttpDataService} objects representing the data that would be sent (unused).
     * @return The predefined {@link PreyHttpResponse} set for this mock object.
     * @see #setPreyHttpResponse(PreyHttpResponse)
     */
    @Override
    public PreyHttpResponse sendPreyHttpData(Context ctx, ArrayList<HttpDataService> dataToSend) {
        int statusCode = preyHttpResponse.getStatusCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            PreyConfig.getPreyConfig(ctx).sendCompilation(false);
        }
        return preyHttpResponse;
    }

    @Override
    public PreyHttpResponse sendLocation(Context ctx, JSONObject jsonParam) {
        return preyHttpResponse;
    }

}