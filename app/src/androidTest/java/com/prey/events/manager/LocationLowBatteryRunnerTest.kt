package com.prey.events.manager

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prey.PreyConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationLowBatteryRunnerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        val config = PreyConfig.getPreyConfig(context)
        config.locationLowBatteryDate = 0L
    }

    @Test
    fun isValid_returnsTrue_whenFirstTime() {
        // Ejecución
        val result = LocationLowBatteryRunner.isValid(context)
        // Verificación
        assertTrue("Debería ser válido la primera vez (valor 0)", result)
        // Verificar que se guardó el timestamp actual (aproximadamente)
        val savedTime = PreyConfig.getPreyConfig(context).locationLowBatteryDate
        assertTrue(savedTime > 0)
    }

    @Test
    fun isValid_returnsFalse_whenCalledTwiceInShortTime() {
        // 1. Primera llamada para setear el tiempo actual
        LocationLowBatteryRunner.isValid(context)
        // 2. Segunda llamada inmediata
        val result = LocationLowBatteryRunner.isValid(context)
        // Verificación
        assertFalse("No debería permitir una segunda ejecución en menos de 3 horas", result)
    }

    @Test
    fun isValid_returnsTrue_afterThreeHours() {
        val config = PreyConfig.getPreyConfig(context)
        // Forzamos un tiempo de hace 4 horas
        val fourHoursAgo = System.currentTimeMillis() - (4 * 60 * 60 * 1000L)
        config.locationLowBatteryDate = fourHoursAgo
        // Ejecución
        val result = LocationLowBatteryRunner.isValid(context)
        // Verificación
        assertTrue("Debería ser válido si ya pasaron más de 3 horas", result)
    }

}