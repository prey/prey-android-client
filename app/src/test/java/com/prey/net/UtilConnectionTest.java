/*******************************************************************************
 * Created by OpenAI Codex
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilConnectionTest {

    @Test
    public void givenNullValue_whenAddingOptionalHeader_thenHeaderIsSkipped() {
        FakeHttpURLConnection connection = new FakeHttpURLConnection();

        UtilConnection.addHeaderIfValuePresent(connection, "X-Prey-State", null);

        assertFalse(connection.headers.containsKey("X-Prey-State"));
    }

    @Test
    public void givenNonNullValue_whenAddingOptionalHeader_thenHeaderIsAdded() {
        FakeHttpURLConnection connection = new FakeHttpURLConnection();

        UtilConnection.addHeaderIfValuePresent(connection, "X-Prey-State", "started");

        assertTrue(connection.headers.containsKey("X-Prey-State"));
        assertEquals("started", connection.headers.get("X-Prey-State"));
    }

    private static final class FakeHttpURLConnection extends HttpURLConnection {

        private final Map<String, String> headers = new HashMap<>();

        private FakeHttpURLConnection() {
            super(null);
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public void setRequestMethod(String method) throws ProtocolException {
        }

        @Override
        public void addRequestProperty(String key, String value) {
            headers.put(key, value);
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return new HashMap<>();
        }
    }
}
