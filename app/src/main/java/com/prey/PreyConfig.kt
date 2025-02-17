/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.View
import com.google.android.gms.location.LocationRequest
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.prey.actions.aware.AwareController
import com.prey.actions.location.PreyLocation
import com.prey.json.actions.Location
import com.prey.managers.PreyConnectivityManager
import com.prey.net.PreyWebServices
import com.prey.net.UtilConnection
import com.prey.preferences.RunBackgroundCheckBoxPreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class PreyConfig private constructor(var context: Context) {

    init {
        saveString(PREY_VERSION, getInfoPreyVersion(context));
    }

    var viewLock: View? = null
    var viewSecure: View? = null

    fun setPage(page: String) {
        saveString(CAN_ACCESS_EXTERNAL_STORAGE, page)
    }

    fun getPage(): String? {
        return getString(CAN_ACCESS_EXTERNAL_STORAGE, null)
    }

    fun getApiKey(): String? {
        return getString(API_KEY, null)
    }

    fun setApikey(apikey: String) {
        saveString(API_KEY, apikey)
    }

    fun getInputWebview(): String? {
        return getString(INPUT_WEBVIEW, "")
    }

    fun setInputWebview(inputWebview: String) {
        saveString(INPUT_WEBVIEW, inputWebview)
    }

    fun getPreyDomain(): String {
        return FileConfigReader.getInstance(context).getPreyDomain()
    }

    fun getPreyCampaign(): String {
        return FileConfigReader.getInstance(context).getPreyCampaign()
    }

    fun getPreyPanelUrl(): String {
        val panel: String = FileConfigReader.getInstance(context).getPreyPanel()
        val url = HTTP + panel + "." + getPreyDomain() + "/" + getPreyCampaign()
        return url
    }

    private fun getPreyJwt(): String {
        return FileConfigReader.getInstance(context).getPreyJwt()
    }

    fun getPreyPanelJwt(): String {
        val panel: String = FileConfigReader.getInstance(context).getPreyPanel()
        val url = HTTP + panel + "." + getPreyDomain() + "/" + getPreyJwt()
        return url
    }

    fun getPreyUrl(): String {
        val subdomain: String = FileConfigReader.getInstance(context).getPreySubdomain()
        return HTTP + subdomain + "." + getPreyDomain() + "/"
    }

    fun getMinutesToQueryServer(): Int {
        return getInt(MINUTES_TO_QUERY_SERVER, 15)
    }

    fun setMinutesToQueryServer(minutesToQueryServer: Int) {
        PreyLogger.d("setMinutesToQueryServer [${minutesToQueryServer}]")
        saveInt(MINUTES_TO_QUERY_SERVER, minutesToQueryServer)
    }

    fun getAwareDate(): String? {
        return getString(AWARE_DATE, "")
    }

    fun setAwareDate(awareDate: String) {
        PreyLogger.d("AWARE setAwareDate [$awareDate]")
        saveString(AWARE_DATE, awareDate)
    }

    fun setAware(aware: Boolean) {
        saveBoolean(AWARE, aware)
    }

    fun getAware(): Boolean {
        return getBoolean(AWARE, false)
    }

    fun isLocatigetAwareonAwareEnabled(): Boolean {
        return getBoolean(AWARE, false)
    }

    fun setAutoConnect(autoConnect: Boolean) {
        saveBoolean(AUTO_CONNECT, autoConnect)
    }

    fun getAutoConnect(): Boolean {
        return getBoolean(AUTO_CONNECT, false)
    }

    /**
     * Method add a minute to request location background permission
     */
    fun setTimeNextLocationBg() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 1)
        saveLong(TIME_NEXT_LOCATIONBG, cal.timeInMillis)
    }

    /**
     * Method that returns if it should request location background permission
     *
     * @return if you must ask
     */
    fun isTimeNextLocationBg(): Boolean {
        val timeLocationBg = getLong(TIME_NEXT_LOCATIONBG, 0)
        val timeNow = Date().time
        return timeNow < timeLocationBg
    }

    fun isChromebook(): Boolean {
        return context.getPackageManager().hasSystemFeature("org.chromium.arc.device_management")
    }

    fun isFroyoOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
    }


    fun getUnlockPass(): String? {
        return getString(UNLOCK_PASS, null)
    }

    fun setUnlockPass(unlockPass: String) {
        saveString(UNLOCK_PASS, unlockPass)
    }

    fun setLock(locked: Boolean) {
        saveBoolean(IS_LOCK_SET, locked)
    }

    fun deleteUnlockPass() {
        removeKey(UNLOCK_PASS)
    }


    fun isLockSet(): Boolean {
        return getBoolean(IS_LOCK_SET, false)
    }

    fun isMarshmallowOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun getProtectReady(): Boolean {
        return getBoolean(PROTECT_READY, false)
    }

    fun setProtectReady(protectReady: Boolean) {
        saveBoolean(PROTECT_READY, protectReady)
    }

    fun isThisDeviceAlreadyRegisteredWithPrey(notifyUser: Boolean): Boolean {
        val deviceId = getString(DEVICE_ID, null)
        val isVerified = (deviceId != null && "" != deviceId)
        return isVerified
    }

    fun isThisDeviceAlreadyRegisteredWithPrey(): Boolean {
        val deviceID: String? = getDeviceId()
        return deviceID != null && "" != deviceID
    }


    fun setApiKey(apikey: String?) {
        this.saveString(API_KEY, apikey!!)
    }

    fun getDeviceId(): String? {
        return getString(DEVICE_ID, null)
    }

    fun setDeviceId(deviceId: String) {
        saveString(DEVICE_ID, deviceId)
    }

    fun getDeviceName(): String? {
        return getString(DEVICE_NAME, "")
    }

    fun setDeviceName(deviceName: String) {
        saveString(DEVICE_NAME, deviceName)
    }

    fun getApiKeyBatch(): String {
        return PreyBatch.getInstance(context).getApiKeyBatch()
    }

    fun getEmailBatch(): String? {
        return PreyBatch.getInstance(context).getEmailBatch()
    }

    fun getJobIdLock(): String? {
        return getString(JOB_ID_LOCK, "")
    }

    fun setJobIdLock(jobIdLock: String) {
        saveString(JOB_ID_LOCK, jobIdLock)
    }

    fun getSsid(): String? {
        return getString(SSID, "")
    }

    fun setSsid(ssid: String) {
        saveString(SSID, ssid)
    }

    fun getImei(): String? {
        return getString(IMEI, "")
    }

    fun setImei(imei: String) {
        saveString(IMEI, imei)
    }

    fun getModel(): String? {
        return getString(MODEL, "")
    }

    fun setModel(model: String) {
        saveString(MODEL, model)
    }

    fun setLocation(location: PreyLocation?) {
        if (location != null) {
            saveFloat(LOCATION_LAT, location.getLat().toFloat())
            saveFloat(LOCATION_LNG, location.getLng().toFloat())
            saveFloat(LOCATION_ACCURACY, location.getAccuracy())
        } else {
            removeKey(LOCATION_LAT)
            removeKey(LOCATION_LNG)
            removeKey(LOCATION_ACCURACY)
        }
    }

    fun getLocation(): PreyLocation? {
        try {
            val location = PreyLocation()
            val lat: Float = getFloat(LOCATION_LAT, 0f)
            val lng: Float = getFloat(LOCATION_LNG, 0f)
            val accuracy: Float = getFloat(LOCATION_ACCURACY, 0f)
            if (lat == 0f || lng == 0f) {
                return null
            }
            location.setLat(lat.toDouble())
            location.setLng(lng.toDouble())
            location.setAccuracy(accuracy)
            return location
        } catch (e: Exception) {
            return null
        }

    }

    private fun getLong(key: String, defaultValue: Long): Long {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getLong(key, defaultValue)
    }

    private fun getFloat(key: String, defaultValue: Float): Float {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getFloat(key, defaultValue)
    }

    private fun containsKey(key: String): Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.contains(key)
    }

    fun setPinNumber(pin: String) {
        saveString(PIN_NUMBER2, pin)
    }

    fun getPinNumber(): String? {
        var pin = getString(PIN_NUMBER2, "")
        if (pin == null) {
            pin = ""
        }
        if (pin.length > 4) {
            pin = pin.substring(0, 4)
        }
        return pin
    }

    fun getPublicIp(): String? {
        return getString(PUBLIC_IP, "")
    }

    fun setPublicIp(publicIp: String) {
        saveString(PUBLIC_IP, publicIp)
    }

    fun isRunBackground(): Boolean {
        return getRunBackground()
    }

    fun getRunBackground(): Boolean {
        return getBoolean(PREFS_RUN_BACKGROUND, false)
    }

    fun getUseBiometric(): Boolean {
        return getBoolean(PREFS_USE_BIOMETRIC, false)
    }

    fun setUseBiometric(useBiometric: Boolean) {
        saveBoolean(PREFS_USE_BIOMETRIC, useBiometric)
    }

    fun isAndroid10OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= BUILD_VERSION_CODES_10
    }


    fun isBlockAppUninstall(): Boolean {
        return getBlockAppUninstall()
    }

    fun getBlockAppUninstall(): Boolean {
        return getBoolean(PREFS_BLOCK_APP_UNINSTALL, false)
    }

    fun setBlockAppUninstall(blockAppUninstall: Boolean) {
        saveBoolean(PREFS_BLOCK_APP_UNINSTALL, blockAppUninstall)
    }

    fun isDisablePowerOptions(): Boolean {
        return getDisablePowerOptions()
    }

    fun getDisablePowerOptions(): Boolean {
        return getBoolean(PREFS_DISABLE_POWER_OPTIONS, false)
    }

    fun setDisablePowerOptions(disablePowerOptions: Boolean) {
        saveBoolean(PREFS_DISABLE_POWER_OPTIONS, disablePowerOptions)
    }

    fun isScheduled(): Boolean {
        return false
    }

    fun setMinuteScheduled(minuteScheduled: Int) {
        saveInt(MINUTE_SCHEDULED, minuteScheduled)
    }

    fun getMinuteScheduled(): Int {
        return getInt(MINUTE_SCHEDULED, 0)
    }

    fun getError(): String? {
        return getString(ERROR, null)
    }

    fun setError(error: String) {
        saveString(ERROR, error)
    }

    fun getTypeBiometric(): String? {
        return getString(TYPE_BIOMETRIC, "")
    }

    fun setTypeBiometric(typeBiometric: String) {
        saveString(TYPE_BIOMETRIC, typeBiometric)
    }

    fun getVerificateBiometric(): Boolean {
        val verificateBiometric = getBoolean(VERIFICATE_BIOMETRIC, false)
        if (verificateBiometric) {
            setVerificateBiometric(false)
        }
        return verificateBiometric
    }

    fun setVerificateBiometric(verificateBiometric: Boolean) {
        saveBoolean(VERIFICATE_BIOMETRIC, verificateBiometric)
    }

    fun removeTimePasswordOk() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, -3)
        saveLong(TIME_PASSWORD_OK, cal.timeInMillis)
    }

    fun setTimePasswordOk() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 3)
        saveLong(TIME_PASSWORD_OK, cal.timeInMillis)
    }

    fun isTimePasswordOk(): Boolean {
        val timePasswordOk = getLong(TIME_PASSWORD_OK, 0)
        val timeNow = Date().time
        return if (timeNow < timePasswordOk) {
            true
        } else {
            false
        }
    }


    fun setDenyNotification(denyNotification: Boolean) {
        saveBoolean(DENY_NOTIFICATION, denyNotification)
    }


    fun getHelpFile(): String? {
        return getString(HELP_FILE, "")
    }

    fun setFileHelp(fileHelp: String) {
        saveString(HELP_FILE, fileHelp)
    }

    fun getMspAccount(): Boolean {
        return getBoolean(MSP_ACCOUNT, false)
    }

    fun setMspAccount(mspAccount: Boolean) {
        saveBoolean(MSP_ACCOUNT, mspAccount)
    }

    /**
     * Method to deny storage permission
     *
     * @param denied
     */
    fun setAllFilesDenied(denied: Boolean) {
        saveBoolean(ALLFILES_DENIED, denied)
    }

    /**
     * Method to deny notification permission
     *
     * @return denied
     */
    fun getDenyNotification(): Boolean {
        return getBoolean(DENY_NOTIFICATION, false)
    }

    /**
     * Method that returns if it should request storage permission
     *
     * @return if you must ask
     */
    fun isTimeNextAllFiles(): Boolean {
        val timeLocationAllfiles = getLong(TIME_NEXT_ALLFILES, 0)
        val timeNow = Date().time
        return timeNow < timeLocationAllfiles
    }

    /**
     * Method add a minute to request storage permission
     */
    fun setTimeNextAllFiles() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 1)
        saveLong(TIME_NEXT_ALLFILES, cal.timeInMillis)
    }

    /**
     * Method to deny accessibility permission
     *
     * @param denied
     */
    fun setAccessibilityDenied(denied: Boolean) {
        saveBoolean(ACCESSIBILITY_DENIED, denied)
    }

    /**
     * Method that gets whether to deny accessibility permission
     *
     * @return if you should deny
     */
    fun getAccessibilityDenied(): Boolean {
        return getBoolean(ACCESSIBILITY_DENIED, false)
    }


    /**
     * Method add a minute to request accessibility permission
     */
    fun setTimeNextAccessibility() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 1)
        saveLong(TIME_NEXT_ACCESSIBILITY, cal.timeInMillis)
    }

    /**
     * Method that returns if it should request accessibility permission
     *
     * @return if you must ask
     */
    fun isTimeNextAccessibility(): Boolean {
        val timeLocationAware = getLong(TIME_NEXT_ACCESSIBILITY, 0)
        val timeNow = Date().time
        return timeNow < timeLocationAware
    }

    fun setPinActivated(number_activated: String) {
        saveString(PIN_NUMBER_ACTIVATE, number_activated)
    }

    fun getPinActivated(): String? {
        return getString(PIN_NUMBER_ACTIVATE, "")
    }


    fun setOpenSecureService(openSecureService: Boolean) {
        saveBoolean(OPEN_SECURE_SERVICE, openSecureService)
    }

    fun isOpenSecureService(): Boolean {
        return getBoolean(OPEN_SECURE_SERVICE, false)
    }

    fun getCounterOff(): Int {
        return getInt(COUNTER_OFF, 0)
    }

    fun setCounterOff(counter: Int) {
        saveInt(COUNTER_OFF, counter)
    }

    fun setTimeSecureLock(timeSecureLock: Long) {
        saveLong(TIME_SECURE_LOCK, timeSecureLock)
    }

    fun getTimeSecureLock(): Long {
        return getLong(TIME_SECURE_LOCK, 0)
    }

    fun getEmail(): String? {
        return getString(EMAIL, "")
    }

    fun setEmail(email: String) {
        saveString(EMAIL, email)
    }

    fun setRunBackground(disablePowerOptions: Boolean) {
        saveBoolean(PREFS_RUN_BACKGROUND, disablePowerOptions)
        saveBoolean(PREFS_BACKGROUND, disablePowerOptions)
    }

    fun getInstallationStatus(): String? {
        return getString(INSTALLATION_STATUS, "")
    }

    fun setInstallationStatus(installationStatus: String) {
        saveString(INSTALLATION_STATUS, installationStatus)
    }

    fun getPreyVersion(): String? {
        return getString(PREY_VERSION, "")
    }

    fun setPreyVersion(version: String) {
        saveString(PREY_VERSION, version)
    }

    fun setTimeTwoStep() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 2)
        saveLong(TIME_TWO_STEP, cal.timeInMillis)
    }

    fun isTimeTwoStep(): Boolean {
        val timePasswordOk = getLong(TIME_TWO_STEP, 0)
        val timeNow = Date().time
        return if (timeNow < timePasswordOk) {
            true
        } else {
            false
        }
    }

    fun getTwoStep(): Boolean {
        return getBoolean(TWO_STEP, false)
    }

    fun setTwoStep(twoStep: Boolean) {
        saveBoolean(TWO_STEP, twoStep)
    }

    fun getOverLock(): Boolean {
        return getBoolean(OVER_LOCK, false)
    }

    fun setOverLock(overLock: Boolean) {
        saveBoolean(OVER_LOCK, overLock)
    }

    fun saveAccount(accountData: PreyAccountData) {
        saveBoolean(ACCOUNT, true)
        saveString(DEVICE_ID, accountData.getDeviceId())
        saveString(API_KEY, accountData.getApiKey())
        saveString(EMAIL, accountData.getEmail())
    }

    fun getCapsLockOn(): Boolean {
        return getBoolean(CAPS_LOCK_ON, false)
    }

    fun setCapsLockOn(capsLockOn: Boolean) {
        saveBoolean(CAPS_LOCK_ON, capsLockOn)
    }

    fun getStart(): Boolean {
        return getBoolean(START, true)
    }

    fun setStart(start: Boolean) {
        saveBoolean(START, start)
    }

    fun getProAccount(): Boolean {
        return getBoolean(PRO_ACCOUNT, false)
    }

    fun setProAccount(proAccount: Boolean) {
        saveBoolean(PRO_ACCOUNT, proAccount)
    }


    /**
     * Method to deny location background permission
     *
     * @param denied
     */
    fun setLocationBgDenied(denied: Boolean) {
        saveBoolean(LOCATIONBG_DENIED, denied)
    }

    /**
     * Method that gets whether to deny location background permission
     *
     * @return if you should deny
     */
    fun getLocationBgDenied(): Boolean {
        return getBoolean(LOCATIONBG_DENIED, false)
    }

    fun getLockMessage(): String? {
        return getString(LOCK_MESSAGE, null)
    }

    fun setLockMessage(unlockPass: String) {
        saveString(LOCK_MESSAGE, unlockPass)
    }

    fun getViewSecure(): Boolean {
        return getBoolean(VIEW_SECURE, false)
    }

    fun setViewSecure(viewSecure: Boolean) {
        saveBoolean(VIEW_SECURE, viewSecure)
    }

    fun isAccount(): Boolean {
        return getBoolean(ACCOUNT, false)
    }

    fun setRegisterC2dm(registerC2dm: Boolean) {
        saveBoolean(REGISTER_C2DM, registerC2dm)
    }

    fun isRegisterC2dm(): Boolean {
        return getBoolean(REGISTER_C2DM, false)
    }

    fun setTimeC2dm() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 5)
        saveLong(TIME_C2DM, cal.timeInMillis)
    }

    fun isTimeC2dm(): Boolean {
        val timeC2dm = getLong(TIME_C2DM, 0)
        val timeNow = Date().time
        return if (timeNow < timeC2dm) {
            true
        } else {
            false
        }
    }

    fun setNotificationId(notificationId: String) {
        saveString(NOTIFICATION_ID, notificationId)
    }

    fun getNotificationId(): String? {
        return getString(NOTIFICATION_ID, "")
    }

    fun sendToken(context: Context, token: String) {
        PreyLogger.d("registerC2dm send token:$token")
        if (token != null && "null" != token && "" != token && UtilConnection.getInstance()
                .isInternetAvailable()
        ) {
            try {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val registration: String =
                    FileConfigReader.getInstance(context).getGcmIdPrefix() + token
                PreyLogger.d("registerC2dm registration:$registration")
                val response =
                    PreyWebServices.getInstance().setPushRegistrationId(context, registration)
                getInstance(context).setNotificationId(registration)
                if (response != null) {
                    PreyLogger.d("registerC2dm response:$response")
                }
                getInstance(context).setRegisterC2dm(true)
                getInstance(context).setTimeC2dm()
            } catch (e: java.lang.Exception) {
                PreyLogger.e("registerC2dm error:" + e.message, e)
            }
        }
    }

    fun registerC2dm() {
        // synchronized(PreyConfig.class) {
        val deviceId: String? = getInstance(context).getDeviceId()
        val isTimeC2dm: Boolean = getInstance(context).isTimeC2dm()
        PreyLogger.d("registerC2dm deviceId:$deviceId isTimeC2dm:$isTimeC2dm")
        if (deviceId != null && "" != deviceId) {
            if (!isTimeC2dm) {
                var token: String? = null
                try {
                    token = FirebaseInstanceId.getInstance().token
                    if (token != null) {
                        PreyLogger.d("registerC2dm token2:$token")
                        sendToken(context, token)
                    }
                } catch (e: java.lang.Exception) {
                    PreyLogger.e("registerC2dm error:" + e.message, e)
                    try {
                        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
                            val token = instanceIdResult.token
                            sendToken(context, token)
                        }
                    } catch (ex: java.lang.Exception) {
                        try {
                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        PreyLogger.e(

                                            "registerC2dm error:${task.exception!!.message}",

                                            task.exception
                                        )
                                    }
                                    val token = task.result
                                    PreyLogger.d("registerC2dm token:${token}")
                                    sendToken(context, token)
                                }
                        } catch (exception: java.lang.Exception) {
                            PreyLogger.e(
                                "registerC2dm error:${exception.message}",
                                exception
                            )
                        }
                    }
                }
            }
        }
        //   }
    }


    /**
     * Retrieves the organization ID from the configuration.
     *
     * @return The organization ID, or an empty string if not set.
     */
    fun getOrganizationId(): String? {
        // Retrieve the organization ID from the configuration, defaulting to an empty string if not set
        return getString(ORGANIZATION_ID, "")
    }

    /**
     * Sets the organization ID in the configuration.
     *
     * @param organizationId The organization ID to set.
     */
    fun setOrganizationId(organizationId: String) {
        // Save the organization ID to the configuration
        saveString(ORGANIZATION_ID, organizationId)
    }

    /**
     * Registers a new device with the given API key.
     *
     * @param apiKey The API key to register the device with.
     * @throws Exception If there is an error during the registration process.
     */
    @Throws(java.lang.Exception::class)
    fun registerNewDeviceWithApiKey(apiKey: String) {
        // Check if the device is already registered with Prey
        if (!isThisDeviceAlreadyRegisteredWithPrey()) {
            // Get the device type and name
            val deviceType = PreyUtils.getDeviceType(context)
            val nameDevice = PreyUtils.getNameDevice(context)
            PreyLogger.d(

                "apikey:${apiKey} type:${deviceType} nameDevice:${nameDevice}"

            )


            // Register the device with the API key, device type, and name
            val accountData: PreyAccountData? = PreyWebServices.getInstance()
                .registerNewDeviceWithApiKeyEmail(context, apiKey, deviceType, nameDevice)
            if (accountData != null) {
                PreyConfig.getInstance(context).saveAccount(accountData)
                // Register C2DM
                PreyConfig.getInstance(context).registerC2dm()
                // Get the email associated with the account
                val email = PreyWebServices.getInstance().getEmail(context)
                if (email != null) {
                    PreyLogger.d("email:$email")
                    PreyConfig.getInstance(context).setEmail(email)
                }
                PreyConfig.getInstance(context).setRunBackground(true)
                PreyConfig.getInstance(context).setInstallationStatus("")
                // Run the Prey app
                PreyApp().run(context)
                // Start a new thread to initialize PreyStatus and Location
                object : Thread() {
                    override fun run() {
                        try {
                            PreyStatus.getInstance().initConfig(context)


                        } catch (e: java.lang.Exception) {
                            // Log any errors that occur during initialization
                            PreyLogger.e("Error:" + e.message, e)
                        }
                    }
                }.start()
            }
        }
    }

    fun setPermissionLocation(permission_location: Boolean) {
        saveBoolean(PERMISSION_LOCATION, permission_location)
    }

    fun getPermissionLocation(): Boolean {
        return getBoolean(PERMISSION_LOCATION, true)
    }

    fun getFlagFeedback(): Int {
        return getInt(FLAG_FEEDBACK, 0)
    }

    fun setFlagFeedback(flagFeedback: Int) {
        saveInt(FLAG_FEEDBACK, flagFeedback)
    }

    fun getTokenJwt(): String? {
        return getString(TOKEN_JWT, "")
    }

    fun setTokenJwt(tokenJwt: String) {
        saveString(TOKEN_JWT, tokenJwt)
    }

    fun setAccountVerified(accountVerified: Boolean) {
        saveBoolean(PREFS_ACCOUNT_VERIFIED, accountVerified)
    }

    fun isAccountVerified(): Boolean {
        return getBoolean(PREFS_ACCOUNT_VERIFIED, false)
    }

    fun setNoficationPopupId(noficationPopupId: Int) {
        saveInt(NOTIFICATION_POPUP_ID, noficationPopupId)
    }

    fun getNoficationPopupId(): Int {
        return getInt(NOTIFICATION_POPUP_ID, 0)
    }

    fun isEclairOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR
    }

    fun setProtectAccount(protectAccount: Boolean) {
        saveBoolean(PROTECT_ACCOUNT, protectAccount)
    }

    fun getProtectAccount(): Boolean {
        return getBoolean(PROTECT_ACCOUNT, false)
    }

    fun setProtectTour(protectTour: Boolean) {
        saveBoolean(PROTECT_TOUR, protectTour)
    }

    fun getProtectTour(): Boolean {
        return getBoolean(PROTECT_TOUR, false)
    }

    fun setProtectPrivileges(protectPrivileges: Boolean) {
        saveBoolean(PROTECT_PRIVILEGES, protectPrivileges)
    }

    fun getProtectPrivileges(): Boolean {
        return getBoolean(PROTECT_PRIVILEGES, false)
    }


    fun initTimeC2dm() {
        saveLong(TIME_C2DM, 0)
    }

    fun isMissing(): Boolean {
        return getBoolean(PREFS_IS_MISSING, false)
    }

    fun setMissing(missing: Boolean) {
        saveBoolean(PREFS_IS_MISSING, missing)
    }

    fun getIntervalReport(): String? {
        return getString(INTERVAL_REPORT, "")
    }

    fun setIntervalReport(intervalReport: String) {
        saveString(INTERVAL_REPORT, intervalReport)
    }

    fun getExcludeReport(): String? {
        return getString(EXCLUDE_REPORT, "")
    }

    fun setExcludeReport(excludeReport: String) {
        saveString(EXCLUDE_REPORT, excludeReport)
    }

    fun setPermission(permission: String, value: Boolean) {
        saveBoolean(permission, value)
    }

    fun getPermission(permission: String, defaultValue: Boolean): Boolean {
        return getBoolean(permission, defaultValue)
    }

    fun isAskForNameBatch(): Boolean {
        return PreyBatch.getInstance(context).isAskForNameBatch()
    }

    fun getSessionId(): String? {
        return getString(SESSION_ID, "")
    }

    fun setSessionId(sessionId: String) {
        saveString(SESSION_ID, sessionId)
    }

    fun setCamouflageSet(camouflageSet: Boolean) {
        saveBoolean(IS_CAMOUFLAGE_SET, camouflageSet)
    }

    fun isCamouflageSet(): Boolean {
        return getBoolean(IS_CAMOUFLAGE_SET, false)
    }

    fun getReportNumber(): Int {
        return getInt(REPORT_NUMBER, 0)
    }

    fun setReportNumber(number_report: Int) {
        saveInt(REPORT_NUMBER, number_report)
    }

    fun setInstallationDate(installationDate: Long) {
        saveLong(INSTALLATION_DATE, installationDate)
    }

    fun getInstallationDate(): Long {
        return getLong(INSTALLATION_DATE, 0)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getBoolean(key, defaultValue)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        try {
            val settings = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = settings.edit()
            editor.putBoolean(key, value)
            editor.commit()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun saveInt(key: String, value: Int) {
        try {
            val settings = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = settings.edit()
            editor.putInt(key, value)
            editor.commit()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun getInt(key: String, defaultValue: Int): Int {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getInt(key, defaultValue)
    }

    private fun saveString(key: String, value: String) {
        try {
            val settings = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = settings.edit()
            editor.putString(key, value)
            editor.commit()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun getString(key: String, defaultValue: String?): String? {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getString(key, defaultValue)
    }

    private fun saveLong(key: String, value: Long) {
        try {
            val settings = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = settings.edit()
            editor.putLong(key, value)
            editor.commit()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun saveFloat(key: String, value: Float) {
        try {
            val settings = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = settings.edit()
            editor.putFloat(key, value)
            editor.commit()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    private fun removeKey(key: String) {
        try {
            val settings = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = settings.edit()
            editor.remove(key)
            editor.commit()
        } catch (e: Exception) {
            PreyLogger.e("removeKey:" + e.message, e)
        }
    }


    fun canAccessFineLocation(): Boolean {
        return getBoolean(CAN_ACCESS_FINE_LOCATION, false)
    }

    fun setCanAccessCoarseLocation(canAccessCoarseLocation: Boolean) {
        this.saveBoolean(CAN_ACCESS_COARSE_LOCATION, canAccessCoarseLocation)
    }

    fun canAccessCoarseLocation(): Boolean {
        return getBoolean(CAN_ACCESS_COARSE_LOCATION, false)
    }

    fun setCanAccessCamara(canAccessCamara: Boolean) {
        this.saveBoolean(CAN_ACCESS_CAMARA, canAccessCamara)
    }

    fun canAccessCamara(): Boolean {
        return getBoolean(CAN_ACCESS_CAMARA, false)
    }

    fun setCanAccessReadPhoneState(canAccessReadPhoneState: Boolean) {
        this.saveBoolean(CAN_ACCESS_READ_PHONE_STATE, canAccessReadPhoneState)
    }

    fun canAccessReadPhoneState(): Boolean {
        return getBoolean(CAN_ACCESS_READ_PHONE_STATE, false)
    }

    fun setCanAccessExternalStorage(canAccessExternalStorage: Boolean) {
        this.saveBoolean(CAN_ACCESS_EXTERNAL_STORAGE, canAccessExternalStorage)
    }

    fun canAccessExternalStorage(): Boolean {
        return getBoolean(CAN_ACCESS_EXTERNAL_STORAGE, false)
    }

    fun getInfoPreyVersion(context: Context): String {
        var versionName: String = VERSION_PREY_DEFAULT
        try {
            val pinfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = pinfo.versionName!!
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return versionName
    }

    fun getLocationInfo(): String? {
        return getString(LOCATION_INFO, "")
    }

    fun setLocationInfo(locationInfo: String) {
        saveString(LOCATION_INFO, locationInfo)
    }

    fun getDistanceLocation(): Int {
        return FileConfigReader.getInstance(context).getDistanceLocation()
    }

    fun unregisterC2dm(updatePrey: Boolean) {
        try {
            if (updatePrey) PreyWebServices.getInstance().setPushRegistrationId(context, "")
            val unregIntent = Intent("com.google.android.c2dm.intent.UNREGISTER")
            unregIntent.putExtra(
                "app",
                PendingIntent.getBroadcast(context, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
            )
            context.startService(unregIntent)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    fun setSecurityPrivilegesAlreadyPrompted(securityPrivilegesAlreadyPrompted: Boolean) {
        //TODO:cambiar
        //this.securityPrivilegesAlreadyPrompted = securityPrivilegesAlreadyPrompted
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = settings.edit()
        editor.putBoolean(PREFS_SECURITY_PROMPT_SHOWN, securityPrivilegesAlreadyPrompted)
        editor.commit()
    }

    fun wipeData() {
        val installationDate = getLong(INSTALLATION_DATE, 0)
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = settings.edit()
        editor.clear()
        editor.commit()
        if (installationDate > 0) {
            saveLong(INSTALLATION_DATE, installationDate)
        }
    }

    fun removeDeviceId() {
        this.removeKey(DEVICE_ID)
    }

    fun removeEmail() {
        this.removeKey(EMAIL)
    }

    fun removeApiKey() {
        this.removeKey(API_KEY)
    }


    fun deleteCacheInstance(context: Context) {
        instance = PreyConfig(context)
    }

    fun setLocationAware(location: PreyLocation?) {
        if (location != null) {
            saveString(AWARE_LAT, location.getLat().toString())
            saveString(AWARE_LNG, location.getLng().toString())
            saveFloat(AWARE_ACC, location.getAccuracy())
        }
    }

    fun removeLocationAware() {
        saveString(AWARE_LAT, "")
        saveString(AWARE_LNG, "")
        saveFloat(AWARE_ACC, 0f)
        saveString(AWARE_DATE, "")
    }

    fun getPrefsBiometric(): Boolean {
        return getBoolean(com.prey.PreyConfig.PREFS_BIOMETRIC, false)
    }

    fun setPrefsBiometric(prefsBiometric: Boolean) {
        saveBoolean(com.prey.PreyConfig.PREFS_BIOMETRIC, prefsBiometric)
    }

    fun getPreviousSsid(): String? {
        return getString(PREVIOUS_SSID, null)
    }

    fun setPreviousSsid(previousSsid: String) {
        this.saveString(PREVIOUS_SSID, previousSsid)
    }

    fun setTimeLocationAware() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 1)
        saveLong(com.prey.PreyConfig.TIME_LOCATION_AWARE, cal.timeInMillis)
    }

    fun isTimeLocationAware(): Boolean {
        val timeLocationAware = getLong(com.prey.PreyConfig.TIME_LOCATION_AWARE, 0)
        val timeNow = Date().time
        return if (timeNow < timeLocationAware) {
            true
        } else {
            false
        }
    }

    fun getLowBatteryDate(): Long {
        return getLong(LOW_BATTERY_DATE, 0)
    }

    fun setLowBatteryDate(lowBatteryDate: Long) {
        saveLong(LOW_BATTERY_DATE, lowBatteryDate)
    }

    fun getSimSerialNumber(): String? {
        return getString(com.prey.PreyConfig.SIM_SERIAL_NUMBER, null)
    }

    fun setSimSerialNumber(simSerialNumber: String) {
        saveString(com.prey.PreyConfig.SIM_SERIAL_NUMBER, simSerialNumber)
    }

    /**
     * Retrieves the location aware settings.
     *
     * @return A PreyLocation object containing the location aware settings, or null if the settings are not available.
     */
    fun getLocationAware(): PreyLocation? {
        try {
            // Initialize latitude and longitude variables
            var lat: String? = ""
            var lng: String? = ""
            // Attempt to retrieve the latitude and longitude values from storage
            //The data saving is changed to string because decimals are lost
            try {
                lat = getString(AWARE_LAT, "")
                lng = getString(AWARE_LNG, "")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error getLocationAware:${e.message}", e)
            }
            // Retrieve the accuracy value from storage
            val acc = getFloat(AWARE_ACC, 0f)
            // Check if the latitude or longitude values are empty or null
            if (lat == null || "" == lat || lng == null || "" == lng) {
                // If either value is empty or null, return null
                return null
            }
            // Create a new PreyLocation object
            val location = PreyLocation()
            location.setLat(lat.toDouble())
            location.setLng(lng.toDouble())
            location.setAccuracy(acc)
            // Return the PreyLocation object
            return location
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error getLocationAware:${e.message}", e)
            return null
        }
    }


    fun getDistanceAware(): Int {
        return FileConfigReader.getInstance(context).getDistanceAware()
    }

    /**
     * Key for storing the aware time in the configuration.
     */

    val AWARE_TIME: String = "AWARE_TIME"

    /**
     * Sets the aware time to 10 minutes in the future.
     *
     * This method updates the aware time stored in the configuration.
     */
    fun setAwareTime() {
        //the date is saved 10 minutes in the future
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, 10)
        val dateTimeLong = cal.timeInMillis
        PreyLogger.d("AWARE WORK AwareTime [${dateTimeLong}]")
        saveLong(Companion.AWARE_TIME, dateTimeLong)
    }

    /**
     * Checks if it's time for the next aware event.
     *
     * This method compares the current time with the saved aware time.
     * It is used to not request the location for at least 10 minutes
     *
     * @return true if it's time for the next aware event, false otherwise
     */
    fun isTimeNextAware(): Boolean {
        //validates if the saved date is old
        val awareTime = getLong(AWARE_TIME, 0)
        if (awareTime == 0L) return true
        val timeNow = Date().time
        PreyLogger.d(

            "AWARE WORK AwareTime difference [${(timeNow - awareTime)}] current[${timeNow}] > save[${awareTime}] "
        )
        return timeNow > awareTime
    }

    fun getLastEvent(): String? {
        return getString(LAST_EVENT, null)
    }

    fun setLastEvent(lastEvent: String) {
        saveString(LAST_EVENT, lastEvent)
    }

    fun setLocationLowBattery(locationLowBattery: Boolean) {
        saveBoolean(PREFERENCE_LOCATION_LOW_BATTERY, locationLowBattery)
    }

    fun isLocationLowBattery(): Boolean {
        return getBoolean(PREFERENCE_LOCATION_LOW_BATTERY, false)
    }

    fun setLocationLowBatteryDate(locationLowBatteryDate: Long) {
        saveLong(LOCATION_LOW_BATTERY_DATE, locationLowBatteryDate)
    }

    fun getLocationLowBatteryDate(): Long {
        return getLong(LOCATION_LOW_BATTERY_DATE, 0)
    }

    val DAILY_LOCATION: String = "DAILY_LOCATION"

    fun getDailyLocation(): String? {
        return getString(Companion.DAILY_LOCATION, "")
    }

    fun setDailyLocation(dailyLocation: String) {
        PreyLogger.d("DAILY setDailyLocation [${dailyLocation}]")
        saveString(Companion.DAILY_LOCATION, dailyLocation)
    }

    fun setLastReportStartDate(lastReportStartDate: Long) {
        saveLong(LAST_REPORT_START_DATE, lastReportStartDate)
    }

    fun getLastReportStartDate(): Long {
        return getLong(LAST_REPORT_START_DATE, 0)
    }

    fun isConnectionExists(): Boolean {
        var isConnectionExists = false
        // There is wifi connexion?
        if (PreyConnectivityManager.getInstance().isWifiConnected(context)) {
            isConnectionExists = true
        }
        // if there is no connexion wifi, verify mobile connection?
        if (!isConnectionExists && PreyConnectivityManager.getInstance()
                .isMobileConnected(context)
        ) {
            isConnectionExists = true
        }
        return isConnectionExists
    }

    fun getHelpFormForFree(): Boolean {
        return getBoolean(CONTACT_FORM_FOR_FREE, false)
    }

    fun setContactFormForFree(contactFree: Boolean) {
        saveBoolean(CONTACT_FORM_FOR_FREE, contactFree)
    }

    fun setVolume(volume: Int) {
        saveInt(VOLUME, volume)
    }

    /**
     * Method that returns the volume before the report
     *
     * @return volume
     */
    fun getVolume(): Int {
        return getInt(VOLUME, 0)
    }

    fun isSentUuidSerialNumber(): Boolean {
        return getBoolean(SENT_UUID_SERIAL_NUMBER, false)
    }

    fun setSentUuidSerialNumber(sentUuidSerialNumber: Boolean) {
        saveBoolean(SENT_UUID_SERIAL_NUMBER, sentUuidSerialNumber)
    }

    fun isSendData(): Boolean {
        return getBoolean(SEND_DATA, false)
    }

    fun setSendData(sendData: Boolean) {
        saveBoolean(SEND_DATA, sendData)
    }

    private var nextNotificationId = 100

    fun getNextNotificationId(): Int {
        return nextNotificationId++
    }

    companion object {
        private var instance: PreyConfig? = null
        fun getInstance(context: Context): PreyConfig {
            return instance ?: PreyConfig(context).also { instance = it }
        }

        //Set false in production
        const val LOG_DEBUG_ENABLED: Boolean = false
        const val TAG: String = "PREY"
        private const val HTTP = "https://"
        const val VERSION_PREY_DEFAULT: String = "2.4.9"

        // Milliseconds per second
        private const val MILLISECONDS_PER_SECOND = 1000

        // Set to 1000 * 60 in production.
        const val DELAY_MULTIPLIER: Long = (60 * 1000).toLong()

        // the minimum time interval for GPS notifications, in milliseconds (default 60000).
        val UPDATE_INTERVAL: Long = (60 * MILLISECONDS_PER_SECOND).toLong()

        // the minimum distance interval for GPS notifications, in meters (default 20)
        const val LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE: Float = 20f

        // max "age" in ms of last location (default 120000).
        val LAST_LOCATION_MAX_AGE: Long =
            (30 * MILLISECONDS_PER_SECOND).toLong()
        const val LOCATION_PRIORITY_HIGHT: Int = LocationRequest.PRIORITY_HIGH_ACCURACY
        val FASTEST_INTERVAL: Long = (40 * MILLISECONDS_PER_SECOND).toLong()
        val UPDATE_INTERVAL_IN_MILLISECONDS: Long =
            (10 * MILLISECONDS_PER_SECOND).toLong()
        val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
        const val REGISTER_C2DM: String = "REGISTER_C2DM"
        const val PROTECT_ACCOUNT: String = "PROTECT_ACCOUNT"
        const val PROTECT_PRIVILEGES: String = "PROTECT_PRIVILEGES"
        const val PROTECT_TOUR: String = "PROTECT_TOUR"
        const val PROTECT_READY: String = "PROTECT_READY"
        const val PREFS_SIM_SERIAL_NUMBER: String = "PREFS_SIM_SERIAL_NUMBER"
        const val PREFS_SECURITY_PROMPT_SHOWN: String = "PREFS_SECURITY_PROMPT_SHOWN"
        const val PREFS_IS_MISSING: String = "PREFS_IS_MISSING"
        const val PREFS_DISABLE_POWER_OPTIONS: String = "PREFS_DISABLE_POWER_OPTIONS"
        const val PREFS_BLOCK_APP_UNINSTALL: String = "PREFS_BLOCK_APP_UNINSTALL"
        const val PREFS_RUN_BACKGROUND: String = "PREFS_RUN_BACKGROUND"
        const val PREFS_USE_BIOMETRIC: String = "PREFS_USE_BIOMETRIC"
        const val PREFS_BACKGROUND: String = "PREFS_BACKGROUND"
        const val IS_LOCK_SET: String = "IS_LOCK_SET"
        const val NEXT_ALERT: String = "NEXT_ALERT"
        const val IS_CAMOUFLAGE_SET: String = "IS_CAMOUFLAGE_SET"
        const val PREFS_RINGTONE: String = "PREFS_RINGTONE"
        const val LAST_EVENT: String = "LAST_EVENT"
        const val LOW_BATTERY_DATE: String = "LOW_BATTERY_DATE"
        const val PREVIOUS_SSID: String = "PREVIOUS_SSID"
        const val ERROR: String = "ERROR"
        const val FLAG_FEEDBACK: String = "FLAG_FEEDBACK"
        const val INSTALLATION_DATE: String = "INSTALLATION_DATE"
        const val PREFS_ACCOUNT_VERIFIED: String = "PREFS_ACCOUNT_VERIFIED"
        const val EMAIL: String = "EMAIL"
        const val TWO_STEP: String = "TWO_STEP"
        const val PRO_ACCOUNT: String = "PRO_ACCOUNT"
        const val SEND_DATA: String = "SEND_DATA"
        const val SCHEDULED: String = "SCHEDULED"
        const val MINUTE_SCHEDULED: String = "MINUTE_SCHEDULED2"
        const val IS_REVOKED_PASSWORD: String = "IS_REVOKED_PASSWORD"
        const val REVOKED_PASSWORD: String = "REVOKED_PASSWORD"
        const val NOTIFICATION_ID: String = "NOTIFICATION_ID"
        const val INTERVAL_REPORT: String = "INTERVAL_REPORT"
        const val EXCLUDE_REPORT: String = "EXCLUDE_REPORT"
        const val LAST_REPORT_START_DATE: String = "LAST_REPORT_START_DATE"
        const val TIMEOUT_REPORT: String = "TIMEOUT_REPORT"
        const val INTERVAL_AWARE: String = "INTERVAL_AWARE"
        const val TIME_SECURE_LOCK: String = "TIME_SECURE_LOCK"
        const val LAST_TIME_SECURE_LOCK: String = "LAST_TIME_SECURE_LOCK"
        const val LOCATION_LOW_BATTERY_DATE: String = "LOCATION_LOW_BATTERY_DATE"
        const val SESSION_ID: String = "SESSION_ID"
        const val PIN_NUMBER2: String = "PIN_NUMBER2"
        const val PREFERENCE_LOCATION_LOW_BATTERY: String = "PREFERENCE_LOCATION_LOW_BATTERY"
        const val TOKEN_JWT: String = "TOKEN_JWT"
        const val PREY_VERSION: String = "PREY_VERSION"
        const val API_KEY: String = "API_KEY"
        const val DEVICE_ID: String = "DEVICE_ID"
        const val ACCOUNT: String = "ACCOUNT"
        const val DEVICE_NAME: String = "DEVICE_NAME"
        const val SIM_SERIAL_NUMBER: String = "SIM_SERIAL_NUMBER"
        const val CAN_ACCESS_FINE_LOCATION: String = "CAN_ACCESS_FINE_LOCATION"
        const val CAN_ACCESS_COARSE_LOCATION: String = "CAN_ACCESS_COARSE_LOCATION"
        const val CAN_ACCESS_CAMARA: String = "CAN_ACCESS_CAMARA"
        const val CAN_ACCESS_READ_PHONE_STATE: String = "CAN_ACCESS_READ_PHONE_STATE"
        const val CAN_ACCESS_EXTERNAL_STORAGE: String = "CAN_ACCESS_EXTERNAL_STORAGE"
        const val TIME_PASSWORD_OK: String = "TIME_PASSWORD_OK"
        const val TIME_TWO_STEP: String = "TIME_TWO_STEP"
        const val TIME_C2DM: String = "TIME_C2DM"
        const val TIME_LOCATION_AWARE: String = "TIME_LOCATION_AWARE"
        const val BUILD_VERSION_CODES_10: Int = 29
        const val BUILD_VERSION_CODES_11: Int = 30
        const val NOTIFY_ANDROID_6: Int = 6
        const val NOTIFICATION_POPUP_ID: String = "NOTIFICATION_POPUP_ID"
        const val SENT_UUID_SERIAL_NUMBER: String = "SENT_UUID_SERIAL_NUMBER"
        const val LAST_EVENT_GEO: String = "LAST_EVENT_GEO"
        const val MESSAGE_ID: String = "messageID"
        const val JOB_ID: String = "device_job_id"
        const val UNLOCK_PASS: String = "unlock_pass"
        const val LOCK_MESSAGE: String = "lock_message"
        const val NOTIFICATION_ANDROID_7: String = "notify_android_7"
        const val JOB_ID_LOCK: String = "job_id_lock"
        const val COUNTER_OFF: String = "counter_off"
        const val SSID: String = "SSID"
        const val IMEI: String = "IMEI"
        const val MODEL: String = "MODEL"
        const val PUBLIC_IP: String = "PUBLIC_IP"
        const val LOCATION_LAT: String = "LOCATION_LAT"
        const val LOCATION_LNG: String = "LOCATION_LNG"
        const val LOCATION_ACCURACY: String = "LOCATION_ACCURACY"
        const val AWARE_LAT: String = "AWARE_LAT"
        const val AWARE_LNG: String = "AWARE_LNG"
        const val AWARE_ACC: String = "AWARE_ACC"
        const val AWARE_DATE: String = "AWARE_DATE"
        const val AUTO_CONNECT: String = "auto_connect"
        const val AWARE: String = "aware"
        const val TIME_BLOCK_APP_UNINSTALL: String = "TIME_BLOCK_APP_UNINSTALL"
        const val REPORT_NUMBER: String = "REPORT_NUMBER"
        const val PREFS_BIOMETRIC: String = "PREFS_BIOMETRIC"
        const val INSTALLATION_STATUS: String = "INSTALLATION_STATUS"
        const val LOCATION_INFO: String = "LOCATION_INFO"
        const val CAPS_LOCK_ON: String = "CAPS_LOCK_ON"
        const val VERIFICATE_BIOMETRIC: String = "VERIFICATE_BIOMETRIC"
        const val TYPE_BIOMETRIC: String = "TYPE_BIOMETRIC"
        const val OVER_LOCK: String = "OVER_LOCK"
        const val PIN_NUMBER_ACTIVATE: String = "PIN_NUMBER_ACTIVATE"
        const val INPUT_WEBVIEW: String = "INPUT_WEBVIEW"
        const val PERMISSION_LOCATION: String = "PERMISSION_LOCATION"
        const val HELP_FILE: String = "HELP_FILE"
        const val CONTACT_FORM_FOR_FREE: String = "CONTACT_FORM_FOR_FREE"
        const val VIEW_SECURE: String = "VIEW_SECURE"
        const val HELP_DIRECTORY: String = "preyHelp"
        const val TIME_NEXT_ACCESSIBILITY: String = "TIME_NEXT_ACCESSIBILITY"
        const val ACCESSIBILITY_DENIED: String = "ACCESSIBILITY_DENIED"
        const val TIME_NEXT_ALLFILES: String = "TIME_NEXT_ALLFILES"
        const val ALLFILES_DENIED: String = "ALLFILES_DENIED"
        const val TIME_NEXT_LOCATIONBG: String = "TIME_NEXT_LOCATIONBG"
        const val LOCATIONBG_DENIED: String = "LOCATIONBG_DENIED"
        const val MSP_ACCOUNT: String = "MSP_ACCOUNT"
        const val START: String = "START"
        const val VOLUME: String = "VOLUME"
        const val DENY_NOTIFICATION: String = "DENY_NOTIFICATION"
        const val OPEN_SECURE_SERVICE: String = "OPEN_SECURE_SERVICE"

        var postUrl: String? = null

        var FORMAT_SDF_AWARE: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

        const val DAILY_LOCATION: String = "DAILY_LOCATION"

        const val MINUTES_TO_QUERY_SERVER: String = "MINUTES_TO_QUERY_SERVER"

        /**
         * Key for storing the aware time in the configuration.
         */
        const val AWARE_TIME: String = "AWARE_TIME"

        /**
         * Key for storing the organization ID in the configuration.
         */
        const val ORGANIZATION_ID: String = "ORGANIZATION_ID"
    }
}