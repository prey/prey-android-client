/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.prey.PreyConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tests for {@link LocationTracker#maybeSendRecentLocation(Context)}.
 *
 * <p>The unit under test has two sources of nondeterminism we control via
 * package-private seams: the wallclock and the actual location-fetch action.
 * Without freezing the clock, the freshness window check would race; without
 * intercepting the fetcher, every test would try to drive a real GPS / HTTP
 * stack from a JVM.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LocationTrackerTest {

    private Context context;
    private PreyConfig preyConfig;
    private AtomicLong fakeNow;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        // Reset persisted state so each test starts from a known timestamp.
        preyConfig.setLastLocationSentAt(0);
        fakeNow = new AtomicLong(1_700_000_000_000L);
        LocationTracker.setClockForTests(new LocationTracker.Clock() {
            @Override
            public long now() {
                return fakeNow.get();
            }
        });
        LocationTracker.resetInFlightForTests();
    }

    @After
    public void tearDown() {
        LocationTracker.resetClockForTests();
        LocationTracker.resetFetcherForTests();
        LocationTracker.resetInFlightForTests();
        preyConfig.setLastLocationSentAt(0);
    }

    @Test
    public void freshLocation_doesNotTriggerFetch() throws InterruptedException {
        // Last sent 1 minute ago — well inside the 5-minute window.
        preyConfig.setLastLocationSentAt(fakeNow.get() - 60_000L);
        AtomicInteger fetchCount = new AtomicInteger(0);
        LocationTracker.setFetcherForTests(ctx -> fetchCount.incrementAndGet());

        LocationTracker.maybeSendRecentLocation(context);

        // Give a hypothetical worker thread a chance to run; assertion is that
        // none was started, so this sleep is a defensive ceiling.
        Thread.sleep(100);
        assertEquals(
                "Within freshness window: no fetch should be queued",
                0,
                fetchCount.get()
        );
    }

    @Test
    public void staleLocation_triggersFetchExactlyOnce() throws InterruptedException {
        // Last sent 6 minutes ago — outside the window.
        preyConfig.setLastLocationSentAt(fakeNow.get() - 6L * 60_000L);
        CountDownLatch fetched = new CountDownLatch(1);
        AtomicInteger fetchCount = new AtomicInteger(0);
        LocationTracker.setFetcherForTests(ctx -> {
            fetchCount.incrementAndGet();
            fetched.countDown();
        });

        LocationTracker.maybeSendRecentLocation(context);

        assertTrue(
                "Stale timestamp should kick the worker thread within 2s",
                fetched.await(2, TimeUnit.SECONDS)
        );
        assertEquals(
                "Stale path must run the fetcher exactly once",
                1,
                fetchCount.get()
        );
    }

    @Test
    public void neverSent_triggersFetch() throws InterruptedException {
        // setUp already wrote 0; assert the never-sent path explicitly.
        preyConfig.setLastLocationSentAt(0);
        CountDownLatch fetched = new CountDownLatch(1);
        LocationTracker.setFetcherForTests(ctx -> fetched.countDown());

        LocationTracker.maybeSendRecentLocation(context);

        assertTrue(
                "When no location has ever been sent, the trigger must fire",
                fetched.await(2, TimeUnit.SECONDS)
        );
    }

    @Test
    public void concurrentTriggers_runFetcherOnlyOnce() throws InterruptedException {
        // Reproduce the alert+alarm+lock-arriving-at-the-same-time scenario.
        preyConfig.setLastLocationSentAt(0);
        CountDownLatch holdFetcher = new CountDownLatch(1);
        AtomicInteger fetchCount = new AtomicInteger(0);
        LocationTracker.setFetcherForTests(ctx -> {
            fetchCount.incrementAndGet();
            // Hold the worker thread inside fetch() so concurrent triggers
            // race with it for the inFlight CAS.
            try {
                holdFetcher.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });

        LocationTracker.maybeSendRecentLocation(context);
        LocationTracker.maybeSendRecentLocation(context);
        LocationTracker.maybeSendRecentLocation(context);

        // Give the first worker time to enter the fetcher and pin inFlight=true.
        Thread.sleep(150);
        assertEquals(
                "Three rapid triggers while one is in flight must collapse to a single fetch",
                1,
                fetchCount.get()
        );
        // Release the worker so it clears the inFlight flag in its finally block.
        holdFetcher.countDown();
    }

    @Test
    public void boundaryAtFreshnessWindow_isTreatedAsStale() throws InterruptedException {
        // Exactly 5 minutes ago: condition is `now - last < WINDOW`, so 5min
        // is NOT inside the window — must trigger.
        preyConfig.setLastLocationSentAt(fakeNow.get() - LocationTracker.FRESHNESS_WINDOW_MS);
        CountDownLatch fetched = new CountDownLatch(1);
        LocationTracker.setFetcherForTests(ctx -> fetched.countDown());

        LocationTracker.maybeSendRecentLocation(context);

        assertTrue(
                "At exactly the freshness boundary, the location is stale and must trigger",
                fetched.await(2, TimeUnit.SECONDS)
        );
    }
}
