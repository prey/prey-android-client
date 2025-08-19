package com.prey.actions.report

import android.content.Context
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.HttpDataService
import com.prey.actions.aware.AwareController
import com.prey.actions.observer.ActionResult
import com.prey.net.http.EntityFile
import com.prey.util.ClassUtil
import org.json.JSONArray
import org.json.JSONObject

class ReportAction {

    fun start(
        context: Context,
        interval: Int
    ): MutableList<HttpDataService>? {
        var listData: MutableList<HttpDataService>? = ArrayList()
        val exclude = PreyConfig.getInstance(context).getExcludeReport()
        var jsonArray = JSONArray()
        AwareController.getInstance().initLastLocation(context)
        PreyLogger.d("REPORT start:$interval")
        jsonArray = JSONArray()
        if (!exclude!!.contains("picture")) jsonArray.put("picture")
        if (!exclude!!.contains("access_points_list")) jsonArray.put("access_points_list")
        if (!exclude!!.contains("active_access_point")) jsonArray.put("active_access_point")
        if (!exclude!!.contains("location")) jsonArray.put("location")
        PreyLogger.d("REPORT jsonArray:$jsonArray")
        try {
            val listActions: MutableList<ActionResult> = ArrayList()
            for (i in 0 until jsonArray.length()) {
                if (PreyConfig.getInstance(context).isMissing()) {
                    val nameAction = jsonArray.getString(i)
                    PreyLogger.d("REPORT start nameAction:$nameAction")
                    val methodAction = "report"
                    val parametersAction: JSONObject? = null
                    listData = ClassUtil.getInstance().execute(
                        context,
                        listActions,
                        nameAction,
                        methodAction,
                        parametersAction,
                        listData
                    )
                    PreyLogger.d("REPORT stop nameAction:$nameAction")
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("REPORT error:${e.message}", e)
        }
        var parms = 0
        var i = 0
        while (listData != null && i < listData.size) {
            val httpDataService = listData[i]
            parms += httpDataService.getDataAsParameters().size
            PreyLogger.d("REPORT ____params size:${httpDataService.getDataAsParameters().size}")
            PreyLogger.d("REPORT ____params:${httpDataService.getDataAsParameters().toString()}")
            PreyLogger.d("REPORT ____files size:${httpDataService.getEntityFiles().size}")
            if (httpDataService.getEntityFiles() != null) {
                for (j in 0 until httpDataService.getEntityFiles().size) {
                    val entity: EntityFile = httpDataService.getEntityFiles()[j]
                    if (entity != null && entity.getFileSize() > 0) {
                        parms += 1
                    }
                }
            }
            i++
        }
        PreyLogger.d("REPORT ____params__ size: ${listData!!.size}")
        if (PreyConfig.getInstance(context).isMissing()) {
            if (parms > 0) {
                val response = PreyConfig.getInstance(context).getWebServices()
                    .sendPreyHttpReport(context, listData)
                if (response != null) {
                    PreyConfig.getInstance(context).setLastEvent("report_send")
                    PreyLogger.d("REPORT response.getStatusCode():${response.getStatusCode()}")
                    if (409 == response.getStatusCode()) {
                        ReportScheduled.getInstance(context).reset()
                        PreyConfig.getInstance(context).setMissing(false)
                        PreyConfig.getInstance(context).setIntervalReport("")
                        PreyConfig.getInstance(context).setExcludeReport("")
                    }
                }
            }
        }
        return listData
    }

}