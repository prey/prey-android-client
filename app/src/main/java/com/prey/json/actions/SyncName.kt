/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.prey.PreyLogger
import com.prey.events.Event
import com.prey.events.manager.EventManager
import com.prey.json.CommandTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Action responsible for synchronizing the device's name with the Prey web panel.
 *
 * This class retrieves the current device name, prioritizing the Bluetooth name defined in
 * system settings. If the Bluetooth name is unavailable, it falls back to a combination
 * of the device manufacturer and model. Once the name is resolved, it triggers a
 * [Event.DEVICE_RENAMED] event to notify the system of the change.
 */
class SyncName : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> scope.launch { start(context, options) }
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Starts the device name synchronization process.
     *
     * This function retrieves the current device name—preferring the Bluetooth name
     * if available—and reports it to the [EventManager] by triggering a
     * [Event.DEVICE_RENAMED] event.
     *
     * @param context The application context.
     * @param options A [JSONObject] containing command configuration options.
     */
    suspend fun start(context: Context, options: JSONObject) {
        val newName=getNameBluetoothOrNative(context)
        PreyLogger.d("SyncName - New name detected: $newName")
        val info = JSONObject().apply {
            put("new_name", newName)
        }
        val event = Event(Event.DEVICE_RENAMED, info.toString())
        EventManager.process(context, event)
    }

    /**
     * Retrieves the device name, prioritizing the Bluetooth name defined in system settings.
     * If the Bluetooth name is unavailable or blank, it returns a fallback string
     * composed of the device manufacturer and model.
     *
     * @param context The application context used to access the content resolver.
     * @return The Bluetooth name if found; otherwise, a string containing the manufacturer and model.
     */
    fun getNameBluetoothOrNative(context: Context): String {
        val bluetoothName = Settings.Secure.getString(context.contentResolver, "bluetooth_name")
        if (!bluetoothName.isNullOrBlank()) {
            return bluetoothName
        }
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return "$manufacturer $model"
    }

}