/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js

import android.app.AlertDialog
import android.app.Dialog
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
import com.prey.R
import com.prey.actions.location.LocationUpdatesService
import com.prey.actions.location.PreyLocation
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.activities.CloseActivity
import com.prey.activities.LoginActivity
import com.prey.activities.PanelWebActivity
import com.prey.activities.PasswordHtmlActivity
import com.prey.activities.PreReportActivity
import com.prey.activities.SecurityActivity
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.barcodereader.BarcodeActivity
import com.prey.json.actions.Location
import com.prey.json.UtilJson
import com.prey.FileConfigReader
import com.prey.PreyAccountData
import com.prey.PreyApp
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.PreyUtils
import com.prey.net.PreyWebServices
import com.prey.preferences.RunBackgroundCheckBoxPreference
import com.prey.services.PreyDisablePowerOptionsService
import com.prey.services.PreyJobService
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreySecureService
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WebAppInterface {
    var mContext: Context
    lateinit var mActivity: CheckPasswordHtmlActivity
    private var wrongPasswordIntents = 0
    private var error: String? = null
    private var noMoreDeviceError = false
    private var from = "setting"

    private var preySecureService: PreySecureService? = null
    private var preyLockHtmlService: PreyLockHtmlService? = null


    constructor(ctx: Context) {
        mContext = ctx
    }

    constructor(context: Context?, activity: CheckPasswordHtmlActivity?) {
        mContext = context!!
        mActivity = activity!!
    }

    constructor(context: Context, activity: PasswordHtmlActivity) {
        mContext = context
    }

    constructor(context: Context, preyLockHtmlService: PreyLockHtmlService) {
        mContext = context
        this.preyLockHtmlService = preyLockHtmlService
    }

    constructor(context: Context, service: PreySecureService) {
        mContext = context
        this.preySecureService = service
    }

    @get:JavascriptInterface
    val data: String
        get() {
            val ssid: String? = PreyConfig.getInstance(mContext).getSsid()
            val model: String? = PreyConfig.getInstance(mContext).getModel()
            val imei: String? = PreyConfig.getInstance(mContext).getImei()
            val preyLocation: PreyLocation? = PreyConfig.getInstance(mContext).getLocation()
            val lat = "" + LocationUpdatesService.round(preyLocation!!.getLat())
            val lng = "" + LocationUpdatesService.round(preyLocation!!.getLng())
            val public_ip: String = PreyConfig.getInstance(mContext).getPublicIp()!!.trim()
            val json =
                "{\"lat\":\"$lat\",\"lng\":\"$lng\",\"ssid\":\"$ssid\",\"public_ip\":\"$public_ip\",\"imei\":\"$imei\",\"model\": \"$model\"}"
            PreyLogger.d("getData:$json")
            return json
        }

    @JavascriptInterface
    fun verifyLock() {
    }

    @JavascriptInterface
    fun initBackground(): Boolean {
        val initBackground: Boolean = PreyConfig.getInstance(mContext).getRunBackground()
        PreyLogger.d("initBackground:$initBackground")
        return initBackground
    }

    @JavascriptInterface
    fun initUseBiometric(): Boolean {
        val useBiometric: Boolean = PreyConfig.getInstance(mContext).getUseBiometric()
        PreyLogger.d("useBiometric:$useBiometric")
        return useBiometric
    }

    @JavascriptInterface
    fun initShowBiometric(): Boolean {
        val showBiometric: Boolean = PreyPermission.checkBiometricSupport(mContext)
        PreyLogger.d("showBiometric:$showBiometric")
        return showBiometric
    }

    @JavascriptInterface
    fun initPin(): Boolean {
        val pinNumber: String? = PreyConfig.getInstance(mContext).getPinNumber()
        val initPin = (pinNumber != null && "" != pinNumber)
        PreyLogger.d("initPin:$initPin")
        return initPin
    }

    @JavascriptInterface
    fun initAdminActive(): Boolean {
        return FroyoSupport.getInstance(mContext)!!.isAdminActive
    }

    @JavascriptInterface
    fun initDrawOverlay(): Boolean {
        val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(mContext)
        return canDrawOverlays
    }

    @JavascriptInterface
    fun initAccessibility(): Boolean {
        val initAccessibility: Boolean = PreyPermission.isAccessibilityServiceEnabled(mContext)
        PreyLogger.d("initAccessibility:$initAccessibility")
        return initAccessibility
    }

    @JavascriptInterface
    fun initLocation(): Boolean {
        val initLocation =
            (PreyPermission.canAccessFineLocation(mContext) || PreyPermission.canAccessCoarseLocation(
                mContext
            ))
        PreyLogger.d("initLocation:$initLocation")
        return initLocation
    }

    @JavascriptInterface
    fun initBackgroundLocation(): Boolean {
        val initBackgroundLocation: Boolean = PreyPermission.canAccessBackgroundLocation(mContext)
        PreyLogger.d("initBackgroundLocation:$initBackgroundLocation")
        return initBackgroundLocation
    }

    @JavascriptInterface
    fun initAndroid10OrAbove(): Boolean {
        val isAndroid10OrAbove: Boolean = PreyConfig.getInstance(mContext).isAndroid10OrAbove()
        PreyLogger.d("isAndroid10OrAbove:$isAndroid10OrAbove")
        return isAndroid10OrAbove
    }

    @JavascriptInterface
    fun initCamera(): Boolean {
        return PreyPermission.canAccessCamera(mContext)
    }

    @JavascriptInterface
    fun initWriteStorage(): Boolean {
        return PreyPermission.canAccessStorage(mContext)
    }

    @get:JavascriptInterface
    val pin: String
        get() {
            val pin: String? = PreyConfig.getInstance(mContext).getPinNumber()
            PreyLogger.d("getPin:$pin")
            return pin!!
        }

    @JavascriptInterface
    fun initUninstall(): Boolean {
        val initUnis: Boolean = PreyConfig.getInstance(mContext).getBlockAppUninstall()
        PreyLogger.d("initUninstall:$initUnis")
        return initUnis
    }

    @JavascriptInterface
    fun initShield(): Boolean {
        val initShi: Boolean = PreyConfig.getInstance(mContext).getDisablePowerOptions()
        PreyLogger.d("initShield:$initShi")
        return initShi
    }

    @JavascriptInterface
    fun initScheduler(): String {
        val initSche: Int = PreyConfig.getInstance(mContext).getMinuteScheduled()
        return "" + initSche
    }

    @JavascriptInterface
    fun changeScheduler(minuteScheduled: String) {
        PreyLogger.d("changeScheduler:$minuteScheduled")
        PreyConfig.getInstance(mContext).setMinuteScheduled(minuteScheduled.toInt())
        PreyJobService.schedule(mContext)
    }

    @JavascriptInterface
    fun report() {
        PreyLogger.d("report:")
        val intent = Intent(mContext, PreReportActivity::class.java)
        mContext!!.startActivity(intent)
        if (mActivity != null) mActivity!!.finish()
    }

    @JavascriptInterface
    fun security() {
        PreyLogger.d("security:")
        val intent = Intent(mContext, SecurityActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext!!.startActivity(intent)
        if (mActivity != null) mActivity!!.finish()
    }

    @JavascriptInterface
    fun reload() {
        PreyLogger.d("reload:")
        val intent = Intent(mContext, CheckPasswordHtmlActivity::class.java)
        mContext!!.startActivity(intent)
        if (mActivity != null) mActivity!!.finish()
    }

    @JavascriptInterface
    fun savePin(pin: String) {
        PreyLogger.d("savepin:$pin")
        PreyConfig.getInstance(mContext).setPinNumber(pin)
    }

    @JavascriptInterface
    fun log(log: String) {
        PreyLogger.d("log:$log")
    }

    @JavascriptInterface
    fun mylogin(email2: String, password2: String): String {
        PreyLogger.d(String.format("mylogin email2:%s password2:%s", email2, password2))
        var email = "orlando@preyhq.com"
        var password = "osito1234"
        PreyLogger.d(String.format("mylogin email:%s password:%s", email, password))
        try {
            noMoreDeviceError = false
            error = null
            val ctx = mContext
            PreyConfig.getInstance(mContext).setError("")
            var errorConfig: String? = null
            object : Thread() {
                override fun run() {
                    try {

                        val accountData: PreyAccountData? = PreyWebServices.getInstance()
                            .registerNewDeviceToAccount(
                                ctx,
                                email,
                                password,
                                PreyUtils.getDeviceType(ctx)
                            )


                        PreyConfig.getInstance(mContext).saveAccount(accountData!!)

                    } catch (e: Exception) {
                        PreyLogger.d(String.format("mylogin error2:%s", e.message))
                        PreyConfig.getInstance(mContext).setError(e.message!!)
                    }
                }
            }.start()
            var isAccount = false
            var i = 0
            do {
                Thread.sleep(1000)
                errorConfig = PreyConfig.getInstance(mContext).getError()
                isAccount = PreyConfig.getInstance(mContext).isAccount()
                PreyLogger.d(String.format("mylogin [%d] isAccount:%s", i, isAccount))
                i++
            } while (i < 30 && !isAccount && errorConfig == null)
            isAccount = PreyConfig.getInstance(mContext).isAccount()
            if (!isAccount) {
                if (errorConfig != null && "" != errorConfig) {
                    error = errorConfig
                } else {
                    val json = JSONObject()
                    json.put(
                        "error",
                        JSONArray().put(mContext!!.getString(R.string.error_communication_exception))
                    )
                    error = json.toString()
                }
            } else {
                PreyConfig.getInstance(mContext).registerC2dm()
                PreyConfig.getInstance(mContext).setEmail(email)
                PreyConfig.getInstance(mContext).setRunBackground(true)
                RunBackgroundCheckBoxPreference.notifyReady(mContext)
                PreyConfig.getInstance(mContext).setInstallationStatus("")
                PreyApp().run(mContext)

                Location().get(mContext, null, null)

            }
        } catch (e: Exception) {
            PreyLogger.d(String.format("mylogin error1:%s", e.message))
            error = e.message
        }
        if (error == null) {
            error = ""
        }
        PreyLogger.d(String.format("mylogin out error:%s", error))
        return error!!
    }

    @get:JavascriptInterface
    val isTimePasswordOk: Boolean
        get() {
            val isTimePasswordOk: Boolean = PreyConfig.getInstance(mContext).isTimePasswordOk()
            PreyLogger.d("isTimePasswordOk:$isTimePasswordOk")
            return isTimePasswordOk
        }

    @JavascriptInterface
    fun newName(name: String): String {
        PreyLogger.d(String.format("newName:%s", name))
        //TODO: cambiar
        //PreyWebServices.getInstance().validateName(mContext, name)
        return ""
    }

    @JavascriptInterface
    fun login_tipo(password: String, password2: String, tipo: String): String? {
        PreyLogger.d(
            String.format(
                "login_tipo2 password:%s password2:%s tipo:%s",
                password,
                password2,
                tipo
            )
        )
        from = tipo
        var error: String? = null
        var isPasswordOk = false
        try {
            val apikey: String? = PreyConfig.getInstance(mContext).getApiKey()
            val twoStep: Boolean = PreyConfig.getInstance(mContext).getTwoStep()
            PreyLogger.d(String.format("login_tipo twoStep:%b", twoStep))
            if (twoStep) {
                PreyLogger.d(
                    String.format(
                        "login_tipo apikey:%s password:%s password2:%s",
                        apikey,
                        password,
                        password2
                    )
                )
                isPasswordOk = PreyWebServices.getInstance()
                    .checkPassword2(mContext, apikey!!, password, password2)
            } else {
                PreyLogger.d(String.format("login_tipo apikey:%s password:%s", apikey, password))
                isPasswordOk =
                    PreyWebServices.getInstance().checkPassword(mContext, apikey!!, password)
            }
            if (isPasswordOk) {
                PreyConfig.getInstance(mContext).setTimePasswordOk()
            }
        } catch (e1: Exception) {
            PreyLogger.e(String.format("login_tipo error1:%s", e1.message), e1)
            error = e1.message!!
        }
        PreyLogger.d(String.format("login_tipo isPasswordOk:%b", isPasswordOk))
        PreyLogger.d(String.format("login_tipo error:%s", error))
        try {
            if (error != null) {
                if (!error.contains("{")) {
                    val json = JSONObject()
                    json.put("error", JSONArray().put(error))
                    error = json.toString()
                }
                return error
            } else if (!isPasswordOk) {
                wrongPasswordIntents++
                val json = JSONObject()
                if (wrongPasswordIntents == 3) {
                    json.put(
                        "error",
                        JSONArray().put(mContext!!.getString(R.string.password_intents_exceed))
                    )
                } else {
                    json.put(
                        "error",
                        JSONArray().put(mContext!!.getString(R.string.password_wrong))
                    )
                }
                error = json.toString()
            } else {
                PreyLogger.d(String.format("login_tipo from:%s", from))
                if ("setting" == from || "rename" == from) {
                    val json = JSONObject()
                    json.put("result", true)
                    return json.toString()
                } else {
                    val intent = Intent(mContext, PanelWebActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    mContext!!.startActivity(intent)
                    if (mActivity != null) mActivity!!.finish()
                }
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("error:%s", e.message), e)
        }
        return error
    }

    @JavascriptInterface
    fun openPanelWeb() {
        val apikey: String? = PreyConfig.getInstance(mContext).getApiKey()
        PreyWebServices.getInstance().getToken(mContext, apikey!!, "X")
        val intent = Intent(mContext, PanelWebActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mContext!!.startActivity(intent)
        if (mActivity != null) mActivity!!.finish()
    }

    @JavascriptInterface
    fun initPin4(): String {
        val initPin4: String? = PreyConfig.getInstance(mContext).getPinNumber()
        PreyLogger.d("initPin4:$initPin4")
        return initPin4!!
    }

    @JavascriptInterface
    fun initVersion(): String {
        val initVersion: String? = PreyConfig.getInstance(mContext).getPreyVersion()
        PreyLogger.d("initVersion:$initVersion")
        return initVersion!!
    }

    @JavascriptInterface
    fun initXiaomi(): Boolean {
        val initXiaomi = "Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
        PreyLogger.d("Manufacter:" + Build.MANUFACTURER + " initXiaomi:" + initXiaomi)
        return initXiaomi
    }

    /**
     * Method to obtain device is Huawei
     * @return true if the device is Huawei, false otherwise
     */
    @JavascriptInterface
    fun initHuawei(): Boolean {
        val initHuawei = "huawei".equals(Build.MANUFACTURER, ignoreCase = true)
        PreyLogger.d(String.format("Manufacter:%s initHuawei:%b", Build.MANUFACTURER, initHuawei))
        return initHuawei
    }

    @JavascriptInterface
    fun initVerify(): Boolean {
        PreyLogger.d("initVerify users:")
        return false
    }

    @JavascriptInterface
    fun setBackground(background: Boolean) {
        if (background) {
            RunBackgroundCheckBoxPreference.notifyReady(mContext)
        } else {
            RunBackgroundCheckBoxPreference.notifyCancel(mContext)
        }
        PreyConfig.getInstance(mContext).setRunBackground(background)
    }

    @JavascriptInterface
    fun setUseBiometric(useBiometric: Boolean) {
        PreyConfig.getInstance(mContext).setUseBiometric(useBiometric)
    }

    @JavascriptInterface
    fun wipe() {
        val builder = AlertDialog.Builder(mContext)
        builder.setMessage(R.string.preferences_detach_summary)
            .setPositiveButton(
                R.string.yes
            ) { dialog, id ->
                PreyLogger.d("wipe:")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    //TODO:cambiar
                    //DetachDevice().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                } else {
                    //TODO:cambiar
                    // DetachDevice().execute()
                }
            }
            .setNegativeButton(
                R.string.no
            ) { dialog, id -> }
        val popup: Dialog = builder.create()
        popup.show()
    }

    @JavascriptInterface
    fun savepin2(pin: String) {
        PreyLogger.d("savepin2:$pin")
        PreyConfig.getInstance(mContext).setPinNumber(pin)
        if ("" == pin) {
            setUninstall(false)
            setShieldOf(false)
        }
    }

    @get:JavascriptInterface
    val twoStepEnabled: Boolean
        get() {
            if (!PreyConfig.getInstance(mContext).isTimeTwoStep()) {
                val twoStepEnabled = PreyWebServices.getInstance().getTwoStepEnabled(mContext)
                PreyConfig.getInstance(mContext).setTwoStep(twoStepEnabled)
                PreyConfig.getInstance(mContext).setTimeTwoStep()
            }
            return PreyConfig.getInstance(mContext).getTwoStep()
        }

    @JavascriptInterface
    fun versionAndroid(): Int {
        return Build.VERSION.SDK_INT
    }

    @JavascriptInterface
    fun versionIsPieOrAbove(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1
    }

    @JavascriptInterface
    fun notificationShieldOf() {
        val alertDialog = AlertDialog.Builder(mContext).create()
        alertDialog.setTitle(R.string.preferences_disable_power_alert_android9_title)
        alertDialog.setMessage(mContext!!.getString(R.string.preferences_disable_power_alert_android9_message))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "OK"
        ) { dialog, which -> }
        alertDialog.show()
        setShieldOf(false)
    }

    @JavascriptInterface
    fun setUninstall(uninstall: Boolean) {
        PreyLogger.d("setUninstall:$uninstall")
        PreyConfig.getInstance(mContext).setBlockAppUninstall(uninstall)
    }

    @JavascriptInterface
    fun setShieldOf(shieldOf: Boolean) {
        PreyLogger.d("setShieldOf:$shieldOf")
        PreyConfig.getInstance(mContext).setDisablePowerOptions(shieldOf)
        if (shieldOf) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = Date().time
            cal.add(Calendar.MINUTE, -1)
            PreyConfig.getInstance(mContext).setTimeSecureLock(cal.timeInMillis)
            PreyConfig.getInstance(mContext).setOpenSecureService(false)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                mContext!!.startService(
                    Intent(
                        mContext,
                        PreyDisablePowerOptionsService::class.java
                    )
                )
            }
        } else {
            mContext!!.stopService(Intent(mContext, PreyDisablePowerOptionsService::class.java))
        }
    }

    @JavascriptInterface
    fun qr() {
        PreyLogger.d("qr")
        val intent = Intent(mContext, BarcodeActivity::class.java)
        mContext!!.startActivity(intent)
        if (mActivity != null) mActivity!!.finish()
    }

    @JavascriptInterface
    fun initMail(): String {
        return PreyConfig.getInstance(mContext).getEmail()!!
    }

    @JavascriptInterface
    fun lock(key: String): String {
        var error2 = ""
        val ctx = mContext
        val unlock: String? = PreyConfig.getInstance(ctx).getUnlockPass()
        PreyLogger.d("lock key:$key  unlock:$unlock")
        if (unlock == null || "" == unlock) {
            PreyConfig.getInstance(ctx).setOverLock(false)
            val pid = Process.myPid()
            Process.killProcess(pid)
            return "{\"ok\":\"ok\"}"
        }
        if (unlock != null && "" != unlock && unlock == key) {
            PreyConfig.getInstance(mContext).setInputWebview("")
            PreyConfig.getInstance(ctx).setUnlockPass("")
            PreyConfig.getInstance(ctx).setOpenSecureService(false)
            val overLock: Boolean = PreyConfig.getInstance(ctx).getOverLock()
            val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(ctx)
            PreyLogger.d("lock key:$key  unlock:$unlock overLock:$overLock canDrawOverlays:$canDrawOverlays")
            object : Thread() {
                override fun run() {
                    val reason = "{\"origin\":\"user\"}"
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        ctx,
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
                if ((unlock == null || "" == unlock)) {
                    PreyLogger.d("lock accc ")
                    PreyConfig.getInstance(ctx).setOverLock(false)
                    if (preyLockHtmlService != null) {
                        preyLockHtmlService!!.stop()
                    } else {
                        val pid = Process.myPid()
                        Process.killProcess(pid)
                    }
                }
            }
            object : Thread() {
                override fun run() {
                    if (canDrawOverlays) {
                        try {
                            if (preyLockHtmlService != null) {
                                preyLockHtmlService!!.stop()
                                val viewLock: View? = PreyConfig.getInstance(ctx).viewLock
                                if (viewLock != null) {
                                    val wm =
                                        ctx!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                    wm.removeView(viewLock)
                                } else {
                                    Process.killProcess(Process.myPid())
                                }
                            }
                        } catch (e: Exception) {
                            Process.killProcess(Process.myPid())
                        }
                    }
                    val intentClose = Intent(ctx, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx!!.startActivity(intentClose)
                    try {
                        sleep(2000)
                    } catch (e: Exception) {
                        PreyLogger.e("Error sleep:" + e.message, e)
                    }
                    ctx.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
                }
            }.start()
            error2 = "{\"ok\":\"ok\"}"
        } else {
            error2 = "{\"error\":[\"" + mContext!!.getString(R.string.password_wrong) + "\"]}"
        }
        PreyLogger.d("error2:$error2")
        return error2
    }

    @JavascriptInterface
    fun changemail(email: String?): String? {
        error = null
        try {
            val ctx = mContext
            //TODO: cambiar
            /*
            val verify: PreyVerify? = PreyWebServices.getInstance().verifyEmail(ctx, email)
            PreyLogger.d("verify:" + (if (verify == null) "" else verify.getStatusCode() + " " + verify.getStatusDescription()))
            if (verify != null) {
                val statusCode: Int = verify.getStatusCode()
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CONFLICT) {
                    PreyConfig.getInstance(mContext).setEmail(email)
                    val okObj = mContext!!.getString(R.string.email_resend)
                    error = "{\"ok\":[\"$okObj\"]}"
                } else {
                    error = verify.getStatusDescription()
                    if (error != null) {
                        if (error!!.indexOf("error") < 0) {
                            error = error!!.replace("\\\"", "'")
                            error = error!!.replace("\"", "")
                            error = error!!.replace("'", "\"")
                        } else {
                            val obj = JSONObject(error)
                            val errorObj = obj.getString("error")
                            if (errorObj != null && errorObj.indexOf("[") < 0) {
                                error = "{\"email\":[\"$errorObj\"]}"
                            }
                        }
                    }
                }
            }
            */
        } catch (e: Exception) {
            error = e.message
            PreyLogger.e("error:$error", e)
        }
        return error
    }

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
            val ctx = mContext
            PreyLogger.d("name:$name")
            PreyLogger.d("email:$email")
            PreyLogger.d("password1:$password1")
            PreyLogger.d("password2:$password2")
            PreyLogger.d("rule_age:$policy_rule_age")
            PreyLogger.d("privacy_terms:$policy_rule_privacy_terms")
            PreyLogger.d("offers:$offers")
            //TODO:cambiar
            /*
            val accountData: PreyAccountData = PreyWebServices.getInstance().registerNewAccount(
                ctx,
                name,
                email,
                password1,
                password2,
                policy_rule_age,
                policy_rule_privacy_terms,
                offers,
                PreyUtils.getDeviceType(mContext)
            )
            PreyLogger.d("Response creating account: " + accountData.toString())
            PreyConfig.getInstance(ctx).saveAccount(accountData)
            */
            PreyConfig.getInstance(ctx).registerC2dm()
            PreyConfig.getInstance(ctx).setEmail(email)
            PreyConfig.getInstance(ctx).setRunBackground(true)
            PreyConfig.getInstance(ctx).setInstallationStatus("Pending")
            PreyApp().run(ctx)
            Location().get(ctx, null, null)
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

    @JavascriptInterface
    fun forgot() {
        PreyLogger.d("forgot")
        val url: String = FileConfigReader.getInstance(mContext)!!.preyForgot
        val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        mContext!!.startActivity(myIntent)
    }

    @JavascriptInterface
    fun givePermissions() {
        PreyLogger.d("givePermissions")
        val canAccessFineLocation: Boolean = PreyPermission.canAccessFineLocation(mContext)
        val canAccessCoarseLocation: Boolean = PreyPermission.canAccessCoarseLocation(mContext)
        val canAccessCamera: Boolean = PreyPermission.canAccessCamera(mContext)
        val canAccessStorage: Boolean = PreyPermission.canAccessStorage(mContext)
        val canAccessBackgroundLocation: Boolean =
            PreyPermission.canAccessBackgroundLocation(mContext)
        val showFineLocation: Boolean = PreyPermission.showRequestFineLocation(mActivity)
        val showCoarseLocation: Boolean = PreyPermission.showRequestCoarseLocation(mActivity)
        val showBackgroundLocation: Boolean =
            PreyPermission.showRequestBackgroundLocation(mActivity)
        val showCamera: Boolean = PreyPermission.showRequestCamera(mActivity)
        val showPhone: Boolean = PreyPermission.showRequestPhone(mActivity)
        val showStorage: Boolean = PreyPermission.showRequestStorage(mActivity)
        val canAccessibility: Boolean = PreyPermission.isAccessibilityServiceView(mContext)
        val canScheduleExactAlarms: Boolean = PreyPermission.canScheduleExactAlarms(mContext)
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
            mActivity!!.openSettings()
        } else {
            if (!canAccessFineLocation || !canAccessCoarseLocation || !canAccessCamera
                || !canAccessStorage
            ) {
                mActivity!!.askForPermission()
            } else {
                val isAdminActive = FroyoSupport.getInstance(mContext)!!.isAdminActive
                if (isAdminActive) {
                    if (canAccessibility) {
                        var canDrawOverlays = true
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) canDrawOverlays =
                            Settings.canDrawOverlays(mContext)
                        if (!canDrawOverlays) {
                            mActivity!!.askForPermissionAndroid7()
                        }
                    } else {
                        mActivity!!.accessibility()
                    }
                } else {
                    mActivity!!.askForAdminActive()
                }
                if (!canScheduleExactAlarms) {
                    mActivity!!.alarms()
                }
            }
        }
    }
    //TODO:Cambiar
    /*
    inner class DetachDevice : AsyncTask<Void?, Void?, Void?>() {
        private var error: String? = null
        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            progressDialog = ProgressDialog(mContext)
            progressDialog!!.setMessage(
                mContext!!.getText(R.string.preferences_detach_dettaching_message).toString()
            )
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }

        protected override fun doInBackground(vararg unused: Void): Void? {
            error = Detach.detachDevice(mContext)
            PreyLogger.d("error:$error")
            return null
        }

        override fun onPostExecute(unused: Void?) {
            try {
                progressDialog!!.dismiss()
            } catch (e: Exception) {
            }
            try {
                if (error != null) {
                    Toast.makeText(mContext, error, Toast.LENGTH_LONG).show()
                } else {
                    val welcome = Intent(mContext, CheckPasswordHtmlActivity::class.java)
                    welcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mContext!!.startActivity(welcome)
                    if (mActivity != null) mActivity!!.finish()
                }
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }
    }*/

    @JavascriptInterface
    fun capsLockOn(): Boolean {
        return PreyConfig.getInstance(mContext).getCapsLockOn()
    }

    @JavascriptInterface
    fun touch() {
        PreyConfig.getInstance(mContext).setCapsLockOn(false)
    }

    @JavascriptInterface
    fun initConfigure(): Boolean {
        PreyConfig.getInstance(mContext).setStart(false)
        val deviceKey: String? = PreyConfig.getInstance(mContext).getDeviceId()
        return if (deviceKey != null && "" != deviceKey) {
            true
        } else {
            false
        }
    }

    @JavascriptInterface
    fun initName(): String {
        var initName = ""
        try {
            initName = PreyConfig.getInstance(mContext).getDeviceName()!!
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("initName:$initName")
        return initName
    }

    @JavascriptInterface
    fun initUserFree(): Boolean {
        var initUserFree = false
        try {
            initUserFree = !PreyConfig.getInstance(mContext).getProAccount()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        PreyLogger.d("initUserFree:$initUserFree")
        return initUserFree
    }

    @JavascriptInterface
    fun rename2(newName: String?): String? {
        PreyLogger.d(String.format("rename:%s", newName))
        var out: String? = null
        try {
            //TODO:cambiar
            /*
            val preyName: PreyName = PreyWebServices.getInstance().renameName(mContext, newName)
            val json = JSONObject()
            json.put("code", preyName.getCode())
            json.put("error", preyName.getError())
            json.put("name", preyName.getName())
            out = json.toString()
            PreyLogger.d(String.format("rename out:%s", out))
            */
        } catch (e: Exception) {
            PreyLogger.e(String.format("rename error:%s", e.message), e)
        }
        return out
    }

    @JavascriptInterface
    fun logger(mylogger: String?) {
        PreyLogger.d(String.format("logger:%s", mylogger))
    }

    @JavascriptInterface
    fun inputwebview(inputwebview: String, page: String) {
        PreyConfig.getInstance(mContext).setInputWebview(inputwebview)
        PreyConfig.getInstance(mContext).setPage(page)
    }

    @JavascriptInterface
    fun approveLocation() {
        PreyLogger.d("approveLocation")
        val canAccessBackgroundLocation: Boolean =
            PreyPermission.canAccessBackgroundLocation(mContext)
        val showBackgroundLocation: Boolean =
            PreyPermission.showRequestBackgroundLocation(mActivity)
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
        mActivity!!.askForPermissionLocation()
    }

    @JavascriptInterface
    fun skipLocation(): Boolean {
        PreyLogger.d("skipLocation")
        val canAccessCamera: Boolean = PreyPermission.canAccessCamera(mContext)
        val canAccessStorage: Boolean = PreyPermission.canAccessStorage(mContext)
        val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(mContext)
        val isAdminActive = FroyoSupport.getInstance(mContext)!!.isAdminActive
        return canAccessCamera && canAccessStorage && canDrawOverlays && canDrawOverlays && isAdminActive
    }

    @JavascriptInterface
    fun skipPermissions() {
        PreyLogger.d("skipPermissions")
    }

    /**
     * Method to bypass background location permission
     */
    @JavascriptInterface
    fun skipPermissionsBg() {
        PreyLogger.d("skipPermissionsBg")
        PreyConfig.getInstance(mContext).setLocationBgDenied(true)
        refresh()
    }

    @JavascriptInterface
    fun verificateAlert(): String {
        return PreyConfig.getInstance(mContext).getLockMessage()!!
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
            openPin = FileConfigReader.getInstance(mContext)!!.openPin
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        return false
    }

    /**
     * Method to open screen disable power button
     */
    @JavascriptInterface
    fun openPin() {
        val time: Long = PreyConfig.getInstance(mContext).getTimeSecureLock()
        val now = Date().time
        var extra: String? = null
        if (now < time) {
            extra = ""
            return
        }
        val pinNumber: String? = PreyConfig.getInstance(mContext).getPinNumber()
        val disablePowerOptions: Boolean =
            PreyConfig.getInstance(mContext).getDisablePowerOptions()
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
                PreyConfig.getInstance(mContext).setViewSecure(true)
                val intentLock = Intent(mContext, PreySecureService::class.java)
                mContext!!.startService(intentLock)
                mActivity!!.finish()
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error CLOSE_SYSTEM:%s", e.message), e)
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
            PreyLogger.e(String.format("closePin error :%s", e.message), e)
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
            PreyLogger.d(String.format("unpin:%s", pinNumber))
            val _pinNumber: String? = PreyConfig.getInstance(mContext).getPinNumber()
            if (_pinNumber == pinNumber) {
                PreyConfig.getInstance(mContext).setPinActivated("")
                PreyConfig.getInstance(mContext).setOpenSecureService(false)
                PreyConfig.getInstance(mContext).setCounterOff(0)
                val cal = Calendar.getInstance()
                cal.timeInMillis = Date().time
                cal.add(Calendar.MINUTE, 2)
                PreyConfig.getInstance(mContext).setTimeSecureLock(cal.timeInMillis)
                closePin()
                out = json.toString()
            } else {
                PreyLogger.d("error")
                json.put("error", JSONArray().put(mContext!!.getString(R.string.password_wrong)))
                out = json.toString()
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error unpin:%s", e.message), e)
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
            initHelpFormForFree = PreyConfig.getInstance(mContext).getHelpFormForFree()
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        return initHelpFormForFree
    }

    /**
     * Method to initialize help file
     */
    @JavascriptInterface
    fun clickInitHelp() {
        PreyConfig.getInstance(mContext).setFileHelp("")
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
        val preyConfig: PreyConfig = PreyConfig.getInstance(mContext)
        val fileHelp: String? = preyConfig.getHelpFile()
        PreyLogger.d(
            String.format(
                "help subject:%s message:%s fileHelp:%s",
                subject,
                message,
                fileHelp
            )
        )
        var out = ""
        try {
            var error = false
            val errorJson = JSONObject()
            if ("-1" == subject) {
                error = true
                errorJson.put(
                    "subject",
                    JSONArray().put(mContext!!.getString(R.string.help_error_subject))
                )
            }
            if (message == null || "" == message || message.length < 10) {
                error = true
                errorJson.put(
                    "message",
                    JSONArray().put(mContext!!.getString(R.string.help_error_message))
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
                        JSONArray().put(mContext!!.getString(R.string.help_error_attachment))
                    )
                }
            }
            PreyLogger.d(String.format("error:%s errorJon:%s", error, errorJson))
            if (error) {
                out = errorJson.toString()
            } else {
                val response = PreyWebServices.getInstance().sendHelp(mContext, subject, message)
                PreyLogger.d(
                    String.format(
                        "response sendHelp:%s",
                        response?.toString() ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error sendHelp:%s", e.message), e)
        }
        return out
    }

    /**
     * Method that opens popup to select image to send
     */
    @JavascriptInterface
    fun searchHelpFile() {
        PreyConfig.getInstance(mContext).setFileHelp("")
        mActivity!!.openImageChooserActivity()
    }

    /**
     * Method method that returns if help file exists
     *
     * @return return help file exists
     */
    @JavascriptInterface
    fun existsHelpFile(): Boolean {
        val displayName: String? = PreyConfig.getInstance(mContext).getHelpFile()
        return if ("" == displayName) {
            false
        } else {
            true
        }
    }

    @get:JavascriptInterface
    val helpFile: String
        /**
         * Method method that returns help file
         *
         * @return return help file
         */
        get() {
            var displayName: String? = PreyConfig.getInstance(mContext).getHelpFile()
            if (!existsHelpFile()) {
                displayName = mContext!!.getString(R.string.help_no_file_chosen)
            }
            return displayName!!
        }

    /**
     * Method to bypass accessibility permission
     */
    @JavascriptInterface
    fun accessibilitySkip() {
        PreyLogger.d("accessibilitySkip")
        PreyConfig.getInstance(mContext).setTimeNextAccessibility()
        refresh()
    }

    /**
     * Method to deny accessibility permission
     */
    @JavascriptInterface
    fun accessibilityDeny() {
        PreyLogger.d("accessibilityDeny")
        PreyConfig.getInstance(mContext).setAccessibilityDenied(true)
        refresh()
    }

    /**
     * Method to grant accessibility permission
     */
    @JavascriptInterface
    fun accessibilityAgree() {
        PreyLogger.d("accessibilityAgree")
        mActivity!!.accessibility()
    }

    /**
     * Method to refresh view
     */
    @JavascriptInterface
    fun refresh() {
        PreyLogger.d("refresh")
        val intentLogin = Intent(mContext, LoginActivity::class.java)
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mContext!!.startActivity(intentLogin)
        mActivity!!.finish()
    }

    /**
     * Method to bypass storage permission
     */
    @JavascriptInterface
    fun allFilesSkip() {
        PreyLogger.d("allFilesSkip")
        PreyConfig.getInstance(mContext).setTimeNextAllFiles()
        refresh()
    }

    /**
     * Method to deny storage permission
     */
    @JavascriptInterface
    fun allFilesDeny() {
        PreyLogger.d("allFilesDeny")
        PreyConfig.getInstance(mContext).setAllFilesDenied(true)
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
        val uri = Uri.fromParts("package", mContext!!.packageName, null)
        intentSetting.setData(uri)
        mContext!!.startActivity(intentSetting)
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

    @JavascriptInterface
    fun initContactFormForFree(): Boolean {
        return PreyConfig.getInstance(mContext).getHelpFormForFree()
    }

    @JavascriptInterface
    fun handleClickHelp() {
        PreyConfig.getInstance(mContext).setFileHelp("")
    }


    @JavascriptInterface
    fun initMspAccount(): Boolean {
        var initMspAccount = false
        try {
            initMspAccount = PreyConfig.getInstance(mContext).getMspAccount()
        } catch (e: Exception) {
            PreyLogger.e("Error initMspAccount:" + e.message, e)
        }
        return initMspAccount
    }

    @JavascriptInterface
    fun uninstall() {
        PreyLogger.d("uninstall")
        val builder = AlertDialog.Builder(mContext)
        builder.setMessage(R.string.preferences_uninstall_summary)
            .setPositiveButton(
                R.string.yes
            ) { dialog, id ->
                PreyLogger.d("uninstall run:")
                //TODO:cambiar
                /*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) DetachDevice()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                else DetachDevice().execute()

                 */
                PreyConfig.getInstance(mContext).setStart(true)
            }
            .setNegativeButton(
                R.string.no
            ) { dialog, id -> }
        val popup: Dialog = builder.create()
        popup.show()
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
            showNotification = if (PreyConfig.getInstance(mContext).getDenyNotification()) {
                false
            } else {
                !PreyPermission.areNotificationsEnabled(mContext)
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
            mActivity!!.askForPermissionNotification()
        } catch (e: Exception) {
            openSettings()
        }
    }

    @JavascriptInterface
    fun openBiometric(typeBiometric: String?) {
        PreyConfig.getInstance(mContext).setTypeBiometric(typeBiometric)
        mActivity!!.openBiometric()
    }

    @JavascriptInterface
    fun verificateBiometric(): Boolean {
        return PreyConfig.getInstance(mContext).getVerificateBiometric()
    }

    @JavascriptInterface
    fun typeBiometric(): String {
        return PreyConfig.getInstance(mContext).getTypeBiometric()!!
    }

    @JavascriptInterface
    fun close() {
        PreyConfig.getInstance(mContext).removeTimePasswordOk()
    }

    @JavascriptInterface
    fun isAccessibilityInformationType(type: String): Boolean {
        return accessibilityInformationType == type
    }

    private val accessibilityInformationType: String
        /**
         * Method that returns what type of accessibility information to show
         *
         * @return type
         */
        get() {
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

    private fun isVendor(vendor: String): Boolean {
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        return manufacturer.lowercase(Locale.getDefault()).contains(vendor) || brand.lowercase(
            Locale.getDefault()
        ).contains(vendor)
    }

    @JavascriptInterface
    fun initAlarms(): Boolean {
        val initAlarms: Boolean = PreyPermission.canScheduleExactAlarms(mContext)
        PreyLogger.d(String.format("initAlarms:%s", initAlarms))
        return initAlarms
    }
}
