/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager

import android.content.Context
import android.net.wifi.WifiManager

import com.prey.events.Event
import com.prey.events.retrieves.EventRetrieveDataBattery
import com.prey.events.retrieves.EventRetrieveDataMinWifi
import com.prey.events.retrieves.EventRetrieveDataMobile
import com.prey.events.retrieves.EventRetrieveDataNullMobile
import com.prey.events.retrieves.EventRetrieveDataOnline
import com.prey.events.retrieves.EventRetrieveDataUptime
import com.prey.events.retrieves.EventRetrieveDataWifi
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone
import com.prey.managers.PreyConnectivityManager

import org.json.JSONObject

/**
 * EventManager is responsible for handling events and sending data to the server.
 *
 * @param context The application context.
 */
class EventManager(var context: Context) {
    private var dataMap: EventMap<String, JSONObject?> = EventMap()
    var event: Event? = null

    /**
     * Execute an event and send data to the server.
     *
     * @param event The event to execute.
     */
    fun execute(event: Event) {
        val isDeviceRegistered = isThisDeviceAlreadyRegisteredWithPrey(context)
        val previousSsid = PreyConfig.getInstance(context).getPreviousSsid()
        var ssid = ""
        var validation = true
        var type_connect = ""
        if (Event.WIFI_CHANGED == event.name) {
            if (event.info!!.indexOf("wifi") > 0) {
                for (i in 0..29) {
                    Thread.sleep(1000)
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    ssid = wifiInfo.ssid
                    PreyLogger.d("i[$i]ssid:$ssid")
                    if ("<unknown ssid>" != ssid) {
                        type_connect = "wifi"
                        break
                    }
                }
            }
            if (event.info!!.indexOf(MOBILE) > 0) {
                var isMobileConnected = false
                for (i in 0..9) {
                    Thread.sleep(1000)
                    isMobileConnected =
                        PreyConnectivityManager.getInstance().isMobileConnected(context)
                    PreyLogger.d("i[$i]isMobileConnected:$isMobileConnected")
                    if (isMobileConnected) {
                        val networkClass = PreyPhone.getInstance(context).getNetworkClass(context)
                        if (networkClass != null) {
                            PreyConfig.getInstance(context).setPreviousSsid(networkClass)
                            event.name = Event.MOBILE_CONNECTED
                            event.info = networkClass
                            type_connect = "mobile"
                        }
                        break
                    }
                }
                validation = isMobileConnected
            }
        }
        if (event.isAlwaysSend) {
            validation = true
        }
        val isMobileConnected = PreyConnectivityManager.getInstance().isMobileConnected(context)

        var i = 0
        while (isMobileConnected && i < 10) {
            val networkClass = PreyPhone.getInstance(context).getNetworkClass(context)
            if (networkClass != null) {
                PreyConfig.getInstance(context).setPreviousSsid(networkClass)
            }
            type_connect = "mobile"
            break
            i++
        }
        PreyLogger.d("EVENT name:${event.name} info:${event.info} ssid[${ssid}] previousSsid[${previousSsid}]")
        PreyLogger.d("validation:$validation")
        if (validation) {
            PreyLogger.d("EVENT change PreviousSsid:$ssid")
            PreyConfig.getInstance(context).setPreviousSsid(ssid)
            if (isDeviceRegistered) {
                PreyLogger.d("EVENT isDeviceRegistered event: ${event.name} type_connect:${type_connect}")
                this.dataMap = EventMap()
                this.event = event
                if (Event.DEVICE_STATUS == event.name) {
                    if (MOBILE == type_connect) {
                        dataMap[MOBILE] = null
                    } else {
                        dataMap[WIFI] = null
                    }
                } else {
                    dataMap[MOBILE] = null
                    dataMap[WIFI] = null
                    dataMap[ONLINE] = null
                    dataMap[UPTIME] = null
                }
                dataMap[BATTERY] = null
                if (Event.DEVICE_STATUS == event.name) {
                    if (MOBILE == type_connect) {
                        EventRetrieveDataMobile().execute(context, this)
                    } else {
                        EventRetrieveDataMinWifi().execute(context, this)
                    }
                } else {
                    if (MOBILE == type_connect) EventRetrieveDataMobile().execute(
                        context,
                        this
                    )
                    else EventRetrieveDataNullMobile().execute(context, this)
                    EventRetrieveDataWifi().execute(context, this)
                    EventRetrieveDataOnline().execute(context, this)
                    EventRetrieveDataUptime().execute(context, this)
                }
                EventRetrieveDataBattery().execute(context, this)
            }
        }
    }

    /**
     * This function is called when the EventManager receives data.
     * It stores the data in the dataMap and checks if all the required data is available.
     * If all the data is available, it calls the sendEvents function.
     *
     * @param key The key of the data.
     * @param data The data to be stored.
     */
    fun receivesData(key: String, data: JSONObject?) {
        dataMap[key] = data
        if (dataMap.isCompleteData()) {
            sendEvents()
        }
    }

    /**
     * This function is called to send the events.
     * It checks if the dataMap is not null and if the event is not null.
     * It then converts the dataMap to a JSONObject.
     * If the event name is BATTERY_LOW and the locationLowBattery is enabled,
     * it checks if the LocationLowBatteryRunner is valid.
     * If it is valid, it starts a new thread for the LocationLowBatteryRunner.
     * It also adds the "locationLowBattery" field to the JSONObject.
     * If the event name is DEVICE_STATUS, it sets the info of the event to the JSONObject.
     * If the event name is not WIFI_CHANGED or the event name is not the same as the lastEvent,
     * it sets the lastEvent to the event name and starts a new EventThread.
     */
    private fun sendEvents() {

        val jsonObjectStatus = dataMap.toJSONObject()
        PreyLogger.d("jsonObjectStatus: $jsonObjectStatus")
        if (event != null) {
            val lastEvent = PreyConfig.getInstance(context).getLastEvent()
            PreyLogger.d("event name[${event!!.name}] lastEvent:${lastEvent}")
            if (Event.BATTERY_LOW == event!!.name) {
                if (PreyConfig.getInstance(context).isLocationLowBattery()) {
                    PreyLogger.d(
                        "LocationLowBatteryRunner.isValid(context):${
                            LocationLowBatteryRunner.getInstance(context).isValid()
                        }"
                    )
                    if (LocationLowBatteryRunner.getInstance(context).isValid()) {
                        Thread(LocationLowBatteryRunner(context)).start()
                        try {
                            jsonObjectStatus.put("locationLowBattery", true)
                        } catch (e: Exception) {
                            PreyLogger.e("Error put:${e.message}", e)
                        }
                    }
                }
            }
            if (Event.DEVICE_STATUS == event!!.name) {
                event!!.info = jsonObjectStatus.toString()
            }
            if (Event.WIFI_CHANGED != event!!.name || event!!.name != lastEvent) {
                PreyConfig.getInstance(context).setLastEvent(event!!.name!!)
                PreyLogger.d("event name[${event!!.name}], info[${event!!.info}]")
                EventThread(context, event!!, jsonObjectStatus).start()
            }
        }
    }

    /**
     * This function checks if the device is already registered with Prey.
     *
     * @param context The application context.
     * @return True if the device is already registered, false otherwise.
     */
    private fun isThisDeviceAlreadyRegisteredWithPrey(context: Context): Boolean {
        return PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey()
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