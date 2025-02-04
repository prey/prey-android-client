package com.prey.json.actions

import android.content.Context
import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.json.JsonAction
import com.prey.PreyPhone
import com.prey.PreyWifi
import org.json.JSONObject


class ActiveAccessPoint : JsonAction() {

    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val listResult: MutableList<HttpDataService>? = super.report(ctx, list, parameters)
        return listResult
    }

    override fun run(ctx: Context, list: MutableList<ActionResult>?, parameters: JSONObject?): HttpDataService {
        var data: HttpDataService = HttpDataService("active_access_point")
        data.setList(true)
        val phone: PreyPhone = PreyPhone.getInstance(ctx)
        val wifiPhone: PreyWifi? = phone.getWifi()!!
        if (wifiPhone!=null && wifiPhone.isWifiEnabled()) {
            val ssid: String = wifiPhone.getSsid()
            if ("" != ssid && "<unknown ssid>" != ssid) {
                val parametersMap = HashMap<String, String?>()
                parametersMap["ssid"] = wifiPhone.getSsid()
                parametersMap["security"] = wifiPhone.getSecurity()
                try {
                    parametersMap["mac_address"] = wifiPhone.getMacAddress()
                } catch (e: Exception) {
                    parametersMap["mac_address"] = null
                }
                parametersMap["signal_strength"] = wifiPhone.getSignalStrength()
                parametersMap["channel"] = wifiPhone.getChannel()
                data.addDataListAll(parametersMap)
            }
        }
        return data
    }
}