/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;

import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.FakeWebServices;

import org.junit.Before;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test suite for the {@link PreyConfig} class.
 * <p>
 * This class contains unit tests that verify the behavior of the {@code PreyConfig} singleton,
 * specifically focusing on how its internal state changes in response to network operations.
 * It uses mock web services to simulate different HTTP response scenarios (e.g., success, failure)
 * and asserts that the configuration flags are updated correctly.
 */
public class PreyConfigTest {

    private SimpleDateFormat sdf;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    /**
     * Verifies that the {@code sendCompilation} flag in {@link PreyConfig} remains {@code true}
     * when a data-sending operation fails due to an HTTP error.
     *
     * <p><b>Scenario:</b> An operation is triggered to send compiled data to the server, and the
     * {@code sendCompilation} flag is initially set to {@code true}.
     *
     * <p><b>Test Steps:</b>
     * <ol>
     *     <li>The {@code sendCompilation} flag is explicitly set to {@code true}.</li>
     *     <li>A mock {@link PreyWebServices} is configured to simulate an HTTP server error
     *         (e.g., 500 Internal Server Error) upon receiving a data request.</li>
     *     <li>The {@code sendPreyHttpData} method is called to attempt sending the data.</li>
     *     <li>The test asserts that the {@code sendCompilation} flag is still {@code true} after
     *         the failed network attempt.</li>
     * </ol>
     *
     * <p><b>Expected Outcome:</b> The flag should not be reset to {@code false} on failure, ensuring
     * that the application will retry sending the compiled data in a subsequent operation.
     */
    @Test
    public void givenHttpError_whenSendingData_thenSendCompilationFlagRemainsTrue() {
        // Arrange: Set up the configuration and mock the web service response
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.sendCompilation(true);// Set the initial state

        // Create a mock HTTP response that simulates a server error
        PreyHttpResponse errorHttpResponse = new PreyHttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error");

        // Set up a mock web service to return the error response
        FakeWebServices mockWebServices = new FakeWebServices();
        mockWebServices.setPreyHttpResponse(errorHttpResponse);
        preyConfig.setWebServices(mockWebServices);

        // Prepare the data to be sent (even if its content isn't critical for this test)
        ArrayList<HttpDataService> dataToSend = createLocationDataPayload();

        // Act: Perform the action being tested - sending data which will result in an error
        preyConfig.getWebServices().sendPreyHttpData(context, dataToSend);

        // Assert: Verify the expected outcome
        assertTrue("The sendCompilation flag should remain true after a network error", preyConfig.isSendCompilation());
    }

    /**
     * Verifies that the {@code sendCompilation} flag in {@link PreyConfig} is reset to {@code false}
     * after data is successfully sent to the server.
     *
     * <p>This test simulates a scenario where the flag is initially {@code true}, indicating that a
     * data compilation needs to be sent. It then mocks a successful HTTP response (HTTP OK) from the
     * web service. After the data sending operation is performed, it asserts that the
     * {@code sendCompilation} flag has been correctly updated to {@code false}, confirming that the
     * system acknowledges the successful transmission and will not attempt to resend the compilation
     * unnecessarily.</p>
     */
    @Test
    public void whenDataIsSentSuccessfully_thenSendCompilationFlagIsReset() {
        // Arrange: Set up the configuration and mock the web service response
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.sendCompilation(true);// Set the initial state

        // Create a mock HTTP response that simulates a server ok
        PreyHttpResponse okHttpResponse = new PreyHttpResponse(HttpURLConnection.HTTP_OK, "OK");

        // Set up a mock web service to return the error response
        FakeWebServices mockWebServices = new FakeWebServices();
        mockWebServices.setPreyHttpResponse(okHttpResponse);
        preyConfig.setWebServices(mockWebServices);

        // Prepare the data to be sent (even if its content isn't critical for this test)
        ArrayList<HttpDataService> dataToSend = createLocationDataPayload();

        // Act: Perform the action being tested - sending data which will result in an ok
        preyConfig.getWebServices().sendPreyHttpData(context, dataToSend);

        // Assert: Verify the expected outcome
        assertFalse("The sendCompilation flag should be false after a successful send", preyConfig.isSendCompilation());
    }


    /**
     * Verifies that the serial number received from MDM restrictions can be stored and retrieved
     * correctly from PreyConfig.
     *
     * <p><b>Scenario:</b> A serial number is set via {@code setMdmSerialNumber()}.
     *
     * <p><b>Expected Outcome:</b> The same value is returned by {@code getMdmSerialNumber()}.
     */
    @Test
    public void givenSerialNumber_whenSet_thenCanBeRetrieved() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);

        // Act
        preyConfig.setMdmSerialNumber("SN-TEST-1234");

        // Assert
        assertEquals("Serial number should be retrievable after being set",
                "SN-TEST-1234", preyConfig.getMdmSerialNumber());
    }

    /**
     * Verifies that the serial number defaults to an empty string when not set.
     *
     * <p><b>Scenario:</b> The serial number is cleared by setting it to an empty string.
     *
     * <p><b>Expected Outcome:</b> {@code getMdmSerialNumber()} returns an empty string.
     */
    @Test
    public void givenNoSerialNumber_whenRetrieved_thenReturnsEmptyString() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmSerialNumber("");

        // Act
        String serialNumber = preyConfig.getMdmSerialNumber();

        // Assert
        assertEquals("Serial number should default to empty string",
                "", serialNumber);
    }

    /**
     * Verifies that setting a new serial number overwrites the previous value.
     *
     * <p><b>Scenario:</b> A serial number is set, then updated with a new value.
     *
     * <p><b>Expected Outcome:</b> The latest value is returned by {@code getMdmSerialNumber()}.
     */
    @Test
    public void givenExistingSerialNumber_whenUpdated_thenNewValueIsReturned() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmSerialNumber("OLD-SN");

        // Act
        preyConfig.setMdmSerialNumber("NEW-SN");

        // Assert
        assertEquals("Serial number should reflect the latest value",
                "NEW-SN", preyConfig.getMdmSerialNumber());
    }

    /**
     * Verifies that the IMEI received from MDM can be stored and retrieved correctly.
     *
     * <p><b>Scenario:</b> An IMEI is set via {@code setMdmImei()}.
     *
     * <p><b>Expected Outcome:</b> The same value is returned by {@code getMdmImei()}.
     */
    @Test
    public void givenImei_whenSet_thenCanBeRetrieved() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);

        // Act
        preyConfig.setMdmImei("354123456789012");

        // Assert
        assertEquals("IMEI should be retrievable after being set",
                "354123456789012", preyConfig.getMdmImei());
    }

    /**
     * Verifies that the IMEI defaults to an empty string when not set.
     *
     * <p><b>Scenario:</b> The IMEI is cleared by setting it to an empty string.
     *
     * <p><b>Expected Outcome:</b> {@code getMdmImei()} returns an empty string.
     */
    @Test
    public void givenNoImei_whenRetrieved_thenReturnsEmptyString() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmImei("");

        // Act
        String imei = preyConfig.getMdmImei();

        // Assert
        assertEquals("IMEI should default to empty string",
                "", imei);
    }

    /**
     * Verifies that setting a new IMEI overwrites the previous value.
     *
     * <p><b>Scenario:</b> An IMEI is set, then updated with a new value.
     *
     * <p><b>Expected Outcome:</b> The latest value is returned by {@code getMdmImei()}.
     */
    @Test
    public void givenExistingImei_whenUpdated_thenNewValueIsReturned() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmImei("111111111111111");

        // Act
        preyConfig.setMdmImei("222222222222222");

        // Assert
        assertEquals("IMEI should reflect the latest value",
                "222222222222222", preyConfig.getMdmImei());
    }

    /**
     * Verifies that the MDM device name can be stored and retrieved correctly.
     *
     * <p><b>Scenario:</b> A device name is set via {@code setMdmDeviceName()}.
     *
     * <p><b>Expected Outcome:</b> The same value is returned by {@code getMdmDeviceName()}.
     */
    @Test
    public void givenMdmDeviceName_whenSet_thenCanBeRetrieved() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);

        // Act
        preyConfig.setMdmDeviceName("Office Laptop 001");

        // Assert
        assertEquals("MDM device name should be retrievable after being set",
                "Office Laptop 001", preyConfig.getMdmDeviceName());
    }

    /**
     * Verifies that the MDM device name defaults to an empty string when not set.
     *
     * <p><b>Scenario:</b> The MDM device name is cleared by setting it to an empty string.
     *
     * <p><b>Expected Outcome:</b> {@code getMdmDeviceName()} returns an empty string.
     */
    @Test
    public void givenNoMdmDeviceName_whenRetrieved_thenReturnsEmptyString() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmDeviceName("");

        // Act
        String deviceName = preyConfig.getMdmDeviceName();

        // Assert
        assertEquals("MDM device name should default to empty string",
                "", deviceName);
    }

    /**
     * Verifies that setting a new MDM device name overwrites the previous value.
     *
     * <p><b>Scenario:</b> A device name is set, then updated with a new value.
     *
     * <p><b>Expected Outcome:</b> The latest value is returned by {@code getMdmDeviceName()}.
     */
    @Test
    public void givenExistingMdmDeviceName_whenUpdated_thenNewValueIsReturned() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmDeviceName("Old Name");

        // Act
        preyConfig.setMdmDeviceName("New Name");

        // Assert
        assertEquals("MDM device name should reflect the latest value",
                "New Name", preyConfig.getMdmDeviceName());
    }

    /**
     * Verifies that the organization ID can be stored and retrieved correctly.
     *
     * <p><b>Scenario:</b> An organization ID is set via {@code setMdmOrganizationId()}.
     *
     * <p><b>Expected Outcome:</b> The same value is returned by {@code getMdmOrganizationId()}.
     */
    @Test
    public void givenOrganizationId_whenSet_thenCanBeRetrieved() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);

        // Act
        preyConfig.setMdmOrganizationId("prey-inc");

        // Assert
        assertEquals("Organization ID should be retrievable after being set",
                "prey-inc", preyConfig.getMdmOrganizationId());
    }

    // =========================================================================
    // Tests for buildDeviceName — device name construction for registration
    // =========================================================================

    /**
     * Verifies that when an MDM device name is set, {@code buildDeviceName} returns it
     * regardless of the default name or serial number.
     *
     * <p><b>Scenario:</b> MDM provides a custom device name via restrictions.
     *
     * <p><b>Expected Outcome:</b> The MDM device name takes priority over default name + serial number.
     */
    @Test
    public void givenMdmDeviceNameSet_whenBuildingName_thenMdmNameIsUsed() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmDeviceName("Office Phone 001");
        preyConfig.setMdmSerialNumber("R1YX100J1ON");

        // Act
        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        // Assert
        assertEquals("MDM device name should take priority",
                "Office Phone 001", result);
    }

    /**
     * Verifies that when no MDM device name is set but a serial number is available,
     * the device name is built as "defaultName - serialNumber".
     *
     * <p><b>Scenario:</b> MDM provides a serial number but no custom device name.
     *
     * <p><b>Expected Outcome:</b> The name is the default name appended with the serial number.
     */
    @Test
    public void givenSerialNumberOnly_whenBuildingName_thenNameIncludesSerialNumber() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmDeviceName("");
        preyConfig.setMdmSerialNumber("R1YX100J1ON");

        // Act
        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        // Assert
        assertEquals("Device name should include serial number suffix",
                "samsung SM-A145R - R1YX100J1ON", result);
    }

    /**
     * Verifies that when neither MDM device name nor serial number are set,
     * the default device name is returned as-is.
     *
     * <p><b>Scenario:</b> No MDM data is available (non-MDM device).
     *
     * <p><b>Expected Outcome:</b> The default name is returned unchanged.
     */
    @Test
    public void givenNoMdmData_whenBuildingName_thenDefaultNameIsUsed() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmDeviceName("");
        preyConfig.setMdmSerialNumber("");

        // Act
        String result = preyConfig.buildDeviceName("samsung SM-A145R");

        // Assert
        assertEquals("Default device name should be returned when no MDM data",
                "samsung SM-A145R", result);
    }

    /**
     * Verifies that MDM device name takes priority even when it differs significantly
     * from the actual device model.
     *
     * <p><b>Scenario:</b> MDM sets a custom organizational name unrelated to the device model.
     *
     * <p><b>Expected Outcome:</b> The MDM name is used, ignoring the default name entirely.
     */
    @Test
    public void givenMdmDeviceNameSet_whenBuildingName_thenSerialNumberIsIgnored() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmDeviceName("CEO-Phone-2026");
        preyConfig.setMdmSerialNumber("ABC123");

        // Act
        String result = preyConfig.buildDeviceName("Google Pixel 9");

        // Assert
        assertEquals("MDM device name should be used, serial number suffix should not be appended",
                "CEO-Phone-2026", result);
    }

    // =========================================================================
    // Tests for increaseData — hardware attributes sent to server
    // =========================================================================

    /**
     * Verifies that {@code increaseData} includes the serial number from MDM restrictions
     * in the hardware attributes parameters.
     *
     * <p><b>Scenario:</b> A serial number is stored in PreyConfig from MDM.
     *
     * <p><b>Expected Outcome:</b> The parameters map contains the MDM serial number
     * under the key {@code hardware_attributes[serial_number]}.
     */
    @Test
    public void givenSerialNumberFromMdm_whenIncreasingData_thenSerialNumberIsInParameters() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmSerialNumber("MDM-SN-9999");
        HashMap<String, String> parameters = new HashMap<String, String>();

        // Act
        parameters = PreyWebServices.getInstance().increaseData(context, parameters);

        // Assert
        assertEquals("Serial number from MDM should be in hardware_attributes",
                "MDM-SN-9999", parameters.get("hardware_attributes[serial_number]"));
    }

    /**
     * Verifies that {@code increaseData} sends an empty serial number when no MDM
     * serial number is configured.
     *
     * <p><b>Scenario:</b> No serial number is stored in PreyConfig (non-MDM device).
     *
     * <p><b>Expected Outcome:</b> The parameters map contains an empty string
     * under the key {@code hardware_attributes[serial_number]}.
     */
    @Test
    public void givenNoSerialNumber_whenIncreasingData_thenSerialNumberIsEmpty() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmSerialNumber("");
        HashMap<String, String> parameters = new HashMap<String, String>();

        // Act
        parameters = PreyWebServices.getInstance().increaseData(context, parameters);

        // Assert
        assertEquals("Serial number should be empty when no MDM data",
                "", parameters.get("hardware_attributes[serial_number]"));
    }

    // =========================================================================
    // Tests for resolveImei — IMEI resolution for device registration
    // =========================================================================

    /**
     * Verifies that when an IMEI is set from MDM, {@code resolveImei} returns it.
     *
     * <p><b>Scenario:</b> MDM provides a real IMEI via restrictions.
     *
     * <p><b>Expected Outcome:</b> The MDM IMEI is returned.
     */
    @Test
    public void givenMdmImei_whenResolvingImei_thenMdmImeiIsReturned() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmImei("354123456789012");

        // Act
        String result = PreyConfig.getPreyConfig(context).resolveImei();

        // Assert
        assertEquals("IMEI from MDM should be returned",
                "354123456789012", result);
    }

    /**
     * Verifies that when no MDM IMEI is set, {@code resolveImei} falls back to
     * the Android device ID.
     *
     * <p><b>Scenario:</b> No MDM IMEI is configured (non-MDM device).
     *
     * <p><b>Expected Outcome:</b> The Android device ID is returned (a non-empty string).
     */
    @Test
    public void givenNoMdmImei_whenResolvingImei_thenAndroidDeviceIdIsReturned() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmImei("");

        // Act
        String result = PreyConfig.getPreyConfig(context).resolveImei();

        // Assert
        assertTrue("Android device ID should be returned when no MDM IMEI",
                result != null && !result.isEmpty());
        // Should NOT be the MDM IMEI we cleared
        assertFalse("Result should not be empty string",
                "".equals(result));
    }

    /**
     * Verifies that when an MDM IMEI is set, the Android device ID is not used.
     *
     * <p><b>Scenario:</b> MDM provides an IMEI that differs from the Android device ID.
     *
     * <p><b>Expected Outcome:</b> The MDM IMEI is returned, not the Android device ID.
     */
    @Test
    public void givenMdmImei_whenResolvingImei_thenAndroidDeviceIdIsNotUsed() {
        // Arrange
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setMdmImei("999000111222333");

        // Act
        String result = PreyConfig.getPreyConfig(context).resolveImei();

        // Assert
        assertEquals("MDM IMEI should take priority over Android device ID",
                "999000111222333", result);
    }

    /**
     * Creates a list of HttpDataService objects containing location data ready to be sent.
     *
     * @return An ArrayList containing the HttpDataService object. Returns an empty list if the input location is null.
     */
    public ArrayList<HttpDataService> createLocationDataPayload() {
        HttpDataService data = new HttpDataService("location");
        data.setList(true);
        HashMap<String, String> parametersMap = new HashMap<String, String>();
        PreyLocation lastLocation = null;
        try {
            lastLocation = new PreyLocation(0, 0, 0f, 0, sdf.parse("2023-09-03T18:29:56.000Z").getTime(), "native");
        } catch (Exception e) {
            PreyLogger.e("Failed to parse location date", e);
        }
        parametersMap.put(LocationUtil.LAT, Double.toString(lastLocation.getLat()));
        parametersMap.put(LocationUtil.LNG, Double.toString(lastLocation.getLng()));
        parametersMap.put(LocationUtil.ACC, Float.toString(Math.round(lastLocation.getAccuracy())));
        parametersMap.put(LocationUtil.METHOD, lastLocation.getMethod());
        data.addDataListAll(parametersMap);
        ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
        dataToBeSent.add(data);
        return dataToBeSent;
    }

}