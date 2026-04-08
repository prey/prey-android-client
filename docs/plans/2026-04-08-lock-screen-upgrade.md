# Lock Screen Upgrade: startLockTask() + AccessibilityService

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the current overlay-based lock screen with a `startLockTask()`-based approach that blocks Home, Recents, and status bar at the OS level — eliminating all current bypass vectors.

**Architecture:** A new `LockScreenActivity` (Kotlin) calls `startLockTask()` to pin itself, reusing the existing WebView+HTML lock UI for password entry. A new `SecurityAccessibilityService` (Kotlin) replaces `AppAccessibilityService` with tighter package filtering. The existing Java code (`PreyDeviceAdmin`, `Lock.java`, `WebAppInterface.java`) is modified minimally to call the new Kotlin classes. New files are written in Kotlin to begin the project's Kotlin migration.

**Tech Stack:** Kotlin + Java (coexisting), Android `startLockTask()` API (available since API 21), `AccessibilityService`, `DevicePolicyManager`, existing WebView HTML lock UI.

---

## Task 0: Configure Kotlin in the project

The project has no Kotlin configured yet. We need to add the Kotlin Gradle plugin.

**Files:**
- Modify: `build.gradle` (root)
- Modify: `app/build.gradle`

**Step 1: Add Kotlin plugin to root build.gradle**

In `build.gradle` (root), add the Kotlin classpath to `buildscript.dependencies`:

```groovy
classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22'
```

**Step 2: Apply Kotlin plugin in app/build.gradle**

In `app/build.gradle`, add after line 1 (`apply plugin: 'com.android.application'`):

```groovy
apply plugin: 'org.jetbrains.kotlin.android'
```

And add to `dependencies` block:

```groovy
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.22'
```

**Step 3: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add build.gradle app/build.gradle
git commit -m "chore: add Kotlin plugin and stdlib to enable Kotlin in project"
```

---

## Task 1: Update minSdk to 24

**Files:**
- Modify: `app/build.gradle:18`

**Step 1: Change minSdk**

In `app/build.gradle`, change line 18:
```
-        minSdk 21
+        minSdk 24
```

**Step 2: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/build.gradle
git commit -m "chore: raise minSdk from 21 to 24 (Android 7 Nougat)"
```

---

## Task 2: Create LockScreenActivity (Kotlin)

This is the core replacement. It uses `startLockTask()` to pin the screen and displays the existing WebView lock UI.

**Files:**
- Create: `app/src/main/java/com/prey/activities/LockScreenActivity.kt`
- Create: `app/src/main/res/layout/activity_lock_screen.xml`

**Step 1: Create the layout**

Create `app/src/main/res/layout/activity_lock_screen.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FF000000">

    <WebView
        android:id="@+id/lock_webview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
```

**Step 2: Create LockScreenActivity.kt**

Create `app/src/main/java/com/prey/activities/LockScreenActivity.kt`:

```kotlin
package com.prey.activities

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.R
import com.prey.activities.js.CustomWebView
import com.prey.activities.js.WebAppInterface
import com.prey.receivers.PreyDeviceAdmin

class LockScreenActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_lock_screen)

        val unlockPass = PreyConfig.getPreyConfig(this).unlockPass
        if (unlockPass.isNullOrEmpty()) {
            finish()
            return
        }

        setupWebView()
        startLockTaskIfPossible()
    }

    private fun setupWebView() {
        webView = findViewById(R.id.lock_webview)
        webView.setOnKeyListener { view, _, keyEvent ->
            CustomWebView.callDispatchKeyEvent(applicationContext, keyEvent)
            false
        }

        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
        }
        webView.setBackgroundColor(0x00000000)

        val lng = PreyUtils.getLanguage()
        val lockMessage = PreyConfig.getPreyConfig(this).lockMessage
        val route = if (!lockMessage.isNullOrEmpty()) "lockmessage" else "lock"
        val url = "${CheckPasswordHtmlActivity.URL_ONB}#/$lng/$route"

        webView.addJavascriptInterface(
            WebAppInterface(this, this),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        webView.loadUrl(url)
    }

    private fun startLockTaskIfPossible() {
        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val adminComponent = ComponentName(this, PreyDeviceAdmin::class.java)

            if (dpm != null && dpm.isDeviceOwnerApp(packageName)) {
                dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
                startLockTask()
                PreyLogger.d("LockScreenActivity: startLockTask (Device Owner)")
            } else {
                startLockTask()
                PreyLogger.d("LockScreenActivity: startLockTask (standard)")
            }
        } catch (e: Exception) {
            PreyLogger.e("LockScreenActivity: startLockTask failed: ${e.message}", e)
        }
    }

    /**
     * Called by WebAppInterface after successful password verification.
     * Must stop lock task before finishing the activity.
     */
    fun unlockAndFinish() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null && am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                stopLockTask()
            }
        } catch (e: Exception) {
            PreyLogger.e("LockScreenActivity: stopLockTask failed: ${e.message}", e)
        }
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        val unlockPass = PreyConfig.getPreyConfig(this).unlockPass
        if (unlockPass.isNullOrEmpty()) {
            try { stopLockTask() } catch (_: Exception) {}
            finishAffinity()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back button while locked
    }
}
```

**Step 3: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/com/prey/activities/LockScreenActivity.kt
git add app/src/main/res/layout/activity_lock_screen.xml
git commit -m "feat(lock): add LockScreenActivity (Kotlin) with startLockTask() screen pinning"
```

---

## Task 3: Register LockScreenActivity in AndroidManifest.xml

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

**Step 1: Add LockScreenActivity declaration**

Add after the `PasswordHtmlActivity` declaration (after line 272):

```xml
        <activity
                android:launchMode="singleInstance"
            android:noHistory="true"
            android:lockTaskMode="if_whitelisted"
            android:excludeFromRecents="true"
            android:windowSoftInputMode="stateHidden|adjustResize"
            android:configChanges="keyboardHidden|orientation"
                android:name=".activities.LockScreenActivity" />
```

Key attributes:
- `android:lockTaskMode="if_whitelisted"` — enables `startLockTask()` without confirmation when Device Owner has whitelisted the package
- `android:excludeFromRecents="true"` — hides from Recents screen
- `android:singleInstance` — only one instance at a time

**Step 2: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "feat(lock): register LockScreenActivity in manifest with lockTaskMode"
```

---

## Task 4: Create SecurityAccessibilityService (Kotlin)

Replaces `AppAccessibilityService` with tighter filtering — blocks Settings and System UI navigation too.

**Files:**
- Create: `app/src/main/java/com/prey/services/SecurityAccessibilityService.kt`
- Create: `app/src/main/res/xml/security_accessibility_service.xml`

**Step 1: Create the accessibility service config**

Create `app/src/main/res/xml/security_accessibility_service.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:notificationTimeout="100"
    android:description="@string/permission_summary" />
```

Key differences from current `accessibilityservice.xml`:
- `typeWindowStateChanged` instead of `typeAllMask` — only fires when windows change, more efficient
- `notificationTimeout="100"` — 100ms debounce to avoid event storms

**Step 2: Create SecurityAccessibilityService.kt**

Create `app/src/main/java/com/prey/services/SecurityAccessibilityService.kt`:

```kotlin
package com.prey.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.activities.LockScreenActivity

class SecurityAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val unlockPass = PreyConfig.getPreyConfig(applicationContext).unlockPass
            if (unlockPass.isNullOrEmpty()) return

            val packageName = event?.packageName?.toString() ?: return

            if (!packageName.startsWith("com.prey")) {
                PreyLogger.d("SecurityAccessibilityService: blocked $packageName, relaunching lock")
                val intent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                applicationContext.startActivity(intent)
            }
        } catch (e: Exception) {
            PreyLogger.e("SecurityAccessibilityService error: ${e.message}", e)
        }
    }

    override fun onInterrupt() {}
}
```

Key improvements over `AppAccessibilityService`:
- Does NOT whitelist `"android"` package — blocks Settings, System UI, etc.
- Only allows `com.prey*` packages
- Uses `FLAG_ACTIVITY_CLEAR_TOP` to bring existing lock to front instead of creating new instances

**Step 3: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/com/prey/services/SecurityAccessibilityService.kt
git add app/src/main/res/xml/security_accessibility_service.xml
git commit -m "feat(lock): add SecurityAccessibilityService (Kotlin) with strict package filtering"
```

---

## Task 5: Register SecurityAccessibilityService in AndroidManifest.xml

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

**Step 1: Add SecurityAccessibilityService declaration**

Add after the existing `AppAccessibilityService` block (after line 478):

```xml
        <service
                android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:enabled="true"
            android:exported="true"
                android:name=".services.SecurityAccessibilityService">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService"/>
            </intent-filter>
            <meta-data android:name="android.accessibilityservice"
                android:resource="@xml/security_accessibility_service"/>
        </service>
```

**Step 2: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "feat(lock): register SecurityAccessibilityService in manifest"
```

---

## Task 6: Wire Lock.java to use LockScreenActivity

This is the critical routing change — `Lock.lock()` now launches `LockScreenActivity` instead of `PreyLockHtmlService`. This file stays in Java since it's deeply integrated with the existing action system.

**Files:**
- Modify: `app/src/main/java/com/prey/json/actions/Lock.java`

**Step 1: Update imports**

Add to imports section:
```java
import com.prey.activities.LockScreenActivity;
```

**Step 2: Replace the lock() method body**

Replace the `lock()` method (lines 175-231) with:

```java
    public void lock(final Context ctx, String unlock, final String messageId, final String reason, String device_job_id) {
        PreyLogger.d(String.format("lock unlock:%s messageId:%s reason:%s", unlock, messageId, reason));
        PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
        PreyConfig.getPreyConfig(ctx).setLock(true);

        // Launch LockScreenActivity with startLockTask() — primary lock mechanism
        Intent lockIntent = new Intent(ctx, LockScreenActivity.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(lockIntent);

        // Also start the monitoring service as backup
        Intent intentCheckLock = new Intent(ctx, CheckLockActivated.class);
        ctx.startService(intentCheckLock);

        // Lock device screen immediately
        FroyoSupport.getInstance(ctx).lockNow();

        // Notify server
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "lock", "started", reason));
                } catch (Exception e) {
                    PreyLogger.e("Error sendNotifyAction:" + e.getMessage(), e);
                }
            }
        }).start();
    }
```

**Step 3: Update stop() method to handle LockScreenActivity**

In the `stop()` method, replace the block from line 105 to line 158 with:

```java
            Thread.sleep(1000);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "lock", "stopped", reason));
            Thread.sleep(2000);

            // Close LockScreenActivity (it checks unlockPass in onResume and finishes)
            Intent intentClose = new Intent(ctx, CloseActivity.class);
            intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intentClose);
```

**Step 4: Update sendUnLock()**

Replace the `sendUnLock()` method (lines 233-265) with:

```java
    public static void sendUnLock(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                String unlockPass = PreyConfig.getPreyConfig(context).getUnlockPass();
                PreyLogger.d("sendUnLock unlockPass:" + unlockPass);
                if (unlockPass != null && !"".equals(unlockPass)) {
                    PreyConfig.getPreyConfig(context).setUnlockPass("");
                    PreyConfig.getPreyConfig(context).setLock(false);
                    Intent intentClose = new Intent(context, CloseActivity.class);
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentClose);
                    final Context ctx = context;
                    new Thread() {
                        public void run() {
                            String jobIdLock = PreyConfig.getPreyConfig(ctx).getJobIdLock();
                            String reason = "{\"origin\":\"user\"}";
                            if (jobIdLock != null && !"".equals(jobIdLock)) {
                                reason = "{\"origin\":\"user\",\"device_job_id\":\"" + jobIdLock + "\"}";
                                PreyConfig.getPreyConfig(ctx).setJobIdLock("");
                            }
                            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped", reason));
                        }
                    }.start();
                }
            }
        }).start();
    }
```

**Step 5: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add app/src/main/java/com/prey/json/actions/Lock.java
git commit -m "feat(lock): route lock action to LockScreenActivity instead of overlay service"
```

---

## Task 7: Update WebAppInterface to call unlockAndFinish()

The JS bridge `lock()` method needs to call `LockScreenActivity.unlockAndFinish()` instead of manually removing overlay views.

**Files:**
- Modify: `app/src/main/java/com/prey/activities/js/WebAppInterface.java`

**Step 1: Update the lock() method**

Add import at top of file:
```java
import com.prey.activities.LockScreenActivity;
```

In the `lock(String key)` method (lines 593-668), replace the success block (lines 604-662) with:

```java
        if (unlock != null && !"".equals(unlock) && unlock.equals(key)) {
            PreyConfig.getPreyConfig(mContext).setInputWebview("");
            PreyConfig.getPreyConfig(ctx).setUnlockPass("");
            PreyConfig.getPreyConfig(ctx).setLock(false);
            PreyConfig.getPreyConfig(ctx).setOpenSecureService(false);

            // Notify server
            new Thread() {
                public void run() {
                    String jobIdLock = PreyConfig.getPreyConfig(ctx).getJobIdLock();
                    String reason = "{\"origin\":\"user\"}";
                    if (jobIdLock != null && !"".equals(jobIdLock)) {
                        reason = "{\"origin\":\"user\",\"device_job_id\":\"" + jobIdLock + "\"}";
                        PreyConfig.getPreyConfig(ctx).setJobIdLock("");
                    }
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped", reason));
                }
            }.start();

            try { Thread.sleep(1000); } catch (Exception e) { }

            // If running inside LockScreenActivity, call unlockAndFinish to stop lock task
            if (mContext instanceof LockScreenActivity) {
                ((LockScreenActivity) mContext).unlockAndFinish();
            } else if (mContext instanceof PasswordHtmlActivity) {
                ((PasswordHtmlActivity) mContext).pfinish();
            } else {
                // Overlay service fallback
                if (preyLockHtmlService != null) {
                    try {
                        preyLockHtmlService.stop();
                        View viewLock = PreyConfig.getPreyConfig(ctx).viewLock;
                        if (viewLock != null) {
                            WindowManager wm = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
                            wm.removeView(viewLock);
                        }
                    } catch (Exception e) {
                        PreyLogger.e("Error removing overlay: " + e.getMessage(), e);
                    }
                }
                Intent intentClose = new Intent(ctx, CloseActivity.class);
                intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intentClose);
            }
            error2 = "{\"ok\":\"ok\"}";
        }
```

**Step 2: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/com/prey/activities/js/WebAppInterface.java
git commit -m "feat(lock): update WebAppInterface to use LockScreenActivity.unlockAndFinish()"
```

---

## Task 8: Update CheckLockActivated to relaunch LockScreenActivity

The monitoring service should relaunch `LockScreenActivity` if it detects the lock escaped.

**Files:**
- Modify: `app/src/main/java/com/prey/services/CheckLockActivated.java`

**Step 1: Replace the service implementation**

Add import:
```java
import com.prey.activities.LockScreenActivity;
```

Replace the full `onStart()` method (lines 28-54) with:

```java
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        new Thread() {
            public void run() {
                boolean run = true;
                while (run) {
                    String unlockPass = PreyConfig.getPreyConfig(getApplicationContext()).getUnlockPass();
                    if (unlockPass == null || "".equals(unlockPass)) {
                        run = false;
                        stopSelf();
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                        // Re-launch LockScreenActivity if it's not in foreground
                        Intent lockIntent = new Intent(ctx, LockScreenActivity.class);
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ctx.startActivity(lockIntent);
                    } catch (Exception e) {
                        PreyLogger.e("CheckLockActivated Error:" + e.getMessage(), e);
                    }
                }
            }
        }.start();
    }
```

**Step 2: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/com/prey/services/CheckLockActivated.java
git commit -m "feat(lock): update CheckLockActivated to relaunch LockScreenActivity"
```

---

## Task 9: Verify full build and test on device

**Step 1: Full clean build**

Run: `./gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 2: Manual test checklist**

1. Install on test device
2. Trigger lock from Prey panel with a test password
3. Verify lock screen appears full-screen
4. Verify Home button does NOT dismiss the lock
5. Verify Recents button does NOT show task switcher
6. Verify status bar cannot be pulled down
7. Enter wrong password — verify error message
8. Enter correct password — verify device unlocks
9. Trigger lock again, then send stop from Prey panel — verify remote unlock works
10. Reboot device while locked — verify lock re-engages on boot (via PreyBootController)

**Step 3: Final commit**

```bash
git add -A
git commit -m "feat(lock): complete startLockTask() lock screen implementation"
```

---

## Summary of Changes

| Action | File | Language | Purpose |
|--------|------|----------|---------|
| **CREATE** | `LockScreenActivity.kt` | Kotlin | New pinned lock screen activity |
| **CREATE** | `activity_lock_screen.xml` | XML | Layout for lock activity |
| **CREATE** | `SecurityAccessibilityService.kt` | Kotlin | Stricter accessibility service |
| **CREATE** | `security_accessibility_service.xml` | XML | Config for new accessibility service |
| **MODIFY** | `build.gradle` (root) | Groovy | Add Kotlin plugin |
| **MODIFY** | `app/build.gradle` | Groovy | Apply Kotlin plugin, add stdlib, minSdk 24 |
| **MODIFY** | `AndroidManifest.xml` | XML | Register new activity + service |
| **MODIFY** | `Lock.java` | Java | Route to LockScreenActivity |
| **MODIFY** | `WebAppInterface.java` | Java | Call unlockAndFinish() |
| **MODIFY** | `CheckLockActivated.java` | Java | Relaunch LockScreenActivity |
| **KEEP** | `PreyDeviceAdmin.java` | Java | No changes needed |
| **KEEP** | `FroyoSupport.java` | Java | Still used for lockNow() |
| **KEEP** | `PreyConfig.java` | Java | Unchanged password storage |
| **DEPRECATED** | `PreyLockHtmlService.java` | Java | Replaced by LockScreenActivity (not deleted) |
| **DEPRECATED** | `AppAccessibilityService.java` | Java | Replaced by SecurityAccessibilityService (not deleted) |
| **DEPRECATED** | `PasswordHtmlActivity.java` | Java | Kept as fallback in WebAppInterface |
