/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.prey.PreyConfig
import com.prey.PreyLogger
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TreeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_get_tree_ok() {
        val config = PreyConfig.getPreyConfig(context)
        config.deleteActions();
        val options = JSONObject().apply {
            put(Tree.PATH, "sdcard")
            put(Tree.DEPTH, 1)
            put(PreyConfig.MESSAGE_ID, "12345")
        }
        PreyLogger.d("test Tree().get(context, jsonObjet)")
        Tree().execute(context, BaseAction.CMD_GET, options)
        Thread.sleep(5000)
        Assert.assertTrue(config.containsActions("get_tree_started"));
        Assert.assertTrue(config.containsActions("get_tree_stopped"));
    }

}