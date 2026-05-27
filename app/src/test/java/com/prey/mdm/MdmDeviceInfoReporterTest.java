package com.prey.mdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MdmDeviceInfoReporterTest {

    private Context context;
    private PreyConfig preyConfig;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        preyConfig = PreyConfig.getPreyConfig(context);
        preyConfig.setDeviceId("");
        preyConfig.setMdmSerialNumber("");
        preyConfig.setMdmImei("");
    }

    @After
    public void tearDown() {
        preyConfig.setDeviceId("");
        preyConfig.setMdmSerialNumber("");
        preyConfig.setMdmImei("");
        MdmDeviceInfoReporter.resetTransportForTests();
    }

    @Test
    public void isEnabled_requiresSetupKeyAndDeviceKey() {
        assertFalse(MdmDeviceInfoReporter.isEnabled("https://mdm.example", "", "device"));
        assertFalse(MdmDeviceInfoReporter.isEnabled("https://mdm.example", "setup", ""));
        assertTrue(MdmDeviceInfoReporter.isEnabled("https://mdm.example", "setup", "device"));
    }

    @Test
    public void reportNow_sendsExpectedEndpointAuthAndPayload() throws Exception {
        preyConfig.setMdmSerialNumber("R8YX100J1WN");
        preyConfig.setMdmImei("123456789012345");

        FakeTransport transport = new FakeTransport();
        MdmDeviceInfoReporter.setTransportForTests(transport);

        MdmDeviceInfoReporter.reportNow(context, "setup-key-xyz", "device-key-123", "Samsung A14");

        String expectedUrl = preyConfig.getPreyUrl()
                + FileConfigReader.getInstance(context).getApiV2()
                + "devices/device-key-123/mdm";
        assertEquals(expectedUrl, transport.url);
        assertEquals("setup-key-xyz", transport.user);
        assertEquals("x", transport.password);
        assertEquals("Samsung A14", transport.body.getString("name"));
        assertEquals("Android", transport.body.getString("os"));
        assertEquals("R8YX100J1WN", transport.body.getString("serial_number"));
        assertEquals("123456789012345", transport.body.getString("imei"));
    }

    private static class FakeTransport implements MdmDeviceInfoReporter.Transport {
        String url;
        String user;
        String password;
        JSONObject body;

        @Override
        public void postJson(String url, String user, String password, JSONObject body) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.body = body;
        }
    }
}
