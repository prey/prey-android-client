/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import com.prey.PreyLogger

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * TimeTrigger is a utility object that provides functionality for working with time-based triggers.
 */
object TimeTrigger {
    var EXACT_TIME_FORMAT_SDF: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    var REPEAT_TIME_FORMAT_SDF: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
    var REPEAT_RANGE_TIME_FORMAT_SDF: SimpleDateFormat = SimpleDateFormat("HHmmss")

    const val REPEAT_TIME: String = "repeat_time"
    const val EXACT_TIME: String = "exact_time"
    const val RANGE_TIME: String = "range_time"
    const val REPEAT_RANGE_TIME: String = "repeat_range_time"

    const val ADD_TRIGGER_ID: Int = 1000

    /**
     * Updates a trigger with the given context and trigger data.
     *
     * @param context The application context.
     * @param trigger The trigger data to update.
     * @throws TriggerException If an error occurs during trigger update.
     */
    @Throws(TriggerException::class)
    fun updateTrigger(context: Context, trigger: TriggerDto) {
        val triggerName = trigger.getName()
        PreyLogger.d("TimeTrigger triggerName:${triggerName} id:${trigger.getId()}")
        val listEvents = TriggerParse.TriggerEvents(trigger.getEvents())
        val now = Date()
        PreyLogger.d("TimeTrigger now:${EXACT_TIME_FORMAT_SDF.format(now)}")
        var j = 0
        while (listEvents != null && j < listEvents.size) {
            val event = listEvents[j]
            PreyLogger.d("TimeTrigger event.type:${event.getType()}")
            if (REPEAT_RANGE_TIME == event.getType()) {
                try {
                    val json = JSONObject(event.getInfo())
                    var hour_from = ""
                    try {
                        hour_from = json.getString("hour_from")
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    var hour_until = ""
                    try {
                        hour_until = json.getString("hour_until")
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    var hourFrom: Date? = null
                    var hourUntil: Date? = null
                    if ("" != hour_from) {
                        try {
                            hourFrom = REPEAT_RANGE_TIME_FORMAT_SDF.parse(hour_from)
                        } catch (e: Exception) {
                            throw TriggerException(2, "Invalid trigger format")
                        }
                    }
                    if ("" != hour_until) {
                        try {
                            hourUntil = REPEAT_RANGE_TIME_FORMAT_SDF.parse(hour_until)
                        } catch (e: Exception) {
                            throw TriggerException(2, "Invalid trigger format")
                        }
                    }
                    val hourFromSt = REPEAT_RANGE_TIME_FORMAT_SDF.format(hourFrom)
                    val hourUntilSt = REPEAT_RANGE_TIME_FORMAT_SDF.format(hourUntil)
                    val hourFromInt = hourFromSt.toInt()
                    val hourUntilInt = hourUntilSt.toInt()
                    if (hourFromInt > hourUntilInt) {
                        throw TriggerException(4, "The execution range dates doesn't make sense")
                    }
                    var dateUntil: Date? = null
                    var until = ""
                    try {
                        until = json.getString("until")
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    if ("" != until) {
                        try {
                            dateUntil = REPEAT_TIME_FORMAT_SDF.parse(until)
                        } catch (e: Exception) {
                            throw TriggerException(2, "Invalid trigger format")
                        }
                    }
                    val dateUntilSt = REPEAT_TIME_FORMAT_SDF.format(dateUntil)
                    val dateNowSt = REPEAT_TIME_FORMAT_SDF.format(now)

                    val intUntil = dateUntilSt.toInt()
                    val intNow = dateNowSt.toInt()
                    if (dateUntil != null && intNow > intUntil) {
                        throw TriggerException(4, "The execution range dates doesn't make sense")
                    }
                } catch (te: TriggerException) {
                    throw te
                } catch (e: Exception) {
                    PreyLogger.e("error:${e.message}", e)
                    throw TriggerException(0, "Unknown error:${e.message}")
                }
            }
            if (RANGE_TIME == event.getType()) {
                try {
                    val json = JSONObject(event.getInfo())
                    var from = ""
                    try {
                        from = json.getString("from")
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    var until = ""
                    try {
                        until = json.getString("until")
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    var dateFrom: Date? = null
                    var dateUntil: Date? = null
                    if ("" != from) {
                        try {
                            dateFrom = REPEAT_TIME_FORMAT_SDF.parse(from)
                        } catch (e: Exception) {
                            throw TriggerException(2, "Invalid trigger format")
                        }
                    }
                    if ("" != until) {
                        try {
                            dateUntil = REPEAT_TIME_FORMAT_SDF.parse(until)
                        } catch (e: Exception) {
                            throw TriggerException(2, "Invalid trigger format")
                        }
                    }
                    if (dateFrom != null && dateUntil != null) {
                        val dateUntilSt = REPEAT_TIME_FORMAT_SDF.format(dateUntil)
                        val dateNowSt = REPEAT_TIME_FORMAT_SDF.format(now)
                        val dateFromSt = REPEAT_TIME_FORMAT_SDF.format(dateFrom)
                        val intUntil = dateUntilSt.toInt()
                        val intNow = dateNowSt.toInt()
                        val intFrom = dateFromSt.toInt()
                        PreyLogger.d("Trigger TimeTrigger intUntil:$intUntil")
                        PreyLogger.d("Trigger TimeTrigger intNow:$intNow")
                        PreyLogger.d("Trigger TimeTrigger intFrom:$intFrom")
                        if (intNow > intUntil) {
                            throw TriggerException(
                                4,
                                "The execution range dates doesn't make sense"
                            )
                        }
                        if (intFrom > intUntil) {
                            throw TriggerException(
                                4,
                                "The execution range dates doesn't make sense"
                            )
                        }
                    } else {
                        throw TriggerException(
                            0,
                            "Unknown error from:$from or until:$until"
                        )
                    }
                } catch (te: TriggerException) {
                    throw te
                } catch (e: Exception) {
                    PreyLogger.e("error:${e.message}", e)
                    throw TriggerException(0, "Unknown error:${e.message}")
                }
            }
            if (EXACT_TIME == event.getType()) {
                try {
                    val json = JSONObject(event.getInfo())
                    var dateTime: String? = null
                    dateTime = json.getString("date")
                    PreyLogger.d("TimeTrigger dateTime:$dateTime")
                    val date = EXACT_TIME_FORMAT_SDF.parse(dateTime)
                    if (now.time <= date.time) {
                        PreyLogger.d("TimeTrigger format:${EXACT_TIME_FORMAT_SDF.format(date)}")
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = date.time
                        PreyLogger.d("TimeTrigger format:${EXACT_TIME_FORMAT_SDF.format(calendar.time)}")
                        val intent = Intent(context, TimeTriggerReceiver::class.java)
                        intent.putExtra("trigger_id", "${trigger.getId()}")
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            (trigger.getId() + ADD_TRIGGER_ID),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val alarmManager =
                            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            PreyLogger.d("TimeTrigger----------set")
                            alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] =
                                pendingIntent
                        } else {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                PreyLogger.d("TimeTrigger----------setExact")
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    pendingIntent
                                )
                            } else {
                                PreyLogger.d("TimeTrigger----------setExactAndAllowWhileIdle")
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    pendingIntent
                                )
                            }
                        }
                    } else {
                        PreyLogger.d("Trigger TimeTrigger----------Date is less ")
                        throw TriggerException(3, "Expired trigger")
                    }
                } catch (te: TriggerException) {
                    throw te
                } catch (e: Exception) {
                    PreyLogger.e("error:${e.message}", e)
                    throw TriggerException(0, "Unknown error:${e.message}")
                }
            }
            if (REPEAT_TIME == event.getType()) {
                try {
                    val json = JSONObject(event.getInfo())
                    var until = ""
                    try {
                        until = json.getString("until")
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    if ("" != until) {
                        var dateUntil: Date? = null
                        try {
                            dateUntil = REPEAT_TIME_FORMAT_SDF.parse(until)
                        } catch (e: Exception) {
                            throw TriggerException(2, "Invalid trigger format")
                        }
                        val dateUntilSt = REPEAT_TIME_FORMAT_SDF.format(dateUntil)
                        val dateNowSt = REPEAT_TIME_FORMAT_SDF.format(now)
                        val intUntil = dateUntilSt.toInt()
                        val intNow = dateNowSt.toInt()
                        PreyLogger.d("Trigger TimeTrigger intUntil:$intUntil")
                        PreyLogger.d("Trigger TimeTrigger intNow:$intNow")
                        if (dateUntil != null && intNow > intUntil) {
                            PreyLogger.d("Trigger TimeTrigger The execution range dat")
                            throw TriggerException(
                                4,
                                "The execution range dates doesn't make sense"
                            )
                        }
                    }
                    val hourSt = json.getString("hour")
                    val minuteSt = json.getString("minute")
                    val secondSt = json.getString("second")
                    PreyLogger.d("Trigger TimeTrigger hour:$hourSt minute:$minuteSt second:$secondSt until:$until")
                    var hour = 0
                    try {
                        hour = hourSt.toInt()
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    var minute = 0
                    try {
                        minute = minuteSt.toInt()
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    var second = 0
                    try {
                        second = secondSt.toInt()
                    } catch (e: Exception) {
                        PreyLogger.e("Error: ${e.message}", e)
                    }
                    val myIntent = Intent(context, TimeTriggerReceiver::class.java)
                    myIntent.putExtra("trigger_id", "${trigger.getId()}")
                    var daysOfWeek = json.getString("days_of_week")
                    daysOfWeek = daysOfWeek.replace("[", "")
                    daysOfWeek = daysOfWeek.replace("]", "")
                    val parts = daysOfWeek.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    var i = 0
                    while (parts != null && i < parts.size) {
                        val daySt = parts[i]
                        val day = TriggerUtil.dayTrigger(daySt)
                        val calender = Calendar.getInstance()
                        calender[Calendar.HOUR_OF_DAY] = hour
                        calender[Calendar.MINUTE] = minute
                        calender[Calendar.SECOND] = 0
                        calender[Calendar.MILLISECOND] = 0
                        PreyLogger.d("Trigger TimeTrigger array DAY_OF_WEEK:$day")
                        calender[Calendar.DAY_OF_WEEK] = day
                        val intent = Intent(context, TimeTriggerReceiver::class.java)
                        intent.putExtra("trigger_id", "${trigger.getId()}")
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            (trigger.getId() * ADD_TRIGGER_ID + day),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val alarmManager =
                            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calender.timeInMillis,
                            AlarmManager.INTERVAL_DAY * 7,
                            pendingIntent
                        )
                        i++
                    }
                } catch (te: TriggerException) {
                    throw te
                } catch (e: Exception) {
                    PreyLogger.e("error:${e.message}", e)
                    TriggerException(0, "Unknown error:${e.message}")
                }
            }
            j++
        }
    }

}