/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.exceptions.PreyException;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Interface defining the contract for web service interactions within the Prey application.
 * This includes operations like fetching device status and sending data to the Prey servers.
 */
public interface WebServices {

    /**
     * Asynchronously fetches the status.
     *
     * @param ctx The application context.
     * @return A Single that emits a JSONObject on success or an error.
     */
    public JSONObject getStatus(Context ctx) throws PreyException;

    /**
     * Asynchronously sends data to the server.
     *
     * @param ctx        The application context.
     * @param dataToSend The list of data to send.
     * @return A Single that emits a PreyHttpResponse on success or an error.
     */
    public PreyHttpResponse sendPreyHttpData(Context ctx, ArrayList<HttpDataService> dataToSend);

    /**
     * Asynchronously sends the device's location to the server.
     *
     * @param ctx       The application context.
     * @param jsonParam A JSONObject containing the location data to be sent.
     * @return A {@link PreyHttpResponse} object representing the server's response.
     */
    public PreyHttpResponse sendLocation(Context ctx,JSONObject jsonParam);

}