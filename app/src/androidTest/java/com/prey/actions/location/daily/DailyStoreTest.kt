/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DailyStoreTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        DailyStore.removeSent(context)
    }

    @Test
    fun wasSentToday_returns_false_when_no_date_is_stored() {
        val result = DailyStore.wasSentToday(context)
        assertFalse(result)
    }

    @Test
    fun wasSentToday_returns_true_after_calling_markSent() {
        DailyStore.markSent(context)
        val result = DailyStore.wasSentToday(context)
        assertTrue(result)
    }

}