package com.prey.net

import android.content.Context

import com.prey.PreyAccountData
import com.prey.PreyName
import com.prey.PreyVerify
import com.prey.PreyWifi
import com.prey.actions.HttpDataService
import com.prey.actions.fileretrieval.FileretrievalDto
import com.prey.actions.location.PreyLocation
import com.prey.events.Event
import com.prey.exceptions.PreyException

import org.json.JSONObject
import java.io.File

interface WebServicesInterface {

    /**
     * Register a new device for a given API_KEY, needed just after obtain the
     * new API_KEY.
     *
     * @throws PreyException
     */
    @Throws(java.lang.Exception::class)
    fun registerNewDevice(
        context: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyHttpResponse?

    fun increaseData(
        context: Context,
        parameters: HashMap<String, String?>
    ): HashMap<String, String?>

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceToAccount(
        context: Context,
        email: String,
        password: String,
        deviceType: String
    ): PreyAccountData?

    fun getEmail(context: Context): String?

    @Throws(PreyException::class)
    fun checkPassword(apikey: String, password: String, context: Context): PreyHttpResponse

    fun getToken(context: Context, apikey: String, password: String): String

    @Throws(java.lang.Exception::class)
    fun checkPassword(context: Context, apikey: String, password: String): Boolean

    @Throws(Exception::class)
    fun checkPassword2(
        context: Context,
        apikey: String,
        password: String,
        password2: String
    ): Boolean

    fun getTwoStepEnabled(context: Context): Boolean

    fun sendNotifyActionResultPreyHttp(
        context: Context,
        params: MutableMap<String, String?>
    ): String?

    fun sendNotifyActionResultPreyHttp(
        context: Context,
        correlationId: String?,
        params: MutableMap<String, String?>
    )

    fun sendNotifyActionResultPreyHttp(
        context: Context,
        status: String?,
        correlationId: String?,
        params: MutableMap<String, String?>
    )

    /**
     * Method to send the help
     *
     * @param context
     * @param subject
     * @param message
     * @return help result
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun sendHelp(context: Context, subject: String, message: String): PreyHttpResponse?

    @Throws(PreyException::class)
    fun getActionsJsonToPerform(context: Context): List<JSONObject>?

    fun sendPreyHttpData(
        context: Context,
        dataToSend: ArrayList<HttpDataService>?
    ): PreyHttpResponse?

    /**
     * Method returns the location using WiFi networks
     *
     * @param context
     * @param listWifi
     * @return PreyLocation
     */
    fun getLocationWithWifi(context: Context, listWifi: List<PreyWifi>?): PreyLocation?

    fun setPushRegistrationId(context: Context, regId: String): PreyHttpResponse?

    fun sendPreyHttpDataName(context: Context, nameDevice: String): PreyHttpResponse?

    fun getNameDevice(context: Context): String?

    @Throws(PreyException::class)
    fun uploadFile(context: Context, file: File, uploadID: String, total: Long): Int

    @Throws(PreyException::class)
    fun sendTree(context: Context, json: JSONObject?): PreyHttpResponse?

    @Throws(PreyException::class)
    fun getDeviceWebControlPanelUiUrl(context: Context): String

    @Throws(PreyException::class)
    fun deleteDevice(context: Context): String?

    fun sendLocation(context: Context, jsonParam: JSONObject?): PreyHttpResponse?

    fun sendPreyHttpEvent(context: Context, event: Event, jsonObject: JSONObject): PreyHttpResponse?

    @Throws(PreyException::class)
    fun triggers(context: Context): String?

    fun sendPreyHttpReport(context: Context, dataToSend: List<HttpDataService>?): PreyHttpResponse?

    @Throws(java.lang.Exception::class)
    fun uploadStatus(context: Context, uploadID: String): FileretrievalDto?

    @Throws(PreyException::class)
    fun geofencing(context: Context): String?

    fun getProfile(context: Context)

    @Throws(PreyException::class)
    fun getStatus(context: Context): JSONObject?

    @Throws(java.lang.Exception::class)
    fun getIPAddress(context: Context): String?

    @Throws(PreyException::class)
    fun getFileUrlJson(context: Context?): String

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceWithApiKeyEmail(
        context: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyAccountData?

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceWithApiKeyEmail(
        context: Context,
        apiKey: String,
        email: String?,
        deviceType: String,
        name: String
    ): PreyAccountData?

    fun validateName(context: Context, name: String): PreyName

    @Throws(java.lang.Exception::class)
    fun registerNewAccount(
        context: Context,
        name: String?,
        email: String?,
        password: String?,
        rule_age: String?,
        privacy_terms: String?,
        offer: String?,
        deviceType: String?
    ): PreyAccountData

    @Throws(java.lang.Exception::class)
    fun registerNewAccount(
        context: Context,
        name: String?,
        email: String?,
        password1: String?,
        password2: String?,
        rule_age: String?,
        privacy_terms: String?,
        offers: String?,
        deviceType: String?
    ): PreyAccountData

    fun renameName(context: Context, name: String): PreyName

    @Throws(java.lang.Exception::class)
    fun validToken(context: Context, token: String): Boolean

    @Throws(java.lang.Exception::class)
    fun verifyEmail(context: Context, email: String): PreyVerify?

}