/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ApplicationProvider
import com.prey.PreyConfig
import com.prey.PreyLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LocationTest {

    private var context: Context? = null
    private val mockJson = mockk<JSONObject>(relaxed = true)
    private lateinit var config: PreyConfig

    @Before
    fun setup() {
        config = PreyConfig.getPreyConfig(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test_location_fails_without_permissions() = runBlocking{
        context = mockk<Context>(relaxed = true)
        mockkStatic(Location::class)
        mockkStatic(ActivityCompat::class)
        config.deleteActions();
        every {
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        val permission = ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        PreyLogger.d("test permission:${permission}")
        Location.get(context!!, mockJson)
        Thread.sleep(2000)
        val containsActions = config.containsActions("get_location_failed_permission_denied")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun test_location_ok_with_permissions() = runBlocking{
        context = ApplicationProvider.getApplicationContext()
        config.deleteActions();
        PreyLogger.d("test location.get(context, jsonObjet)")
        Location.get(context!!, JSONObject())
        Thread.sleep(4000)
        val containsActions = config.containsActions("get_location_stopped")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

}