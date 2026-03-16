/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AwareStoreTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun save_and_load_location() {
        val location = Location("test").apply {
            latitude = -33.45
            longitude = -70.66
        }
        AwareStore.save(context, location)
        val stored = AwareStore.load(context)
        assertNotNull(stored)
        assertEquals(location.latitude, stored!!.location.latitude, 0.0001)
        assertEquals(location.longitude, stored.location.longitude, 0.0001)
        assertTrue(stored.time > 0)
    }

    @Test
    fun load_returns_null_if_no_data() {
        AwareStore.remove(context)
        val stored = AwareStore.load(context)
        assertNull(stored)
    }

}