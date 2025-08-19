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
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

class TestWebServices : WebServicesInterface {
    private var accountData: PreyAccountData? = null

    fun setAccountData(accountData: PreyAccountData) {
        this.accountData = accountData
    }

    private var nameDevice: String? = null

    fun setNameDevice(nameDevice: String) {
        this.nameDevice = nameDevice
    }

    private var status: JSONObject? = null

    fun setStatus(status: JSONObject) {
        this.status = status
    }

    private var errorException: Exception? = null

    fun setErrorException(errorException: Exception) {
        this.errorException = errorException
    }

    override fun registerNewDevice(
        context: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyHttpResponse? {
        TODO("Not yet implemented")
    }

    override fun increaseData(
        context: Context,
        parameters: HashMap<String, String?>
    ): HashMap<String, String?> {
        TODO("Not yet implemented")
    }

    override fun registerNewDeviceToAccount(
        context: Context,
        email: String,
        password: String,
        deviceType: String
    ): PreyAccountData? {
        if (errorException != null)
            throw errorException!!
        return accountData!!
    }

    override fun getEmail(context: Context): String? {
        return ""
    }

    override fun checkPassword(
        apikey: String,
        password: String,
        context: Context
    ): PreyHttpResponse {
        TODO("Not yet implemented")
    }

    override fun checkPassword(context: Context, apikey: String, password: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getToken(context: Context, apikey: String, password: String): String {
        TODO("Not yet implemented")
    }

    override fun checkPassword2(
        context: Context,
        apikey: String,
        password: String,
        password2: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getTwoStepEnabled(context: Context): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendNotifyActionResultPreyHttp(
        context: Context,
        params: MutableMap<String, String?>
    ): String? {
        return ""
    }

    private var notificationParameters: MutableMap<String, String?>? = null

    fun getNotificationParameters(): MutableMap<String, String?>? {
        return notificationParameters
    }

    override fun sendNotifyActionResultPreyHttp(
        context: Context,
        correlationId: String?,
        params: MutableMap<String, String?>
    ) {
        correlationId?.let { params["correlationId"] = it }
        notificationParameters = params
    }

    override fun sendNotifyActionResultPreyHttp(
        context: Context,
        status: String?,
        correlationId: String?,
        params: MutableMap<String, String?>
    ) {
        correlationId?.let { params["correlationId"] = it }
        notificationParameters = params
    }

    override fun sendHelp(context: Context, subject: String, message: String): PreyHttpResponse? {
        TODO("Not yet implemented")
    }

    override fun getActionsJsonToPerform(context: Context): List<JSONObject>? {
        return null
    }

    override fun sendPreyHttpData(
        context: Context,
        dataToSend: ArrayList<HttpDataService>?
    ): PreyHttpResponse? {
        TODO("Not yet implemented")
    }

    override fun getLocationWithWifi(context: Context, listWifi: List<PreyWifi>?): PreyLocation? {
        TODO("Not yet implemented")
    }

    override fun setPushRegistrationId(context: Context, regId: String): PreyHttpResponse? {
        return PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
    }

    override fun sendPreyHttpDataName(context: Context, nameDevice: String): PreyHttpResponse? {
        return PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
    }

    override fun getNameDevice(context: Context): String? {
        return nameDevice
    }

    override fun uploadFile(context: Context, file: File, uploadID: String, total: Long): Int {
        return HttpURLConnection.HTTP_OK
    }

    override fun sendTree(context: Context, json: JSONObject?): PreyHttpResponse? {
        return PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
    }

    override fun getDeviceWebControlPanelUiUrl(context: Context): String {
        TODO("Not yet implemented")
    }

    override fun deleteDevice(context: Context): String? {
        TODO("Not yet implemented")
    }

    override fun sendLocation(context: Context, jsonParam: JSONObject?): PreyHttpResponse? {
        return PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
    }

    override fun sendPreyHttpEvent(
        context: Context,
        event: Event,
        jsonObject: JSONObject
    ): PreyHttpResponse? {
        return PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
    }

    override fun triggers(context: Context): String? {
        TODO("Not yet implemented")
    }

    override fun sendPreyHttpReport(
        context: Context,
        dataToSend: List<HttpDataService>?
    ): PreyHttpResponse? {
        return PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK")
    }

    override fun uploadStatus(context: Context, uploadID: String): FileretrievalDto? {
        TODO("Not yet implemented")
    }

    override fun geofencing(context: Context): String? {
        TODO("Not yet implemented")
    }

    override fun getProfile(context: Context) {

    }

    override fun getStatus(context: Context): JSONObject? {
        return status
    }

    override fun getIPAddress(context: Context): String? {
        return "190.101.28.85"
    }

    override fun getFileUrlJson(context: Context?): String {
        TODO("Not yet implemented")
    }

    override fun registerNewDeviceWithApiKeyEmail(
        context: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyAccountData? {
        return registerNewDeviceWithApiKeyEmail(context, apiKey, "", deviceType, name)
    }

    override fun registerNewDeviceWithApiKeyEmail(
        context: Context,
        apiKey: String,
        email: String?,
        deviceType: String,
        name: String
    ): PreyAccountData? {
        val newAccount = PreyAccountData()
        newAccount.setApiKey(apiKey)
        newAccount.setDeviceId("")
        newAccount.setEmail(email)
        newAccount.setPassword("")
        return newAccount
    }

    override fun validateName(context: Context, name: String): PreyName {
        TODO("Not yet implemented")
    }

    public
    override fun registerNewAccount(
        context: Context,
        name: String?,
        email: String?,
        password: String?,
        rule_age: String?,
        privacy_terms: String?,
        offer: String?,
        deviceType: String?
    ): PreyAccountData {
        if (errorException != null)
            throw errorException!!
        return accountData!!
    }

    override fun registerNewAccount(
        context: Context,
        name: String?,
        email: String?,
        password1: String?,
        password2: String?,
        rule_age: String?,
        privacy_terms: String?,
        offers: String?,
        deviceType: String?
    ): PreyAccountData {
        if (errorException != null)
            throw errorException!!
        return accountData!!
    }

    override fun renameName(context: Context, name: String): PreyName {
        TODO("Not yet implemented")
    }

    override fun validToken(context: Context, token: String): Boolean {
        return true
    }

    override fun verifyEmail(context: Context, email: String): PreyVerify? {
        TODO("Not yet implemented")
    }

}