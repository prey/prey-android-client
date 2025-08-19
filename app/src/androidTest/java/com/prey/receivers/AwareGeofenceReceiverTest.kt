package com.prey.receivers

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.Geofence

import com.prey.PreyConfig
import com.prey.PreyLogger

import com.prey.net.TestWebServices
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AwareGeofenceReceiverTest {

    private lateinit var context: Context
    private val awareGeofenceReceiver = AwareGeofenceReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_geofence_transition_exit() {
        val latitude = -33.608284
        val longitude = -70.695053
        val location: Location = Location("")
        location.setLatitude(latitude)
        location.setLongitude(longitude)
        location.setAccuracy(100.0f)
        location.setAltitude(100.0)
        val GEOFENCE_RADIUS_IN_METERS = 1609f
        val geofence = Geofence.Builder()
        geofence.setRequestId("1")
        geofence.setCircularRegion(
            latitude,
            longitude,
            GEOFENCE_RADIUS_IN_METERS
        )
        val array = ArrayList<Geofence>()
        array.add(geofence.build())
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        val intent = Intent()
        //  intent.putExtra("gms_error_code",0)
        intent.putExtra(
            "com.google.android.location.intent.extra.transition",
            Geofence.GEOFENCE_TRANSITION_EXIT
        )
        intent.putExtra("com.google.android.location.intent.extra.triggering_location", location)
        val var5: java.util.ArrayList<*> = java.util.ArrayList<Any?>(0)
        intent.putExtra("com.google.android.location.intent.extra.geofence_list", var5)
        awareGeofenceReceiver.onReceive(context, intent)
        val preyLocation = PreyConfig.getInstance(context).getLocationAware()
        PreyLogger.i("lat:${preyLocation!!.getLat()}")
        PreyLogger.i("lng:${preyLocation!!.getLng()}")
        assertEquals("${latitude}", "${preyLocation!!.getLat()}")
        assertEquals("${longitude}", "${preyLocation!!.getLng()}")
    }

}