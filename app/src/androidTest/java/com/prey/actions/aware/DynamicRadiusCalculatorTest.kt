/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.location.Location
import org.junit.Test
import org.junit.Assert.*

class DynamicRadiusCalculatorTest {

    private fun location(lat: Double, lng: Double): Location {
        return Location("test").apply {
            latitude = lat
            longitude = lng
        }
    }

    @Test
    fun walking_speed_returns_300m() {
        val prev = location(-33.0, -70.0)
        val curr = location(-33.0005, -70.0005)
        val radius = DynamicRadiusCalculator.calculateRadius(
            prev,
            System.currentTimeMillis() - 10 * 60 * 1000,
            curr,
            System.currentTimeMillis()
        )
        assertEquals(300f, radius)
    }

    @Test
    fun car_speed_returns_larger_radius() {
        val prev = location(-33.0, -70.0)
        val curr = location(-33.02, -70.02)
        val radius = DynamicRadiusCalculator.calculateRadius(
            prev,
            System.currentTimeMillis() - 60 * 1000,
            curr,
            System.currentTimeMillis()
        )
        assertTrue(radius >= 1500f)
    }
}