/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLHandshakeException;

/**
 * Tests for {@link UtilConnection#isTransientIOException(IOException)}.
 *
 * <p>The retry policy in {@link UtilConnection#connection} used to bypass
 * any thrown {@link IOException} — it propagated straight to the caller even
 * though the surrounding {@code do-while} was already wired for retries on
 * HTTP error codes. The classic symptom was the bundled okhttp's
 * "unexpected end of stream" coming from a stale keep-alive connection,
 * which would have been resolved by a single retry. This predicate decides
 * which exceptions deserve to go through the retry loop and which propagate.
 */
public class UtilConnectionTransientRetryTest {

    // -------------------------------------------------------------------------
    // Transient — should retry
    // -------------------------------------------------------------------------

    @Test
    public void eofExceptionIsTransient() {
        // The exact failure mode reported in the field log: the bundled okhttp
        // wraps stale-pool reads in an EOFException with "\\n not found".
        assertTrue(UtilConnection.isTransientIOException(
                new EOFException("\\n not found: size=0 content=...")));
    }

    @Test
    public void unexpectedEndOfStreamMessageIsTransientEvenOnPlainIOException() {
        // The bundled okhttp wraps the EOF in a generic IOException at the
        // outer layer ("unexpected end of stream on com.android.okhttp.Address@..").
        // We must detect it by message, not just by type.
        assertTrue(UtilConnection.isTransientIOException(
                new IOException("unexpected end of stream on com.android.okhttp.Address@67f35fed")));
    }

    @Test
    public void socketExceptionIsTransient() {
        // "Connection reset" / "Broken pipe" — peer closed the socket mid-flight.
        assertTrue(UtilConnection.isTransientIOException(
                new SocketException("Connection reset")));
    }

    @Test
    public void socketTimeoutExceptionIsTransient() {
        // Read timeout under transient congestion — usually clears on retry.
        assertTrue(UtilConnection.isTransientIOException(
                new SocketTimeoutException("Read timed out")));
    }

    @Test
    public void transientExceptionWrappedAsCauseIsStillDetected() {
        // The okhttp internals sometimes nest the EOF as the cause inside a
        // generic IOException without copying the message up. The predicate
        // walks one level of cause to catch this case.
        IOException wrapped = new IOException("network failure",
                new EOFException("\\n not found: size=0 content=..."));
        assertTrue(UtilConnection.isTransientIOException(wrapped));
    }

    // -------------------------------------------------------------------------
    // Non-transient — must propagate
    // -------------------------------------------------------------------------

    @Test
    public void sslHandshakeExceptionIsNotTransient() {
        // A failed handshake will fail the same way on every retry — burning
        // attempts on it just delays the inevitable error to the caller.
        assertFalse(UtilConnection.isTransientIOException(
                new SSLHandshakeException("Trust anchor for certification path not found")));
    }

    @Test
    public void plainIOExceptionWithUnrelatedMessageIsNotTransient() {
        // An IOException with a message that doesn't match any known
        // transient signature should NOT be retried — we don't want to mask
        // genuine bugs (e.g. a malformed multipart body) behind silent retries.
        assertFalse(UtilConnection.isTransientIOException(
                new IOException("multipart boundary missing")));
    }

    @Test
    public void nullIsNotTransient() {
        // Defensive: predicate must not NPE on a null argument.
        assertFalse(UtilConnection.isTransientIOException(null));
    }

    @Test
    public void selfReferencingCauseDoesNotInfiniteLoop() {
        // Pathological case: an IOException whose cause points back to itself.
        // The predicate must terminate without recursing forever.
        IOException ioe = new IOException("weird");
        try {
            ioe.initCause(ioe);
        } catch (IllegalArgumentException ignored) {
            // initCause refuses self-references on most JDKs — that's fine.
            return;
        }
        assertFalse(UtilConnection.isTransientIOException(ioe));
    }
}
