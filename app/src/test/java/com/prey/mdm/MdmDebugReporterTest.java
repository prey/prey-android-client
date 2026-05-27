package com.prey.mdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MdmDebugReporterTest {

    @Test
    public void normalizeDebugUrl_returnsEmptyStringWhenUnset() {
        assertEquals("", MdmDebugReporter.normalizeDebugUrl(null));
        assertEquals("", MdmDebugReporter.normalizeDebugUrl(""));
        assertEquals("", MdmDebugReporter.normalizeDebugUrl("   "));
        assertFalse(MdmDebugReporter.isEnabledForUrl(null));
        assertFalse(MdmDebugReporter.isEnabledForUrl("   "));
    }

    @Test
    public void normalizeDebugUrl_trimsConfiguredValue() {
        assertEquals(
                "https://example.com/api/v1/debug/event",
                MdmDebugReporter.normalizeDebugUrl(" https://example.com/api/v1/debug/event ")
        );
        assertTrue(MdmDebugReporter.isEnabledForUrl("https://example.com/api/v1/debug/event"));
    }
}
