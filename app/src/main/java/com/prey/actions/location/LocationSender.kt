/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.content.Context
import android.location.Location
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A thread-safe object responsible for sending location updates to the Prey servers.
 * It uses a [Mutex] to ensure that only one location update is sent at a time, preventing
 * race conditions and redundant network calls.
 */
object LocationSender {

    private val mutex = Mutex()

    /**
     * Sends the device's location to the Prey servers in a thread-safe manner.
     * This function ensures that only one location-sending operation can occur at a time,
     * preventing race conditions and multiple simultaneous network requests for location updates.
     * It uses a [Mutex] to serialize access to the underlying network call.
     *
     * @param context The application context, used for accessing system services and resources.
     * @param location The [Location] object containing the geographical data to be sent.
     * @return `true` if the location was sent successfully, `false` otherwise.
     */
    suspend fun sendLocationAware(
        context: Context,
        location: Location
    ): Boolean {
        return mutex.withLock {
            //Only one coroutine enters here at a time.
            PreyWebServicesKt.doSendLocation(context, location, true)
        }
    }

}