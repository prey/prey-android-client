/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net

import android.content.Context
import android.os.Build
import android.os.Environment
import com.prey.FileConfigReader
import com.prey.PreyAccountData
import com.prey.PreyConfig
import com.prey.PreyHardware
import com.prey.PreyLogger
import com.prey.PreyName
import com.prey.PreyPhone
import com.prey.PreyUtils
import com.prey.PreyUtils.isGooglePlayServicesAvailable
import com.prey.PreyVerify
import com.prey.PreyWifi
import com.prey.R
import com.prey.actions.HttpDataService
import com.prey.actions.fileretrieval.FileretrievalDto
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager
import com.prey.actions.observer.ActionsController
import com.prey.events.Event
import com.prey.exceptions.PreyException
import com.prey.json.parser.JSONParser
import com.prey.net.http.EntityFile
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


    /**
     * Register a new device for a given API_KEY, needed just after obtain the
     * new API_KEY.
     *
     * @throws PreyException
     */
    @Throws(java.lang.Exception::class)
    private fun registerNewDevice(
        context: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyHttpResponse? {
        var name: String? = name
        if (name == null || "" == name) {
            name = PreyUtils.getNameDevice(context)
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


        //  parameters = increaseData(context, parameters)

        val imei: String = PreyPhone.getInstance(context).getAndroidDeviceId()
        parameters["physical_address"] = imei
        val lang = Locale.getDefault().language
        parameters["lang"] = lang

        var response: PreyHttpResponse? = null
        val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
        val url: String =
            PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("devices.json")
        PreyLogger.d("url:$url")
        response = PreyRestHttpClient.getInstance(context).post(url, parameters)
        if (response == null) {
            throw PreyException(
                context.getString(
                    R.string.error_cant_add_this_device,
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

    fun increaseData(
        context: Context,
        parameters: HashMap<String, String?>
    ): HashMap<String, String?> {
        val phone = PreyPhone(context)
        val hardware: PreyHardware = phone.hardware!!
        var prefix = "hardware_attributes"
        parameters["$prefix[uuid]"] = hardware.getUuid()
        parameters["$prefix[bios_vendor]"] = hardware.getBiosVendor()
        parameters["$prefix[bios_version]"] = hardware.getBiosVersion()
        parameters["$prefix[mb_vendor]"] = hardware.getMbVendor()
        parameters["$prefix[mb_serial]"] = hardware.getMbSerial()
        parameters["$prefix[mb_model]"] = hardware.getMbModel()
        parameters["$prefix[cpu_model]"] = hardware.getCpuModel()
        parameters["$prefix[cpu_speed]"] = hardware.getCpuSpeed()
        parameters["$prefix[cpu_cores]"] = hardware.getCpuCores()
        parameters["$prefix[ram_size]"] = "" + hardware.getTotalMemory()
        parameters["$prefix[serial_number]"] = hardware.getSerialNumber()
        parameters["$prefix[uuid]"] = hardware.getUuid()
        parameters["$prefix[google_services]"] = isGooglePlayServicesAvailable(context).toString()
        val nic = 0
        val wifi: PreyWifi? = phone.getWifi()
        if (wifi != null) {
            prefix = "hardware_attributes[network]"
            parameters["$prefix[nic_$nic][name]"] = wifi.getName()
            parameters["$prefix[nic_$nic][interface_type]"] = wifi.getInterfaceType()
            parameters["$prefix[nic_$nic][ip_address]"] = wifi.getIpAddress()
            parameters["$prefix[nic_$nic][gateway_ip]"] = wifi.getGatewayIp()
            parameters["$prefix[nic_$nic][netmask]"] = wifi.getNetmask()
            parameters["$prefix[nic_$nic][mac_address]"] = wifi.getMacAddress()
        }
        return parameters
    }

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceToAccount(
        context: Context,
        email: String,
        password: String,
        deviceType: String
    ): PreyAccountData? {
        PreyLogger.d("registerNewDeviceToAccount email:$email password:$password")

        val parameters = HashMap<String, String?>()
        var response: PreyHttpResponse? = null
        var json: String? = null
        try {
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val lang = Locale.getDefault().language
            val url: String = PreyConfig.getInstance(context).getPreyUrl().plus(apiv2)
                .plus("profile.json?lang=").plus(lang)
            PreyLogger.d("_____url:$url")
            response = PreyRestHttpClient.getInstance(context).get(url, parameters, email, password)
            PreyLogger.d("response:$response")
            if (response != null) {
                json = response.getResponseAsString()
                PreyLogger.d("json:$json")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error!" + e.message, e)
            throw PreyException(
                "{\"error\":[\"" + context.getText(R.string.error_communication_exception)
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
            throw PreyException(context.getString(R.string.error_cant_add_this_device, status))
        }
        var deviceId: String? = null
        val responseDevice: PreyHttpResponse? =
            registerNewDevice(context, apiKey, deviceType, PreyUtils.getNameDevice(context))
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
            throw PreyException(context.getString(R.string.error_cant_add_this_device, status))
        }
        var newAccount: PreyAccountData = PreyAccountData()
        newAccount.setApiKey(apiKey)
        newAccount.setDeviceId(deviceId!!)
        newAccount.setEmail(email)
        newAccount.setPassword(password)
        return newAccount
    }

    fun getEmail(context: Context): String? {
        var email: String? = null
        try {
            val parameters = HashMap<String, String?>()
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val url: String =
                PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("profile.json")
            PreyLogger.d("url:$url")
            val response = PreyRestHttpClient.getInstance(context).getAutentication(url, parameters)
            if (response != null) {
                val out = response.getResponseAsString()
                PreyLogger.d("out:$out")
                val jsnobject = JSONObject(out)
                email = jsnobject.getString("email")
                PreyLogger.d("email:$email")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error get email", e)
        }
        return email
    }

    @Throws(PreyException::class)
    private fun checkPassword(apikey: String, password: String, context: Context): PreyHttpResponse {

        val parameters = HashMap<String, String?>()
        var response: PreyHttpResponse? = null
        var json: String? = ""
        try {
            val uri: String = PreyConfig.getInstance(context).getPreyUrl()
                .plus("api/v2/profile.json?lang=" + Locale.getDefault().language)
            response = PreyRestHttpClient.getInstance(context).get(uri, parameters, apikey, password)
            json = response!!.getResponseAsString()
        } catch (e: java.lang.Exception) {
            response = null
            val err = "" + context.getText(com.prey.R.string.error_communication_exception)
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
            val err = context.getText(com.prey.R.string.error_communication_500)
            json = StringBuffer("{\"error\":[\"").append(err).append("\"]}").toString()
            throw PreyException(json)
        }

        PreyLogger.d("____[token]_________________apikey:$apikey password:$password")
        getToken(context, apikey, password)

        return response
    }

    fun getToken(context: Context, apikey: String, password: String): String {
        var tokenJwt = ""
        try {
            val parameters = HashMap<String, String?>()
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val uri2: String =
                PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("get_token.json")
            val response2 = PreyRestHttpClient.getInstance(
                context
            ).get(uri2, parameters, apikey, password, "application/json")
            if (response2 != null) {
                PreyLogger.d("get_token:" + response2.getResponseAsString())
                val jsnobject = JSONObject(response2.getResponseAsString())
                tokenJwt = jsnobject.getString("token")
                PreyLogger.d("tokenJwt:$tokenJwt")
                PreyConfig.getInstance(context).setTokenJwt(tokenJwt)
            } else {
                PreyLogger.d("token: nulo")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error:" + e.message, e)
        }
        return tokenJwt
    }

    @Throws(java.lang.Exception::class)
    fun checkPassword(context: Context, apikey: String, password: String): Boolean {
        val response: PreyHttpResponse? = checkPassword(apikey, password, context)
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
        context: Context,
        apikey: String,
        password: String,
        password2: String
    ): Boolean {
        PreyLogger.d("checkPassword2 password:${password} password2:${password2}:%s")
        val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
        val url: String =
            PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("authenticate")
        val parameters = HashMap<String, String?>()
        parameters["email"] = PreyConfig.getInstance(context).getEmail()!!
        parameters["password"] = password
        parameters["otp_code"] = password2
        parameters["lang"] = Locale.getDefault().language
        var response: PreyHttpResponse? = null
        try {
            response = PreyRestHttpClient.getInstance(context).postAutentication(url, parameters)
        } catch (e: Exception) {
            PreyLogger.e("error:${e.message}", e)
        }
        if (response != null) {
            PreyLogger.d("authenticate:${response.getResponseAsString()}")
            if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                var tokenJwt = ""
                try {
                    val jsnobject = JSONObject(response.getResponseAsString())
                    tokenJwt = jsnobject.getString("token")
                    PreyConfig.getInstance(context).setTokenJwt(tokenJwt)
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
                    PreyLogger.e("error:${e.message}", e)
                }
                throw PreyException(json)
            }
        } else {
            throw PreyException(context.getText(R.string.password_wrong).toString())
        }
    }


    fun getTwoStepEnabled(context: Context): Boolean {
        var TwoStepEnabled = false
        try {
            val parameters = HashMap<String, String?>()

            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val url: String = PreyConfig.getInstance(context).getPreyUrl().plus(apiv2)
                .plus("profile?api_key=" + PreyConfig.getInstance(context).getApiKey())
            PreyLogger.d("url:$url")
            val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(
                context
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
    private fun getDeviceUrlApiv2(context: Context): String {

        val deviceKey = PreyConfig.getInstance(context).getDeviceId()
        if (deviceKey == null || deviceKey === "") throw PreyException("Device key not found on the configuration")
        val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
        val url: String =
            PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("devices/")
                .plus(deviceKey)
        return url
    }

    @Throws(PreyException::class)
    private fun getResponseUrlJson(context: Context): String {
        return getDeviceUrlApiv2(context).plus("/response")
    }

    fun sendNotifyActionResultPreyHttp(context: Context, params: MutableMap<String, String?>): String? {
        var response: String? = null
        try {

            val url: String = getResponseUrlJson(context)
            PreyConfig.postUrl = null
            val httpResponse = PreyRestHttpClient.getInstance(
                context
            ).postAutentication(url, params)
            response = httpResponse.toString()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Notify Action Result wasn't send:" + e.message, e)
        }
        return response
    }

    fun sendNotifyActionResultPreyHttp(
        context: Context,
        correlationId: String?,
        params: MutableMap<String, String?>
    ) {
        sendNotifyActionResultPreyHttp(context, null, correlationId, params)
    }

    fun sendNotifyActionResultPreyHttp(
        context: Context,
        status: String?,
        correlationId: String?,
        params: MutableMap<String, String?>
    ) {
        object : Thread() {
            override fun run() {
                val preyConfig: PreyConfig = PreyConfig.getInstance(context)
                var response: String? = null
                try {
                    val url = getResponseUrlJson(context!!)
                    PreyConfig.postUrl = null
                    val httpResponse: PreyHttpResponse? = PreyRestHttpClient.getInstance(context)
                        .postAutenticationCorrelationId(context, url, status, correlationId, params)
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
     * @param context
     * @param subject
     * @param message
     * @return help result
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun sendHelp(context: Context, subject: String, message: String): PreyHttpResponse? {
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
            val displayName = PreyConfig.getInstance(context).getHelpFile()
            PreyLogger.d("displayName:${displayName}")
            if (displayName != null && "" != displayName) {
                entityFiles = ArrayList<EntityFile>()
                val initialFile: File = File(dir, displayName)
                val inputStream: InputStream =
                    DataInputStream(FileInputStream(initialFile))
                val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmZ")
                val entityFile: EntityFile = EntityFile()
                entityFile.setFileInputStream(inputStream)
                entityFile.setFileMimeType("image/jpeg")
                entityFile.setName("file")
                entityFile.setFileName(displayName)
                entityFile.setFileType("image/jpeg")
                entityFile.setFileId(sdf.format(Date()) + "_" + entityFile.getFileType())
                entityFiles.add(entityFile)
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.d("Error contact:${e.message}")
        }
        val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
        val uri: String = PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("contact")
        val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(
            context
        ).sendHelp(context, uri, params, entityFiles!!)
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
    private fun getDeviceUrlJson(context: Context): String {
        return getDeviceUrlApiv2(context) + ".json"
    }

    @Throws(PreyException::class)
    fun getActionsJsonToPerform(context: Context): List<JSONObject>? {
        val url: String = getDeviceUrlJson(context)
        return JSONParser().getJSONFromUrl(context, url)
    }

    @Throws(com.prey.exceptions.PreyException::class)
    fun getDataUrlJson(context: Context?): String {
        return getDeviceUrlApiv2(context!!) + "/data.json"
    }

    fun sendPreyHttpData(
        context: Context,
        dataToSend: ArrayList<HttpDataService>?
    ): PreyHttpResponse? {

        var parameters: MutableMap<String, String?> = java.util.HashMap()
        var entityFiles: MutableList<EntityFile> = java.util.ArrayList()

        for (httpDataService in dataToSend!!) {
            if (httpDataService != null) {

                parameters.putAll(httpDataService.getDataAsParameters())
                if (httpDataService.getEntityFiles() != null && httpDataService.getEntityFiles()!!.size > 0) {
                    entityFiles.plus(httpDataService.getEntityFiles())
                }
            }
        }

        var preyHttpResponse: PreyHttpResponse? = null
        if (parameters.size > 0 || entityFiles.size > 0) {
            val hardware = PreyPhone.getInstance(context).hardware
            if (!PreyConfig.getInstance(context).isSendData() && hardware!!.getTotalMemory() > 0) {
                PreyConfig.getInstance(context).setSendData(true);

                parameters["hardware_attributes[ram_size]"] = "" + hardware!!.getTotalMemory()
            }
            if ("" != hardware!!.getUuid() && !com.prey.PreyConfig.getInstance(context)
                    .isSentUuidSerialNumber()
            ) {
                parameters["hardware_attributes[uuid]"] = hardware!!.getUuid()
                parameters["hardware_attributes[serial_number]"] = hardware!!.getSerialNumber()
                com.prey.PreyConfig.getInstance(context).setSentUuidSerialNumber(true)
            }
            try {
                val url: String = getDataUrlJson(context)
                com.prey.PreyLogger.d("URL:$url")
                com.prey.PreyConfig.postUrl = null
                if (UtilConnection.getInstance().isInternetAvailable()) {
                    if (entityFiles.size == 0) {
                        preyHttpResponse =
                            PreyRestHttpClient.getInstance(context).postAutentication(url, parameters);
                    } else {
                        preyHttpResponse = PreyRestHttpClient.getInstance(context)
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
     * @param context
     * @param listWifi
     * @return PreyLocation
     */
    fun getLocationWithWifi(context: Context, listWifi: List<PreyWifi>?): PreyLocation? {
        var location: PreyLocation? = null
        try {
            val jsonParam = JSONObject()
            val array = JSONArray()
            var i = 0
            while (listWifi != null && i < listWifi.size && i < 15) {
                val wifi = listWifi[i]
                val jsonRed = JSONObject()
                jsonRed.put("macAddress", wifi!!.getMacAddress())
                jsonRed.put("ssid", wifi!!.getSsid())
                jsonRed.put("signalStrength", wifi!!.getSignalStrength()!!.toInt())
                jsonRed.put("channel", wifi!!.getChannel()!!.toInt())
                array!!.put(jsonRed)
                i++
            }
            jsonParam.put("wifiAccessPoints", array)
            if (array != null && array.length() > 0) {
                val url: String = PreyConfig.getInstance(context).getPreyUrl() + "geo"
                PreyLogger.d("url:${url}")
                val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(
                    context
                ).jsonMethodAutentication(context, url, UtilConnection.REQUEST_METHOD_POST, jsonParam)
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
            PreyLogger.e("Error:${e.message}",  e)
        }
        if (location != null) {
            PreyLocationManager.getInstance().setLastLocation(location)
            PreyConfig.getInstance(context).setLocation(location)
        }
        return location
    }

    fun setPushRegistrationId(context: Context, regId: String): PreyHttpResponse? {
        //this.updateDeviceAttribute(context, "notification_id", regId);
        val data = HttpDataService("notification_id")
        data.setList(false)
        data.setKeyValue("notification_id")
        data.setSingleData(regId)
        val dataToBeSent = ArrayList<HttpDataService>()
        dataToBeSent.add(data)
        val response: PreyHttpResponse? = sendPreyHttpData(
            context, dataToBeSent
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

    fun sendPreyHttpDataName(context: Context, nameDevice: String): PreyHttpResponse? {
        val parameters: MutableMap<String, String?> = HashMap()
        var preyHttpResponse: PreyHttpResponse? = null
        parameters["name"] = nameDevice
        try {
            val url = getDataUrlJson(context)
            if (UtilConnection.getInstance().isInternetAvailable()) {
                preyHttpResponse =
                    PreyRestHttpClient.getInstance(context).postAutentication(url, parameters)
                PreyLogger.d(
                        "Data sent_: ${  (if (preyHttpResponse == null) "" else preyHttpResponse.getResponseAsString())}"
                )
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Data wasn't send", e)
        }
        return preyHttpResponse
    }

    @Throws(PreyException::class)
    fun getInfoUrlJson(context: Context): String {
        return getDeviceUrlApiv2(context) + "/info.json"
    }

    fun getNameDevice(context: Context): String? {
        var name: String? = null
        try {
            val uri: String = getInfoUrlJson(context)
            val response = PreyRestHttpClient.getInstance(context).getAutentication(uri, null)
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
    fun uploadFile(context: Context, file: File, uploadID: String, total: Long): Int {
        val uri: String =
            PreyConfig.getInstance(context).getPreyUrl() + "upload/upload?uploadID=" + uploadID
        return PreyRestHttpClient.getInstance(context).uploadFile(context, uri, file, total)
    }

    @Throws(PreyException::class)
    fun sendTree(context: Context, json: JSONObject?): PreyHttpResponse? {
        val uri = getDeviceUrlApiv2(context) + "/data.json"
        return PreyRestHttpClient.getInstance(context)
            .jsonMethodAutentication(context, uri, UtilConnection.REQUEST_METHOD_POST, json)
    }

    @Throws(PreyException::class)
    fun getDeviceWebControlPanelUiUrl(context: Context): String {
        val preyConfig: PreyConfig = PreyConfig.getInstance(context)
        val deviceKey = preyConfig.getDeviceId()
        if (deviceKey == null || deviceKey === "") throw PreyException("Device key not found on the configuration")
        val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
        return PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("devices/")
            .plus(deviceKey)
    }

    @Throws(PreyException::class)
    fun deleteDevice(context: Context): String? {
        val preyConfig: PreyConfig = PreyConfig.getInstance(context)
        val parameters: MutableMap<String, String?> = HashMap()
        var xml: String? = null
        try {
            val url: String = this.getDeviceWebControlPanelUiUrl(context)
            val response: PreyHttpResponse? = PreyRestHttpClient.getInstance(context)
                .delete(context, url, parameters)
            if (response != null) {
                PreyLogger.d(response.toString())
                xml = response.getResponseAsString()
            }
        } catch (e: java.lang.Exception) {
            throw PreyException(
                context.getText(R.string.error_communication_exception).toString(), e
            )
        }
        return xml
    }

    @Throws(PreyException::class)
    fun getLocationUrlJson(context: Context?): String {
        return getDeviceUrlApiv2(context!!) + "/location.json"
    }

    fun sendLocation(context: Context, jsonParam: JSONObject?): PreyHttpResponse? {
        PreyLogger.i("AWARE sendLocation.. ${jsonParam.toString()}")
        var preyHttpResponse: PreyHttpResponse? = null
        try {
            val url: String = getLocationUrlJson(context)
            if (UtilConnection.getInstance().isInternetAvailable()) {
                preyHttpResponse = PreyRestHttpClient.getInstance(context)
                    .jsonMethodAutentication(
                        context,
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
    private fun getEventsUrlJson(context: Context): String {
        return getDeviceUrlApiv2(context) + "/events"
    }

    fun sendPreyHttpEvent(context: Context, event: Event, jsonObject: JSONObject): PreyHttpResponse? {
        var preyHttpResponse: PreyHttpResponse? = null
        try {
            val url: String = getEventsUrlJson(context)
            val parameters: MutableMap<String, String?> = HashMap()
            parameters["name"] = event.name
            parameters["info"] = event.info
            parameters["status"] = jsonObject.toString()
            PreyLogger.d("EVENT sendPreyHttpEvent url:$url")
            PreyLogger.d(("EVENT name:" + event.name).toString() + " info:" + event.info)
            PreyLogger.d("EVENT status:$jsonObject")
            val status = jsonObject.toString()
            preyHttpResponse = PreyRestHttpClient.getInstance(context)
                .postStatusAutentication(url, status, parameters)
            if (preyHttpResponse != null) {
                val jsonString = preyHttpResponse.getResponseAsString()
                if (jsonString != null && jsonString.length > 0) {
                    val jsonObjectList = JSONParser().getJSONFromTxt(
                        context, jsonString.toString()
                    )
                    if (jsonObjectList != null && jsonObjectList.size > 0) {
                        ActionsController.getInstance().runActionJson(context, jsonObjectList)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Event wasn't send", e)
        }
        return preyHttpResponse
    }

    @Throws(PreyException::class)
    fun triggers(context: Context): String? {
        val url = getDeviceUrlApiv2(context!!) + "/triggers.json"
        PreyLogger.d("url:$url")
        var sb: String? = null
        try {
            val params: MutableMap<String, String?>? = null
            val response = PreyRestHttpClient.getInstance(
                context
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
    private fun getReportUrlJson(context: Context): String {
        return getDeviceUrlApiv2(context) + "/reports.json"
    }

    fun sendPreyHttpReport(context: Context, dataToSend: List<HttpDataService>?): PreyHttpResponse? {
        val preyConfig: PreyConfig = PreyConfig.getInstance(context)

        val parameters: MutableMap<String, String?> = HashMap()
        val entityFiles: MutableList<EntityFile> = ArrayList()
        if (dataToSend != null) {
            for (httpDataService in dataToSend) {
                if (httpDataService != null) {
                    PreyLogger.d("REPORT sendPreyHttpReport size:" + httpDataService.getReportAsParameters().size)
                    parameters.putAll(httpDataService.getReportAsParameters())
                    if (httpDataService.getEntityFiles() != null && httpDataService.getEntityFiles()!!.size > 0) {
                        entityFiles.addAll(httpDataService.getEntityFiles()!!)
                    }
                }
            }
        }
        var preyHttpResponse: PreyHttpResponse? = null
        try {
            val url: String = getReportUrlJson(context)
            PreyConfig.postUrl = null
            PreyLogger.d("report url:$url")
            if (entityFiles == null || entityFiles.size == 0) preyHttpResponse =
                PreyRestHttpClient.getInstance(context).postAutenticationTimeout(context, url, parameters)
            else preyHttpResponse = PreyRestHttpClient.getInstance(context!!)
                .postAutentication(url, parameters, entityFiles)
            PreyLogger.d("Report sent: " + (if (preyHttpResponse == null) "" else preyHttpResponse.getResponseAsString()))
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Report wasn't send:" + e.message, e)
        }
        return preyHttpResponse
    }


    @Throws(java.lang.Exception::class)
    fun uploadStatus(context: Context, uploadID: String): FileretrievalDto? {
        var dto: FileretrievalDto? = null
        val uri: String =
            PreyConfig.getInstance(context).getPreyUrl() + "upload/upload?uploadID=" + uploadID
        val response = PreyRestHttpClient.getInstance(context).get(uri, null)
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
    fun geofencing(context: Context): String? {
        val url = getDeviceUrlApiv2(context) + "/geofencing.json"
        PreyLogger.d("url:$url")
        var sb: String? = null
        val preyRestHttpClient = PreyRestHttpClient.getInstance(context)
        try {
            val params: MutableMap<String, String?>? = null
            val response = PreyRestHttpClient.getInstance(
                context!!
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


    fun getProfile(context: Context) {
        try {
            val parameters = HashMap<String, String?>()
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val url: String =
                PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("profile.json")
            PreyLogger.d("url:$url")
            val response = PreyRestHttpClient.getInstance(
                context!!
            ).getAutentication(url, parameters)
            if (response != null) {
                val out = response.getResponseAsString()
                PreyLogger.d("out:${out}")
                val jsnobject = JSONObject(out)
                val email = jsnobject.getString("email")
                PreyLogger.d("email:${email}")
                PreyConfig.getInstance(context).setEmail(email)
                val pro_account = jsnobject.getBoolean("pro_account")
                PreyConfig.getInstance(context).setProAccount(pro_account)
                val twoStepEnabled = jsnobject.getBoolean("two_step_enabled?")
                PreyConfig.getInstance(context).setTwoStep(twoStepEnabled)
                if (jsnobject.has("contact_form_for_free")) {
                    val contactFormForFree = jsnobject.getBoolean("contact_form_for_free")
                    PreyConfig.getInstance(context).setContactFormForFree(contactFormForFree)
                }
                if (jsnobject.has("msp_account")) {
                    val mspAccount = jsnobject.getBoolean("msp_account")
                    PreyConfig.getInstance(context).setMspAccount(mspAccount)
                }
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error get profile:${e.message}", e)
        }
    }

    @Throws(PreyException::class)
    fun getStatus(context: Context): JSONObject? {
        var jsnobject: JSONObject? = null
        val url = getDeviceUrlApiv2(context!!) + "/status.json"
        PreyLogger.d("getStatus url:$url")
        var response: PreyHttpResponse? = null
        val preyRestHttpClient = PreyRestHttpClient.getInstance(
            context!!
        )
        try {
            val params: MutableMap<String, String?>? = null
            response = PreyRestHttpClient.getInstance(context!!).getAutentication(url, params)
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

    @Throws(java.lang.Exception::class)
    fun getIPAddress(context: Context): String? {
        val uri = "http://ifconfig.me/ip"
        val response = PreyRestHttpClient.getInstance(
            context
        ).get(uri, null)
        var responseAsString: String? = ""
        if (response != null) {
            responseAsString = response.getResponseAsString()
            PreyLogger.d("responseAsString:$responseAsString")
        }
        return responseAsString
    }

    @Throws(PreyException::class)
    fun getFileUrlJson(context: Context?): String {
        return getDeviceUrlApiv2(context!!) + "/files.json"
    }

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceWithApiKeyEmail(
        context: Context,
        apiKey: String,
        deviceType: String,
        name: String
    ): PreyAccountData? {
        return registerNewDeviceWithApiKeyEmail(context, apiKey, null, deviceType, name)
    }

    @Throws(java.lang.Exception::class)
    fun registerNewDeviceWithApiKeyEmail(
        context: Context,
        apiKey: String,
        email: String?,
        deviceType: String,
        name: String
    ): PreyAccountData? {
        var deviceId: String? = null
        val responseDevice = registerNewDevice(context, apiKey, deviceType, name)
        var xmlDeviceId: String? = null
        if (responseDevice != null) {
            xmlDeviceId = responseDevice.getResponseAsString()
        }
        //if json
        if (xmlDeviceId != null && xmlDeviceId.contains("{\"key\"")) {
            try {
                val jsnobject = JSONObject(xmlDeviceId)
                deviceId = jsnobject.getString("key")
            } catch (e: java.lang.Exception) {
            }
        }
        var newAccount: PreyAccountData? = null
        if (deviceId != null && "" != deviceId) {
            newAccount = PreyAccountData()
            newAccount.setApiKey(apiKey)
            newAccount.setDeviceId(deviceId)
            newAccount.setEmail(email)
            newAccount.setPassword("")
        }
        return newAccount
    }

    fun validateName(context: Context, name: String): PreyName {
        val preyName = PreyName()
        try {
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val config: PreyConfig = PreyConfig.getInstance(context)
            val deviceKey = config.getDeviceId()
            val url: String =
                PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("devices/")
                    .plus(deviceKey).plus("/validate.json")
            PreyLogger.d("validateName name:$name")
            val jsonParam = JSONObject()
            jsonParam.put("name", name)
            jsonParam.put("lang", Locale.getDefault().language)
            val response = PreyRestHttpClient.getInstance(context)
                .jsonMethodAutentication(context, url, UtilConnection.REQUEST_METHOD_POST, jsonParam)
            PreyLogger.d("validateName getStatusCode:" + response!!.getStatusCode())
            preyName.setCode(response!!.getStatusCode())
        } catch (e: java.lang.Exception) {
            PreyLogger.d("validateName error validate:" + e.message)
        }
        return preyName
    }

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
    ): PreyAccountData {
        return registerNewAccount(
            context,
            name,
            email,
            password,
            password,
            rule_age,
            privacy_terms,
            offer,
            deviceType
        )
    }

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
    ): PreyAccountData {
        val parameters = HashMap<String, String?>()
        parameters["name"] = name
        parameters["email"] = email
        parameters["password"] = password1
        parameters["password_confirmation"] = password2
        parameters["country_name"] = Locale.getDefault().displayCountry
        parameters["policy_rule_age"] = rule_age
        parameters["policy_rule_privacy_terms"] = privacy_terms
        parameters["mkt_newsletter"] = offers
        parameters["lang"] = Locale.getDefault().language

        var response: PreyHttpResponse? = null
        var xml: String? = ""
        try {
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val url: String =
                PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("signup.json")
            PreyLogger.d("url:$url")
            response = PreyRestHttpClient.getInstance(context).post(url, parameters)
            if (response != null) {
                xml = response.getResponseAsString()
                PreyLogger.d("code:" + response.getStatusCode() + " xml:" + xml)
            } else {
                PreyLogger.d("response nulo")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error: " + e.message, e)
            throw PreyException(
                "{\"error\":[\"" + context.getText(com.prey.R.string.error_communication_exception)
                    .toString() + "\"]}"
            )
        }

        var apiKey = ""
        if (xml!!.contains("\"key\"")) {
            try {
                val jsnobject = JSONObject(xml)
                apiKey = jsnobject.getString("key")
            } catch (e: java.lang.Exception) {
            }
        } else {
            if (response != null && response.getStatusCode() > 299) {
                PreyLogger.d("response.getStatusCode() >299 :" + response.getStatusCode())
                throw PreyException(xml)
            }
        }
        var deviceId: String? = null
        val responseDevice =
            registerNewDevice(context, apiKey!!, deviceType!!, PreyUtils.getNameDevice(context))
        if (responseDevice != null) {
            val xmlDeviceId = responseDevice.getResponseAsString()
            if (xmlDeviceId!!.contains("{\"key\"")) {
                try {
                    val jsnobject = JSONObject(xmlDeviceId)
                    deviceId = jsnobject.getString("key")
                } catch (e: java.lang.Exception) {
                }
            } else {
                throw PreyException(context.getString(R.string.error_cant_add_this_device, ""))
            }
        } else {
            throw PreyException(context.getString(R.string.error_cant_add_this_device, ""))
        }

        val newAccount = PreyAccountData()
        newAccount.setApiKey(apiKey)
        newAccount.setDeviceId(deviceId)
        newAccount.setEmail(email)
        newAccount.setPassword(password1)
        newAccount.setName(name)
        return newAccount
    }

    fun renameName(context: Context, name: String): PreyName {
        val preyName = PreyName()
        try {
            val apiv2: String = FileConfigReader.getInstance(context)!!.getApiV2()
            val config: PreyConfig = PreyConfig.getInstance(context)
            val deviceKey = config.getDeviceId()
            val url: String =
                PreyConfig.getInstance(context).getPreyUrl().plus(apiv2).plus("devices/")
                    .plus(deviceKey).plus(".json")
            val jsonParam = JSONObject()
            jsonParam.put("name", name)
            jsonParam.put("lang", Locale.getDefault().language)
            val response = PreyRestHttpClient.getInstance(context)
                .jsonMethodAutentication(context, url, UtilConnection.REQUEST_METHOD_PUT, jsonParam)
            PreyLogger.d("renameName:${response!!.getStatusCode()}")
            preyName.setCode(response.getStatusCode())
            if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                val out = response.getResponseAsString()
                PreyConfig.getInstance(context).setDeviceName(name)
                PreyLogger.d("renameName:${out}")
            }
            if (response.getStatusCode() == 422) {
                val out = response.getResponseAsString()
                PreyLogger.d("renameName:${out}")
                val outJson = JSONObject(out)
                var name_available_error: String? = ""
                var name_available = ""
                if (out!!.indexOf("\"title\"") > 0) {
                    val array2 = outJson.getJSONArray("title")
                    var i = 0
                    while (array2 != null && i < array2.length()) {
                        try {
                            val outJson1 = array2.getString(i) as String
                            if ("" == name_available_error) {
                                val s = outJson1.substring(0, 1).uppercase(Locale.getDefault())
                                name_available_error = s + outJson1.substring(1)
                            } else {
                                name_available_error += ", $outJson1"
                            }
                        } catch (e: java.lang.Exception) {
                            name_available_error = e.message
                        }
                        i++
                    }
                } else {
                    val array1 = outJson.getJSONArray("name_available_error")
                    run {
                        var i = 0
                        while (array1 != null && i < array1.length()) {
                            try {
                                val outJson1 = array1.getString(i) as String
                                if ("" == name_available_error) {
                                    name_available_error = outJson1
                                } else {
                                    name_available_error += ", $outJson1"
                                }
                            } catch (e: java.lang.Exception) {
                                name_available_error = e.message
                            }
                            i++
                        }
                    }
                    val array2 = outJson.getJSONArray("name_available")
                    var i = 0
                    while (array2 != null && i < array2.length()) {
                        try {
                            val outJson2 = array2.getString(i) as String
                            if ("" == name_available) {
                                name_available = outJson2
                            } else {
                                name_available += ", $outJson2"
                            }
                        } catch (e: java.lang.Exception) {
                            name_available_error = e.message
                        }
                        i++
                    }
                }
                preyName.setError(name_available_error)
                preyName.setName(name_available)
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.d("error validate:${e.message}")
        }
        return preyName
    }

    @Throws(java.lang.Exception::class)
    fun validToken(context: Context, token: String): Boolean {
        val json = JSONObject()
        json.put("token", token)
        json.put("action", "deploy")
        val uri: String = PreyConfig.getInstance(context).getPreyUrl().plus("token/v2/check")
        val response: PreyHttpResponse? =
            PreyRestHttpClient.getInstance(context).getValidToken(context, uri, json)
        var statusCode = -1
        try {
            statusCode = response!!.getStatusCode()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error validateToken:${ e.message}" , e)
        }
        return statusCode == 200
    }

    @Throws(java.lang.Exception::class)
    fun verifyEmail(context: Context, email: String): PreyVerify? {
        val apiKey: String? = PreyConfig.getInstance(context).getApiKey()
        val apiV2: String = FileConfigReader.getInstance(context)!!.getApiV2()
        val url: String = PreyConfig.getInstance(context).getPreyUrl().plus(apiV2)
            .plus("users/verify_email.json")
        var preyHttpResponse: PreyHttpResponse? = null
        val jsonParam = JSONObject()
        jsonParam.put("email", email)
        jsonParam.put("lang", Locale.getDefault().language)
        preyHttpResponse = PreyRestHttpClient.getInstance(context)
            .jsonMethodAutentication(context, url, UtilConnection.REQUEST_METHOD_PUT, jsonParam)
        var verify: PreyVerify? = null
        if (preyHttpResponse != null) {
            var body = preyHttpResponse.getResponseAsString()
            if (body != null) body = body.trim { it <= ' ' }
            val statusCode = preyHttpResponse.getStatusCode()
            PreyLogger.d("verify code:$statusCode")
            PreyLogger.d("verify body:$body")
            verify = PreyVerify()
            verify.setStatusCode(statusCode)
            verify.setStatusDescription(body)
        }
        return verify
    }

    companion object {
        private var instance: PreyWebServices? = null

        fun getInstance(): PreyWebServices {
            if (instance == null) {
                instance = PreyWebServices()
            }
            return instance!!
        }
    }
}