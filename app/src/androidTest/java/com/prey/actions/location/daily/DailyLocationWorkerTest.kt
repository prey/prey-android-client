package com.prey.actions.location.daily

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.prey.PreyUtilsKt
import com.prey.net.PreyWebServicesKt
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DailyLocationWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        //We mocked the static objects/Kt files
        mockkObject(PreyUtilsKt)
        mockkObject(DailyLocationProvider)
        mockkObject(PreyWebServicesKt)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun dailyLocationWorker_Success_ReturnsSuccess() = runBlocking {
        //Simulate that there is internet access
        every { PreyUtilsKt.isNetworkAvailable(any()) } returns true
        //Simulate a location with good accuracy (e.g., 10 meters)
        val mockLocation = Location("gps").apply {
            latitude = -33.4489
            longitude = -70.6693
            accuracy = 10f
        }
        coEvery { DailyLocationProvider.fetchPreciseLocation(any()) } returns mockLocation
        //Simulate that the server successfully receives the location
        every { PreyWebServicesKt.sendDailyLocation(any(), any()) } returns true
        //Create the worker using the TestListenableWorkerBuilder
        val worker = TestListenableWorkerBuilder<DailyLocationWorker>(context).build()
        //Execute the work
        val result = worker.doWork()
        //Verify that the result is Success
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun dailyLocationWorker_BadAccuracy_ReturnsRetry() = runBlocking {
        every { PreyUtilsKt.isNetworkAvailable(any()) } returns true
        //Simulate location with poor accuracy (150 meters)
        val mockLocation = Location("gps").apply {
            accuracy = 150f
        }
        coEvery { DailyLocationProvider.fetchPreciseLocation(any()) } returns mockLocation
        val worker = TestListenableWorkerBuilder<DailyLocationWorker>(context).build()
        val result = worker.doWork()
        //Debe retornar retry porque 150 > 75
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun dailyLocationWorker_NoNetwork_ReturnsRetry() = runBlocking {
        //Pretend there is NO internet
        every { PreyUtilsKt.isNetworkAvailable(any()) } returns false
        val worker = TestListenableWorkerBuilder<DailyLocationWorker>(context).build()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }

}