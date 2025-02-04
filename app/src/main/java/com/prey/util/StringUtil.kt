/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util

import java.util.Locale

class StringUtil {

    companion object {
        fun firstCharUpper(myString: String?): String? {
            var myString = myString
            if (myString != null && myString.length > 1) {
                val stringArray = myString.toCharArray()
                stringArray[0] = stringArray[0].uppercaseChar()
                myString = String(stringArray)
            }
            return myString
        }

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
}