/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.kotlin

import android.content.Context
import android.os.Build
import android.os.Environment
import com.prey.actions.fileretrieval.kotlin.FileretrievalDto
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.location.kotlin.PreyLocation
import com.prey.actions.location.kotlin.PreyLocationManager
import com.prey.actions.observer.kotlin.ActionsController
import com.prey.events.kotlin.Event
import com.prey.exceptions.kotlin.PreyException
import com.prey.json.parser.kotlin.JSONParser
import com.prey.kotlin.FileConfigReader
import com.prey.kotlin.PreyAccountData
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPhone
import com.prey.kotlin.PreyUtils
import com.prey.net.http.kotlin.EntityFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreyWebServices {


    fun increaseData(
        ctx: Context,
        parameters: HashMap<String, String>
    ): HashMap<String, String> {
        val phone = PreyPhone(ctx)
        val hardware: PreyPhone.Hardware = phone.hardware!!
        var prefix = "hardware_attributes"
        parameters["$prefix[uuid]"] = hardware.uuid!!
        parameters["$prefix[bios_vendor]"] = hardware.biosVendor!!
        parameters["$prefix[bios_version]"] = hardware.biosVersion!!
        parameters["$prefix[mb_vendor]"] = hardware.mbVendor!!
        parameters["$prefix[mb_serial]"] = hardware.mbSerial!!
        parameters["$prefix[mb_model]"] = hardware.mbModel!!
        parameters["$prefix[cpu_model]"] = hardware.cpuModel!!
        parameters["$prefix[cpu_speed]"] = hardware.cpuSpeed!!
        parameters["$prefix[cpu_cores]"] = hardware.cpuCores!!
        parameters["$prefix[ram_size]"] = "" + hardware.totalMemory
        parameters["$prefix[serial_number]"] = hardware.serialNumber!!
        parameters["$prefix[google_services]"] =
            java.lang.String.valueOf(PreyUtils.isGooglePlayServicesAvailable(ctx))
        val nic = 0
        val wifi: PreyPhone.Wifi = phone.wifi!!
        if (wifi != null) {
            prefix = "hardware_attributes[network]"
            parameters["$prefix[nic_$nic][name]"] = wifi.name!!
            parameters["$prefix[nic_$nic][interface_type]"] = wifi.interfaceType!!
            parameters["$prefix[nic_$nic][ip_address]"] = wifi.ipAddress!!
            parameters["$prefix[nic_$nic][gateway_ip]"] = wifi.gatewayIp!!
            parameters["$prefix[nic_$nic][netmask]"] = wifi.netmask!!
            parameters["$prefix[nic_$nic][mac_address]"] = wifi.macAddress!!
        }
        return parameters
    }

    /**
     * Register a new device for a given API_KEY, needed just after obtain the
     * new API_KEY.
     *
     * @throws PreyException
     */
    @Throws(java.lang.Exception::class)
    private fun registerNewDevice(
        ctx: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyHttpResponse {
        var name: String? = name
        if (name == null || "" == name) {
            name = PreyUtils.getNameDevice(ctx)
        }

        val model = Build.MODEL
        var vendor: String = "Google"
        try {
            vendor = Build.MANUFACTURER
        } catch (e: java.lang.Exception) {
        }
        var parameters = HashMap<String, String?>()
        parameters["api_key"] = apiKey
        parameters["title"] = name
        parameters["device_type"] = deviceType
        parameters["os"] = "Android"
        parameters["os_version"] = Build.VERSION.RELEASE
        parameters["referer_device_id"] = ""
        parameters["plan"] = "free"
        parameters["model_name"] = model
        parameters["vendor_name"] = vendor

        //TODO: falta
        //   parameters = increaseData(ctx, parameters)

        val imei: String = "11"//PreyPhone(ctx).getAndroidDeviceId()
        parameters["physical_address"] = imei
        val lang = Locale.getDefault().language
        parameters["lang"] = lang

        var response: PreyHttpResponse? = null
        val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
        val url: String =
            PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("devices.json")
        PreyLogger.d("url:$url")
        response = PreyRestHttpClient.getInstance(ctx).post(url, parameters)
        if (response == null) {
            throw PreyException(
                ctx.getString(
                    com.prey.R.string.error_cant_add_this_device,
                    "[" + -1 + "]"
                )
            )
        } else {
            PreyLogger.d("response:" + response.getStatusCode() + " " + response.getResponseAsString())
            val json = response.getResponseAsString()
            PreyLogger.d("json:$json")
            if (response.getStatusCode() > 299) {
                if ("es" == lang) throw PreyException("{\"error\":[\"No queda espacio disponible para agregar este dispositivo!\"]}")
                else throw PreyException("{\"error\":[\"No slots left for new devices\"]}")
            }
        }
        return response
    }

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceToAccount(
        ctx: Context,
        email: String,
        password: String,
        deviceType: String
    ): PreyAccountData? {
        PreyLogger.d("registerNewDeviceToAccount email:$email password:$password")

        val parameters = HashMap<String, String?>()
        var response: PreyHttpResponse? = null
        var json: String? = null
        try {
            val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
            val lang = Locale.getDefault().language
            val url: String = PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2)
                .plus("profile.json?lang=").plus(lang)
            PreyLogger.d("_____url:$url")
            response = PreyRestHttpClient.getInstance(ctx).get(url, parameters, email, password)
            PreyLogger.d("response:$response")
            if (response != null) {
                json = response.getResponseAsString()
                PreyLogger.d("json:$json")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error!" + e.message, e)
            throw PreyException(
                "{\"error\":[\"" + ctx.getText(com.prey.R.string.error_communication_exception)
                    .toString() + "\"]}"
            )
        }
        var status = ""
        if (response != null) {
            status = "[" + response.getStatusCode() + "]"
        }
        if (json == null || !json.contains("key")) {
            PreyLogger.d("no key")
            throw PreyException(json)
        }

        var from: Int
        var to: Int
        var apiKey: String? = null
        try {
            val jsonObject = JSONObject(json)
            apiKey = jsonObject.getString("key")
            PreyLogger.d("apikey:$apiKey")
        } catch (e: java.lang.Exception) {
            throw PreyException(ctx.getString(com.prey.R.string.error_cant_add_this_device, status))
        }
        var deviceId: String? = null
        val responseDevice: PreyHttpResponse =
            registerNewDevice(ctx, apiKey, deviceType, PreyUtils.getNameDevice(ctx))
        if (responseDevice != null) {
            val xmlDeviceId = responseDevice.getResponseAsString()
            //if json
            if (xmlDeviceId!!.contains("key")) {
                try {
                    val jsnobject = JSONObject(xmlDeviceId)
                    deviceId = jsnobject.getString("key")
                } catch (e: java.lang.Exception) {
                }
            }
        } else {
            throw PreyException(ctx.getString(com.prey.R.string.error_cant_add_this_device, status))
        }
        var newAccount: PreyAccountData = PreyAccountData()
        newAccount.setApiKey(apiKey)
        newAccount.setDeviceId(deviceId!!)
        newAccount.setEmail(email)
        newAccount.setPassword(password)
        return newAccount
    }

    @Throws(PreyException::class)
    private fun checkPassword(apikey: String, password: String, ctx: Context): PreyHttpResponse {

        val parameters = HashMap<String, String?>()
        var response: PreyHttpResponse? = null
        var json: String? = ""
        try {
            val uri: String = PreyConfig.getInstance(ctx).getPreyUrl()
                .plus("api/v2/profile.json?lang=" + Locale.getDefault().language)
            response = PreyRestHttpClient.getInstance(ctx).get(uri, parameters, apikey, password)
            json = response!!.getResponseAsString()
        } catch (e: java.lang.Exception) {
            response = null
            val err = "" + ctx.getText(com.prey.R.string.error_communication_exception)
            json = "{\"error\":[\"$err\"]}"
        }
        if (response == null) {
            throw PreyException(json)
        }
        if (response != null && response.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw PreyException(json)
        }
        //StatusCode 500-504
        if (response != null && (response.getStatusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR &&
                    response.getStatusCode() <= HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
        ) {
            val err = ctx.getText(com.prey.R.string.error_communication_500)
            json = StringBuffer("{\"error\":[\"").append(err).append("\"]}").toString()
            throw PreyException(json)
        }

        PreyLogger.d("____[token]_________________apikey:$apikey password:$password")
        getToken(ctx, apikey, password)

        return response
    }

    fun getToken(ctx: Context, apikey: String, password: String): String {
        var tokenJwt = ""
        try {
            val parameters = HashMap<String, String?>()
            val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
            val uri2: String =
                PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("get_token.json")
            val response2 = PreyRestHttpClient.getInstance(
                ctx
            ).get(uri2, parameters, apikey, password, "application/json")
            if (response2 != null) {
                PreyLogger.d("get_token:" + response2.getResponseAsString())
                val jsnobject = JSONObject(response2.getResponseAsString())
                tokenJwt = jsnobject.getString("token")
                PreyLogger.d("tokenJwt:$tokenJwt")
                PreyConfig.getInstance(ctx).setTokenJwt(tokenJwt)
            } else {
                PreyLogger.d("token: nulo")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error:" + e.message, e)
        }
        return tokenJwt
    }

    @Throws(java.lang.Exception::class)
    fun checkPassword(ctx: Context, apikey: String, password: String): Boolean {
        val response: PreyHttpResponse? = checkPassword(apikey, password, ctx)
        if (response != null) {
            val xml = response.getResponseAsString()
            if (xml != null) {
                return xml.contains("key")
            }
        }
        return false
    }

    @Throws(Exception::class)
    fun checkPassword2(
        ctx: Context,
        apikey: String,
        password: String,
        password2: String
    ): Boolean {
        PreyLogger.d(String.format("checkPassword2 password:%s password2:%s", password, password2))
        val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
        val url: String =
            PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("authenticate")
        val parameters = HashMap<String, String?>()
        parameters["email"] = PreyConfig.getInstance(ctx).getEmail()!!
        parameters["password"] = password
        parameters["otp_code"] = password2
        parameters["lang"] = Locale.getDefault().language
        var response: PreyHttpResponse? = null
        try {
            response = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters)
        } catch (e: Exception) {
            PreyLogger.e(String.format("error:%s", e.message), e)
        }
        if (response != null) {
            PreyLogger.d(String.format("authenticate:%s", response.getResponseAsString()))
            if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                var tokenJwt = ""
                try {
                    val jsnobject = JSONObject(response.getResponseAsString())
                    tokenJwt = jsnobject.getString("token")
                    PreyConfig.getInstance(ctx).setTokenJwt(tokenJwt)
                } catch (e: Exception) {
                    PreyLogger.e("error:" + e.message, e)
                }
                return true
            } else {
                var json: String? = ""
                try {
                    val jsnobject = JSONObject(response.getResponseAsString())
                    json = response.getResponseAsString()
                } catch (e: Exception) {
                    PreyLogger.e("error:" + e.message, e)
                }
                try {
                    val jsnobject = JSONObject(response.getResponseAsString())
                    val array = jsnobject.getJSONArray("error")
                    var json2: String? = ""
                    var i = 0
                    while (array != null && i < array.length()) {
                        json2 += array[i]
                        if ((i + 1) < array.length()) {
                            json2 += " ,"
                        }
                        i++
                    }
                    json = json2
                } catch (e: Exception) {
                    PreyLogger.e(String.format("error:%s", e.message), e)
                }
                throw PreyException(json)
            }
        } else {
            throw PreyException(ctx.getText(com.prey.R.string.password_wrong).toString())
        }
    }


    fun getTwoStepEnabled(ctx: Context): Boolean {
        var TwoStepEnabled = false
        try {
            val parameters = HashMap<String, String?>()

            val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
            val url: String = PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2)
                .plus("profile?api_key=" + PreyConfig.getInstance(ctx).getApiKey())
            PreyLogger.d("url:$url")
            val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(
                ctx
            ).getAutentication(url, parameters)
            if (response != null) {
                val out = response.getResponseAsString()
                PreyLogger.d("out:$out")
                val jsnobject = JSONObject(out)
                TwoStepEnabled = jsnobject.getBoolean("two_step_enabled?")
                PreyLogger.d("TwoStepEnabled:$TwoStepEnabled")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error get TwoStepEnabled", e)
        }
        return TwoStepEnabled
    }

    @Throws(PreyException::class)
    private fun getDeviceUrlApiv2(ctx: Context): String {

        val deviceKey = PreyConfig.getInstance(ctx).getDeviceId()
        if (deviceKey == null || deviceKey === "") throw PreyException("Device key not found on the configuration")
        val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
        val url: String =
            PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("devices/")
                .plus(deviceKey)
        return url
    }

    @Throws(PreyException::class)
    private fun getResponseUrlJson(ctx: Context): String {
        return getDeviceUrlApiv2(ctx).plus("/response")
    }

    fun sendNotifyActionResultPreyHttp(ctx: Context, params: MutableMap<String, String?>): String? {
        var response: String? = null
        try {

            val url: String = getResponseUrlJson(ctx)
            PreyConfig.postUrl = null
            val httpResponse = PreyRestHttpClient.getInstance(
                ctx
            ).postAutentication(url, params)
            response = httpResponse.toString()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Notify Action Result wasn't send:" + e.message, e)
        }
        return response
    }

    fun sendNotifyActionResultPreyHttp(
        ctx: Context,
        correlationId: String?,
        params: MutableMap<String, String?>
    ) {
        sendNotifyActionResultPreyHttp(ctx, null, correlationId, params)
    }

    fun sendNotifyActionResultPreyHttp(
        ctx: Context,
        status: String?,
        correlationId: String?,
        params: MutableMap<String, String?>
    ) {
        object : Thread() {
            override fun run() {
                val preyConfig: PreyConfig = PreyConfig.getInstance(ctx)
                var response: String? = null
                try {
                    val url = getResponseUrlJson(ctx!!)
                    PreyConfig.postUrl = null
                    val httpResponse: PreyHttpResponse? = PreyRestHttpClient.getInstance(ctx)
                        .postAutenticationCorrelationId(ctx, url, status, correlationId, params)
                    response = httpResponse.toString()
                } catch (e: java.lang.Exception) {
                    PreyLogger.e("error:" + e.message, e)
                }
            }
        }.start()
    }


    /**
     * Method to send the help
     *
     * @param ctx
     * @param subject
     * @param message
     * @return help result
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun sendHelp(ctx: Context, subject: String, message: String): PreyHttpResponse? {
        val params: MutableMap<String, String?> = HashMap()
        params["support_category"] = "support"
        params["message"] = message
        params["support_topic"] = subject
        var entityFiles: List<EntityFile>? = null
        try {

            val dir: File = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "preyHelp"
            )
            val displayName = PreyConfig.getInstance(ctx).getHelpFile()
            PreyLogger.d(String.format("displayName:%s", displayName))
            if (displayName != null && "" != displayName) {
                entityFiles = ArrayList<EntityFile>()
                val initialFile: File = File(dir, displayName)
                val inputStream: InputStream =
                    DataInputStream(FileInputStream(initialFile))
                val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmZ")
                val entityFile: EntityFile = EntityFile()
                entityFile.file = inputStream
                entityFile.mimeType = "image/jpeg"
                entityFile.name = "file"
                entityFile.filename = displayName
                entityFile.type = "image/jpeg"
                entityFile.idFile = sdf.format(Date()) + "_" + entityFile.type
                entityFiles.add(entityFile)
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.d(String.format("Error contact:%s", e.message))
        }
        val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
        val uri: String = PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("contact")
        val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(
            ctx
        ).sendHelp(ctx, uri, params, entityFiles!!)
        val dir: File = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            PreyConfig.HELP_DIRECTORY
        )
        val files: Array<File> = dir.listFiles()
        var i = 0
        while (files != null && i < files.size) {
            val file: File = files[i]
            file.delete()
            i++
        }
        return response
    }

    @Throws(PreyException::class)
    private fun getDeviceUrlJson(ctx: Context): String {
        return getDeviceUrlApiv2(ctx) + ".json"
    }

    @Throws(PreyException::class)
    fun getActionsJsonToPerform(ctx: Context): List<JSONObject>? {
        val url: String = getDeviceUrlJson(ctx)
        return JSONParser().getJSONFromUrl(ctx, url)
    }

    @Throws(com.prey.exceptions.PreyException::class)
    fun getDataUrlJson(ctx: Context?): String {
        return getDeviceUrlApiv2(ctx!!) + "/data.json"
    }

    fun sendPreyHttpData(
        ctx: Context,
        dataToSend: ArrayList<HttpDataService>?
    ): PreyHttpResponse? {
        val preyConfig = PreyConfig.getInstance(ctx)
        var parameters: MutableMap<String, String?> = java.util.HashMap()
        var entityFiles: MutableList<EntityFile> = java.util.ArrayList()

        for (httpDataService in dataToSend!!) {
            if (httpDataService != null) {
                parameters.plus(httpDataService.getDataAsParameters())
                if (httpDataService.getEntityFiles() != null && httpDataService.getEntityFiles().size > 0) {
                    entityFiles.plus(httpDataService.getEntityFiles())
                }
            }
        }
        var preyHttpResponse: PreyHttpResponse? = null
        if (parameters.size > 0 || entityFiles.size > 0) {
            val hardware = com.prey.PreyPhone(ctx).hardware
            if (!com.prey.PreyConfig.getPreyConfig(ctx).isSendData && hardware.totalMemory > 0) {
                com.prey.PreyConfig.getPreyConfig(ctx).isSendData = true

                parameters["hardware_attributes[ram_size]"] = "" + hardware.totalMemory
            }
            if ("" != hardware.uuid && !com.prey.PreyConfig.getPreyConfig(ctx).isSentUuidSerialNumber) {
                parameters["hardware_attributes[uuid]"] = hardware.uuid
                parameters["hardware_attributes[serial_number]"] = hardware.serialNumber
                com.prey.PreyConfig.getPreyConfig(ctx).isSentUuidSerialNumber = true
            }
            try {
                val url: String = getDataUrlJson(ctx)
                com.prey.PreyLogger.d("URL:$url")
                com.prey.PreyConfig.postUrl = null
                if (UtilConnection.getInstance().isInternetAvailable()) {
                    if (entityFiles.size == 0) {
                        preyHttpResponse =
                            PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters);
                    } else {
                        preyHttpResponse = PreyRestHttpClient.getInstance(ctx)
                            .postAutentication(url, parameters, entityFiles);
                    }
                    PreyLogger.d("Data sent_: " + (if (preyHttpResponse == null) "" else preyHttpResponse.getResponseAsString()))
                }
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Data wasn't send", e)
            }
        }
        return preyHttpResponse
    }

    /**
     * Method returns the location using WiFi networks
     *
     * @param ctx
     * @param listWifi
     * @return PreyLocation
     */
    fun getLocationWithWifi(ctx: Context, listWifi: List<PreyPhone.Wifi>?): PreyLocation? {
        var location: PreyLocation? = null
        try {
            val jsonParam = JSONObject()
            val array = JSONArray()
            var i = 0
            while (listWifi != null && i < listWifi.size && i < 15) {
                val wifi: PreyPhone.Wifi = listWifi[i]
                val jsonRed = JSONObject()
                jsonRed.put("macAddress", wifi.macAddress)
                jsonRed.put("ssid", wifi.ssid)
                jsonRed.put("signalStrength", wifi.signalStrength!!.toInt())
                jsonRed.put("channel", wifi.channel!!.toInt())
                array!!.put(jsonRed)
                i++
            }
            jsonParam.put("wifiAccessPoints", array)
            if (array != null && array.length() > 0) {
                val url: String = PreyConfig.getInstance(ctx).getPreyUrl() + "geo"
                PreyLogger.d(String.format("url:%s", url))
                val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(
                    ctx
                ).jsonMethodAutentication(ctx, url, UtilConnection.REQUEST_METHOD_POST, jsonParam)
                if (response != null) {
                    if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                        val out = response.getResponseAsString()
                        val outJson = JSONObject(out)
                        if (!outJson.isNull("geolocation")) {
                            val geolocationJson = outJson.getJSONObject("geolocation")
                            val locationJSon = geolocationJson.getJSONObject("location")
                            val lat = locationJSon.getDouble("lat")
                            val lng = locationJSon.getDouble("lng")
                            val accuracy = geolocationJson.getInt("accuracy")
                            location = PreyLocation()
                            location.setLat(lat)
                            location.setLng(lng)
                            location.setAccuracy(accuracy)
                            location.setMethod("wifi")
                        }
                        if (!outJson.isNull("endpoint")) {
                            val endpointJson = outJson.getJSONObject("endpoint")
                            val urlJson = endpointJson.getString("url")
                            val userAgentJson = endpointJson.getString("user-agent")
                            val response2: PreyHttpResponse? =
                                UtilConnection.getInstance()
                                    .postJson(urlJson!!, userAgentJson!!, jsonParam)
                            if (response2!!.getStatusCode() == HttpURLConnection.HTTP_OK) {
                                val outEndpoint = response2.getResponseAsString()
                                val outJsonEndpoint = JSONObject(outEndpoint)
                                val locationJsonEndpoint = outJsonEndpoint.getJSONObject("location")
                                val lat = locationJsonEndpoint.getDouble("lat")
                                val lng = locationJsonEndpoint.getDouble("lng")
                                val accuracy = outJsonEndpoint.getInt("accuracy")
                                location = PreyLocation()
                                location.setLat(lat)
                                location.setLng(lng)
                                location.setAccuracy(accuracy)
                                location.setMethod("wifi")
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        if (location != null) {
            PreyLocationManager.getInstance().setLastLocation(location)
            PreyConfig.getInstance(ctx).setLocation(location)
        }
        return location
    }

    fun setPushRegistrationId(ctx: Context, regId: String): PreyHttpResponse? {
        //this.updateDeviceAttribute(ctx, "notification_id", regId);
        val data = HttpDataService("notification_id")
        data.setList(false)
        data.setKeyValue("notification_id")
        data.setSingleData(regId)
        val dataToBeSent = ArrayList<HttpDataService>()
        dataToBeSent.add(data)
        val response: PreyHttpResponse? = sendPreyHttpData(
            ctx, dataToBeSent
        )
        if (response != null) {
            val code = response.getStatusCode()
            PreyLogger.d("setPushRegistrationId code:$code")
            if (code == HttpURLConnection.HTTP_OK) {
                PreyLogger.d("setPushRegistrationId c2dm registry id set succesfully")
            }
        }
        return response
    }

    fun sendPreyHttpDataName(ctx: Context, nameDevice: String): PreyHttpResponse? {
        val parameters: MutableMap<String, String?> = HashMap()
        var preyHttpResponse: PreyHttpResponse? = null
        parameters["name"] = nameDevice
        try {
            val url = getDataUrlJson(ctx)
            if (UtilConnection.getInstance().isInternetAvailable()) {
                preyHttpResponse =
                    PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters)
                PreyLogger.d(
                    String.format(
                        "Data sent_: %s",
                        (if (preyHttpResponse == null) "" else preyHttpResponse.getResponseAsString())
                    )
                )
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Data wasn't send", e)
        }
        return preyHttpResponse
    }

    @Throws(PreyException::class)
    fun getInfoUrlJson(ctx: Context): String {
        return getDeviceUrlApiv2(ctx) + "/info.json"
    }

    fun getNameDevice(ctx: Context): String? {
        var name: String? = null
        try {
            val uri: String = getInfoUrlJson(ctx)
            val response = PreyRestHttpClient.getInstance(ctx).getAutentication(uri, null)
            if (response != null) {
                val out = response.getResponseAsString()
                if (out != null && "" != out && out.indexOf("Invalid") < 0) {
                    PreyLogger.d("getNameDevice:$out")
                    val jsnobject = JSONObject(out)
                    name = jsnobject.getString("name")
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error getNameDevice:" + e.message, e)
        }
        return name
    }

    @Throws(PreyException::class)
    fun uploadFile(ctx: Context, file: File, uploadID: String, total: Long): Int {
        val uri: String =
            PreyConfig.getInstance(ctx).getPreyUrl() + "upload/upload?uploadID=" + uploadID
        return PreyRestHttpClient.getInstance(ctx).uploadFile(ctx, uri, file, total)
    }

    @Throws(PreyException::class)
    fun sendTree(ctx: Context, json: JSONObject?): PreyHttpResponse? {
        val uri = getDeviceUrlApiv2(ctx) + "/data.json"
        return PreyRestHttpClient.getInstance(ctx)
            .jsonMethodAutentication(ctx, uri, UtilConnection.REQUEST_METHOD_POST, json)
    }

    @Throws(PreyException::class)
    fun getDeviceWebControlPanelUiUrl(ctx: Context): String {
        val preyConfig: PreyConfig = PreyConfig.getInstance(ctx)
        val deviceKey = preyConfig.getDeviceId()
        if (deviceKey == null || deviceKey === "") throw PreyException("Device key not found on the configuration")
        val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
        return PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("devices/")
            .plus(deviceKey)
    }

    @Throws(PreyException::class)
    fun deleteDevice(ctx: Context): String? {
        val preyConfig: PreyConfig = PreyConfig.getInstance(ctx)
        val parameters: MutableMap<String, String?> = HashMap()
        var xml: String? = null
        try {
            val url: String = this.getDeviceWebControlPanelUiUrl(ctx)
            val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(ctx)
                .delete(ctx, url, parameters)
            if (response != null) {
                PreyLogger.d(response.toString())
                xml = response.getResponseAsString()
            }
        } catch (e: java.lang.Exception) {
            throw PreyException(
                ctx.getText(com.prey.R.string.error_communication_exception).toString(), e
            )
        }
        return xml
    }

    @Throws(PreyException::class)
    fun getLocationUrlJson(ctx: Context?): String {
        return getDeviceUrlApiv2(ctx!!) + "/location.json"
    }

    fun sendLocation(ctx: Context, jsonParam: JSONObject?): PreyHttpResponse? {
        var preyHttpResponse: PreyHttpResponse? = null
        try {
            val url: String = getLocationUrlJson(ctx)
            if (UtilConnection.getInstance().isInternetAvailable()) {
                preyHttpResponse = PreyRestHttpClient.getInstance(ctx)
                    .jsonMethodAutentication(
                        ctx,
                        url,
                        UtilConnection.REQUEST_METHOD_POST,
                        jsonParam
                    )
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Contact wasn't send", e)
        }
        return preyHttpResponse
    }

    @Throws(PreyException::class)
    private fun getEventsUrlJson(ctx: Context): String {
        return getDeviceUrlApiv2(ctx) + "/events"
    }

    fun sendPreyHttpEvent(ctx: Context, event: Event, jsonObject: JSONObject): PreyHttpResponse? {
        var preyHttpResponse: PreyHttpResponse? = null
        try {
            val url: String = getEventsUrlJson(ctx)
            val parameters: MutableMap<String, String?> = HashMap()
            parameters["name"] = event.name!!
            parameters["info"] = event.info!!
            parameters["status"] = jsonObject.toString()
            PreyLogger.d("EVENT sendPreyHttpEvent url:$url")
            PreyLogger.d(("EVENT name:" + event.name!!).toString() + " info:" + event.info!!)
            PreyLogger.d("EVENT status:$jsonObject")
            val status = jsonObject.toString()
            preyHttpResponse = PreyRestHttpClient.getInstance(ctx)
                .postStatusAutentication(url, status, parameters)
            if (preyHttpResponse != null) {
                val jsonString = preyHttpResponse.getResponseAsString()
                if (jsonString != null && jsonString.length > 0) {
                    val jsonObjectList = JSONParser().getJSONFromTxt(
                        ctx!!, jsonString.toString()
                    )
                    if (jsonObjectList != null && jsonObjectList.size > 0) {
                        ActionsController.getInstance().runActionJson(ctx, jsonObjectList)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Event wasn't send", e)
        }
        return preyHttpResponse
    }

    @Throws(PreyException::class)
    fun triggers(ctx: Context): String? {
        val url = getDeviceUrlApiv2(ctx!!) + "/triggers.json"
        PreyLogger.d("url:$url")
        var sb: String? = null
        try {
            val params: MutableMap<String, String?>? = null
            val response = PreyRestHttpClient.getInstance(
                ctx
            ).getAutentication(url, params)
            if (response != null) {
                if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    sb = response.getResponseAsString()
                    if (sb != null) sb = sb.trim { it <= ' ' }
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error, causa:" + e.message, e)
            return null
        }
        return sb
    }

    @Throws(PreyException::class)
    private fun getReportUrlJson(ctx: Context): String {
        return getDeviceUrlApiv2(ctx) + "/reports.json"
    }

    fun sendPreyHttpReport(ctx: Context, dataToSend: List<HttpDataService>?): PreyHttpResponse? {
        val preyConfig: PreyConfig = PreyConfig.getInstance(ctx)

        val parameters: MutableMap<String, String?> = HashMap()
        val entityFiles: MutableList<EntityFile> = ArrayList()
        if (dataToSend != null) {
            for (httpDataService in dataToSend) {
                if (httpDataService != null) {
                    parameters.plus(httpDataService.getReportAsParameters())
                    if (httpDataService.getEntityFiles() != null && httpDataService.getEntityFiles().size > 0) {
                        entityFiles!!.addAll(httpDataService.getEntityFiles())
                    }
                }
            }
        }
        var preyHttpResponse: PreyHttpResponse? = null
        try {
            val url: String = getReportUrlJson(ctx)
            PreyConfig.postUrl = null
            PreyLogger.d("report url:$url")
            if (entityFiles == null || entityFiles.size == 0) preyHttpResponse =
                PreyRestHttpClient.getInstance(ctx).postAutenticationTimeout(ctx, url, parameters)
            else preyHttpResponse = PreyRestHttpClient.getInstance(ctx!!)
                .postAutentication(url, parameters, entityFiles)
            PreyLogger.d("Report sent: " + (if (preyHttpResponse == null) "" else preyHttpResponse.getResponseAsString()))
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Report wasn't send:" + e.message, e)
        }
        return preyHttpResponse
    }


    @Throws(java.lang.Exception::class)
    fun uploadStatus(ctx: Context, uploadID: String): FileretrievalDto? {
        var dto: FileretrievalDto? = null
        val uri: String =
            PreyConfig.getInstance(ctx).getPreyUrl() + "upload/upload?uploadID=" + uploadID
        val response = PreyRestHttpClient.getInstance(ctx).get(uri, null)
        if (response != null) {
            val responseAsString = response.getResponseAsString()
            PreyLogger.d("uploadStatus resp:$responseAsString")
            if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                if (responseAsString != null) {
                    val jsnobject = JSONObject(response.getResponseAsString())
                    val id = jsnobject.getString("ID")
                    val name = jsnobject.getString("Name")
                    val size = jsnobject.getString("Size")
                    val total = jsnobject.getString("Total")
                    val status = jsnobject.getString("Status")
                    val path = jsnobject.getString("Path")
                    dto = FileretrievalDto()
                    dto.setFileId(id)
                    dto.setName(name)
                    dto.setSize(size.toLong())
                    dto.setTotal(total.toLong())
                    dto.setStatus(status.toInt())
                    dto.setPath(path)
                }
            }
            if (response.getStatusCode() == 404) {
                dto = FileretrievalDto()
                dto.setStatus(response.getStatusCode())
            }
        }
        return dto
    }

    @Throws(PreyException::class)
    fun geofencing(ctx: Context): String? {
        val url = getDeviceUrlApiv2(ctx) + "/geofencing.json"
        PreyLogger.d("url:$url")
        var sb: String? = null
        val preyRestHttpClient = PreyRestHttpClient.getInstance(ctx)
        try {
            val params: MutableMap<String, String?>? = null
            val response = PreyRestHttpClient.getInstance(
                ctx!!
            ).getAutentication(url, params)
            if (response != null) {
                if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    sb = response.getResponseAsString()
                    if (sb != null) sb = sb.trim { it <= ' ' }
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error, causa:" + e.message, e)
            return null
        }
        return sb
    }


    fun getProfile(ctx: Context) {
        try {
            val parameters = HashMap<String, String?>()
            val apiv2: String = FileConfigReader.getInstance(ctx)!!.apiV2
            val url: String =
                PreyConfig.getInstance(ctx).getPreyUrl().plus(apiv2).plus("profile.json")
            PreyLogger.d("url:$url")
            val response = PreyRestHttpClient.getInstance(
                ctx!!
            ).getAutentication(url, parameters)
            if (response != null) {
                val out = response.getResponseAsString()
                PreyLogger.d(String.format("out:%s", out))
                val jsnobject = JSONObject(out)
                val email = jsnobject.getString("email")
                PreyLogger.d(String.format("email:%s", email))
                PreyConfig.getInstance(ctx).setEmail(email)
                val pro_account = jsnobject.getBoolean("pro_account")
                PreyConfig.getInstance(ctx).setProAccount(pro_account)
                val twoStepEnabled = jsnobject.getBoolean("two_step_enabled?")
                PreyConfig.getInstance(ctx).setTwoStep(twoStepEnabled)
                if (jsnobject.has("contact_form_for_free")) {
                    val contactFormForFree = jsnobject.getBoolean("contact_form_for_free")
                    PreyConfig.getInstance(ctx).setContactFormForFree(contactFormForFree)
                }
                if (jsnobject.has("msp_account")) {
                    val mspAccount = jsnobject.getBoolean("msp_account")
                    PreyConfig.getInstance(ctx).setMspAccount(mspAccount)
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e(String.format("error get profile:%s", e.message), e)
        }
    }

    @Throws(PreyException::class)
    fun getStatus(ctx: Context): JSONObject? {
        var jsnobject: JSONObject? = null
        val url = getDeviceUrlApiv2(ctx!!) + "/status.json"
        PreyLogger.d("getStatus url:$url")
        var response: PreyHttpResponse? = null
        val preyRestHttpClient = PreyRestHttpClient.getInstance(
            ctx!!
        )
        try {
            val params: MutableMap<String, String?>? = null
            response = PreyRestHttpClient.getInstance(ctx!!).getAutentication(url, params)
            if (response != null) {
                if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    val responseAsString = response.getResponseAsString()
                    if (responseAsString != null) {
                        jsnobject = JSONObject(response.getResponseAsString())
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error, causa:" + e.message, e)
            return null
        }
        return jsnobject
    }

    companion object {
        private var instance: PreyWebServices? = null

        fun getInstance(): PreyWebServices {
            if (PreyWebServices.instance == null) {
                PreyWebServices.instance = PreyWebServices()
            }
            return PreyWebServices.instance!!
        }
    }
}