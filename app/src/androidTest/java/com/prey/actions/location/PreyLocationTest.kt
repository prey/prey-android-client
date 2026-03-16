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
        val preyLoc = PreyLocation(mockLocation)
        assertEquals(-33.45, preyLoc.lat, 0.001)
        assertEquals(-70.66, preyLoc.lng, 0.001)
        assertEquals(10f, preyLoc.accuracy)
        assertEquals("native", preyLoc.method)
    }

    @Test
    fun isValid_should_return_false_if_latitude_or_longitude_is_zero() {
        val locCero = PreyLocation()
        locCero.lat = 0.0
        locCero.lng = 0.0
        assertFalse(locCero.isValid)
        locCero.lat = 10.5
        locCero.lng = 20.5
        assertTrue(locCero.isValid)
    }

}