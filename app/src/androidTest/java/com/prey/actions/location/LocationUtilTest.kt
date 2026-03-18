/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationUtilTest {

    @Test
    fun round_must_limit_to_6_decimals() {
        val longValue = -33.123456789
        val result = LocationUtil.round(longValue)
        assertEquals(-33.123457, result, 0.000001)
    }

    @Test
    fun distance_must_return_0_if_a_location_is_null() {
        val preyLocation = PreyLocation()
        val distance = LocationUtil.distance(preyLocation, null)
        assertEquals(0.0, distance, 0.0)
    }

    @Test
    fun save_must_guard_values_in_SharedPreferences() {
        val context = mockk<Context>()
        val prefs = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()
        val location = mockk<android.location.Location>()
        every { location.latitude } returns 1.0
        every { location.longitude } returns 2.0
        every { location.accuracy } returns 3f
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putFloat(any(), any()) } returns editor
        every { editor.apply() } just Runs
        LocationUtil.save(context, location)
        verify { editor.putFloat("lat", 1.0f) }
        verify { editor.putFloat("lng", 2.0f) }
        verify { editor.apply() }
    }

}