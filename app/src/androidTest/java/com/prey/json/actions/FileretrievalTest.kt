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
import com.prey.net.FakeWebServices
import com.prey.net.PreyWebServices
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class FileretrievalTest {

    private lateinit var context: Context
    private lateinit var fileRetrieval: Fileretrieval
    private lateinit var config: PreyConfig

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fileRetrieval = Fileretrieval()
        config = PreyConfig.getPreyConfig(context)
    }

    @Test
    fun executeStart_withValidParams_completesSuccessfully() = runTest {
        config.webServices = FakeWebServices()
        val options = JSONObject().apply {
            put(PreyConfig.MESSAGE_ID, "msg123")
            put(PreyConfig.JOB_ID, "job456")
            put("path", "test_file.txt")
            put("file_id", "file_uuid_999")
        }
        val tempFile = File(context.cacheDir, "test_file.txt")
        tempFile.writeText("Prey Test Content")
        mockkStatic(android.os.Environment::class)
        every { android.os.Environment.getExternalStorageDirectory() } returns context.cacheDir
        fileRetrieval.execute(context, "start", options)
        Thread.sleep(2000)
        tempFile.delete()
        config.webServices = PreyWebServices.getInstance()
        val containsActions = config.containsActions("start_fileretrieval_stopped")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun executeStart_withMissingFile_notifiesFailure() = runTest {
        val options = JSONObject().apply {
            put("path", "non_existent.txt")
            put("file_id", "id_1")
        }
        config.webServices = FakeWebServices()
        fileRetrieval.execute(context, "start", options)
        Thread.sleep(2000)
        config.webServices = PreyWebServices.getInstance()
        val containsActions = config.containsActions("start_fileretrieval_failed")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun executeStart_withMissingFileId_notifiesFailure() = runTest {
        val options = JSONObject().apply {
            put("path", "non_existent.txt")
        }
        config.webServices = FakeWebServices()
        fileRetrieval.execute(context, "start", options)
        Thread.sleep(2000)
        config.webServices = PreyWebServices.getInstance()
        val containsActions = config.containsActions("start_fileretrieval_failed")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

}