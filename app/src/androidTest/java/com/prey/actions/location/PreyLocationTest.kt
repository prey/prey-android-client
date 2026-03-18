/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.location.Location
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class PreyLocationTest {

    @Test
    fun constructor_with_Location_must_map_fields_correctly() {
        val mockLocation = mockk<Location>()
        every { mockLocation.latitude } returns -33.45
        every { mockLocation.longitude } returns -70.66
        every { mockLocation.accuracy } returns 10f
        every { mockLocation.altitude } returns 500.0
        val preyLocation = PreyLocation(mockLocation)
        assertEquals(-33.45, preyLocation.lat, 0.001)
        assertEquals(-70.66, preyLocation.lng, 0.001)
        assertEquals(10f, preyLocation.accuracy)
        assertEquals("native", preyLocation.method)
    }

    @Test
    fun isValid_should_return_false_if_latitude_or_longitude_is_zero() {
        val preyLocation = PreyLocation()
        preyLocation.lat = 0.0
        preyLocation.lng = 0.0
        assertFalse(preyLocation.isValid)
        preyLocation.lat = 10.5
        preyLocation.lng = 20.5
        assertTrue(preyLocation.isValid)
    }

}