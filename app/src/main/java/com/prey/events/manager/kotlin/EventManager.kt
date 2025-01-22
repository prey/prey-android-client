/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager.kotlin

import android.content.Context
import android.net.wifi.WifiManager
import com.prey.events.factories.kotlin.EventFactory
import com.prey.events.kotlin.Event
import com.prey.events.retrieves.kotlin.EventRetrieveDataBattery
import com.prey.events.retrieves.kotlin.EventRetrieveDataMinWifi
import com.prey.events.retrieves.kotlin.EventRetrieveDataMobile
import com.prey.events.retrieves.kotlin.EventRetrieveDataNullMobile
import com.prey.events.retrieves.kotlin.EventRetrieveDataOnline
import com.prey.events.retrieves.kotlin.EventRetrieveDataUptime
import com.prey.events.retrieves.kotlin.EventRetrieveDataWifi
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPhone
import com.prey.managers.kotlin.PreyConnectivityManager
import org.json.JSONObject

class EventManager(var ctx: Context) {
    private var mapData: EventMap<String, JSONObject?>? = null
    var event: Event? = null

    fun execute(event: Event) {
        val isDeviceRegistered = isThisDeviceAlreadyRegisteredWithPrey(ctx)
        val previousSsid = PreyConfig.getInstance(ctx).getPreviousSsid()
        var ssid = ""
        var validation = true
        var type_connect = ""
        if (Event.WIFI_CHANGED == event.name) {
            if (event.info!!.indexOf("wifi") > 0) {
                for (i in 0..29) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                    }
                    val wifiManager = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    ssid = wifiInfo.ssid
                    PreyLogger.d("i[$i]ssid:$ssid")
                    if ("<unknown ssid>" != ssid) {
                        type_connect = "wifi"
                        break
                    }
                }
            }
            if (ssid != null && "" != ssid && ssid != previousSsid && "<unknown ssid>" != ssid && "0x" != ssid) {
                validation = true
                object : Thread() {
                    override fun run() {
                        try {
                            EventFactory.sendLocationAware(ctx)
                        } catch (e3: Exception) {
                        }
                    }
                }.start()
            } else {
                validation = false
            }
            if (event.info!!.indexOf(MOBILE) > 0) {
                var isMobileConnected = false
                for (i in 0..9) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                    }
                    isMobileConnected = PreyConnectivityManager.getInstance().isMobileConnected(ctx)
                    PreyLogger.d("i[$i]isMobileConnected:$isMobileConnected")
                    if (isMobileConnected) {
                        val networkClass = PreyPhone.getInstance(ctx)!!.getNetworkClass(ctx)
                        PreyConfig.getInstance(ctx).setPreviousSsid(networkClass)
                        event.name = Event.MOBILE_CONNECTED
                        event.info = networkClass
                        type_connect = "mobile"
                        break
                    }
                }
                validation = isMobileConnected
            }
        }
        if (event.isAlwaysSend) {
            validation = true
        }
        val isMobileConnected = PreyConnectivityManager.getInstance().isMobileConnected(ctx)

        var i = 0
        while (isMobileConnected && i < 10) {
            val networkClass = PreyPhone.getInstance(ctx)!!.getNetworkClass(ctx)
            PreyConfig.getInstance(ctx).setPreviousSsid(networkClass)
            type_connect = "mobile"
            break
            i++
        }

        PreyLogger.d("EVENT name:" + event.name + " info:" + event.info + " ssid[" + ssid + "] previousSsid[" + previousSsid + "]")
        PreyLogger.d("validation:$validation")
        if (validation) {
            PreyLogger.d("EVENT change PreviousSsid:$ssid")
            PreyConfig.getInstance(ctx).setPreviousSsid(ssid)
            if (isDeviceRegistered) {
                PreyLogger.d("EVENT isDeviceRegistered event: " + event.name + " type_connect:" + type_connect)
                this.mapData = EventMap()
                this.event = event
                if (Event.DEVICE_STATUS == event.name) {
                    if (MOBILE == type_connect) {
                        mapData!![MOBILE] = null
                    } else {
                        mapData!![WIFI] = null
                    }
                } else {
                    mapData!![MOBILE] = null
                    mapData!![WIFI] = null
                    mapData!![ONLINE] = null
                    mapData!![UPTIME] = null
                }
                mapData!![BATTERY] = null
                if (Event.DEVICE_STATUS == event.name) {
                    if (MOBILE == type_connect) {
                        EventRetrieveDataMobile().execute(ctx, this)
                    } else {
                        EventRetrieveDataMinWifi().execute(ctx, this)
                    }
                } else {
                    if (MOBILE == type_connect) EventRetrieveDataMobile().execute(
                        ctx,
                        this
                    )
                    else EventRetrieveDataNullMobile().execute(ctx, this)
                    EventRetrieveDataWifi().execute(ctx, this)
                    EventRetrieveDataOnline().execute(ctx, this)
                    EventRetrieveDataUptime().execute(ctx, this)
                }
                EventRetrieveDataBattery().execute(ctx, this)
            }
        }
    }

    fun receivesData(key: String, data: JSONObject?) {
        mapData!![key] = data
        if (mapData!!.isCompleteData) {
            sendEvents()
        }
    }

    private fun sendEvents() {
        if (mapData != null) {
            val jsonObjectStatus = mapData!!.toJSONObject()
            PreyLogger.d("jsonObjectStatus: $jsonObjectStatus")
            if (event != null) {
                val lastEvent = PreyConfig.getInstance(ctx).getLastEvent()
                PreyLogger.d("event name[" + event!!.name + "] lastEvent:" + lastEvent)
                if (Event.BATTERY_LOW == event!!.name) {
                    if (PreyConfig.getInstance(ctx).isLocationLowBattery()) {
                        PreyLogger.d(
                            "LocationLowBatteryRunner.isValid(ctx):" + LocationLowBatteryRunner.getInstance(
                                ctx
                            ).isValid(
                                ctx
                            )
                        )
                        if (LocationLowBatteryRunner.getInstance(ctx).isValid(ctx)) {
                            Thread(LocationLowBatteryRunner(ctx)).start()
                            try {
                                jsonObjectStatus.put("locationLowBattery", true)
                            } catch (e: Exception) {
                                PreyLogger.e("Error put:" + e.message, e)
                            }
                        }
                    }
                }
                if (Event.DEVICE_STATUS == event!!.name) {
                    event!!.info = jsonObjectStatus.toString()
                }
                if (Event.WIFI_CHANGED != event!!.name || event!!.name != lastEvent) {
                    PreyConfig.getInstance(ctx).setLastEvent(event!!.name)
                    PreyLogger.d("event name[" + event!!.name + "], info[" + event!!.info + "]")
                    EventThread(ctx, event!!, jsonObjectStatus).start()
                }
            }
        }
    }

    private fun isThisDeviceAlreadyRegisteredWithPrey(ctx: Context): Boolean {
        return PreyConfig.getInstance(ctx).isThisDeviceAlreadyRegisteredWithPrey()
    }

    companion object {
        const val ONLINE: String = "online"
        const val WIFI: String = "wifi"
        const val UPTIME: String = "uptime"
        const val PRIVATE_IP: String = "privateip"
        const val BATTERY: String = "battery"
        const val MOBILE: String = "mobile"
    }
}