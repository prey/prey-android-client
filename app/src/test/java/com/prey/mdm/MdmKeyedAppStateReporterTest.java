package com.prey.mdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.enterprise.feedback.FakeKeyedAppStatesReporter;
import androidx.enterprise.feedback.KeyedAppState;

import org.junit.Test;

public class MdmKeyedAppStateReporterTest {

    @Test
    public void reportSetupLinked_sendsImmediateLinkedState() {
        FakeKeyedAppStatesReporter reporter = new FakeKeyedAppStatesReporter();
        MdmKeyedAppStateReporter sut = new MdmKeyedAppStateReporter(reporter);

        sut.reportSetupLinked();

        assertEquals(1, reporter.getNumberOfUploads());
        KeyedAppState state = reporter.getUploadedKeyedAppStatesByKey().get(MdmKeyedAppStateReporter.SETUP_STATE_KEY);
        assertNotNull(state);
        assertEquals(MdmKeyedAppStateReporter.SETUP_STATE_DATA_LINKED, state.getData());
        assertEquals(KeyedAppState.SEVERITY_INFO, state.getSeverity());
        assertEquals("Prey MDM setup completed", state.getMessage());
    }
}
