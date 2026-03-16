/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AwareGeofenceReceiverTest {

    private lateinit var context: Context
    private lateinit var receiver: AwareGeofenceReceiver

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        receiver = AwareGeofenceReceiver()
    }

    @Test
    fun onReceive_handles_geofence_intent() {
        val intent = Intent("GEOFENCE_EVENT")
        receiver.onReceive(context, intent)
        // no crash
        assertTrue(true)
    }

}