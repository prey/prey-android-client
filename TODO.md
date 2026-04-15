# TODO

- Replace the remaining background `startService()` calls that can still break on Android 8+ and especially 14/15:
  - `LocationUtil` -> `LocationService`
  - `AlarmReceiver` -> `ReportService`
  - lock/secure flows started from receivers and boot paths
- Rework `ReportService` away from `IntentService` to a modern background execution model.
- Review `PreyDisablePowerOptionsService` startup paths on Android 8/9 and remove legacy service starts where possible.
- Revisit the legacy C2DM unregister flow in `PreyConfig.unregisterC2dm()`.
