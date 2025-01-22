/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.kotlin

import com.prey.net.http.kotlin.EntityFile

class HttpDataService(private var keyValue: String) {
    private var dataList: MutableMap<String, String?>? = HashMap()
    private var singleData: String? = null
    private var isList: Boolean = false
    private var httpMethod: String? = null
    private var url: String? = null
    private var entityFiles: List<EntityFile>

    fun getKeyValue(): String {
        return keyValue
    }

    fun setKeyValue(keyValue: String?) {
        this.keyValue = keyValue!!
    }

    fun getDataList(): MutableMap<String, String?>? {
        return dataList
    }

    fun setDataList(dataList: MutableMap<String, String?>?) {
        this.dataList = dataList!!
    }

    fun isList(): Boolean {
        return isList
    }

    fun setList(isList: Boolean) {
        this.isList = isList
    }


    fun getSingleData(): String {
        return singleData!!
    }

    fun setSingleData(singleData: String?) {
        this.singleData = singleData
    }

    fun addDataListAll(map: MutableMap<String, String?>?) {
        dataList!!.putAll(map!!)
    }

    init {
        entityFiles = ArrayList()
    }

    fun getReportAsParameters(): MutableMap<String, String?> {
        val parameters = HashMap<String, String?>()
        if (isList) {
            var key: StringBuffer? = StringBuffer()
            for (valueKey in dataList!!.keys) {
                val valueData = dataList!![valueKey]
                key!!.append("")
                key.append(this.getKeyValue())
                key.append("[")
                key.append(valueKey)
                key.append("]")
                parameters[key.toString()] = valueData
                key.delete(0, key.length)
            }
            key = null
        } else parameters[" $keyValue "] = singleData
        return parameters
    }

    fun getDataAsParameters(): MutableMap<String, String?> {

        val parameters = HashMap<String, String?>()
        if (isList) {
            var key: StringBuffer? = StringBuffer()
            for (valueKey in dataList!!.keys) {
                val valueData = dataList!![valueKey]
                key!!.append(this.keyValue)
                key.append("[")
                key.append(valueKey)
                key.append("]")
                parameters[key.toString()] = valueData
                key.delete(0, key.length)
            }
            key = null
        } else parameters[keyValue] = singleData
        return parameters
    }

    fun getDataAsString(): String {
        val sb = StringBuffer()
        if (isList) {
            for (valueKey in dataList!!.keys) {
                val valueData = dataList!![keyValue]
                sb.append(keyValue)
                sb.append("[")
                sb.append(valueKey)
                sb.append("]=")
                sb.append(valueData)
                sb.append("&")
            }
        } else sb.append(keyValue).append("=").append(singleData).append("&")
        return sb.toString()
    }

    fun getDataListKey(key: String): String? {
        var value: String? = ""
        if (dataList != null && dataList!!.containsKey(key)) {
            value = dataList!![key]
        }
        return value
    }

    fun putData(dataList: Map<String, String>?) {
        this.dataList!!.putAll(dataList!!)
    }

    fun addEntityFile(entityFile: EntityFile) {
        entityFiles.plus(entityFile)
    }

    fun getEntityFiles(): List<EntityFile> {
        return entityFiles
    }

    fun setEntityFiles(entityFiles: List<EntityFile>) {
        this.entityFiles = entityFiles
    }
}