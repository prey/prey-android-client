package com.prey.actions.aware;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.prey.PreyLogger;
import com.prey.actions.location.PreyLocation;
import com.prey.net.PreyHttpResponse;

import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;

public class AwareControllerTest {
    Context ctx;
    private PreyLocation locationNull;
    private PreyLocation locationZero;
    private PreyLocation location01;
    private PreyLocation location02;
    private PreyLocation location03;
    private PreyLocation location04;
    private PreyLocation location05;
    private PreyLocation location06;
    private PreyLocation location07;
    private PreyLocation location08;
    private PreyLocation location09;
    private PreyLocation location10;
    private PreyLocation location11;
    private PreyLocation location12;
    private PreyLocation location13;
    private PreyLocation location14;
    private PreyLocation location15;
    private PreyLocation location16;
    private PreyLocation location17;
    private PreyLocation location18;
    private PreyLocation location19;
    private PreyLocation location20;
    private PreyLocation location21;
    private PreyLocation location22;
    private PreyLocation location23;
    private PreyLocation location24;
    private PreyLocation location25;
    private PreyLocation location26;
    private PreyLocation location27;
    private PreyLocation location28;
    private PreyLocation location29;
    private PreyLocation location30;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Before
    public void setUp() throws Exception {
        ctx = ApplicationProvider.getApplicationContext();
        locationNull = null;
        locationZero = new PreyLocation(0, 0, 0f, 0, sdf.parse("2023-09-03T18:29:56.000Z").getTime(), "native");
        location01 = new PreyLocation(38.7166081, -9.1765765, 3.83f, 0, sdf.parse("2023-09-03T18:29:56.000Z").getTime(), "native");
        location02 = new PreyLocation(38.720958, -9.1733035, 3.83f, 0, sdf.parse("2023-09-03T18:30:36.000Z").getTime(), "native");
        location03 = new PreyLocation(38.722818, -9.1679742, 3.83f, 0, sdf.parse("2023-09-03T18:31:11.000Z").getTime(), "native");
        location04 = new PreyLocation(38.7210003, -9.1620796, 3.83f, 0, sdf.parse("2023-09-03T18:32:16.000Z").getTime(), "native");
        location05 = new PreyLocation(38.716213, -9.154899, 3.83f, 0, sdf.parse("2023-09-03T18:35:36.000Z").getTime(), "native");
        location06 = new PreyLocation(38.7199697, -9.1506466, 3.83f, 0, sdf.parse("2023-09-03T21:45:05.000Z").getTime(), "native");
        location07 = new PreyLocation(38.7213485, -9.1483292, 3.83f, 0, sdf.parse("2023-09-03T21:45:49.000Z").getTime(), "native");
        location08 = new PreyLocation(38.7258066, -9.1496634, 3.83f, 0, sdf.parse("2023-09-03T21:47:59.000Z").getTime(), "native");
        location09 = new PreyLocation(38.7304543, -9.1504832, 3.83f, 0, sdf.parse("2023-09-03T21:52:54.000Z").getTime(), "native");
        location10 = new PreyLocation(38.7257845, -9.1508771, 3.83f, 0, sdf.parse("2023-09-03T21:58:54.000Z").getTime(), "native");
        location11 = new PreyLocation(38.7297893, -9.147341, 3.83f, 0, sdf.parse("2023-09-04T11:05:10.000Z").getTime(), "native");
        location12 = new PreyLocation(38.7281653, -9.1484635, 3.79f, 0, sdf.parse("2023-09-04T11:05:48.000Z").getTime(), "native");
        location13 = new PreyLocation(38.7330589, -9.1536148, 27.95f, 0, sdf.parse("2023-09-04T11:21:52.000Z").getTime(), "native");
        location14 = new PreyLocation(38.8898924, -9.1957024, 3.79f, 0, sdf.parse("2023-09-04T12:55:18.000Z").getTime(), "native");
        location15 = new PreyLocation(38.8945217, -9.196622, 3.79f, 0, sdf.parse("2023-09-04T12:55:38.000Z").getTime(), "native");
        location16 = new PreyLocation(38.9173817, -9.2112138, 3.81f, 0, sdf.parse("2023-09-04T12:57:15.000Z").getTime(), "native");
        location17 = new PreyLocation(38.9259691, -9.2169652, 3.81f, 0, sdf.parse("2023-09-04T12:57:50.000Z").getTime(), "native");
        location18 = new PreyLocation(38.9308599, -9.2179385, 3.81f, 0, sdf.parse("2023-09-04T12:58:08.000Z").getTime(), "native");
        location19 = new PreyLocation(38.9357081, -9.2190566, 3.81f, 0, sdf.parse("2023-09-04T12:58:26.000Z").getTime(), "native");
        location20 = new PreyLocation(38.9395969, -9.2150528, 3.81f, 0, sdf.parse("2023-09-04T12:58:45.000Z").getTime(), "native");
        location21 = new PreyLocation(38.6947511, -9.2131757, 12.52f, 0, sdf.parse("2023-09-04T12:59:03.000Z").getTime(), "native");
        location22 = new PreyLocation(38.7225368, -9.1696888, 3.79f, 0, sdf.parse("2023-09-04T09:46:30.000Z").getTime(), "native");
        location23 = new PreyLocation(38.7330589, -9.1536148, 27.95f, 0, sdf.parse("2023-09-04T11:21:52.000Z").getTime(), "native");
        location24 = new PreyLocation(38.8945217, -9.196622, 3.79f, 0, sdf.parse("2023-09-04T12:55:38.000Z").getTime(), "native");
        location25 = new PreyLocation(38.908392, -9.2059572, 3.79f, 0, sdf.parse("2023-09-04T12:56:38.000Z").getTime(), "native");
        location26 = new PreyLocation(38.9429847, -9.2105076, 3.81f, 0, sdf.parse("2023-09-04T12:59:03.000Z").getTime(), "native");
        location27 = new PreyLocation(39.0441557, -9.2378003, 3.79f, 0, sdf.parse("2023-09-04T13:05:50.000Z").getTime(), "native");
        location28 = new PreyLocation(39.9127509, -8.7990496, 3.f, 0, sdf.parse("2023-09-04T14:06:20.000Z").getTime(), "native");
        location29 = new PreyLocation(40.1578164, -8.8578124, 12.62f, 0, sdf.parse("2023-09-04T19:16:26.000Z").getTime(), "native");
        location30 = new PreyLocation(40.1586107, -8.8488221, 13.7f, 0, sdf.parse("2023-09-04T19:16:32.000Z").getTime(), "native");
    }

    @Test
    public void distance200() {
        int maxDistance = 200;
        assertFalse(AwareController.mustSendAware(ctx, locationNull, locationNull, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, locationNull, location01, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, location02, location01, maxDistance));//.561
        assertTrue(AwareController.mustSendAware(ctx, location03, location02, maxDistance));//.507
        assertTrue(AwareController.mustSendAware(ctx, location04, location03, maxDistance));//.551
        assertTrue(AwareController.mustSendAware(ctx, location05, location04, maxDistance));//.820
        assertTrue(AwareController.mustSendAware(ctx, location06, location05, maxDistance));//.557
        assertTrue(AwareController.mustSendAware(ctx, location07, location06, maxDistance));//.253
        assertTrue(AwareController.mustSendAware(ctx, location08, location07, maxDistance));//.508
        assertTrue(AwareController.mustSendAware(ctx, location09, location08, maxDistance));//.521
        assertTrue(AwareController.mustSendAware(ctx, location10, location09, maxDistance));//.520
        assertTrue(AwareController.mustSendAware(ctx, location11, location10, maxDistance));//.541
        assertTrue(AwareController.mustSendAware(ctx, location12, location11, maxDistance));//.21
        assertTrue(AwareController.mustSendAware(ctx, location13, location12, maxDistance));//.70
        assertTrue(AwareController.mustSendAware(ctx, location14, location13, maxDistance));//17.83
        assertTrue(AwareController.mustSendAware(ctx, location15, location14, maxDistance));//.52
        assertTrue(AwareController.mustSendAware(ctx, location16, location15, maxDistance));//2.84
        assertTrue(AwareController.mustSendAware(ctx, location17, location16, maxDistance));//1.08
        assertTrue(AwareController.mustSendAware(ctx, location18, location17, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location19, location18, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location20, location19, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location21, location20, maxDistance));//27.25
        assertTrue(AwareController.mustSendAware(ctx, location22, location21, maxDistance));//4.8
        assertTrue(AwareController.mustSendAware(ctx, location23, location22, maxDistance));//1.8
        assertTrue(AwareController.mustSendAware(ctx, location24, location23, maxDistance));//18.35
        assertTrue(AwareController.mustSendAware(ctx, location25, location24, maxDistance));//1.74
        assertTrue(AwareController.mustSendAware(ctx, location26, location25, maxDistance));//3.87
        assertTrue(AwareController.mustSendAware(ctx, location27, location26, maxDistance));//11.51
        assertTrue(AwareController.mustSendAware(ctx, location28, location27, maxDistance));//103.76
        assertTrue(AwareController.mustSendAware(ctx, location29, location28, maxDistance));//27.73
        assertTrue(AwareController.mustSendAware(ctx, location30, location29, maxDistance));//.77
    }

    @Test
    public void distance300() {
        int maxDistance = 300;
        assertFalse(AwareController.mustSendAware(ctx, locationNull, locationNull, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, locationNull, location01, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, location02, location01, maxDistance));//.561
        assertTrue(AwareController.mustSendAware(ctx, location03, location02, maxDistance));//.507
        assertTrue(AwareController.mustSendAware(ctx, location04, location03, maxDistance));//.551
        assertTrue(AwareController.mustSendAware(ctx, location05, location04, maxDistance));//.820
        assertTrue(AwareController.mustSendAware(ctx, location06, location05, maxDistance));//.557
        assertFalse(AwareController.mustSendAware(ctx, location07, location06, maxDistance));//.253
        assertTrue(AwareController.mustSendAware(ctx, location08, location07, maxDistance));//.508
        assertTrue(AwareController.mustSendAware(ctx, location09, location08, maxDistance));//.521
        assertTrue(AwareController.mustSendAware(ctx, location10, location09, maxDistance));//.520
        assertTrue(AwareController.mustSendAware(ctx, location11, location10, maxDistance));//.541
        assertFalse(AwareController.mustSendAware(ctx, location12, location11, maxDistance));//.21
        assertTrue(AwareController.mustSendAware(ctx, location13, location12, maxDistance));//.70
        assertTrue(AwareController.mustSendAware(ctx, location14, location13, maxDistance));//17.83
        assertTrue(AwareController.mustSendAware(ctx, location15, location14, maxDistance));//.52
        assertTrue(AwareController.mustSendAware(ctx, location16, location15, maxDistance));//2.84
        assertTrue(AwareController.mustSendAware(ctx, location17, location16, maxDistance));//1.08
        assertTrue(AwareController.mustSendAware(ctx, location18, location17, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location19, location18, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location20, location19, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location21, location20, maxDistance));//27.25
        assertTrue(AwareController.mustSendAware(ctx, location22, location21, maxDistance));//4.8
        assertTrue(AwareController.mustSendAware(ctx, location23, location22, maxDistance));//1.8
        assertTrue(AwareController.mustSendAware(ctx, location24, location23, maxDistance));//18.35
        assertTrue(AwareController.mustSendAware(ctx, location25, location24, maxDistance));//1.74
        assertTrue(AwareController.mustSendAware(ctx, location26, location25, maxDistance));//3.87
        assertTrue(AwareController.mustSendAware(ctx, location27, location26, maxDistance));//11.51
        assertTrue(AwareController.mustSendAware(ctx, location28, location27, maxDistance));//103.76
        assertTrue(AwareController.mustSendAware(ctx, location29, location28, maxDistance));//27.73
        assertTrue(AwareController.mustSendAware(ctx, location30, location29, maxDistance));//.77
    }

    @Test
    public void distance500() {
        int maxDistance = 500;
        assertFalse(AwareController.mustSendAware(ctx, locationNull, locationNull, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, locationNull, location01, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, location02, location01, maxDistance));//.561
        assertTrue(AwareController.mustSendAware(ctx, location03, location02, maxDistance));//.507
        assertTrue(AwareController.mustSendAware(ctx, location04, location03, maxDistance));//.551
        assertTrue(AwareController.mustSendAware(ctx, location05, location04, maxDistance));//.820
        assertTrue(AwareController.mustSendAware(ctx, location06, location05, maxDistance));//.557
        assertFalse(AwareController.mustSendAware(ctx, location07, location06, maxDistance));//.253
        assertTrue(AwareController.mustSendAware(ctx, location08, location07, maxDistance));//.508
        assertTrue(AwareController.mustSendAware(ctx, location09, location08, maxDistance));//.521
        assertTrue(AwareController.mustSendAware(ctx, location10, location09, maxDistance));//.520
        assertTrue(AwareController.mustSendAware(ctx, location11, location10, maxDistance));//.541
        assertFalse(AwareController.mustSendAware(ctx, location12, location11, maxDistance));//.21
        assertTrue(AwareController.mustSendAware(ctx, location13, location12, maxDistance));//.70
        assertTrue(AwareController.mustSendAware(ctx, location14, location13, maxDistance));//17.83
        assertTrue(AwareController.mustSendAware(ctx, location15, location14, maxDistance));//.52
        assertTrue(AwareController.mustSendAware(ctx, location16, location15, maxDistance));//2.84
        assertTrue(AwareController.mustSendAware(ctx, location17, location16, maxDistance));//1.08
        assertTrue(AwareController.mustSendAware(ctx, location18, location17, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location19, location18, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location20, location19, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location21, location20, maxDistance));//27.25
        assertTrue(AwareController.mustSendAware(ctx, location22, location21, maxDistance));//4.8
        assertTrue(AwareController.mustSendAware(ctx, location23, location22, maxDistance));//1.8
        assertTrue(AwareController.mustSendAware(ctx, location24, location23, maxDistance));//18.35
        assertTrue(AwareController.mustSendAware(ctx, location25, location24, maxDistance));//1.74
        assertTrue(AwareController.mustSendAware(ctx, location26, location25, maxDistance));//3.87
        assertTrue(AwareController.mustSendAware(ctx, location27, location26, maxDistance));//11.51
        assertTrue(AwareController.mustSendAware(ctx, location28, location27, maxDistance));//103.76
        assertTrue(AwareController.mustSendAware(ctx, location29, location28, maxDistance));//27.73
        assertTrue(AwareController.mustSendAware(ctx, location30, location29, maxDistance));//.77
    }

    @Test
    public void distance800() {
        int maxDistance = 800;
        assertFalse(AwareController.mustSendAware(ctx, locationNull, locationNull, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, locationNull, location01, maxDistance));
        assertFalse(AwareController.mustSendAware(ctx, location02, location01, maxDistance));//.561
        assertFalse(AwareController.mustSendAware(ctx, location03, location02, maxDistance));//.507
        assertFalse(AwareController.mustSendAware(ctx, location04, location03, maxDistance));//.551
        assertTrue(AwareController.mustSendAware(ctx, location05, location04, maxDistance));//.820
        assertFalse(AwareController.mustSendAware(ctx, location06, location05, maxDistance));//.557
        assertFalse(AwareController.mustSendAware(ctx, location07, location06, maxDistance));//.253
        assertFalse(AwareController.mustSendAware(ctx, location08, location07, maxDistance));//.508
        assertFalse(AwareController.mustSendAware(ctx, location09, location08, maxDistance));//.521
        assertFalse(AwareController.mustSendAware(ctx, location10, location09, maxDistance));//.520
        assertFalse(AwareController.mustSendAware(ctx, location11, location10, maxDistance));//.541
        assertFalse(AwareController.mustSendAware(ctx, location12, location11, maxDistance));//.21
        assertFalse(AwareController.mustSendAware(ctx, location13, location12, maxDistance));//.70
        assertTrue(AwareController.mustSendAware(ctx, location14, location13, maxDistance));//17.83
        assertFalse(AwareController.mustSendAware(ctx, location15, location14, maxDistance));//.52
        assertTrue(AwareController.mustSendAware(ctx, location16, location15, maxDistance));//2.84
        assertTrue(AwareController.mustSendAware(ctx, location17, location16, maxDistance));//1.08
        assertFalse(AwareController.mustSendAware(ctx, location18, location17, maxDistance));//.55
        assertFalse(AwareController.mustSendAware(ctx, location19, location18, maxDistance));//.55
        assertFalse(AwareController.mustSendAware(ctx, location20, location19, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location21, location20, maxDistance));//27.25
        assertTrue(AwareController.mustSendAware(ctx, location22, location21, maxDistance));//4.8
        assertTrue(AwareController.mustSendAware(ctx, location23, location22, maxDistance));//1.8
        assertTrue(AwareController.mustSendAware(ctx, location24, location23, maxDistance));//18.35
        assertTrue(AwareController.mustSendAware(ctx, location25, location24, maxDistance));//1.74
        assertTrue(AwareController.mustSendAware(ctx, location26, location25, maxDistance));//3.87
        assertTrue(AwareController.mustSendAware(ctx, location27, location26, maxDistance));//11.51
        assertTrue(AwareController.mustSendAware(ctx, location28, location27, maxDistance));//103.76
        assertTrue(AwareController.mustSendAware(ctx, location29, location28, maxDistance));//27.73
        assertFalse(AwareController.mustSendAware(ctx, location30, location29, maxDistance));//.77
    }

    @Test
    public void distance1000() {
        int maxDistance = 1000;
        assertFalse(AwareController.mustSendAware(ctx, locationNull, locationNull, maxDistance));
        assertTrue(AwareController.mustSendAware(ctx, locationNull, location01, maxDistance));
        assertFalse(AwareController.mustSendAware(ctx, location02, location01, maxDistance));//.561
        assertFalse(AwareController.mustSendAware(ctx, location03, location02, maxDistance));//.507
        assertFalse(AwareController.mustSendAware(ctx, location04, location03, maxDistance));//.551
        assertFalse(AwareController.mustSendAware(ctx, location05, location04, maxDistance));//.820
        assertFalse(AwareController.mustSendAware(ctx, location06, location05, maxDistance));//.557
        assertFalse(AwareController.mustSendAware(ctx, location07, location06, maxDistance));//.253
        assertFalse(AwareController.mustSendAware(ctx, location08, location07, maxDistance));//.508
        assertFalse(AwareController.mustSendAware(ctx, location09, location08, maxDistance));//.521
        assertFalse(AwareController.mustSendAware(ctx, location10, location09, maxDistance));//.520
        assertFalse(AwareController.mustSendAware(ctx, location11, location10, maxDistance));//.541
        assertFalse(AwareController.mustSendAware(ctx, location12, location11, maxDistance));//.21
        assertFalse(AwareController.mustSendAware(ctx, location13, location12, maxDistance));//.70
        assertTrue(AwareController.mustSendAware(ctx, location14, location13, maxDistance));//17.83
        assertFalse(AwareController.mustSendAware(ctx, location15, location14, maxDistance));//.52
        assertTrue(AwareController.mustSendAware(ctx, location16, location15, maxDistance));//2.84
        assertTrue(AwareController.mustSendAware(ctx, location17, location16, maxDistance));//1.08
        assertFalse(AwareController.mustSendAware(ctx, location18, location17, maxDistance));//.55
        assertFalse(AwareController.mustSendAware(ctx, location19, location18, maxDistance));//.55
        assertFalse(AwareController.mustSendAware(ctx, location20, location19, maxDistance));//.55
        assertTrue(AwareController.mustSendAware(ctx, location21, location20, maxDistance));//27.25
        assertTrue(AwareController.mustSendAware(ctx, location22, location21, maxDistance));//4.8
        assertTrue(AwareController.mustSendAware(ctx, location23, location22, maxDistance));//1.8
        assertTrue(AwareController.mustSendAware(ctx, location24, location23, maxDistance));//18.35
        assertTrue(AwareController.mustSendAware(ctx, location25, location24, maxDistance));//1.74
        assertTrue(AwareController.mustSendAware(ctx, location26, location25, maxDistance));//3.87
        assertTrue(AwareController.mustSendAware(ctx, location27, location26, maxDistance));//11.51
        assertTrue(AwareController.mustSendAware(ctx, location28, location27, maxDistance));//103.76
        assertTrue(AwareController.mustSendAware(ctx, location29, location28, maxDistance));//27.73
        assertFalse(AwareController.mustSendAware(ctx, location30, location29, maxDistance));//.77
    }

    @Test
    public void mustSendAware() {
        try {
            assertNull(AwareController.sendNowAware(ctx, locationNull));
            assertNull(AwareController.sendNowAware(ctx, locationZero));
            PreyHttpResponse response = AwareController.sendNowAware(ctx, location01);
            assertNotNull(response);
            assertEquals(response.getStatusCode(), HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            PreyLogger.e("error mustSendAware:" + e.getMessage(), e);
        }
    }

}