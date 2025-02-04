/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import com.prey.PreyLogger
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object TriggerUtil {
    var sdf_dh: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    var sdf_h: SimpleDateFormat = SimpleDateFormat("HHmmss")
    var sdf_d: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")

    fun haveRange(listEvents: List<TriggerEventDto>?): Boolean {
        var j = 0
        while (listEvents != null && j < listEvents.size) {
            val event = listEvents[j]
            if (TimeTrigger.RANGE_TIME == event.getType()) {
                return true
            }
            if (TimeTrigger.REPEAT_RANGE_TIME == event.getType()) {
                return true
            }
            j++
        }
        return false
    }

    fun validRange(listEvents: List<TriggerEventDto>?): Boolean {
        var j = 0
        while (listEvents != null && j < listEvents.size) {
            val event = listEvents[j]
            if (TimeTrigger.RANGE_TIME == event.getType()) {
                return validRange(event)
            }
            if (TimeTrigger.REPEAT_RANGE_TIME == event.getType()) {
                return validRangeTime(event)
            }
            j++
        }
        return false
    }

    fun validRangeTime(event: TriggerEventDto): Boolean {
        val now = Date()
        val cal = Calendar.getInstance()
        cal.time = now
        val day_od_week = cal[Calendar.DAY_OF_WEEK]
        PreyLogger.d("day_od_week:$day_od_week")
        try {
            val root = JSONObject(event.getInfo())
            var daysOfWeek = root.getString("days_of_week")
            daysOfWeek = daysOfWeek.replace("[", "")
            daysOfWeek = daysOfWeek.replace("]", "")
            val parts =
                daysOfWeek.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val hour_fromSt = root.getString("hour_from")
            val hour_untilSt = root.getString("hour_until")
            val hour_from = hour_fromSt.toInt()
            val hour_until = hour_untilSt.toInt()
            var until = -1
            try {
                until = root.getInt("until")
                PreyLogger.d("until:$until")
                val fechaSt = sdf_d.format(now)
                val fecha = fechaSt.toInt()
                if (fecha > until) {
                    PreyLogger.d("fecha>until")
                    return false
                } else {
                    PreyLogger.d("fecha<=until")
                }
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            var isDay = false
            var i = 0
            while (parts != null && i < parts.size) {
                val daySt = parts[i]
                val day = dayTrigger(daySt)
                if (day == day_od_week) isDay = true
                i++
            }
            PreyLogger.d("isDay:$isDay")
            val horaSt = sdf_h.format(now)
            val hora = horaSt.toInt()
            PreyLogger.d("horaSt:$horaSt")
            if (isDay) {
                var a = false
                if (hour_from <= hora) {
                    PreyLogger.d("a:")
                    a = true
                }
                var b = false
                if (hour_until >= hora) {
                    PreyLogger.d("b:")
                    b = true
                }
                if (a && b) {
                    return true
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("error validRangeTime:" + e.message, e)
        }
        return false
    }

    fun validRange(event: TriggerEventDto): Boolean {
        try {
            val nowDate = Date()
            val nowSt = sdf_d.format(nowDate)
            val now = nowSt.toDouble()
            val root = JSONObject(event.getInfo())
            val from = root.getDouble("from")
            val until = root.getDouble("until")
            var a = false
            if (from <= now) {
                a = true
            }
            var b = false
            if (until >= now) {
                b = true
            }
            if (a && b) {
                return true
            }
        } catch (e: Exception) {
            PreyLogger.e("error validRange:" + e.message, e)
        }
        return false
    }

    fun validateTrigger(trigger: TriggerDto): Boolean {
        val now = Date()
        val listEvents = TriggerParse.TriggerEvents(trigger.getEvents()!!)
        var j = 0
        while (listEvents != null && j < listEvents.size) {
            val event = listEvents[j]
            if (TimeTrigger.EXACT_TIME == event.getType()) {
                var valid = true
                try {
                    val json = JSONObject(event.getInfo())
                    var dateTime: String? = null
                    dateTime = json.getString("date")
                    PreyLogger.d(String.format("TimeTrigger dateTime:%s", dateTime))
                    val date = TimeTrigger.EXACT_TIME_FORMAT_SDF.parse(dateTime)
                    //increased to 15
                    valid = validDateAroundMinutes(date, 15)
                } catch (e: Exception) {
                    PreyLogger.e(String.format("Error :%s", e.message), e)
                }
                return valid
            }
            if (TimeTrigger.REPEAT_TIME == event.getType()) {
                try {
                    val json = JSONObject(event.getInfo())
                    var daysOfWeek = json.getString("days_of_week")
                    daysOfWeek = daysOfWeek.replace("[", "")
                    daysOfWeek = daysOfWeek.replace("]", "")
                    val parts = daysOfWeek.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val cal = Calendar.getInstance()
                    cal.time = now
                    val dayNow = cal[Calendar.DAY_OF_WEEK]
                    val hourSt = json.getString("hour")
                    val minuteSt = json.getString("minute")
                    val secondSt = json.getString("second")
                    var hour = 0
                    try {
                        hour = hourSt.toInt()
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                    var minute = 0
                    try {
                        minute = minuteSt.toInt()
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                    var second = 0
                    try {
                        second = secondSt.toInt()
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                    val calender = Calendar.getInstance()
                    calender[Calendar.HOUR_OF_DAY] = hour
                    calender[Calendar.MINUTE] = minute
                    calender[Calendar.SECOND] = 0
                    calender[Calendar.MILLISECOND] = 0
                    val dateTime = calender.time
                    PreyLogger.d("TimeTrigger dateTime:$dateTime")
                    var valid = false
                    var i = 0
                    while (parts != null && i < parts.size) {
                        val daySt = parts[i]
                        val day = dayTrigger(daySt)
                        if (day == dayNow) {
                            valid = true
                            PreyLogger.d("TimeTrigger day==dayNow")
                        }
                        i++
                    }
                    if (valid) {
                        //increased to 15
                        valid = validDateAroundMinutes(dateTime, 15)
                        PreyLogger.d("TimeTrigger validDateAroundMinutes")
                    }
                    return valid
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            j++
        }
        return true
    }

    fun dayTrigger(daySt: String?): Int {
        var day = -1
        day = when (daySt) {
            "0" -> Calendar.SUNDAY
            "1" -> Calendar.MONDAY
            "2" -> Calendar.TUESDAY
            "3" -> Calendar.WEDNESDAY
            "4" -> Calendar.THURSDAY
            "5" -> Calendar.FRIDAY
            else -> Calendar.SATURDAY
        }
        return day
    }


    fun validDateAroundMinutes(date: Date, minutes: Int): Boolean {
        val now = Date()
        var valid = true
        val cal = Calendar.getInstance()
        cal.time = now
        cal.add(Calendar.MINUTE, -2)
        val lessMinutes = cal.time
        val datetime = date.time
        if (datetime < lessMinutes.time) {
            PreyLogger.d("less minutes")
            valid = false
        }
        cal.time = now
        cal.add(Calendar.MINUTE, minutes)
        val moreMinutes = cal.time
        if (datetime > moreMinutes.time) {
            PreyLogger.d("more minutes")
            valid = false
        }
        return valid
    }
}