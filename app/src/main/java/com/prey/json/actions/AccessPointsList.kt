package com.prey.json.actions

import android.content.Context
import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.json.JsonAction
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone
import com.prey.PreyWifi
import com.prey.managers.PreyConnectivityManager
import com.prey.net.PreyWebServices
import org.json.JSONObject


class AccessPointsList : JsonAction() {


    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val listResult: MutableList<HttpDataService>? = super.report(ctx, list, parameters)
        return listResult
    }

    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val listResult: MutableList<HttpDataService>? = super.get(ctx, list, parameters)
        return listResult
    }

    override fun run(ctx: Context, list: MutableList<ActionResult>?, parameters: JSONObject?): HttpDataService {
        val dataWifi: HttpDataService = HttpDataService("access_points_list")
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        try {
            if (PreyConnectivityManager.getInstance().isWifiConnected(ctx)) {
                val parametersMapWifi = HashMap<String, String?>()
                val preyPhone: PreyPhone = PreyPhone.getInstance(ctx)
                val listWifi: List<PreyWifi>? = preyPhone.getListWifi()
                PreyLogger.d("REPORT listWifi:${listWifi!!.size}" )
                var i = 0
                while (listWifi != null && i < listWifi.size) {
                    val wifi = listWifi[i]
                    parametersMapWifi.put(""+i + "][ssid", wifi.getSsid());
                    parametersMapWifi.put(""+i + "][mac_address", wifi.getMacAddress());
                    parametersMapWifi.put(""+i + "][security", wifi.getSecurity());
                    parametersMapWifi.put(""+i + "][signal_strength", wifi.getSignalStrength());
                    parametersMapWifi.put(""+i + "][channel", wifi.getChannel());
                    i++
                }
                dataWifi.setList(true)
                dataWifi.getDataList().putAll(parametersMapWifi)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error causa:" + e.message + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("get", "access_points_list", "failed", e.message)
            )
        }
        return dataWifi
    }
}