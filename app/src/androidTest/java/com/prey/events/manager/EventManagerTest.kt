package com.prey.events.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.test.platform.app.InstrumentationRegistry
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhoneKt
import com.prey.events.Event
import com.prey.net.PreyWebServicesKt
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventManagerTest {

    private lateinit var context: Context
    private val mockConfig = mockk<PreyConfig>(relaxed = true)

    @Before
    fun setUp() {
        context = spyk(InstrumentationRegistry.getInstrumentation().targetContext)

        // Mocking Singleton Objects
        mockkObject(PreyConfig.getPreyConfig(context))

        mockkObject(PreyPhoneKt)
        mockkObject(PreyWebServicesKt)

        // Default setup: Device registered
        every { PreyConfig.getPreyConfig(any()) } returns mockConfig
        every { mockConfig.isThisDeviceAlreadyRegisteredWithPrey() } returns true

        // Mute Loggers
        every { PreyLogger.d(any()) } returns Unit
        every { PreyLogger.e(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun process_cuandoNoEstaRegistrado_noHaceNada() = runTest {
        // Arrange
        every { mockConfig.isThisDeviceAlreadyRegisteredWithPrey() } returns false
        val event = Event(Event.TURNED_ON)

        // Act
        EventManager.process(context, event)

        // Assert
        verify(exactly = 0) { PreyPhoneKt.getWifi(any()) }
    }
/*
    @Test
    fun process_conWifiValido_actualizaEventoYEnvia() = runTest {
        // Arrange
        val event = Event(Event.TURNED_ON)

        // Mock ConnectivityManager para simular que estamos en WIFI
        val mockConnectivity = mockk<ConnectivityManager>()
        val mockNetworkInfo = mockk<NetworkInfo>()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivity
        every { mockConnectivity.activeNetworkInfo } returns mockNetworkInfo
        every { mockNetworkInfo.type } returns ConnectivityManager.TYPE_WIFI

        // Mock de datos de Wifi
        val mockWifi = mockk<PreyPhoneKt.WifiData>()
        every { mockWifi.ssid } returns "Prey_Guest_Network"
        every { PreyPhoneKt.getWifi(context) } returns mockWifi

        // Mock de envío exitoso
        coEvery { PreyWebServicesKt.sendPreyHttpEvent(any(), any(), any()) } returns "OK_RESPONSE"

        // Act
        EventManager.process(context, event)

        // Assert
        assertEquals("Prey_Guest_Network", event.info)
        coVerify(atLeast = 1) { PreyWebServicesKt.sendPreyHttpEvent(eq(context), eq(event), any()) }
    }

    @Test
    fun process_cuandoSsidEsInvalido_reintentaYFalla() = runTest {
        // Arrange
        val event = Event(Event.TURNED_ON)

        // Forzamos modo WIFI
        val mockConnectivity = mockk<ConnectivityManager>()
        val mockNetworkInfo = mockk<NetworkInfo>()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivity
        every { mockConnectivity.activeNetworkInfo } returns mockNetworkInfo
        every { mockNetworkInfo.type } returns ConnectivityManager.TYPE_WIFI

        // SSID inválido común en Android cuando no hay permisos o está conectando
        val mockWifi = mockk<PreyPhoneKt.WifiData>()
        every { mockWifi.ssid } returns "<unknown ssid>"
        every { PreyPhoneKt.getWifi(any()) } returns mockWifi

        // Act
        EventManager.process(context, event)

        // Assert
        // Debería haber intentado obtener el wifi varias veces (retrySuspending)
        verify(atLeast = 2) { PreyPhoneKt.getWifi(any()) }
        // No debería haber enviado nada porque isValid nunca fue true
        coVerify(exactly = 0) { PreyWebServicesKt.sendPreyHttpEvent(any(), any(), any()) }
    }

    @Test
    fun getBatteryData_retornaValoresCorrectos() {
        // Nota: Este test es difícil de mockear porque ACTION_BATTERY_CHANGED
        // es un sticky intent del sistema.
        // En un test instrumentado real, obtendrá la batería actual del dispositivo.

        // Act
        val batteryInfo = EventManager.getBatteryData(context)

        // Assert
        // Verificamos que al menos devuelva valores coherentes
        assert(batteryInfo.percentage in 0..100)
    }*/
}