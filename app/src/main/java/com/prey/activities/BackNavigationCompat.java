/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.os.Build;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.RequiresApi;

/**
 * Routes the system back action to a handler on every API level the app supports.
 * <p>
 * The manifest sets {@code android:enableOnBackInvokedCallback="true"}, which means
 * that from Android 13 on the platform dispatches back through
 * {@link OnBackInvokedDispatcher} and stops calling {@code Activity.onBackPressed()}.
 * Activities that only overrode {@code onBackPressed} were therefore doing nothing
 * on modern devices — including the ones whose empty override existed to block back.
 * <p>
 * Activities built on AndroidX ({@code AppCompatActivity}, {@code FragmentActivity})
 * should use {@code getOnBackPressedDispatcher().addCallback(...)} instead, which
 * covers both paths. This helper exists for the activities that still extend the
 * platform {@link Activity} directly and have no such dispatcher: they register here
 * for Android 13+ and keep their {@code onBackPressed} override for older releases,
 * where {@code enableOnBackInvokedCallback} is ignored.
 */
public final class BackNavigationCompat {

    private BackNavigationCompat() {
    }

    /**
     * Registers {@code onBack} as the back handler on Android 13 and later. On older
     * releases this is a no-op and the caller's {@code onBackPressed} override keeps
     * handling back.
     *
     * @param activity the activity whose back action is being handled
     * @param onBack   what to run when back is invoked; do nothing to block back
     */
    public static void register(Activity activity, Runnable onBack) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Api33.register(activity, onBack);
        }
    }

    /**
     * Isolated so the API 33 types are only ever loaded on devices that have them.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private static final class Api33 {

        private Api33() {
        }

        static void register(Activity activity, Runnable onBack) {
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBack::run);
        }
    }
}
