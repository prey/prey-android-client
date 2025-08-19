package com.prey

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PreyConfigTest {

    private lateinit var context: Context
    private lateinit var config: PreyConfig

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        config = PreyConfig.getInstance(context)
    }

    @Test
    fun test_set_and_get_page() {
        config.setPage("testPage")
        assertEquals("testPage", config.getPage())
    }

    @Test
    fun test_set_and_get_api_key() {
        config.setApikey("test-api-key")
        assertEquals("test-api-key", config.getApiKey())
    }

    @Test
    fun test_set_and_get_input_webview() {
        val config = PreyConfig.getInstance(context)
        config.setInputWebview("test-input")
        assertEquals("test-input", config.getInputWebview())
    }

    @Test
    fun test_minutes_to_query_server() {
        config.setMinutesToQueryServer(30)
        assertEquals(30, config.getMinutesToQueryServer())
    }

}