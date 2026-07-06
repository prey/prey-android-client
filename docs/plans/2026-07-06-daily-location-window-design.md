# Daily location: UTC scheduling, allowed windows, and hardening

Date: 2026-07-06

## Problem

The daily-location feature (`com.prey.actions.location.daily`) captures one location per
day and posts it to the server. Today it:

- Schedules an `AlarmManager.setInexactRepeating` alarm every 15 min, which Doze throttles.
- Uses a **device-local** `yyyy-MM-dd` guard to decide "already sent today".
- Sends the first non-zero fix regardless of accuracy, within a ~6 s window.
- Has no notion of *when* a customer wants the location taken.
- Has minor hygiene issues (`throw new RuntimeException`, a duplicated `setLastLocation(null)`).

## Goals

1. Survive Doze so the daily point is not silently dropped on idle devices.
2. Evaluate the "already sent today" boundary in **UTC**.
3. Restrict acquisition/send to server-configured **allowed windows** (days + hours), in UTC.
   With no config, send daily ASAP (current behaviour).
4. Improve the fix (accuracy preference with fallback) and clean up error handling.

## Config source and shape

`PreyStatus.initConfig()` already fetches `/devices/:key/status.json`. The window config lives at
`settings.local.location_schedule`:

```json
{
  "start_at": "10:00", "end_at": "11:00",
  "sunday": false, "monday": true, "tuesday": true, "wednesday": true,
  "thursday": true, "friday": true, "saturday": false
}
```

- Each weekday is a boolean flag, evaluated in UTC. If the config carries no weekday flags at
  all, any day is allowed; otherwise only days whose flag is present and `true` are allowed.
- `start_at` / `end_at`: `"HH:mm"` in UTC, **inclusive** on both ends.
- Missing / null / malformed → treated as "no restriction" (send ASAP). Fail-open.

The raw JSON string is persisted verbatim in `PreyConfig` under key `LOCATION_SCHEDULE`
(empty string when absent).

## Design

### 1. Scheduling — self-rescheduling, Doze-resilient (`LocationScheduled`)

Replace `setInexactRepeating` with a single alarm that re-arms itself:

- API 23+: `setAndAllowWhileIdle(RTC_WAKEUP, triggerAt, pi)` — fires in Doze, no exact-alarm permission.
- API 21–22: `setExact(RTC_WAKEUP, triggerAt, pi)` — Doze doesn't exist pre-M.

`run(ctx)` (called from `PreyApp` at start) arms an immediate first fire.
`AlarmLocationReceiver.onReceive` re-arms the next fire at `now + 15 min`, then runs `DailyLocation`.
A fixed request code + `FLAG_UPDATE_CURRENT` keeps at most one pending alarm.

### 2. UTC day guard (`DailyLocation`)

A dedicated UTC `yyyy-MM-dd` formatter (leaving the shared `FORMAT_SDF_AWARE` used by the
Aware system untouched). `DAILY_LOCATION` stores the UTC date → one send per UTC calendar day.

### 3. Allowed-window restriction (`LocationScheduleWindow` — new, pure, unit-tested)

`static boolean isWithinAllowedWindow(String scheduleJson, Date now)`:

- Empty/absent/malformed → `true`.
- Any weekday flag present → the current UTC day's flag must be `true`; if no flags present, any day.
- `start_at`/`end_at` present → `startMin <= nowMin <= endMin` (UTC minutes-of-day); else any time.
- Constraints AND together.

`DailyLocation.run` guard becomes:
`!alreadySentTodayUtc && !airplaneMode && isWithinAllowedWindow(schedule, now)`.

### 4. Acquisition + hygiene

- Keep the best (lowest-accuracy) non-zero fix across attempts; break early once accuracy is
  good enough; after the loop send the best fix obtained (never skip a day just because the
  fix is mediocre).
- Replace `throw new RuntimeException(e)` with logging. Remove the duplicated `setLastLocation(null)`.

## Testing

Robolectric tests for `LocationScheduleWindow` (org.json needs Robolectric): no-config,
day in/out, time before/in/after, inclusive boundaries, malformed → ASAP, and a fixed-epoch
UTC-correctness case.

## Decisions

- Malformed config **fails open** (sends ASAP) rather than blocking sends.
- Accuracy is a **preference with fallback**, not a hard reject.
