package com.prey.events.receivers

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.prey.PreyConfig
import com.prey.events.Event
import com.prey.events.factories.EventFactory
import com.prey.events.manager.EventManager
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class EventReceiverTest {

    private lateinit var context: Context
    private lateinit var eventReceiver: EventReceiver
    private val mockConfig = mockk<PreyConfig>()


    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        eventReceiver = EventReceiver()


        // Mockeamos los objetos Singleton/Estáticos
        //mockkObject(PreyConfig)
        mockkObject(EventFactory)
        mockkObject(EventManager)

        val config= PreyConfig.getPreyConfig(context)
        config.apiKey="123"
        config.deviceId="123"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onReceive_cuandoElDispositivoNoEstaRegistrado_noProcesaNada() {
        // Arrange
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)



        // Act
        eventReceiver.onReceive(context, intent)

        // Assert
        // Verificamos que NUNCA se llamó a EventFactory si no está registrado
        verify(exactly = 0) { EventFactory.getEvent(any(), any()) }
        verify(exactly = 0) { EventManager.processCoroutine(any(), any()) }
    }

    @Test
    fun onReceive_cuandoEstaRegistradoYHayEvento_llamaAProcessCoroutine() {
        // Arrange
        val action = Intent.ACTION_POWER_CONNECTED
        val intent = Intent(action)
        val expectedEvent = Event(Event.POWER_CONNECTED)

        every { EventFactory.getEvent(any(), any()) } returns expectedEvent

        // Mockeamos la función que procesa el evento (asumimos que devuelve Unit)
        every { EventManager.processCoroutine(any(), any()) } returns Unit

        // Act
        eventReceiver.onReceive(context, intent)

        // Assert
        verify(exactly = 1) { EventFactory.getEvent(context, intent) }
        verify(exactly = 1) { EventManager.processCoroutine(context, expectedEvent) }
    }
/*
    @Test
    fun onReceive_cuandoIntentEsNull_retornaSilenciosamente() {
        // Act
        eventReceiver.onReceive(context, null)

        // Assert
        verify(exactly = 0) { PreyConfig.getPreyConfig(any()) }
    }

    @Test
    fun onReceive_cuandoFactoryRetornaNull_noLlamaAEventManager() {
        // Arrange
        val intent = Intent("ACCION_NO_SOPORTADA")
        every { EventFactory.getEvent(any(), any()) } returns null

        // Act
        eventReceiver.onReceive(context, intent)

        // Assert
        verify(exactly = 1) { EventFactory.getEvent(any(), any()) }
        verify(exactly = 0) { EventManager.processCoroutine(any(), any()) }
    }*/
}