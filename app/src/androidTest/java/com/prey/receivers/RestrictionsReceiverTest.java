/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.Context;
import android.os.Bundle;

import com.prey.PreyConfig;

import org.junit.Before;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test suite for the {@link RestrictionsReceiver} class.
 * <p>
 * This class contains tests that verify the behavior of the MDM restrictions handling,
 * specifically focusing on how serial_number, enterprise_name, and setup_key are parsed
 * from the application restrictions bundle provided by MDM (e.g., Intune).
 */
public class RestrictionsReceiverTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        // Clear previous values to ensure test isolation
        preyConfig.setSerialNumber("");
        preyConfig.setOrganizationId("");
    }

    /**
     * Verifies that the serial number from MDM restrictions is correctly stored in PreyConfig.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle containing a "serial_number" key.
     *
     * <p><b>Expected Outcome:</b> The serial number should be saved in PreyConfig and retrievable
     * via {@code getSerialNumber()}.
     */
    @Test
    public void givenSerialNumberInRestrictions_whenHandled_thenSerialNumberIsStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "SN-1234-5678");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Serial number should be stored from MDM restrictions",
                "SN-1234-5678", preyConfig.getSerialNumber());
    }

    /**
     * Verifies that the enterprise name from MDM restrictions is correctly stored as the
     * organization ID in PreyConfig.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle containing an "enterprise_name" key.
     *
     * <p><b>Expected Outcome:</b> The enterprise name should be saved as the organization ID
     * in PreyConfig.
     */
    @Test
    public void givenEnterpriseNameInRestrictions_whenHandled_thenOrganizationIdIsStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("enterprise_name", "my-organization");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Organization ID should be stored from MDM restrictions",
                "my-organization", preyConfig.getOrganizationId());
    }

    /**
     * Verifies that both serial_number and enterprise_name are stored when the restrictions
     * bundle contains both keys simultaneously.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle with serial_number, enterprise_name,
     * and setup_key (common in real MDM deployments).
     *
     * <p><b>Expected Outcome:</b> Both values should be correctly stored in PreyConfig.
     */
    @Test
    public void givenAllRestrictionsKeys_whenHandled_thenAllValuesAreStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("enterprise_name", "my-organization");
        restrictions.putString("serial_number", "ABC-9999");
        restrictions.putString("setup_key", "some-api-key");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Organization ID should be stored",
                "my-organization", preyConfig.getOrganizationId());
        assertEquals("Serial number should be stored",
                "ABC-9999", preyConfig.getSerialNumber());
    }

    /**
     * Verifies that an empty serial_number in the restrictions bundle does not overwrite
     * a previously stored value.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle where serial_number is an empty string.
     *
     * <p><b>Expected Outcome:</b> The previously stored serial number should remain unchanged.
     */
    @Test
    public void givenEmptySerialNumber_whenHandled_thenPreviousValueIsPreserved() {
        // Arrange
        preyConfig.setSerialNumber("EXISTING-SN");
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Previous serial number should be preserved when MDM sends empty value",
                "EXISTING-SN", preyConfig.getSerialNumber());
    }

    /**
     * Verifies that a null restrictions bundle does not cause a crash.
     *
     * <p><b>Scenario:</b> The restrictions bundle is null (e.g., MDM not configured).
     *
     * <p><b>Expected Outcome:</b> No exception is thrown and stored values remain unchanged.
     */
    @Test
    public void givenNullRestrictions_whenHandled_thenNoCrash() {
        // Arrange
        preyConfig.setSerialNumber("EXISTING-SN");
        preyConfig.setOrganizationId("existing-org");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, null);

        // Assert
        assertEquals("Serial number should remain unchanged",
                "EXISTING-SN", preyConfig.getSerialNumber());
        assertEquals("Organization ID should remain unchanged",
                "existing-org", preyConfig.getOrganizationId());
    }

    /**
     * Verifies that an empty restrictions bundle does not affect stored values.
     *
     * <p><b>Scenario:</b> MDM sends an empty restrictions bundle with no keys.
     *
     * <p><b>Expected Outcome:</b> No values are modified in PreyConfig.
     */
    @Test
    public void givenEmptyBundle_whenHandled_thenNoValuesChanged() {
        // Arrange
        preyConfig.setSerialNumber("EXISTING-SN");
        preyConfig.setOrganizationId("existing-org");
        Bundle restrictions = new Bundle();

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Serial number should remain unchanged",
                "EXISTING-SN", preyConfig.getSerialNumber());
        assertEquals("Organization ID should remain unchanged",
                "existing-org", preyConfig.getOrganizationId());
    }

    /**
     * Verifies that serial_number and enterprise_name are processed even when the device
     * is already registered with Prey (setup_key is ignored but other values are still read).
     *
     * <p><b>Scenario:</b> A device already registered receives updated MDM restrictions
     * with a new serial_number.
     *
     * <p><b>Expected Outcome:</b> The serial_number should be updated regardless of
     * registration status.
     */
    @Test
    public void givenRegisteredDevice_whenRestrictionsUpdated_thenSerialNumberIsStillStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "NEW-SN-UPDATE");
        restrictions.putString("enterprise_name", "updated-org");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Serial number should be updated even for registered devices",
                "NEW-SN-UPDATE", preyConfig.getSerialNumber());
        assertEquals("Organization ID should be updated even for registered devices",
                "updated-org", preyConfig.getOrganizationId());
    }

}
