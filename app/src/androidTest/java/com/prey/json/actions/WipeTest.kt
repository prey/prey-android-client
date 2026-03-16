/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.backwardcompatibility.FroyoSupport
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class WipeTest {

    private val context = mockk<Context>(relaxed = true)
    private val froyoSupport = mockk<FroyoSupport>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(FroyoSupport::class)
        every { FroyoSupport.getInstance(context) } returns froyoSupport
    }

    @Test
    fun test_factory_reset_is_called_when_option_is_true() = runBlocking {
        val options = JSONObject().apply { put("factory_reset", "true") }
        Wipe().start(context, options)
        //We verified that the FroyoSupport wipe method is being called
        verify(timeout = 1000) { froyoSupport.wipe() }
    }

    @Test
    fun test_factory_reset() = runBlocking {
        val options = JSONObject().apply { put("factory_reset", "true") }
        val froyoSupport = mockk<FroyoSupport>(relaxed = true)
        mockkStatic(FroyoSupport::class)
        every { FroyoSupport.getInstance(context) } returns froyoSupport
        Wipe().start(context, options)
        verify(timeout = 2000) { froyoSupport.wipe() }
    }

}