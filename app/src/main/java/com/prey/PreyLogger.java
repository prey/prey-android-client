/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class PreyLogger {

    public static final String LOG_FILE_NAME = "prey.log";
    static final String OLD_LOG_FILE_NAME = "prey.log.1";

    // Cap for each segment. With dual-file rotation that's up to ~2 MB on
    // disk, matching the previous configuration without the 1 MB byte-copy
    // the old rotation performed under the file lock.
    private static final long MAX_LOG_SIZE_BYTES = 1L * 1024L * 1024L;

    private static final SimpleDateFormat TIMESTAMP_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private static volatile File logFile = null;
    private static volatile File oldLogFile = null;

    // Single-thread, low-priority, daemon executor. PreyLogger.d/i/e is called
    // from the main thread, workers, services and broadcast receivers all over
    // the app — file I/O on those threads would risk ANRs and StrictMode hits.
    // Pushing writes onto this executor makes logging effectively free for the
    // caller (just an enqueue) and keeps the cost of rare rotation off any hot
    // path. Daemon so it never blocks JVM exit.
    private static final ExecutorService WRITER_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "prey-logger-writer");
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            });

    public static void init(Context ctx) {
        if (ctx == null) return;
        try {
            File dir = ctx.getFilesDir();
            if (dir != null) {
                logFile = new File(dir, LOG_FILE_NAME);
                oldLogFile = new File(dir, OLD_LOG_FILE_NAME);
            }
        } catch (Exception e) {
            Log.e(PreyConfig.TAG, "PreyLogger init error: " + e.getMessage(), e);
        }
    }

    public static File getLogFile() {
        return logFile;
    }

    /** Returns the rotated previous segment if it exists, else null. */
    public static File getOldLogFile() {
        File f = oldLogFile;
        return (f != null && f.exists()) ? f : null;
    }

    public static void d(String message) {
        String tagged = withCallerTag(message);
        if (PreyConfig.LOG_DEBUG_ENABLED) {
            Log.d(PreyConfig.TAG, tagged);
        }
        enqueue("D", tagged, null);
    }

    public static void i(String message) {
        String tagged = withCallerTag(message);
        Log.i(PreyConfig.TAG, tagged);
        enqueue("I", tagged, null);
    }

    public static void e(final String message, Throwable e) {
        String tagged = withCallerTag(message);
        if (e != null)
            Log.e(PreyConfig.TAG, tagged, e);
        else
            Log.e(PreyConfig.TAG, tagged);
        enqueue("E", tagged, e);
    }

    private static String withCallerTag(String message) {
        String tag = callerTag();
        String body = message == null ? "" : message;
        if (tag == null) return body;
        return tag + " " + body;
    }

    /**
     * Walks the current thread's stack and returns "[SimpleClass][method]" for
     * the first frame outside PreyLogger — i.e. the call site of d/i/e.
     */
    private static String callerTag() {
        try {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String selfClass = PreyLogger.class.getName();
            for (StackTraceElement frame : stack) {
                String cls = frame.getClassName();
                if (cls.equals("java.lang.Thread")) continue;
                if (cls.equals(selfClass)) continue;
                int dot = cls.lastIndexOf('.');
                String simple = dot >= 0 ? cls.substring(dot + 1) : cls;
                int dollar = simple.indexOf('$');
                if (dollar >= 0) simple = simple.substring(0, dollar);
                return "[" + simple + "][" + frame.getMethodName() + "]";
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Blocks until every log entry queued before this call has been written.
     * Callers that read the log file from disk (e.g. the log retrieval action)
     * must invoke this first so they don't observe a partial tail. Returns
     * almost immediately when the queue is empty.
     */
    public static void flush() {
        try {
            // Single-thread executor: when this no-op completes, every task
            // submitted before it has also completed.
            WRITER_EXECUTOR.submit(() -> { }).get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Best-effort. If we time out or are interrupted the caller just
            // reads whatever is on disk at that moment.
        }
    }

    private static void enqueue(String level, String message, Throwable t) {
        final File file = logFile;
        if (file == null) return;
        // Snapshot timestamp and stack trace on the caller thread so the entry
        // reflects when the event happened, not when the writer drains.
        final String timestamp = formatTimestamp();
        final String stack = stackTraceOrNull(t);
        try {
            WRITER_EXECUTOR.execute(() -> writeEntry(file, timestamp, level, message, stack));
        } catch (RejectedExecutionException ignored) {
            // Executor shut down (e.g. process tear-down). Drop silently.
        }
    }

    private static String formatTimestamp() {
        // SimpleDateFormat is not thread-safe — guard the format call.
        synchronized (TIMESTAMP_FORMAT) {
            return TIMESTAMP_FORMAT.format(new Date());
        }
    }

    private static String stackTraceOrNull(Throwable t) {
        if (t == null) return null;
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static void writeEntry(
            File file, String timestamp, String level, String message, String stack) {
        try {
            if (file.length() > MAX_LOG_SIZE_BYTES) {
                rotate(file);
            }
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.write(timestamp);
                fw.write(' ');
                fw.write(level);
                fw.write(' ');
                fw.write(message == null ? "" : message);
                fw.write('\n');
                if (stack != null) {
                    fw.write(stack);
                }
            }
        } catch (Exception ex) {
            // Never throw from the logger.
        }
    }

    /**
     * O(1) rotation: rename the active segment over the previous one, dropping
     * whatever was there. The active file becomes empty and ready for new
     * entries. Replaces the previous implementation which copied ~1 MB inside
     * the writer lock on every rotation.
     */
    private static void rotate(File file) {
        try {
            File old = oldLogFile;
            if (old == null) {
                if (!file.delete()) {
                    Log.w(PreyConfig.TAG, "PreyLogger rotate: could not truncate active segment");
                }
                return;
            }
            if (old.exists() && !old.delete()) {
                Log.w(PreyConfig.TAG, "PreyLogger rotate: could not delete previous segment");
            }
            if (!file.renameTo(old)) {
                // Cross-device or platform quirk — fall back to truncating so
                // the active file does not grow without bound.
                if (!file.delete()) {
                    Log.w(PreyConfig.TAG, "PreyLogger rotate: rename and delete both failed");
                }
            }
        } catch (Exception ex) {
            // Rotation is best-effort.
        }
    }
}
