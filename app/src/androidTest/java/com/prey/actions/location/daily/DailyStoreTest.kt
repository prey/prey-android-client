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

    @Test
    fun wasSentToday_returnsFalse_afterRemovingSent() {
        // Arrange
        DailyStore.markSent(context)
        // Act
        DailyStore.removeSent(context)
        val result = DailyStore.wasSentToday(context)
        // Assert
        assertFalse("It should revert to false after running removeSent", result)
    }

    @Test
    fun persistenceTest_dataIsStillThere_whenRechecking() {
        // Este test simula que la app se cierra y se abre (re-leyendo los SharedPreferences)
        DailyStore.markSent(context)
        val result = DailyStore.wasSentToday(context)
        assertTrue("The data must persist in SharedPreferences", result)
    }

}