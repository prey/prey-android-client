/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.parser

import android.content.Context
import com.prey.PreyLogger
import com.prey.json.CommandTarget
import com.prey.json.actions.Alarm
import com.prey.json.actions.Alert
import com.prey.json.actions.Detach
import com.prey.json.actions.Fileretrieval
import com.prey.json.actions.Location
import com.prey.json.actions.Lock
import com.prey.json.actions.Ping
import com.prey.json.actions.Report
import com.prey.json.actions.Tree
import com.prey.json.actions.Triggers
import com.prey.json.actions.Wipe
import com.prey.net.PreyWebServicesKt
import com.prey.util.StringUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * A singleton object responsible for parsing and dispatching commands received in JSON format.
 *
 * This dispatcher acts as the central hub for handling actions pushed from the Prey server.
 * It can process single JSON command objects or arrays of them. For each command, it identifies
 * the appropriate target handler (e.g., `Alert`, `Lock`, `Wipe`), instantiates it, and executes
 * the specified command with its options.
 *
 * The core logic involves:
 * 1. Receiving a JSON string or fetching one from the web service.
 * 2. Parsing the JSON to extract the `target`, `command`, and `options`.
 * 3. Mapping the `target` string to a corresponding `CommandTarget` class using a predefined
 *    `allowedTargets` map for security and control.
 * 4. Dynamically creating an instance of the target class.
 * 5. Calling the `execute` method on the instance, passing the context, command, and options.
 *
 * All operations are launched in a coroutine on the `Dispatchers.IO` thread to avoid blocking
 * the main thread.
 */
object JsonCommandDispatcher {

    /**
     * A map that associates command target names received from the server with their
     * corresponding class implementations.
     *
     * This map acts as a whitelist, ensuring that only predefined and secure commands can be
     * instantiated and executed. The keys are the string representations of the targets (e.g., "Alert"),
     * and the values are the [KClass] references to the action classes that implement [CommandTarget].
     * The target name from the JSON is formatted (capitalized) before being used to look up a class in this map.
     */
    val allowedTargets = mapOf(
        "Alert" to Alert::class,
        "Location" to Location::class,
        "Alarm" to Alarm::class,
        "Lock" to Lock::class,
        "Detach" to Detach::class,
        "Fileretrieval" to Fileretrieval::class,
        "Wipe" to Wipe::class,
        "Tree" to Tree::class,
        "Report" to Report::class,
        "Triggers" to Triggers::class,
        "Ping" to Ping::class
    )

    /**
     * Parses a JSON command string and dispatches it for execution.
     *
     * This function processes the provided [jsonString] asynchronously on the [Dispatchers.IO] thread.
     */
    fun getActionsJson(context: Context, jsonString: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (jsonString == "" || jsonString == "OK") return@launch
            val json = JSONObject(jsonString)
            dispatchJson(context, json)
        }
    }

    /**
     * Fetches a JSON array of pending actions from the Prey web service and dispatches them.
     *
     * This function initiates a network request via [PreyWebServicesKt.getActionsJson] to retrieve
     * commands assigned to the device. If the response is not empty, it parses the resulting
     * string into a [JSONArray] and passes it to [dispatchArray] for individual processing.
     *
     * The operation is performed asynchronously within a coroutine scoped to [Dispatchers.IO]
     * to prevent blocking the calling thread during network I/O or JSON parsing.
     *
     * @param context The application context used for web service communication and action execution.
     */
    fun getActionsJsonArray(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val actions = PreyWebServicesKt.getActionsJson(context)
            if (!actions.isNullOrEmpty()) {
                val jsonArray = JSONArray(actions)
                dispatchArray(context, jsonArray)
            }
        }
    }

    /**
     * Iterates through a [JSONArray] of command objects and dispatches each one.
     *
     * This function processes multiple commands in sequence by extracting each [JSONObject]
     * from the array and passing it to [dispatchJson].
     *
     * @param context The application context.
     * @param jsonArray A [JSONArray] containing one or more command objects to be executed.
     */
    fun dispatchArray(context: Context, jsonArray: JSONArray) {
        PreyLogger.d("JsonCommandDispatcher jsonArray:${jsonArray}")
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            dispatchJson(context, json)
        }
    }

    /**
     * Parses a single JSON command and dispatches it to the appropriate target class for execution.
     *
     * This function extracts the 'target', 'command', and 'options' from the provided [JSONObject].
     * It then looks up the corresponding target class from a list of allowed targets, instantiates it,
     * and calls its `execute` method, passing the command and options.
     *
     * The JSON object is expected to have the following structure:
     * ```json
     * {
     *   "target": "TargetClassName",
     *   "command": "commandName",
     *   "options": { ... }
     * }
     * ```
     *
     * @param context The application context.
     * @param json The [JSONObject] containing the command to be executed.
     * @throws IllegalArgumentException if the specified 'target' is not found in the `allowedTargets` map,
     * or if the target class does not implement the [CommandTarget] interface.
     * @throws org.json.JSONException if required keys ('target', 'command') are missing from the [json] object.
     */
    fun dispatchJson(context: Context, json: JSONObject) {
        PreyLogger.d("JsonCommandDispatcher json:${json}")
        val targetName = json.getString("target")
        val command = json.getString("command")
        val options = json.optJSONObject("options") ?: JSONObject()
        val targetName2 = StringUtil.classFormat(targetName)
        PreyLogger.d("targetName:${targetName2}")
        PreyLogger.d("command:${command}")
        PreyLogger.d("options:${options}")
        val targetClass = allowedTargets[targetName2]
            ?: throw IllegalArgumentException("Target not allowed: $targetName2")
        val instance = targetClass.java.getDeclaredConstructor().newInstance()
        if (instance !is CommandTarget) {
            throw IllegalArgumentException("Target does not implement CommandTarget")
        }
        instance.execute(context, command, options)
    }
}