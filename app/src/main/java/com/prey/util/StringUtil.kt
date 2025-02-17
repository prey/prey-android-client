/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util

import java.util.Locale

/**
 * A utility class for string manipulation.
 */
class StringUtil {

    /**
     * Converts the first character of a string to uppercase.
     *
     * @param myString the input string
     * @return the string with the first character converted to uppercase, or null if the input string is null
     */
    fun firstCharUpper(myString: String?): String? {
        var myString = myString
        if (myString != null && myString.length > 1) {
            val stringArray = myString.toCharArray()
            stringArray[0] = stringArray[0].uppercaseChar()
            myString = String(stringArray)
        }
        return myString
    }

    /**
     * Checks if a string represents a boolean value.
     *
     * @param texto the input string
     * @return true if the string represents a boolean value, false otherwise
     */
    fun isTextBoolean(texto: String?): Boolean {
        var texto = texto
        val out = false
        if (texto != null) {
            texto = texto.lowercase(Locale.getDefault()).trim { it <= ' ' }
            if ("true" == texto || "false" == texto) {
                return true
            }
        }
        return out
    }

    /**
     * Checks if a string represents an integer value.
     *
     * @param texto the input string
     * @return true if the string represents an integer value, false otherwise
     */
    fun isTextInteger(texto: String?): Boolean {
        var texto = texto
        val out = false
        if (texto != null) {
            texto = texto.lowercase(Locale.getDefault()).trim { it <= ' ' }
            try {
                texto.toInt()
                return true
            } catch (e: Exception) {
                return false
            }
        }
        return out
    }

    /**
     * Formats a string to follow the conventional Java class naming conventions.
     *
     * @param myString the input string
     * @return the formatted string
     */
    fun classFormat(myString: String): String {
        val out = StringBuffer()
        val array = myString.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var i = 0
        while (array != null && i < array.size) {
            out.append(firstCharUpper(array[i]))
            i++
        }
        return out.toString()
    }

}