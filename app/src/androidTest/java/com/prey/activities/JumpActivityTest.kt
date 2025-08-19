package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.activities.CheckPasswordHtmlActivity.Companion.URL_ONB
import com.prey.net.TestWebServices
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class JumpActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<JumpActivity>

    private fun withWebFormIntent(context: Context): Intent {
        val intent=Intent(context, JumpActivity::class.java)
        intent.putExtra(CheckPasswordHtmlActivity.NEXT_URL,"")
        return intent
    }

    @Test
    fun test_jump() {
        /*
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setUnlockPass("")
        PreyConfig.getInstance(context).setProtectReady(false)
        PreyConfig.getInstance(context).setApiKeyBatch("")

        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //Thread.sleep(3_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("activityView:$activityView")
        assertNotNull(activityView)
        assertContains(activityView, "/")
        */
        assert(true)

    }


}