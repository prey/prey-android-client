/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Tests for {@link LocationScheduleWindow}, the pure allowed-window evaluation
 * used by the daily-location feature. All times are interpreted in UTC.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LocationScheduleWindowTest {

    /** Builds a UTC Date for the given day-of-week/hour/minute in a fixed reference week. */
    private static Date utc(int year, int month, int day, int hour, int minute) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(year, month, day, hour, minute, 0);
        return c.getTime();
    }

    // 2026-07-05 is a Sunday, 2026-07-03 is a Friday,
    // 2026-07-04 is a Saturday, 2026-07-06 is a Monday.
    // Allowed: Sunday, Friday, Saturday between 10:01 and 11:59 UTC.
    private static final String CONFIG =
            "{\"start_at\":\"10:01\",\"end_at\":\"11:59\","
                    + "\"sunday\":true,\"monday\":false,\"tuesday\":false,\"wednesday\":false,"
                    + "\"thursday\":false,\"friday\":true,\"saturday\":true}";

    @Test
    public void nullConfigSendsAsap() {
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(null, utc(2026, 6, 6, 3, 0)));
    }

    @Test
    public void emptyConfigSendsAsap() {
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow("", utc(2026, 6, 6, 3, 0)));
    }

    @Test
    public void malformedConfigSendsAsap() {
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow("{not json", utc(2026, 6, 6, 3, 0)));
    }

    @Test
    public void allowedDayInsideHoursIsAllowed() {
        // Sunday 10:30 UTC
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(CONFIG, utc(2026, 6, 5, 10, 30)));
    }

    @Test
    public void disallowedDayIsBlockedEvenInsideHours() {
        // Monday 10:30 UTC — "monday":false
        assertFalse(LocationScheduleWindow.isWithinAllowedWindow(CONFIG, utc(2026, 6, 6, 10, 30)));
    }

    @Test
    public void beforeStartIsBlocked() {
        // Sunday 10:00 UTC — one minute before start 10:01
        assertFalse(LocationScheduleWindow.isWithinAllowedWindow(CONFIG, utc(2026, 6, 5, 10, 0)));
    }

    @Test
    public void afterEndIsBlocked() {
        // Sunday 12:00 UTC — one minute after end 11:59
        assertFalse(LocationScheduleWindow.isWithinAllowedWindow(CONFIG, utc(2026, 6, 5, 12, 0)));
    }

    @Test
    public void startBoundaryIsInclusive() {
        // Friday 10:01 UTC exactly
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(CONFIG, utc(2026, 6, 3, 10, 1)));
    }

    @Test
    public void endBoundaryIsInclusive() {
        // Saturday 11:59 UTC exactly
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(CONFIG, utc(2026, 6, 4, 11, 59)));
    }

    @Test
    public void daysOnlyConfigAllowsAnyTimeOnAllowedDay() {
        String daysOnly = "{\"sunday\":true,\"monday\":false}";
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(daysOnly, utc(2026, 6, 5, 3, 0)));
        assertFalse(LocationScheduleWindow.isWithinAllowedWindow(daysOnly, utc(2026, 6, 6, 3, 0)));
    }

    @Test
    public void hoursOnlyConfigAllowsAnyDayInsideHours() {
        String hoursOnly = "{\"start_at\":\"10:01\",\"end_at\":\"11:59\"}";
        // Monday is fine because no weekday flags are set, and 10:30 is inside hours
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(hoursOnly, utc(2026, 6, 6, 10, 30)));
        assertFalse(LocationScheduleWindow.isWithinAllowedWindow(hoursOnly, utc(2026, 6, 6, 9, 0)));
    }

    @Test
    public void dayNotPresentWhileOtherDaysSetIsBlocked() {
        // Only weekdays enabled; Sunday flag absent -> Sunday blocked.
        String weekdays = "{\"monday\":true,\"tuesday\":true,\"wednesday\":true,"
                + "\"thursday\":true,\"friday\":true}";
        assertFalse(LocationScheduleWindow.isWithinAllowedWindow(weekdays, utc(2026, 6, 5, 3, 0)));
        assertTrue(LocationScheduleWindow.isWithinAllowedWindow(weekdays, utc(2026, 6, 6, 3, 0)));
    }
}
