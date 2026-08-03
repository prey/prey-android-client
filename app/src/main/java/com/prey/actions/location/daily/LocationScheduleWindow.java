/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily;

import com.prey.PreyLogger;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Pure evaluation of the server-configured allowed window for the daily location.
 * <p>
 * The configuration comes from {@code settings.local.location_schedule} in
 * {@code /devices/:key/status.json} and has the shape:
 * <pre>
 * {
 *   "start_at": "10:00", "end_at": "11:00",
 *   "sunday": false, "monday": true, "tuesday": true, "wednesday": true,
 *   "thursday": true, "friday": true, "saturday": false
 * }
 * </pre>
 * where each weekday is a boolean flag and {@code start_at}/{@code end_at} are
 * {@code "HH:mm"}. Both days and hours are evaluated in <b>UTC</b> and the hour range
 * is inclusive on both ends.
 * <p>
 * When the configuration is absent, empty or malformed, the location may be sent at any
 * time (fail-open) so the daily-location feature keeps working when no window is set.
 */
public class LocationScheduleWindow {

    /** UTC day-of-week keys, indexed so {@code Calendar.SUNDAY - 1 == 0}. */
    private static final String[] DAY_KEYS = {
            "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"
    };

    private LocationScheduleWindow() {
    }

    /**
     * Decides whether a location may be acquired/sent at {@code now} given the schedule.
     *
     * @param scheduleJson the raw {@code location_schedule} JSON, or null/empty when unset
     * @param now          the instant to evaluate (interpreted in UTC)
     * @return true when there is no restriction or {@code now} falls inside the window
     */
    public static boolean isWithinAllowedWindow(String scheduleJson, Date now) {
        if (scheduleJson == null || scheduleJson.trim().isEmpty()) {
            return true;
        }
        try {
            JSONObject schedule = new JSONObject(scheduleJson);
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            calendar.setTime(now);
            return isDayAllowed(schedule, calendar) && isTimeAllowed(schedule, calendar);
        } catch (Exception e) {
            PreyLogger.d(String.format("DAILY window malformed schedule, sending ASAP:%s", e.getMessage()));
            return true;
        }
    }

    /**
     * @return true when the schedule carries no weekday flags at all (any day), or the flag
     * for the current UTC day-of-week is present and {@code true}. A weekday flag that is
     * present and {@code false}, or absent while other weekdays are set, blocks that day.
     */
    private static boolean isDayAllowed(JSONObject schedule, Calendar calendar) {
        boolean hasAnyDayFlag = false;
        for (String dayKey : DAY_KEYS) {
            if (schedule.has(dayKey)) {
                hasAnyDayFlag = true;
                break;
            }
        }
        if (!hasAnyDayFlag) {
            return true;
        }
        String today = DAY_KEYS[calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY]; // SUNDAY == 1
        return schedule.optBoolean(today, false);
    }

    /**
     * @return true when {@code start_at}/{@code end_at} are missing/unparseable, or
     * {@code start_at <= now <= end_at} in UTC minutes-of-day (inclusive).
     */
    private static boolean isTimeAllowed(JSONObject schedule, Calendar calendar) {
        int start = parseMinuteOfDay(schedule.optString("start_at", null));
        int end = parseMinuteOfDay(schedule.optString("end_at", null));
        if (start < 0 || end < 0) {
            return true;
        }
        int nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        return nowMinutes >= start && nowMinutes <= end;
    }

    /**
     * Parses a {@code "HH:mm"} string into minutes since midnight.
     *
     * @return minutes-of-day, or -1 when the value is missing or unparseable
     */
    private static int parseMinuteOfDay(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }
        String[] parts = value.trim().split(":");
        if (parts.length != 2) {
            return -1;
        }
        try {
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return -1;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
