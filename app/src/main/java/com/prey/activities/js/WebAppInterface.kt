/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js


import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.widget.Toast

import com.prey.FileConfigReader
import com.prey.PreyAccountData
import com.prey.PreyApp
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyName
import com.prey.PreyPermission
import com.prey.PreyUtils
import com.prey.R
import com.prey.actions.location.LastLocationService
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.activities.CloseActivity
import com.prey.activities.LoginActivity
import com.prey.activities.PanelWebActivity
import com.prey.activities.PasswordHtmlActivity
import com.prey.activities.PreReportActivity
import com.prey.activities.SecurityActivity
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.barcodereader.BarcodeActivity
import com.prey.json.UtilJson
import com.prey.json.actions.Detach
import com.prey.json.actions.Location
import com.prey.net.PreyWebServices
import com.prey.preferences.RunBackgroundCheckBoxPreference
import com.prey.services.PreyDisablePowerOptionsService
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreySecureService

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * WebAppInterface is a class that provides a JavaScript interface for the web app.
 * It allows the web app to access and manipulate the device's data and settings.
 */
class WebAppInterface {
    private lateinit var context: Context
    private lateinit var activity: CheckPasswordHtmlActivity
    private var wrongPasswordIntents = 0
    private var error: String? = null
    private var noMoreDeviceError = false
    private var from = "setting"
    private var preySecureService: PreySecureService? = null
    private var preyLockHtmlService: PreyLockHtmlService? = null

    /**
     * Constructor for WebAppInterface.
     *
     * @param context Context of the application
     */
    constructor(context: Context) {
        this.context = context
    }

    /**
     * Constructor for WebAppInterface.
     *
     * @param context Context of the application
     * @param activity Activity associated with the web app
     */
    constructor(context: Context, activity: CheckPasswordHtmlActivity) {
        this.context = context
        this.activity = activity
    }

    /**
     * Constructor for WebAppInterface.
     *
     * @param context Context of the application
     * @param activity Activity associated with the web app
     */
    constructor(context: Context, activity: PasswordHtmlActivity) {
        this.context = context
    }

    /**
     * Constructor for WebAppInterface.
     *
     * @param context Context of the application
     * @param preyLockHtmlService Prey lock HTML service instance
     */
    constructor(context: Context, preyLockHtmlService: PreyLockHtmlService) {
        this.context = context
        this.preyLockHtmlService = preyLockHtmlService
    }

    /**
     * Constructor for WebAppInterface.
     *
     * @param context Context of the application
     * @param service Prey secure service instance
     */
    constructor(context: Context, service: PreySecureService) {
        this.context = context
        this.preySecureService = service
    }

    /**
     * Gets the device's data as a JSON string.
     *
     * @return JSON string containing the device's data
     */
    @JavascriptInterface
    fun getData(): String {
        val config = PreyConfig.getInstance(context)
        val ssid = config.getSsid()
        val model = config.getModel()
        val imei = config.getImei()
        val location = config.getLocation()
        val lat = location?.let { LastLocationService.round(it.getLat()).toString() } ?: ""
        val lng = location?.let { LastLocationService.round(it.getLng()).toString() } ?: ""
        val publicIp = config.getPublicIp()?.trim() ?: ""
        val json =
            "{\"lat\":\"$lat\",\"lng\":\"$lng\",\"ssid\":\"$ssid\",\"public_ip\":\"$publicIp\",\"imei\":\"$imei\",\"model\": \"$model\"}"
        PreyLogger.d("getData:$json")
        return json
    }

    /**
     * Verifies the lock.
     */
    @JavascriptInterface
    fun verifyLock() {
    }

    /**
     * Initializes the background setting.
     *
     * @return True if the background setting is enabled, false otherwise
     */
    @JavascriptInterface
    fun initBackground(): Boolean {
        val initBackground: Boolean = PreyConfig.getInstance(context).getRunBackground()
        PreyLogger.d("initBackground:$initBackground")
        return initBackground
    }

    /**
     * Initializes the biometric setting.
     *
     * @return True if the biometric setting is enabled, false otherwise
     */
    @JavascriptInterface
    fun initUseBiometric(): Boolean {
        val useBiometric: Boolean = PreyConfig.getInstance(context).getUseBiometric()
        PreyLogger.d("useBiometric:$useBiometric")
        return useBiometric
    }

    /**
     * Checks if biometric authentication is supported on the device.
     *
     * @return True if biometric authentication is supported, false otherwise.
     */
    @JavascriptInterface
    fun initShowBiometric(): Boolean {
        val showBiometric: Boolean = PreyPermission.checkBiometricSupport(context)
        PreyLogger.d("showBiometric:$showBiometric")
        return showBiometric
    }

    /**
     * Checks if a PIN number is set on the device.
     *
     * @return True if a PIN number is set, false otherwise.
     */
    @JavascriptInterface
    fun initPin(): Boolean {
        val pinNumber: String? = PreyConfig.getInstance(context).getPinNumber()
        val initPin = pinNumber != null && pinNumber.isNotEmpty()
        PreyLogger.d("initPin:$initPin")
        return initPin
    }

    /**
     * Checks if the device administrator is active.
     *
     * @return True if the device administrator is active, false otherwise.
     */
    @JavascriptInterface
    fun initAdminActive(): Boolean {
        return FroyoSupport.getInstance(context).isAdminActive() ?: false
    }

    /**
     * Checks if the app has permission to draw overlays on the device.
     *
     * @return True if the app has permission to draw overlays, false otherwise.
     */
    @JavascriptInterface
    fun initDrawOverlay(): Boolean {
        return PreyPermission.canDrawOverlays(context)
    }

    /**
     * Checks if the accessibility service is enabled on the device.
     *
     * @return True if the accessibility service is enabled, false otherwise.
     */
    @JavascriptInterface
    fun initAccessibility(): Boolean {
        val initAccessibility: Boolean = PreyPermission.isAccessibilityServiceEnabled(context)
        PreyLogger.d("initAccessibility:$initAccessibility")
        return initAccessibility
    }

    /**
     * Checks if the app has permission to access the device's location.
     *
     * @return True if the app has permission to access the device's location, false otherwise.
     */
    @JavascriptInterface
    fun initLocation(): Boolean {
        val initLocation =
            (PreyPermission.canAccessFineLocation(context) || PreyPermission.canAccessCoarseLocation(
                context
            ))
        PreyLogger.d("initLocation:$initLocation")
        return initLocation
    }

    /**
     * Checks if the app has permission to access the device's location in the background.
     *
     * @return True if the app has permission to access the device's location in the background, false otherwise.
     */
    @JavascriptInterface
    fun initBackgroundLocation(): Boolean {
        val initBackgroundLocation: Boolean = PreyPermission.canAccessBackgroundLocation(context)
        PreyLogger.d("initBackgroundLocation:$initBackgroundLocation")
        return initBackgroundLocation
    }

    /**
     * Checks if the device is running Android 10 or above.
     *
     * @return True if the device is running Android 10 or above, false otherwise.
     */
    @JavascriptInterface
    fun initAndroid10OrAbove(): Boolean {
        val isAndroid10OrAbove: Boolean = PreyConfig.getInstance(context).isAndroid10OrAbove()
        PreyLogger.d("isAndroid10OrAbove:$isAndroid10OrAbove")
        return isAndroid10OrAbove
    }

    /**
     * Checks if the app has permission to access the device's camera.
     *
     * @return True if the app has permission to access the device's camera, false otherwise.
     */
    @JavascriptInterface
    fun initCamera(): Boolean {
        return PreyPermission.canAccessCamera(context)
    }

    /**
     * Checks if the app has permission to write to the device's storage.
     *
     * @return True if the app has permission to write to the device's storage, false otherwise.
     */
    @JavascriptInterface
    fun initWriteStorage(): Boolean {
        return PreyPermission.canAccessStorage(context)
    }

    /**
     * Returns the PIN number set on the device.
     *
     * @return The PIN number as a string.
     */
    @JavascriptInterface
    fun pin(): String {
        val pin: String? = PreyConfig.getInstance(context).getPinNumber()
        PreyLogger.d("getPin:$pin")
        return pin!!
    }

    /**
     * Checks if the app uninstallation is blocked.
     *
     * @return True if the app uninstallation is blocked, false otherwise.
     */
    @JavascriptInterface
    fun initUninstall(): Boolean {
        val initUnis: Boolean = PreyConfig.getInstance(context).getBlockAppUninstall()
        PreyLogger.d("initUninstall:$initUnis")
        return initUnis
    }

    /**
     * Checks if the power options are disabled.
     *
     * @return True if the power options are disabled, false otherwise.
     */
    @JavascriptInterface
    fun initShield(): Boolean {
        val initShi: Boolean = PreyConfig.getInstance(context).getDisablePowerOptions()
        PreyLogger.d("initShield:$initShi")
        return initShi
    }

    /**
     * Gets the scheduled minute.
     *
     * @return The scheduled minute as a string.
     */
    @JavascriptInterface
    fun initScheduler(): String {
        val initScheduler = PreyConfig.getInstance(context).getMinuteScheduled()
        return "${initScheduler}"
    }

    /**
     * Changes the scheduled minute.
     *
     * @param minuteScheduled The new scheduled minute.
     */
    @JavascriptInterface
    fun changeScheduler(minuteScheduled: String) {
        PreyLogger.d("changeScheduler:$minuteScheduled")
    }

    /**
     * Starts the report activity.
     */
    @JavascriptInterface
    fun report() {
        PreyLogger.d("report:")
        val intent = Intent(context, PreReportActivity::class.java)
        context.startActivity(intent)
        if (activity != null) activity.finish()
    }

    /**
     * Starts the security activity.
     */
    @JavascriptInterface
    fun security() {
        PreyLogger.d("security:")
        val intent = Intent(context, SecurityActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (activity != null) activity.finish()
    }

    /**
     * Reloads the CheckPasswordHtmlActivity.
     */
    @JavascriptInterface
    fun reload() {
        PreyLogger.d("reload:")
        val intent = Intent(context, CheckPasswordHtmlActivity::class.java)
        context.startActivity(intent)
        if (activity != null) activity.finish()
    }

    /**
     * Saves the PIN number.
     *
     * @param pin The PIN number to save.
     */
    @JavascriptInterface
    fun savePin(pin: String) {
        PreyLogger.d("savepin:$pin")
        PreyConfig.getInstance(context).setPinNumber(pin)
    }

    /**
     * Logs a message.
     *
     * @param log The message to log.
     */
    @JavascriptInterface
    fun log(log: String) {
        PreyLogger.d("log:$log")
    }

    /**
     * Logs in to the account.
     *
     * @param email The email address to log in with.
     * @param password The password to log in with.
     * @return The result of the login action as a string.
     */
    @JavascriptInterface
    fun mylogin(email: String, password: String): String {
        PreyLogger.d("mylogin email:${email} password:${password}")
        try {
            noMoreDeviceError = false
            error = null
            PreyConfig.getInstance(context).setError("")
            var errorConfig: String? = null
            object : Thread() {
                override fun run() {
                    try {
                        val accountData = PreyWebServices.getInstance()
                            .registerNewDeviceToAccount(
                                context,
                                email,
                                password,
                                PreyUtils.getDeviceType(context)
                            )
                        PreyConfig.getInstance(context).saveAccount(accountData!!)
                    } catch (e: Exception) {
                        PreyLogger.d("mylogin error2:${e.message}")
                        PreyConfig.getInstance(context).setError(e.message!!)
                    }
                }
            }.start()
            var isAccount = false
            var i = 0
            do {
                Thread.sleep(1000)
                errorConfig = PreyConfig.getInstance(context).getError()
                isAccount = PreyConfig.getInstance(context).isAccount()
                PreyLogger.d("mylogin [${i}] isAccount:${isAccount}")
                i++
            } while (i < 30 && !isAccount && errorConfig == null)
            isAccount = PreyConfig.getInstance(context).isAccount()
            if (!isAccount) {
                if (errorConfig != null && "" != errorConfig) {
                    error = errorConfig
                } else {
                    val json = JSONObject()
                    json.put(
                        "error",
                        JSONArray().put(context.getString(R.string.error_communication_exception))
                    )
                    error = json.toString()
                }
            } else {
                PreyConfig.getInstance(context).registerC2dm()
                PreyConfig.getInstance(context).setEmail(email)
                PreyConfig.getInstance(context).setRunBackground(true)
                PreyConfig.getInstance(context).setInstallationStatus("")
                PreyApp().run(context)
                Location().get(context, null, null)
            }
        } catch (e: Exception) {
            PreyLogger.d("mylogin error1:${e.message}")
            error = e.message
        }
        if (error == null) {
            error = ""
        }
        PreyLogger.d("mylogin out error:${error}")
        return error!!
    }

    /**
     * Checks if the time password is okay.
     *
     * @return true if the time password is okay, false otherwise
     */
    @JavascriptInterface
    fun isTimePasswordOk(): Boolean {
        val isTimePasswordOk: Boolean = PreyConfig.getInstance(context).isTimePasswordOk()
        PreyLogger.d("isTimePasswordOk:$isTimePasswordOk")
        return isTimePasswordOk
    }

    /**
     * Validates a new name.
     *
     * @param name the new name to validate
     * @return an empty string (TODO: what is the purpose of this method?)
     */
    @JavascriptInterface
    fun newName(name: String): String {
        PreyLogger.d("newName:$name")
        PreyWebServices.getInstance().validateName(context, name)
        return ""
    }

    /**
     * Logs in with the given password and type.
     *
     * @param password the password to use for login
     * @param password2 the second password to use for login (if two-step enabled)
     * @param type the type of login (e.g. "setting", "rename")
     * @return a JSON string containing the login result or an error message
     */
    @JavascriptInterface
    fun login_tipo(password: String, password2: String, type: String): String? {
        PreyLogger.d("login_tipo password:${password} password2:${password2} tipo:${type}")
        from = type
        var error: String? = null
        var isPasswordValid = false
        try {
            val apikey: String? = PreyConfig.getInstance(context).getApiKey()
            val isTwoStepEnabled: Boolean = PreyConfig.getInstance(context).getTwoStep()
            PreyLogger.d("login_tipo isTwoStepEnabled:${isTwoStepEnabled}")
            if (isTwoStepEnabled) {
                PreyLogger.d("login_tipo apikey:${apikey} password:${password} password2:${password2}")
                isPasswordValid = PreyWebServices.getInstance()
                    .checkPassword2(context, apikey!!, password, password2)
            } else {
                PreyLogger.d("login_tipo apikey:${apikey} password:${password}")
                isPasswordValid =
                    PreyWebServices.getInstance().checkPassword(context, apikey!!, password)
            }
            if (isPasswordValid) {
                PreyConfig.getInstance(context).setTimePasswordOk()
            }
        } catch (e1: Exception) {
            PreyLogger.e("login_tipo error1:${e1.message}", e1)
            error = e1.message!!
        }
        PreyLogger.d("login_tipo isPasswordValid:${isPasswordValid}")
        PreyLogger.d("login_tipo error:${error}")
        try {
            if (error != null) {
                if (!error.contains("{")) {
                    val json = JSONObject()
                    json.put("error", JSONArray().put(error))
                    error = json.toString()
                }
                return error
            } else if (!isPasswordValid) {
                wrongPasswordIntents++
                val errorMessage = if (wrongPasswordIntents == 3) {
                    context.getString(R.string.password_intents_exceed)
                } else {
                    context.getString(R.string.password_wrong)
                }
                return JSONObject().apply {
                    put("error", JSONArray().put(errorMessage))
                }.toString()
            } else {
                PreyLogger.d("login_tipo from:${from}")
                if ("setting" == from || "rename" == from) {
                    return JSONObject().apply {
                        put("result", true)
                    }.toString()
                } else {
                    val intent = Intent(context, PanelWebActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)
                    if (activity != null) activity.finish()
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("error:${e.message}", e)
        }
        return error
    }

    /**
     * Opens the PanelWebActivity.
     */
    @JavascriptInterface
    fun openPanelWeb() {
        val apikey = PreyConfig.getInstance(context).getApiKey()
        PreyWebServices.getInstance().getToken(context, apikey!!, "X")
        val intent = Intent(context, PanelWebActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        if (activity != null) activity.finish()
    }

    /**
     * Initializes the 4-digit PIN.
     *
     * @return The 4-digit PIN as a string.
     */
    @JavascriptInterface
    fun initPin4(): String {
        val initPin4: String? = PreyConfig.getInstance(context).getPinNumber()
        PreyLogger.d("initPin4:$initPin4")
        return initPin4!!
    }

    /**
     * Initializes the Prey version.
     *
     * @return The Prey version as a string.
     */
    @JavascriptInterface
    fun initVersion(): String {
        val initVersion: String? = PreyConfig.getInstance(context).getPreyVersion()
        PreyLogger.d("initVersion:$initVersion")
        return initVersion!!
    }

    /**
     * Checks if the device is a Xiaomi device.
     *
     * @return True if the device is a Xiaomi device, false otherwise.
     */
    @JavascriptInterface
    fun initXiaomi(): Boolean {
        val initXiaomi = "Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
        PreyLogger.d("Manufacter:" + Build.MANUFACTURER + " initXiaomi:" + initXiaomi)
        return initXiaomi
    }

    /**
     * Checks if the device is a Huawei device.
     *
     * @return True if the device is a Huawei device, false otherwise.
     */
    @JavascriptInterface
    fun initHuawei(): Boolean {
        val initHuawei = "huawei".equals(Build.MANUFACTURER, ignoreCase = true)
        PreyLogger.d("Manufacter:${Build.MANUFACTURER} initHuawei:${initHuawei}")
        return initHuawei
    }

    /**
     * Initializes the verification process.
     *
     * @return Always returns false (TODO: implement verification logic)
     */
    @JavascriptInterface
    fun initVerify(): Boolean {
        PreyLogger.d("initVerify users:")
        return false
    }

    /**
     * Sets the background mode.
     *
     * @param background True to enable background mode, false to disable it.
     */
    @JavascriptInterface
    fun setBackground(background: Boolean) {
        PreyConfig.getInstance(context).setRunBackground(background)
    }

    /**
     * Sets the use of biometric authentication.
     *
     * @param useBiometric True to enable biometric authentication, false to disable it.
     */
    @JavascriptInterface
    fun setUseBiometric(useBiometric: Boolean) {
        PreyConfig.getInstance(context).setUseBiometric(useBiometric)
    }

    /**
     * Wipes the device data.
     */
    @JavascriptInterface
    fun wipe() {
        PreyLogger.d("wipe")
        val dialog = AlertDialog.Builder(activity)
            .setMessage(R.string.preferences_detach_summary)
            .setPositiveButton(
                R.string.yes
            ) { view, _ ->
                detachDevice()
                view.dismiss()
            }
            .setNegativeButton(
                R.string.no
            ) { view, _ ->
                view.dismiss()
            }
            .create()
        dialog.show()
    }

    /**
     * Saves the 4-digit PIN.
     *
     * @param pin The new 4-digit PIN as a string.
     */
    @JavascriptInterface
    fun savepin2(pin: String) {
        PreyLogger.d("savepin2:$pin")
        PreyConfig.getInstance(context).setPinNumber(pin)
        if ("" == pin) {
            setUninstall(false)
            setShieldOf(false)
        }
    }

    /**
     * Checks if two-step verification is enabled.
     *
     * @return True if two-step verification is enabled, false otherwise.
     */
    @JavascriptInterface
    fun getTwoStepEnabled(): Boolean {
        if (!PreyConfig.getInstance(context).isTimeTwoStep()) {
            val twoStepEnabled = PreyWebServices.getInstance().getTwoStepEnabled(context)
            PreyConfig.getInstance(context).setTwoStep(twoStepEnabled)
            PreyConfig.getInstance(context).setTimeTwoStep()
        }
        return PreyConfig.getInstance(context).getTwoStep()
    }

    /**
     * Returns the Android version of the device.
     *
     * @return The Android version as an integer.
     */
    @JavascriptInterface
    fun versionAndroid(): Int {
        return Build.VERSION.SDK_INT
    }

    /**
     * Checks if the Android version is Pie (9.0) or above.
     *
     * @return True if the Android version is Pie or above, false otherwise.
     */
    @JavascriptInterface
    fun versionIsPieOrAbove(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1
    }

    /**
     * Displays an alert dialog to notify the user about disabling power options.
     */
    @JavascriptInterface
    fun notificationShieldOf() {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(R.string.preferences_disable_power_alert_android9_title)
        alertDialog.setMessage(context.getString(R.string.preferences_disable_power_alert_android9_message))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "OK"
        ) { dialog, which -> }
        alertDialog.show()
        setShieldOf(false)
    }

    /**
     * Sets whether the app can be uninstalled.
     *
     * @param uninstall True to block app uninstallation, false otherwise.
     */
    @JavascriptInterface
    fun setUninstall(uninstall: Boolean) {
        PreyLogger.d("setUninstall:$uninstall")
        PreyConfig.getInstance(context).setBlockAppUninstall(uninstall)
    }

    /**
     * Sets whether power options are disabled.
     *
     * @param shieldOf True to disable power options, false otherwise.
     */
    @JavascriptInterface
    fun setShieldOf(shieldOf: Boolean) {
        PreyLogger.d("setShieldOf:$shieldOf")
        PreyConfig.getInstance(context).setDisablePowerOptions(shieldOf)
        if (shieldOf) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = Date().time
            cal.add(Calendar.MINUTE, -1)
            PreyConfig.getInstance(context).setTimeSecureLock(cal.timeInMillis)
            PreyConfig.getInstance(context).setOpenSecureService(false)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                context.startService(
                    Intent(
                        context,
                        PreyDisablePowerOptionsService::class.java
                    )
                )
            }
        } else {
            context.stopService(Intent(context, PreyDisablePowerOptionsService::class.java))
        }
    }

    /**
     * Displays the QR code activity.
     */
    @JavascriptInterface
    fun qr() {
        PreyLogger.d("qr!!")
        val intent = Intent(context, BarcodeActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        activity.finish()
    }

    /**
     * Returns the email address of the user.
     *
     * @return The email address as a string.
     */
    @JavascriptInterface
    fun initMail(): String {
        return PreyConfig.getInstance(context).getEmail()!!
    }

    /**
     * Locks the device with the given key.
     *
     * @param key The key to lock the device with.
     * @return A JSON string indicating the result of the lock operation.
     */
    @JavascriptInterface
    fun lock(key: String): String {
        val config = PreyConfig.getInstance(context)
        var error2 = ""
        val unlock = config.getUnlockPass()
        PreyLogger.d("lock key:$key  unlock:$unlock")
        if (unlock == null || unlock.isEmpty()) {
            config.setOverLock(false)
            Process.killProcess(Process.myPid())
            return "{\"ok\":\"ok\"}"
        }
        if (unlock == key) {
            config.setInputWebview("")
            config.setUnlockPass("")
            config.setOpenSecureService(false)
            val overLock = config.getOverLock()
            val canDrawOverlays = PreyPermission.canDrawOverlays(context)
            PreyLogger.d("lock key:$key  unlock:$unlock overLock:$overLock canDrawOverlays:$canDrawOverlays")
            object : Thread() {
                override fun run() {
                    val reason = "{\"origin\":\"user\"}"
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        context,
                        UtilJson.makeMapParam("start", "lock", "stopped", reason)
                    )
                }
            }.start()
            try {
                Thread.sleep(2000)
            } catch (e: Exception) {
                PreyLogger.e("Error sleep:" + e.message, e)
            }
            if (overLock) {
                if (unlock.isEmpty()) {
                    PreyLogger.d("lock accc ")
                    config.setOverLock(false)
                    if (preyLockHtmlService != null) {
                        preyLockHtmlService!!.stop()
                    } else {
                        Process.killProcess(Process.myPid())
                    }
                }
            }
            object : Thread() {
                override fun run() {
                    if (canDrawOverlays) {
                        try {
                            if (preyLockHtmlService != null) {
                                preyLockHtmlService!!.stop()
                                val viewLock: View? = config.viewLock
                                if (viewLock != null) {
                                    val wm =
                                        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                    wm.removeView(viewLock)
                                } else {
                                    Process.killProcess(Process.myPid())
                                }
                            }
                        } catch (e: Exception) {
                            Process.killProcess(Process.myPid())
                        }
                    }
                    val intentClose = Intent(context, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentClose)
                    try {
                        sleep(2000)
                    } catch (e: Exception) {
                        PreyLogger.e("Error sleep:" + e.message, e)
                    }
                    context.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
                }
            }.start()
            error2 = "{\"ok\":\"ok\"}"
        } else {
            error2 = "{\"error\":[\"" + context.getString(R.string.password_wrong) + "\"]}"
        }
        PreyLogger.d("error2:$error2")
        return error2
    }

    /**
     * This function is called from JavaScript to change the email address associated with the user's account.
     *
     * @param email The new email address to be set.
     * @return A JSON string containing an "ok" message if the email was successfully changed, or an error message if there was an issue.
     */
    @JavascriptInterface
    fun changemail(email: String): String? {
        var result: String? = null
        try {
            val verificationResult = PreyWebServices.getInstance().verifyEmail(context, email)
            PreyLogger.d(
                "verify:" + (if (verificationResult == null) "" else (verificationResult.getStatusCode()
                    .toString() + " " + verificationResult.getStatusDescription()))
            )
            if (verificationResult != null) {
                val statusCode: Int = verificationResult.getStatusCode()
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CONFLICT) {
                    PreyConfig.getInstance(context).setEmail(email)
                    result = "{\"ok\":[\"${context.getString(R.string.email_resend)}\"]}"
                } else {
                    result = formatErrorMessage(verificationResult.getStatusDescription())
                }
            }
        } catch (e: Exception) {
            result = e.message
            PreyLogger.e("error:$error", e)
        }
        return result
    }

    /**
     * Formats an error message by removing unnecessary characters and wrapping the error message in a JSON string.
     *
     * @param error The error message to be formatted.
     * @return A JSON string containing the formatted error message.
     */
    private fun formatErrorMessage(error: String?): String? {
        return if (error != null && error.indexOf("error") < 0) {
            error.replace("\\\"", "'").replace("\"", "").replace("'", "\"")
        } else {
            val errorObject = JSONObject(error)
            val errorString = errorObject.getString("error")
            if (errorString.indexOf("[") < 0) {
                "{\"email\":[\"$errorString\"]}"
            } else {
                error
            }
        }
    }

    /**
     * This function is called from JavaScript to sign up a new user.
     *
     * @param name The name of the new user.
     * @param email The email address of the new user.
     * @param password1 The password of the new user.
     * @param password2 A confirmation of the password of the new user.
     * @param policy_rule_age The age of the new user.
     * @param policy_rule_privacy_terms The privacy terms of the new user.
     * @param offers The offers of the new user.
     * @return An error message if there was an issue signing up the user, or null if the user was successfully signed up.
     */
    @JavascriptInterface
    fun signup(
        name: String,
        email: String,
        password1: String,
        password2: String,
        policy_rule_age: String,
        policy_rule_privacy_terms: String,
        offers: String
    ): String? {
        PreyLogger.d("signup name: $name email:$email policy_rule_age:$policy_rule_age policy_rule_privacy_terms:$policy_rule_privacy_terms offers:$offers")
        try {
            error = null
            val context = context
            PreyLogger.d("name:$name")
            PreyLogger.d("email:$email")
            PreyLogger.d("password1:$password1")
            PreyLogger.d("password2:$password2")
            PreyLogger.d("rule_age:$policy_rule_age")
            PreyLogger.d("privacy_terms:$policy_rule_privacy_terms")
            PreyLogger.d("offers:$offers")
            val accountData: PreyAccountData = PreyWebServices.getInstance().registerNewAccount(
                context,
                name,
                email,
                password1,
                password2,
                policy_rule_age,
                policy_rule_privacy_terms,
                offers,
                PreyUtils.getDeviceType(context)
            )
            PreyLogger.d("Response creating account:${accountData.toString()}")
            PreyConfig.getInstance(context).saveAccount(accountData)
            PreyConfig.getInstance(context).registerC2dm()
            PreyConfig.getInstance(context).setEmail(email)
            PreyConfig.getInstance(context).setRunBackground(true)
            PreyConfig.getInstance(context).setInstallationStatus("Pending")
            PreyApp().run(context)
            Location().get(context, null, null)
        } catch (e: Exception) {
            error = e.message
            PreyLogger.e("error:$error", e)
        }
        try {
            if (error == null) {
                error = ""
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("signup out:$error")
        return error
    }

    /**
     * Opens the forgot password page in the default browser.
     */
    @JavascriptInterface
    fun forgot() {
        PreyLogger.d("forgot")
        val url: String = FileConfigReader.getInstance(context)!!.getPreyForgot()
        val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(myIntent)
    }

    /**
     * Checks and requests permissions for the app.
     */
    @JavascriptInterface
    fun givePermissions() {
        PreyLogger.d("givePermissions")
        val canAccessFineLocation: Boolean = PreyPermission.canAccessFineLocation(context)
        val canAccessCoarseLocation: Boolean = PreyPermission.canAccessCoarseLocation(context)
        val canAccessCamera: Boolean = PreyPermission.canAccessCamera(context)
        val canAccessStorage: Boolean = PreyPermission.canAccessStorage(context)
        val canAccessBackgroundLocation: Boolean =
            PreyPermission.canAccessBackgroundLocation(context)
        val showFineLocation: Boolean = PreyPermission.showRequestFineLocation(activity)
        val showCoarseLocation: Boolean = PreyPermission.showRequestCoarseLocation(activity)
        val showBackgroundLocation: Boolean =
            PreyPermission.showRequestBackgroundLocation(activity)
        val showCamera: Boolean = PreyPermission.showRequestCamera(activity)
        val showPhone: Boolean = PreyPermission.showRequestPhone(activity)
        val showStorage: Boolean = PreyPermission.showRequestStorage(activity)
        val canAccessibility: Boolean = PreyPermission.isAccessibilityServiceView(context)
        val canScheduleExactAlarms: Boolean = PreyPermission.canScheduleExactAlarms(context)
        var showDeniedPermission = false
        if (!canAccessStorage) {
            if (!showStorage) showDeniedPermission = true
        }
        if (!canAccessFineLocation) {
            if (!showFineLocation) showDeniedPermission = true
        }
        if (!canAccessCoarseLocation) {
            if (!showCoarseLocation) showDeniedPermission = true
        }
        if (!canAccessCamera) {
            if (!showCamera) showDeniedPermission = true
        }
        PreyLogger.d("canAccessFineLocation:$canAccessFineLocation")
        PreyLogger.d("canAccessCoarseLocation:$canAccessCoarseLocation")
        PreyLogger.d("canAccessBackgroundLocation:$canAccessBackgroundLocation")
        PreyLogger.d("canAccessCamera:$canAccessCamera")
        PreyLogger.d("canAccessStorage:$canAccessStorage")
        PreyLogger.d("canScheduleExactAlarms:$canScheduleExactAlarms")
        PreyLogger.d("showBackgroundLocation:$showBackgroundLocation")
        PreyLogger.d("showFineLocation:$showFineLocation")
        PreyLogger.d("showCoarseLocation:$showCoarseLocation")
        PreyLogger.d("showCamera:$showCamera")
        PreyLogger.d("showPhoneState:$showPhone")
        PreyLogger.d("showWriteStorage:$showStorage")
        PreyLogger.d("showDeniedPermission:$showDeniedPermission")
        if (showDeniedPermission) {
            activity.openSettings()
        } else {
            if (!canAccessFineLocation || !canAccessCoarseLocation || !canAccessCamera
                || !canAccessStorage
            ) {
                activity.askForPermission()
            } else {
                val isAdminActive = FroyoSupport.getInstance(context).isAdminActive()
                if (isAdminActive) {
                    if (canAccessibility) {
                        var canDrawOverlays = true
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) canDrawOverlays =
                            Settings.canDrawOverlays(context)
                        if (!canDrawOverlays) {
                            activity.askForPermissionAndroid7()
                        }
                    } else {
                        activity.accessibility()
                    }
                } else {
                    activity.askForAdminActive()
                }
                if (!canScheduleExactAlarms) {
                    activity.alarms()
                }
            }
        }
    }

    /**
     * Checks if the caps lock is on.
     *
     * @return true if the caps lock is on, false otherwise
     */
    @JavascriptInterface
    fun capsLockOn(): Boolean {
        return PreyConfig.getInstance(context).getCapsLockOn()
    }

    /**
     * Simulates a touch event to turn off the caps lock.
     */
    @JavascriptInterface
    fun touch() {
        PreyConfig.getInstance(context).setCapsLockOn(false)
    }

    /**
     * Initializes the configuration.
     *
     * @return true if the device is configured, false otherwise
     */
    @JavascriptInterface
    fun initConfigure(): Boolean {
        PreyConfig.getInstance(context).setStart(false)
        val deviceKey: String? = PreyConfig.getInstance(context).getDeviceId()
        return if (deviceKey != null && "" != deviceKey) {
            true
        } else {
            false
        }
    }

    /**
     * Gets the device name.
     *
     * @return the device name
     */
    @JavascriptInterface
    fun initName(): String {
        var initName = ""
        try {
            initName = PreyConfig.getInstance(context).getDeviceName()!!
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("initName:$initName")
        return initName
    }

    /**
     * Checks if the user is a free user.
     *
     * @return true if the user is a free user, false otherwise
     */
    @JavascriptInterface
    fun initUserFree(): Boolean {
        var initUserFree = false
        try {
            initUserFree = !PreyConfig.getInstance(context).getProAccount()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("initUserFree:$initUserFree")
        return initUserFree
    }

    /**
     * Renames the device.
     *
     * @param newName the new name for the device
     * @return a JSON string containing the result of the rename operation
     */
    @JavascriptInterface
    fun rename2(newName: String): String? {
        PreyLogger.d("rename:${newName}")
        var result: String? = null
        try {
            val renamedDevice: PreyName = PreyWebServices.getInstance().renameName(context, newName)
            val responseJson = JSONObject()
            responseJson.put("code", renamedDevice.getCode())
            responseJson.put("error", renamedDevice.getError())
            responseJson.put("name", renamedDevice.getName())
            result = responseJson.toString()
            PreyLogger.d("rename out:${result}")
        } catch (e: Exception) {
            PreyLogger.e("rename error:${e.message}", e)
        }
        return result
    }

    /**
     * Logs a message.
     *
     * @param mylogger the message to log
     */
    @JavascriptInterface
    fun logger(mylogger: String?) {
        PreyLogger.d("logger:${mylogger}")
    }

    /**
     * Sets the input web view and page.
     *
     * @param inputwebview The input web view to set.
     * @param page The page to set.
     */
    @JavascriptInterface
    fun inputwebview(inputwebview: String, page: String) {
        PreyConfig.getInstance(context).setInputWebview(inputwebview)
        PreyConfig.getInstance(context).setPage(page)
    }

    /**
     * Approves the location permission.
     *
     * Checks if the app has background location permission and shows a request if necessary.
     */
    @JavascriptInterface
    fun approveLocation() {
        PreyLogger.d("approveLocation")
        val canAccessBackgroundLocation: Boolean =
            PreyPermission.canAccessBackgroundLocation(context)
        val showBackgroundLocation: Boolean =
            PreyPermission.showRequestBackgroundLocation(activity)
        var showDeniedPermission = false
        PreyLogger.d("canAccessBackgroundLocation:$canAccessBackgroundLocation")
        PreyLogger.d("showBackgroundLocation:$showBackgroundLocation")
        if (Build.VERSION.SDK_INT == PreyConfig.BUILD_VERSION_CODES_10 && !canAccessBackgroundLocation) {
            if (!showBackgroundLocation) {
                showDeniedPermission = true
            }
        }
        if (Build.VERSION.SDK_INT == PreyConfig.BUILD_VERSION_CODES_11 && !canAccessBackgroundLocation) {
            if (!showBackgroundLocation) {
                showDeniedPermission = true
            }
        }
        PreyLogger.d("showDeniedPermission:$showDeniedPermission")
        activity!!.askForPermissionLocation()
    }

    /**
     * Skips location permission check.
     *
     * Returns true if the app has camera, storage, and overlay permissions, and is an admin.
     *
     * @return True if the app has the required permissions, false otherwise.
     */
    @JavascriptInterface
    fun skipLocation(): Boolean {
        PreyLogger.d("skipLocation")
        val canAccessCamera = PreyPermission.canAccessCamera(context)
        val canAccessStorage = PreyPermission.canAccessStorage(context)
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        val isAdminActive = FroyoSupport.getInstance(context).isAdminActive()
        return canAccessCamera && canAccessStorage && canDrawOverlays && canDrawOverlays && isAdminActive
    }

    /**
     * Skips permissions check.
     *
     * Currently does nothing.
     */
    @JavascriptInterface
    fun skipPermissions() {
        PreyLogger.d("skipPermissions")
    }

    /**
     * Skips background location permission check.
     *
     * Sets the location background denied flag to true and refreshes the view.
     */
    @JavascriptInterface
    fun skipPermissionsBg() {
        PreyLogger.d("skipPermissionsBg")
        PreyConfig.getInstance(context).setLocationBgDenied(true)
        refresh()
    }

    /**
     * Returns the lock message from the PreyConfig instance.
     *
     * @return The lock message as a string.
     */
    @JavascriptInterface
    fun verificateAlert(): String {
        return PreyConfig.getInstance(context).getLockMessage()!!
    }

    /**
     * Method that returns if it should show disable power button
     *
     * @return should show disable power button
     */
    @JavascriptInterface
    fun initOpenPin(): Boolean {
        var openPin = false
        try {
            openPin = FileConfigReader.getInstance(context)!!.getOpenPin()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return false
    }

    /**
     * Method to open screen disable power button
     */
    @JavascriptInterface
    fun openPin() {
        val time: Long = PreyConfig.getInstance(context).getTimeSecureLock()
        val now = Date().time
        var extra: String? = null
        if (now < time) {
            extra = ""
            return
        }
        val pinNumber: String? = PreyConfig.getInstance(context).getPinNumber()
        val disablePowerOptions: Boolean =
            PreyConfig.getInstance(context).getDisablePowerOptions()
        if (pinNumber == null || "" == pinNumber) {
            extra = ""
            return
        }
        if (!disablePowerOptions) {
            extra = ""
            return
        }
        if (extra == null) {
            try {
                PreyConfig.getInstance(context).setViewSecure(true)
                val intentLock = Intent(context, PreySecureService::class.java)
                context.startService(intentLock)
                activity!!.finish()
            } catch (e: Exception) {
                PreyLogger.e("Error CLOSE_SYSTEM:${e.message}", e)
            }
        }
    }

    /**
     * Method to close screen disable power button
     */
    @JavascriptInterface
    fun closePin() {
        try {
            if (preySecureService == null) {
                Process.killProcess(Process.myPid())
            } else {
                preySecureService!!.stop()
            }
        } catch (e: Exception) {
            PreyLogger.e("closePin error :${e.message}", e)
            Process.killProcess(Process.myPid())
        }
    }

    /**
     * Method to unlock off the screen disable the power button
     *
     * @param pinNumber
     * @return returns an empty json if it is ok and if not the error
     */
    @JavascriptInterface
    fun unpin(pinNumber: String): String {
        var out = ""
        try {
            val json = JSONObject()
            PreyLogger.d("unpin:${pinNumber}")
            val _pinNumber: String? = PreyConfig.getInstance(context).getPinNumber()
            if (_pinNumber == pinNumber) {
                PreyConfig.getInstance(context).setPinActivated("")
                PreyConfig.getInstance(context).setOpenSecureService(false)
                PreyConfig.getInstance(context).setCounterOff(0)
                val cal = Calendar.getInstance()
                cal.timeInMillis = Date().time
                cal.add(Calendar.MINUTE, 2)
                PreyConfig.getInstance(context).setTimeSecureLock(cal.timeInMillis)
                closePin()
                out = json.toString()
            } else {
                PreyLogger.d("error")
                json.put("error", JSONArray().put(context.getString(R.string.password_wrong)))
                out = json.toString()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error unpin:${e.message}", e)
        }
        return out
    }

    /**
     * Method that returns if it should show help
     *
     * @return returns if it should show help
     */
    @JavascriptInterface
    fun initHelpFormForFree(): Boolean {
        var initHelpFormForFree = false
        try {
            initHelpFormForFree = PreyConfig.getInstance(context).getHelpFormForFree()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return initHelpFormForFree
    }

    /**
     * Method to initialize help file
     */
    @JavascriptInterface
    fun clickInitHelp() {
        PreyConfig.getInstance(context).setFileHelp("")
    }

    /**
     * Method to send the help
     *
     * @param subject Possible values: Issues,Questions or Other
     * @param message Help message
     * @return returns a json with the result
     */
    @JavascriptInterface
    fun sendHelp(subject: String, message: String): String {
        val preyConfig: PreyConfig = PreyConfig.getInstance(context)
        val fileHelp: String? = preyConfig.getHelpFile()
        PreyLogger.d("help subject:${subject} message:${message} fileHelp:${fileHelp}")
        var out = ""
        try {
            var error = false
            val errorJson = JSONObject()
            if ("-1" == subject) {
                error = true
                errorJson.put(
                    "subject",
                    JSONArray().put(context.getString(R.string.help_error_subject))
                )
            }
            if (message == null || "" == message || message.length < 10) {
                error = true
                errorJson.put(
                    "message",
                    JSONArray().put(context.getString(R.string.help_error_message))
                )
            }
            if (fileHelp != null && "" != fileHelp) {
                PreyLogger.d("fileHelp:$fileHelp")
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    PreyConfig.HELP_DIRECTORY
                )
                val file = File(dir, fileHelp)
                val fileSizeInBytes = file.length()
                val fileSizeInKB = fileSizeInBytes / 1024
                val fileSizeInMB = fileSizeInKB / 1024
                PreyLogger.d("img fileSizeInMB:$fileSizeInMB")
                if (fileSizeInMB > 5) {
                    error = true
                    errorJson.put(
                        "attachment",
                        JSONArray().put(context.getString(R.string.help_error_attachment))
                    )
                }
            }
            PreyLogger.d("error:${error} errorJon:${errorJson}")
            if (error) {
                out = errorJson.toString()
            } else {
                val response = PreyWebServices.getInstance().sendHelp(context, subject, message)
                PreyLogger.d(
                    "response sendHelp:${(response?.toString() ?: "")}"
                )
            }
        } catch (e: Exception) {
            PreyLogger.e("Error sendHelp:${e.message}", e)
        }
        return out
    }

    /**
     * Method that opens popup to select image to send
     */
    @JavascriptInterface
    fun searchHelpFile() {
        PreyConfig.getInstance(context).setFileHelp("")
        activity!!.openImageChooserActivity()
    }

    /**
     * Method method that returns if help file exists
     *
     * @return return help file exists
     */
    @JavascriptInterface
    fun existsHelpFile(): Boolean {
        val displayName: String? = PreyConfig.getInstance(context).getHelpFile()
        return if ("" == displayName) {
            false
        } else {
            true
        }
    }

    /**
     * Method method that returns help file
     *
     * @return return help file
     */
    @JavascriptInterface
    fun getHelpFile(): String {
        var displayName: String? = PreyConfig.getInstance(context).getHelpFile()
        if (!existsHelpFile()) {
            displayName = context.getString(R.string.help_no_file_chosen)
        }
        return displayName!!
    }

    /**
     * Method to bypass accessibility permission
     */
    @JavascriptInterface
    fun accessibilitySkip() {
        PreyLogger.d("accessibilitySkip")
        PreyConfig.getInstance(context).setTimeNextAccessibility()
        refresh()
    }

    /**
     * Method to deny accessibility permission
     */
    @JavascriptInterface
    fun accessibilityDeny() {
        PreyLogger.d("accessibilityDeny")
        PreyConfig.getInstance(context).setAccessibilityDenied(true)
        refresh()
    }

    /**
     * Method to grant accessibility permission
     */
    @JavascriptInterface
    fun accessibilityAgree() {
        PreyLogger.d("accessibilityAgree")
        activity!!.accessibility()
    }

    /**
     * Method to refresh view
     */
    @JavascriptInterface
    fun refresh() {
        PreyLogger.d("refresh")
        val intentLogin = Intent(context, LoginActivity::class.java)
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intentLogin)
        activity!!.finish()
    }

    /**
     * Method to bypass storage permission
     */
    @JavascriptInterface
    fun allFilesSkip() {
        PreyLogger.d("allFilesSkip")
        PreyConfig.getInstance(context).setTimeNextAllFiles()
        refresh()
    }

    /**
     * Method to deny storage permission
     */
    @JavascriptInterface
    fun allFilesDeny() {
        PreyLogger.d("allFilesDeny")
        PreyConfig.getInstance(context).setAllFilesDenied(true)
        refresh()
    }

    /**
     * Method to grant storage permission
     */
    @JavascriptInterface
    fun allFilesAgree() {
        PreyLogger.d("allFilesAgree")
    }

    /**
     * Method to verify that permissions are not removed
     */
    @JavascriptInterface
    fun appIsntUsed() {
        PreyLogger.d("appIsntUsed")
        openSettings()
    }

    /**
     * Method open prey settings
     */
    fun openSettings() {
        PreyLogger.d("openSettings")
        val intentSetting = Intent()
        intentSetting.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intentSetting.setData(uri)
        context.startActivity(intentSetting)
    }

    /**
     * Method that returns if the version is velvet
     *
     * @return version is velvet
     */
    @JavascriptInterface
    fun versionIsRedVelvetCake(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    /**
     * This method is used to check if the help form is available for free.
     *
     * @return true if the help form is available for free, false otherwise.
     */
    @JavascriptInterface
    fun initContactFormForFree(): Boolean {
        return PreyConfig.getInstance(context).getHelpFormForFree()
    }

    /**
     * This method is used to handle the click on the help button.
     */
    @JavascriptInterface
    fun handleClickHelp() {
        PreyConfig.getInstance(context).setFileHelp("")
    }

    /**
     * This method is used to check if the MSP account is initialized.
     *
     * @return true if the MSP account is initialized, false otherwise.
     */
    @JavascriptInterface
    fun initMspAccount(): Boolean {
        var initMspAccount = false
        try {
            initMspAccount = PreyConfig.getInstance(context).getMspAccount()
        } catch (e: Exception) {
            PreyLogger.e("Error initMspAccount:" + e.message, e)
        }
        return initMspAccount
    }

    /**
     * This method is used to handle the uninstallation of the app.
     */
    @JavascriptInterface
    fun uninstall() {
        PreyLogger.d("uninstall")
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.preferences_uninstall_summary)
            .setPositiveButton(
                R.string.yes
            ) { dialog, id ->
                PreyLogger.d("uninstall run:")
                detachDevice()
                PreyConfig.getInstance(context).setStart(true)
            }
            .setNegativeButton(
                R.string.no
            ) { dialog, id -> }
        val popup: Dialog = builder.create()
        popup.show()
    }

    /**
     * This method is used to detach the device.
     */
    fun detachDevice() {
        var error: String? = null
        var progressDialog: ProgressDialog? = null
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage(
            context.getText(R.string.preferences_detach_dettaching_message).toString()
        )
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.show()
        error = Detach().detachDevice(context)
        PreyLogger.d("error:$error")
        try {
            progressDialog.dismiss()
        } catch (e: java.lang.Exception) {
        }
        try {
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            } else {
                val welcome = Intent(context, CheckPasswordHtmlActivity::class.java)
                welcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(welcome)
                if (activity != null) activity.finish()
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    /**
     * Method to valid if it should ask for notification permission
     *
     * @return returns should ask
     */
    @JavascriptInterface
    fun showNotification(): Boolean {
        var showNotification = false
        try {
            showNotification = if (PreyConfig.getInstance(context).getDenyNotification()) {
                false
            } else {
                !PreyPermission.areNotificationsEnabled(context)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error showNotification:" + e.message, e)
        }
        return showNotification
    }

    /**
     * Method to request notification permission
     */
    @JavascriptInterface
    fun turnOnNotifications() {
        try {
            activity!!.askForPermissionNotification()
        } catch (e: Exception) {
            openSettings()
        }
    }

    /**
     * Opens the biometric authentication dialog.
     *
     * @param typeBiometric The type of biometric authentication to use.
     */
    @JavascriptInterface
    fun openBiometric(typeBiometric: String?) {
        if (typeBiometric != null) {
            PreyConfig.getInstance(context).setTypeBiometric(typeBiometric)
            activity!!.openBiometric()
        }
    }

    /**
     * Verifies whether biometric authentication is enabled.
     *
     * @return True if biometric authentication is enabled, false otherwise.
     */
    @JavascriptInterface
    fun verificateBiometric(): Boolean {
        return PreyConfig.getInstance(context).getVerificateBiometric()
    }

    /**
     * Gets the type of biometric authentication being used.
     *
     * @return The type of biometric authentication as a string.
     */
    @JavascriptInterface
    fun typeBiometric(): String {
        return PreyConfig.getInstance(context).getTypeBiometric()!!
    }

    /**
     * Closes the current activity.
     */
    @JavascriptInterface
    fun close() {
        PreyConfig.getInstance(context).removeTimePasswordOk()
    }

    /**
     * Checks if the accessibility information type matches the given type.
     *
     * @param type The type to check against.
     * @return True if the accessibility information type matches, false otherwise.
     */
    @JavascriptInterface
    fun isAccessibilityInformationType(type: String): Boolean {
        return getAccessibilityInformationType().equals(type);
    }

    /**
     * Method that returns what type of accessibility information to show
     *
     * @return type
     */
    private fun getAccessibilityInformationType(): String {
        if (isVendor("google") || isVendor("poco") || isVendor("xiaomi") || isVendor("realme")) {
            return "downloaded_apps"
        }
        if (isVendor("samsung")) {
            return "installed_apps"
        }
        if (isVendor("oneplus")) {
            return "more"
        }
        return "downloaded_services"
    }

    /**
     * Checks if the device manufacturer or brand matches the given vendor.
     *
     * @param vendor The vendor to check against.
     * @return True if the device manufacturer or brand matches the vendor, false otherwise.
     */
    private fun isVendor(vendor: String): Boolean {
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        return manufacturer.lowercase(Locale.getDefault()).contains(vendor) || brand.lowercase(
            Locale.getDefault()
        ).contains(vendor)
    }

    /**
     * Initializes alarms and checks if the app has permission to schedule exact alarms.
     *
     * @return True if the app has permission to schedule exact alarms, false otherwise.
     */
    @JavascriptInterface
    fun initAlarms(): Boolean {
        val initAlarms: Boolean = PreyPermission.canScheduleExactAlarms(context)
        PreyLogger.d("initAlarms:${initAlarms}")
        return initAlarms
    }

}