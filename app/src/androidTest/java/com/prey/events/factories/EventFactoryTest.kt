package com.prey.events.factories

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.prey.actions.triggers.BatteryTriggerReceiver
import com.prey.actions.triggers.SimTriggerReceiver
import com.prey.events.Event
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EventFactoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext


        // Mockeamos los Receivers que se instancian dentro de la factory
        mockkConstructor(BatteryTriggerReceiver::class)
        mockkConstructor(SimTriggerReceiver::class)

        every { anyConstructed<BatteryTriggerReceiver>().onReceive(any(), any()) } returns Unit
        every { anyConstructed<SimTriggerReceiver>().onReceive(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getEvent_cuandoEsBootCompleted_retornaEventoTurnedOn() {
        // Arrange
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        // Act
        val event = EventFactory.getEvent(context, intent)

        // Assert
        assertNotNull(event)
        assertEquals(Event.TURNED_ON, event?.name)
    }

    @Test
    fun getEvent_cuandoLaBateriaEstaBaja_llamaAlReceiverYRetornaEvento() {
        // Arrange
        val intent = Intent(Intent.ACTION_BATTERY_LOW)

        // Act
        val event = EventFactory.getEvent(context, intent)

        // Assert
        assertNotNull(event)
        assertEquals(Event.BATTERY_LOW, event?.name)
        // Verificamos que se llamó al receiver interno
        verify { anyConstructed<BatteryTriggerReceiver>().onReceive(context, intent) }
    }

    @Test
    fun getEvent_cuandoAccionEsDesconocida_retornaNull() {
        // Arrange
        val intent = Intent("ACCION_INVENTADA_CUALQUIERA")

        // Act
        val event = EventFactory.getEvent(context, intent)

        // Assert
        assertNull(event)
    }


}