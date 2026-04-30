/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.json.actions.Location;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Side-channel that fires a Location report when high-signal actions
 * (alert, alarm, lock) execute and the device hasn't already pushed a
 * fresh fix recently. The panel cares about device location alongside
 * those events; rather than asking the operator to also issue a "get
 * location" command, we attach a freshness check at the trigger sites.
 *
 * <p>Freshness is measured by {@link PreyConfig#getLastLocationSentAt()},
 * which {@link Location#get} stamps only after a successful upload to the
 * server. If the server is down or GPS keeps failing, we will retry on
 * every trigger — by design.
 */
public final class LocationTracker {

    static final long FRESHNESS_WINDOW_MS = 5L * 60L * 1000L;

    /**
     * Guard against three triggers (alert + alarm + lock) all kicking off
     * a fetch in the same second. Cleared in the worker thread's finally.
     */
    private static final AtomicBoolean inFlight = new AtomicBoolean(false);

    private LocationTracker() {
    }

    /**
     * Fires {@link Location#get} in a background thread when the cached
     * "last sent" timestamp is older than {@link #FRESHNESS_WINDOW_MS}.
     * Returns immediately — the caller never blocks on the GPS fetch.
     */
    public static void maybeSendRecentLocation(final Context ctx) {
        long now = clockProvider.now();
        long last = PreyConfig.getPreyConfig(ctx).getLastLocationSentAt();
        if (last > 0 && now - last < FRESHNESS_WINDOW_MS) {
            PreyLogger.d("LocationTracker: location fresh ("
                    + (now - last) + "ms < " + FRESHNESS_WINDOW_MS + "ms), skipping");
            return;
        }
        if (!inFlight.compareAndSet(false, true)) {
            PreyLogger.d("LocationTracker: another fetch in flight, skipping");
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fetcher.fetch(ctx);
                } catch (Exception e) {
                    PreyLogger.e("LocationTracker fetch error: " + e.getMessage(), e);
                } finally {
                    inFlight.set(false);
                }
            }
        }, "prey-location-tracker");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------
    // Test seams
    //
    // Unit tests must be able to (a) freeze the wallclock so freshness
    // assertions are deterministic, and (b) intercept the actual fetch
    // so they don't try to hit the GPS / network from a JVM. Both seams
    // are package-private and reset between tests via the helpers below.
    // ------------------------------------------------------------------

    interface Clock {
        long now();
    }

    interface LocationFetcher {
        void fetch(Context ctx) throws Exception;
    }

    private static volatile Clock clockProvider = new Clock() {
        @Override
        public long now() {
            return System.currentTimeMillis();
        }
    };

    private static volatile LocationFetcher fetcher = new LocationFetcher() {
        @Override
        public void fetch(Context ctx) {
            new Location().get(ctx, null, new JSONObject());
        }
    };

    static void setClockForTests(Clock clock) {
        clockProvider = clock;
    }

    static void resetClockForTests() {
        clockProvider = new Clock() {
            @Override
            public long now() {
                return System.currentTimeMillis();
            }
        };
    }

    static void setFetcherForTests(LocationFetcher f) {
        fetcher = f;
    }

    static void resetFetcherForTests() {
        fetcher = new LocationFetcher() {
            @Override
            public void fetch(Context ctx) {
                new Location().get(ctx, null, new JSONObject());
            }
        };
    }

    static void resetInFlightForTests() {
        inFlight.set(false);
    }
}
