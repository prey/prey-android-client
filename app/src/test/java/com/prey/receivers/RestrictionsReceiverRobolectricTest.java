/*******************************************************************************
 * Created by Patricio Jofré
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.os.Bundle;

import com.prey.PreyConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowRestrictionsManager;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric test suite for the {@link RestrictionsReceiver} class.
 * <p>
 * Tests MDM restrictions parsing and storage into PreyConfig.
 * Runs on the JVM without an emulator.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class RestrictionsReceiverRobolectricTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmSerialNumber("");
        preyConfig.setMdmOrganizationId("");
        preyConfig.setMdmDeviceName("");
        preyConfig.setMdmImei("");
        preyConfig.setMdmSkipManualPermissions(false);
        preyConfig.setDeviceId("");
        preyConfig.setApiKey("");
    }

    @Test
    public void givenSerialNumberInRestrictions_whenHandled_thenSerialNumberIsStored() {
        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "SN-1234-5678");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("SN-1234-5678", preyConfig.getMdmSerialNumber());
    }

    @Test
    public void givenEnterpriseNameInRestrictions_whenHandled_thenOrganizationIdIsStored() {
        Bundle restrictions = new Bundle();
        restrictions.putString("enterprise_name", "my-organization");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("my-organization", preyConfig.getMdmOrganizationId());
    }

    @Test
    public void givenDeviceNameInRestrictions_whenHandled_thenDeviceNameIsStored() {
        Bundle restrictions = new Bundle();
        restrictions.putString("device_name", "Sales Team Phone 42");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("Sales Team Phone 42", preyConfig.getMdmDeviceName());
    }

    @Test
    public void givenImeiInRestrictions_whenHandled_thenImeiIsStored() {
        Bundle restrictions = new Bundle();
        restrictions.putString("imei", "354123456789012");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("354123456789012", preyConfig.getMdmImei());
    }

    @Test
    public void givenAllRestrictionsKeys_whenHandled_thenAllValuesAreStored() {
        Bundle restrictions = new Bundle();
        restrictions.putString("enterprise_name", "my-organization");
        restrictions.putString("serial_number", "ABC-9999");
        restrictions.putString("device_name", "Office Phone 001");
        restrictions.putString("imei", "354123456789012");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("my-organization", preyConfig.getMdmOrganizationId());
        assertEquals("ABC-9999", preyConfig.getMdmSerialNumber());
        assertEquals("Office Phone 001", preyConfig.getMdmDeviceName());
        assertEquals("354123456789012", preyConfig.getMdmImei());
    }

    @Test
    public void givenEmptyValues_whenHandled_thenPreviousValuesArePreserved() {
        preyConfig.setMdmSerialNumber("EXISTING-SN");
        preyConfig.setMdmDeviceName("Existing Device");
        preyConfig.setMdmImei("111222333444555");

        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "");
        restrictions.putString("device_name", "");
        restrictions.putString("imei", "");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("EXISTING-SN", preyConfig.getMdmSerialNumber());
        assertEquals("Existing Device", preyConfig.getMdmDeviceName());
        assertEquals("111222333444555", preyConfig.getMdmImei());
    }

    @Test
    public void givenNullRestrictions_whenHandled_thenAllValuesPreserved() {
        preyConfig.setMdmSerialNumber("EXISTING-SN");
        preyConfig.setMdmOrganizationId("existing-org");
        preyConfig.setMdmDeviceName("Existing Device");
        preyConfig.setMdmImei("111222333444555");

        RestrictionsReceiver.saveRestrictionValues(context, null);

        assertEquals("EXISTING-SN", preyConfig.getMdmSerialNumber());
        assertEquals("existing-org", preyConfig.getMdmOrganizationId());
        assertEquals("Existing Device", preyConfig.getMdmDeviceName());
        assertEquals("111222333444555", preyConfig.getMdmImei());
    }

    @Test
    public void givenSkipManualPermissionsTrue_whenHandled_thenFlagIsStored() {
        Bundle restrictions = new Bundle();
        restrictions.putBoolean("skip_manual_permissions", true);

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertTrue(preyConfig.isMdmSkipManualPermissions());
    }

    @Test
    public void givenSkipManualPermissionsFalse_whenHandled_thenFlagIsFalse() {
        preyConfig.setMdmSkipManualPermissions(true);

        Bundle restrictions = new Bundle();
        restrictions.putBoolean("skip_manual_permissions", false);

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertFalse(preyConfig.isMdmSkipManualPermissions());
    }

    @Test
    public void givenNoSkipManualPermissionsKey_whenHandled_thenFlagIsPreserved() {
        preyConfig.setMdmSkipManualPermissions(true);

        Bundle restrictions = new Bundle();
        restrictions.putString("serial_number", "SN-1234");

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertTrue(preyConfig.isMdmSkipManualPermissions());
    }

    @Test
    public void givenEmptyBundle_whenHandled_thenNoValuesChanged() {
        preyConfig.setMdmSerialNumber("EXISTING-SN");
        preyConfig.setMdmOrganizationId("existing-org");
        preyConfig.setMdmDeviceName("Existing Device");
        preyConfig.setMdmImei("111222333444555");

        Bundle restrictions = new Bundle();

        RestrictionsReceiver.saveRestrictionValues(context, restrictions);

        assertEquals("EXISTING-SN", preyConfig.getMdmSerialNumber());
        assertEquals("existing-org", preyConfig.getMdmOrganizationId());
        assertEquals("Existing Device", preyConfig.getMdmDeviceName());
        assertEquals("111222333444555", preyConfig.getMdmImei());
    }

    @Test
    public void givenRestrictionsChangedBroadcast_whenSetupKeyPresent_thenOnlyStoresRestrictionsAndDoesNotRegisterDevice() {
        Bundle restrictions = new Bundle();
        restrictions.putString("setup_key", "valid-setup-key");
        restrictions.putString("serial_number", "SN-1234");
        setApplicationRestrictions(restrictions);

        Intent intent = new Intent(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);
        new RestrictionsReceiver().onReceive(context, intent);

        assertEquals("SN-1234", preyConfig.getMdmSerialNumber());
        assertEquals("", preyConfig.getDeviceId());
        assertFalse(preyConfig.isThisDeviceAlreadyRegisteredWithPrey());
    }

    private void setApplicationRestrictions(Bundle restrictions) {
        RestrictionsManager manager =
                (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
        ShadowRestrictionsManager shadow = Shadows.shadowOf(manager);
        shadow.setApplicationRestrictions(restrictions);
    }

}
