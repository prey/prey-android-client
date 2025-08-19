package com.prey.actions.report

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.prey.PreyConfig
import com.prey.net.TestWebServices

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportActionTest {

    private lateinit var reportAction: ReportAction
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        reportAction = ReportAction()
    }

    @Test
    fun test_report() {
        PreyConfig.getInstance(context).setExcludeReport("")
        PreyConfig.getInstance(context).setMissing(true)
        reportAction.start(context, 10)
    }

}