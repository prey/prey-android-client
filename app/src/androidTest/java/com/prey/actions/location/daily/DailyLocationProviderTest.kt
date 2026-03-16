/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DailyLocationProviderTest {

    private val fusedClient = mockk<FusedLocationProviderClient>()
    private lateinit var provider: DailyLocationProvider

    @Test
    fun get_currentLocation_returns_location_on_success(): Unit = runBlocking {
        val mockLocation = mockk<Location>()
        val mockTask = mockk<Task<Location>>()
        //We simulated the behavior of the Google Play Services Task
        every { fusedClient.getCurrentLocation(any<CurrentLocationRequest>(), null) } returns mockTask
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<Location>>()
            listener.onSuccess(mockLocation)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask
    }
}