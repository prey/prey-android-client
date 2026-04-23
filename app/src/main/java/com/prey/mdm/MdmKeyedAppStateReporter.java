package com.prey.mdm;

import android.content.Context;

import androidx.enterprise.feedback.KeyedAppState;
import androidx.enterprise.feedback.KeyedAppStatesReporter;

import com.prey.PreyLogger;

import java.util.Collections;

public class MdmKeyedAppStateReporter {
    public static final String SETUP_STATE_KEY = "mdm_setup";
    public static final String SETUP_STATE_DATA_LINKED = "linked";
    private static final String SETUP_STATE_MESSAGE = "Prey MDM setup completed";

    private final KeyedAppStatesReporter reporter;

    public MdmKeyedAppStateReporter(Context context) {
        this(KeyedAppStatesReporter.create(context.getApplicationContext()));
    }

    MdmKeyedAppStateReporter(KeyedAppStatesReporter reporter) {
        this.reporter = reporter;
    }

    public void reportSetupLinked() {
        reporter.setStatesImmediate(Collections.singleton(buildSetupLinkedState()), null);
    }

    public static void reportSetupLinked(Context context) {
        try {
            new MdmKeyedAppStateReporter(context).reportSetupLinked();
        } catch (RuntimeException e) {
            PreyLogger.e("Error reporting keyed app state", e);
        }
    }

    static KeyedAppState buildSetupLinkedState() {
        return KeyedAppState.builder()
                .setKey(SETUP_STATE_KEY)
                .setSeverity(KeyedAppState.SEVERITY_INFO)
                .setMessage(SETUP_STATE_MESSAGE)
                .setData(SETUP_STATE_DATA_LINKED)
                .build();
    }
}
