/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions

import com.prey.PreyLogger
import com.prey.net.http.EntityFile

/**
 * A service class for handling HTTP data.
 *
 * This class provides methods for setting and getting key-value pairs, data lists, and single data.
 * It also provides methods for converting data to parameters and string formats.
 */
class HttpDataService(private var keyValue: String) {
    private var dataList: MutableMap<String, String?> = HashMap()
    private var singleData: String? = null
    private var isList: Boolean = false
    private var httpMethod: String? = null
    private var url: String? = null
    private var entityFiles: MutableList<EntityFile> = ArrayList()

    fun getKeyValue(): String {
        return keyValue
    }

    fun setKeyValue(keyValue: String?) {
        this.keyValue = keyValue!!
    }

    fun getDataList(): MutableMap<String, String?> {
        return dataList
    }

    fun setDataList(dataList: MutableMap<String, String?>) {
        this.dataList = dataList
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
        dataList.putAll(map!!)
    }

    init {
        entityFiles = ArrayList()
    }

    /**
     * Returns the data as parameters.
     *
     * If the data is a list, each key-value pair is added to the parameters map.
     * If the data is not a list, the single data is added to the parameters map.
     *
     * @return the data as parameters
     */
    fun getReportAsParameters(): MutableMap<String, String?> {
        val parameters = HashMap<String, String?>()
        if (isList) {
            for (valueKey in dataList.keys) {
                val valueData = dataList[valueKey]
                parameters["${getKeyValue()}[${valueKey}]"] = valueData
            }
        } else parameters[" $keyValue "] = singleData
        return parameters
    }

    /**
     * Returns the data as parameters.
     *
     * If the data is a list, each key-value pair is added to the parameters map.
     * If the data is not a list, the single data is added to the parameters map.
     *
     * @return the data as parameters
     */
    fun getDataAsParameters(): MutableMap<String, String?> {
        val parameters = HashMap<String, String?>()
        if (isList) {
            for (valueKey in dataList.keys) {
                val valueData = dataList[valueKey]
                parameters["${keyValue}[${valueKey}]"] = valueData
            }
        } else parameters[keyValue] = singleData
        return parameters
    }

    /**
     * Returns the data as a string.
     *
     * If the data is a list, each key-value pair is appended to the string.
     * If the data is not a list, the single data is appended to the string.
     *
     * @return the data as a string
     */
    fun getDataAsString(): String {
        val buffer = StringBuffer()
        if (isList) {
            for (valueKey in dataList.keys) {
                val valueData = dataList[keyValue]
                buffer.append("$keyValue[$valueKey]=$valueData&")
            }
        } else {
            buffer.append("$keyValue=$singleData&")
        }
        return buffer.toString()
    }

    /**
     * Retrieves the value associated with the specified key from the data list.
     *
     * @param key the key to look up in the data list
     * @return the value associated with the key, or null if the key is not present
     */
    fun getDataListKey(key: String): String? {
        var value: String? = ""
        if (dataList != null && dataList.containsKey(key)) {
            value = dataList[key]
        }
        return value
    }

    fun putData(dataList: Map<String, String>?) {
        this.dataList.putAll(dataList!!)
    }

    fun addEntityFile(entityFile: EntityFile) {
        entityFiles.add(entityFile)
        PreyLogger.d("report entityFiles size:${entityFiles.size}")
    }

    fun getEntityFiles(): MutableList<EntityFile> {
        return entityFiles
    }

    fun setEntityFiles(entityFiles: MutableList<EntityFile>) {
        this.entityFiles = entityFiles
    }

}