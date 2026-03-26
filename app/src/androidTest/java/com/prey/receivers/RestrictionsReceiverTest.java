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
        preyConfig.setMdmSerialNumber("");
        preyConfig.setMdmOrganizationId("");
        preyConfig.setMdmDeviceName("");
        preyConfig.setMdmImei("");
    }

    /**
     * Verifies that the serial number from MDM restrictions is correctly stored in PreyConfig.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle containing a "serial_number" key.
     *
     * <p><b>Expected Outcome:</b> The serial number should be saved in PreyConfig and retrievable
     * via {@code getMdmSerialNumber()}.
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
                "SN-1234-5678", preyConfig.getMdmSerialNumber());
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
                "my-organization", preyConfig.getMdmOrganizationId());
    }

    /**
     * Verifies that all MDM restriction values are stored when the restrictions
     * bundle contains all keys simultaneously.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle with all supported keys
     * (common in real MDM deployments).
     *
     * <p><b>Expected Outcome:</b> All values should be correctly stored in PreyConfig.
     */
    @Test
    public void givenAllRestrictionsKeys_whenHandled_thenAllValuesAreStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("enterprise_name", "my-organization");
        restrictions.putString("serial_number", "ABC-9999");
        restrictions.putString("device_name", "Office Phone 001");
        restrictions.putString("imei", "354123456789012");
        restrictions.putString("setup_key", "some-api-key");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Organization ID should be stored",
                "my-organization", preyConfig.getMdmOrganizationId());
        assertEquals("Serial number should be stored",
                "ABC-9999", preyConfig.getMdmSerialNumber());
        assertEquals("Device name should be stored",
                "Office Phone 001", preyConfig.getMdmDeviceName());
        assertEquals("IMEI should be stored",
                "354123456789012", preyConfig.getMdmImei());
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
        preyConfig.setMdmSerialNumber("EXISTING-SN");
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Previous serial number should be preserved when MDM sends empty value",
                "EXISTING-SN", preyConfig.getMdmSerialNumber());
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
        preyConfig.setMdmSerialNumber("EXISTING-SN");
        preyConfig.setMdmOrganizationId("existing-org");
        preyConfig.setMdmDeviceName("Existing Device");
        preyConfig.setMdmImei("111222333444555");
        Bundle restrictions = new Bundle();

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Serial number should remain unchanged",
                "EXISTING-SN", preyConfig.getMdmSerialNumber());
        assertEquals("Organization ID should remain unchanged",
                "existing-org", preyConfig.getMdmOrganizationId());
        assertEquals("Device name should remain unchanged",
                "Existing Device", preyConfig.getMdmDeviceName());
        assertEquals("IMEI should remain unchanged",
                "111222333444555", preyConfig.getMdmImei());
    }

    /**
     * Verifies that all MDM values are processed even when the device
     * is already registered with Prey (setup_key is ignored but other values are still read).
     *
     * <p><b>Scenario:</b> A device already registered receives updated MDM restrictions.
     *
     * <p><b>Expected Outcome:</b> All values except setup_key should be updated regardless of
     * registration status.
     */
    @Test
    public void givenRegisteredDevice_whenRestrictionsUpdated_thenValuesAreStillStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "NEW-SN-UPDATE");
        restrictions.putString("enterprise_name", "updated-org");
        restrictions.putString("device_name", "Updated Device");
        restrictions.putString("imei", "999888777666555");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Serial number should be updated even for registered devices",
                "NEW-SN-UPDATE", preyConfig.getMdmSerialNumber());
        assertEquals("Organization ID should be updated even for registered devices",
                "updated-org", preyConfig.getMdmOrganizationId());
        assertEquals("Device name should be updated even for registered devices",
                "Updated Device", preyConfig.getMdmDeviceName());
        assertEquals("IMEI should be updated even for registered devices",
                "999888777666555", preyConfig.getMdmImei());
    }

    /**
     * Verifies that the device name from MDM restrictions is correctly stored in PreyConfig.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle containing a "device_name" key.
     *
     * <p><b>Expected Outcome:</b> The device name should be saved in PreyConfig and retrievable
     * via {@code getMdmDeviceName()}.
     */
    @Test
    public void givenDeviceNameInRestrictions_whenHandled_thenDeviceNameIsStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("device_name", "Sales Team Phone 42");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Device name should be stored from MDM restrictions",
                "Sales Team Phone 42", preyConfig.getMdmDeviceName());
    }

    /**
     * Verifies that an empty device_name in the restrictions bundle does not overwrite
     * a previously stored value.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle where device_name is an empty string.
     *
     * <p><b>Expected Outcome:</b> The previously stored device name should remain unchanged.
     */
    @Test
    public void givenEmptyDeviceName_whenHandled_thenPreviousValueIsPreserved() {
        // Arrange
        preyConfig.setMdmDeviceName("Existing Device");
        Bundle restrictions = new Bundle();
        restrictions.putString("device_name", "");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Previous device name should be preserved when MDM sends empty value",
                "Existing Device", preyConfig.getMdmDeviceName());
    }

    /**
     * Verifies that the IMEI from MDM restrictions is correctly stored in PreyConfig.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle containing an "imei" key.
     *
     * <p><b>Expected Outcome:</b> The IMEI should be saved in PreyConfig and retrievable
     * via {@code getMdmImei()}.
     */
    @Test
    public void givenImeiInRestrictions_whenHandled_thenImeiIsStored() {
        // Arrange
        Bundle restrictions = new Bundle();
        restrictions.putString("imei", "354123456789012");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("IMEI should be stored from MDM restrictions",
                "354123456789012", preyConfig.getMdmImei());
    }

    /**
     * Verifies that an empty IMEI in the restrictions bundle does not overwrite
     * a previously stored value.
     *
     * <p><b>Scenario:</b> MDM sends a restrictions bundle where imei is an empty string.
     *
     * <p><b>Expected Outcome:</b> The previously stored IMEI should remain unchanged.
     */
    @Test
    public void givenEmptyImei_whenHandled_thenPreviousValueIsPreserved() {
        // Arrange
        preyConfig.setMdmImei("111222333444555");
        Bundle restrictions = new Bundle();
        restrictions.putString("imei", "");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, restrictions);

        // Assert
        assertEquals("Previous IMEI should be preserved when MDM sends empty value",
                "111222333444555", preyConfig.getMdmImei());
    }

    /**
     * Verifies that null restrictions bundle preserves all previously stored values
     * including device_name and imei.
     *
     * <p><b>Scenario:</b> All MDM values are set, then null restrictions are received.
     *
     * <p><b>Expected Outcome:</b> No values are modified in PreyConfig.
     */
    @Test
    public void givenNullRestrictions_whenHandled_thenAllValuesPreserved() {
        // Arrange
        preyConfig.setMdmSerialNumber("EXISTING-SN");
        preyConfig.setMdmOrganizationId("existing-org");
        preyConfig.setMdmDeviceName("Existing Device");
        preyConfig.setMdmImei("111222333444555");

        // Act
        RestrictionsReceiver.handleApplicationRestrictions(context, null);

        // Assert
        assertEquals("Serial number should remain unchanged",
                "EXISTING-SN", preyConfig.getMdmSerialNumber());
        assertEquals("Organization ID should remain unchanged",
                "existing-org", preyConfig.getMdmOrganizationId());
        assertEquals("Device name should remain unchanged",
                "Existing Device", preyConfig.getMdmDeviceName());
        assertEquals("IMEI should remain unchanged",
                "111222333444555", preyConfig.getMdmImei());
    }

}
