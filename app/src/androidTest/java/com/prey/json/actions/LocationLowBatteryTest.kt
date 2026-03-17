package com.prey.json.actions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.FakeWebServices
import com.prey.net.PreyHttpResponse
import com.prey.net.PreyWebServices
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class LocationLowBatteryTest {


    private lateinit var context: Context
    private lateinit var config: PreyConfig
    private val mockJson = mockk<JSONObject>(relaxed = true)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        config = PreyConfig.getPreyConfig(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun get_shouldSendLocation_whenPermissionIsGranted(): Unit = runBlocking {
        config.deleteActions()
        val mockLocation = mockk<android.location.Location> {
            every { latitude } returns -33.45
            every { longitude } returns -70.66
            every { accuracy } returns 15.0f
        }
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val mockWebServices = mockk<FakeWebServices>(relaxed = true)
        //We mocked the PreyWebServices Singleton
        mockkObject(mockWebServices)
        //We mocked Prey's Location class to return a null location
        mockkObject(com.prey.json.actions.Location)
        coEvery { com.prey.json.actions.Location.getLocation(context) } returns mockLocation
        mockkStatic(Location::class)
        mockkStatic(ActivityCompat::class)
        val permission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        PreyLogger.d("test permission:${permission}")
        // Create a mock HTTP response that simulates a server ok
        val okHttpResponse = PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
        mockWebServices.setPreyHttpResponse(okHttpResponse)
        config.webServices = mockWebServices
        LocationLowBattery.get(context, mockJson)
        Thread.sleep(2000)
        config.webServices = PreyWebServices.getInstance()
        config.showActions()
        val containsActions = config.containsActions("get_location_low_battery_stopped")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun get_shouldSendLocation_whenPermissionIsDenied(): Unit = runBlocking {
        config.deleteActions()
        context = mockk<Context>(relaxed = true)
        mockkStatic(Location::class)
        mockkStatic(ActivityCompat::class)
        every {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        val permission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        PreyLogger.d("test permission:${permission}")
        LocationLowBattery.get(context, mockJson)
        Thread.sleep(500)
        config.showActions()
        val containsActions = config.containsActions("get_location_low_battery_failed_not_permission")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun get_shouldAbort_whenLocationIsNull(): Unit = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val mockWebServices = mockk<FakeWebServices>(relaxed = true)
        //We mocked the PreyWebServices Singleton
        mockkObject(mockWebServices)
        //We mocked Prey's Location class to return a null location
        mockkObject(com.prey.json.actions.Location)
        coEvery { com.prey.json.actions.Location.getLocation(context) } returns null
        config.deleteActions()
        LocationLowBattery.get(context, JSONObject())
        Thread.sleep(500)
        config.showActions()
        val containsActions = config.containsActions("get_location_low_battery_failed_not_data")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

    @Test
    fun get_shouldNotSendLocation(): Unit = runBlocking {
        config.deleteActions()
        val fakeWebServices=FakeWebServices()
        // Create a mock HTTP response that simulates a server error
        val okHttpResponse = PreyHttpResponse(HttpURLConnection.HTTP_UNAUTHORIZED, "")
        fakeWebServices.setPreyHttpResponse(okHttpResponse)
        config.webServices = fakeWebServices
        LocationLowBattery.get(context, JSONObject())
        Thread.sleep(500)
        config.webServices = PreyWebServices.getInstance()
        config.showActions()
        val containsActions = config.containsActions("get_location_low_battery_failed")
        PreyLogger.d("test containsActions: $containsActions")
        Assert.assertTrue(containsActions);
    }

}