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
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationSenderTest {

    @Test
    fun sendLocationAware_must_call_PreyWebServices() = runBlocking {
        val context = mockk<Context>()
        val location = mockk<Location>()
        mockkObject(PreyWebServicesKt)
        coEvery {
            PreyWebServicesKt.doSendLocation(context, location, true)
        } returns true
        val result = LocationSender.sendLocationAware(context, location)
        assertTrue(result)
        coVerify(exactly = 1) {
            PreyWebServicesKt.doSendLocation(context, location, true)
        }
        unmockkObject(PreyWebServicesKt)
    }

}