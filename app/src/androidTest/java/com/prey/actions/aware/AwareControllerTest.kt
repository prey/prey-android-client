package com.prey.actions.aware

import org.junit.Assert.*
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.prey.PreyLogger
import com.prey.actions.location.PreyLocation
import com.prey.net.PreyHttpResponse
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.text.SimpleDateFormat

class AwareControllerTest {

    var ctx: Context? = null
    private var locationNull: PreyLocation? = null
    private var locationZero: PreyLocation? = null
    private var location01: PreyLocation? = null
    private var location02: PreyLocation? = null
    private var location03: PreyLocation? = null
    private var location04: PreyLocation? = null
    private var location05: PreyLocation? = null
    private var location06: PreyLocation? = null
    private var location07: PreyLocation? = null
    private var location08: PreyLocation? = null
    private var location09: PreyLocation? = null
    private var location10: PreyLocation? = null
    private var location11: PreyLocation? = null
    private var location12: PreyLocation? = null
    private var location13: PreyLocation? = null
    private var location14: PreyLocation? = null
    private var location15: PreyLocation? = null
    private var location16: PreyLocation? = null
    private var location17: PreyLocation? = null
    private var location18: PreyLocation? = null
    private var location19: PreyLocation? = null
    private var location20: PreyLocation? = null
    private var location21: PreyLocation? = null
    private var location22: PreyLocation? = null
    private var location23: PreyLocation? = null
    private var location24: PreyLocation? = null
    private var location25: PreyLocation? = null
    private var location26: PreyLocation? = null
    private var location27: PreyLocation? = null
    private var location28: PreyLocation? = null
    private var location29: PreyLocation? = null
    private var location30: PreyLocation? = null

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        locationNull = null
        locationZero =
            PreyLocation(0, 0, 0f, 0f, sdf.parse("2023-09-03T18:29:56.000Z").time, "native")
        location01 = PreyLocation(
            38.7166081,
            -9.1765765,
            3.83f,
            0f,
            sdf.parse("2023-09-03T18:29:56.000Z").time,
            "native"
        )
        location02 = PreyLocation(
            38.720958,
            -9.1733035,
            3.83f,
            0f,
            sdf.parse("2023-09-03T18:30:36.000Z").time,
            "native"
        )
        location03 = PreyLocation(
            38.722818,
            -9.1679742,
            3.83f,
            0f,
            sdf.parse("2023-09-03T18:31:11.000Z").time,
            "native"
        )
        location04 = PreyLocation(
            38.7210003,
            -9.1620796,
            3.83f,
            0f,
            sdf.parse("2023-09-03T18:32:16.000Z").time,
            "native"
        )
        location05 = PreyLocation(
            38.716213,
            -9.154899,
            3.83f,
            0f,
            sdf.parse("2023-09-03T18:35:36.000Z").time,
            "native"
        )
        location06 = PreyLocation(
            38.7199697,
            -9.1506466,
            3.83f,
            0f,
            sdf.parse("2023-09-03T21:45:05.000Z").time,
            "native"
        )
        location07 = PreyLocation(
            38.7213485,
            -9.1483292,
            3.83f,
            0f,
            sdf.parse("2023-09-03T21:45:49.000Z").time,
            "native"
        )
        location08 = PreyLocation(
            38.7258066,
            -9.1496634,
            3.83f,
            0f,
            sdf.parse("2023-09-03T21:47:59.000Z").time,
            "native"
        )
        location09 = PreyLocation(
            38.7304543,
            -9.1504832,
            3.83f,
            0f,
            sdf.parse("2023-09-03T21:52:54.000Z").time,
            "native"
        )
        location10 = PreyLocation(
            38.7257845,
            -9.1508771,
            3.83f,
            0f,
            sdf.parse("2023-09-03T21:58:54.000Z").time,
            "native"
        )
        location11 = PreyLocation(
            38.7297893,
            -9.147341,
            3.83f,
            0f,
            sdf.parse("2023-09-04T11:05:10.000Z").time,
            "native"
        )
        location12 = PreyLocation(
            38.7281653,
            -9.1484635,
            3.79f,
            0f,
            sdf.parse("2023-09-04T11:05:48.000Z").time,
            "native"
        )
        location13 = PreyLocation(
            38.7330589,
            -9.1536148,
            27.95f,
            0f,
            sdf.parse("2023-09-04T11:21:52.000Z").time,
            "native"
        )
        location14 = PreyLocation(
            38.8898924,
            -9.1957024,
            3.79f,
            0f,
            sdf.parse("2023-09-04T12:55:18.000Z").time,
            "native"
        )
        location15 = PreyLocation(
            38.8945217,
            -9.196622,
            3.79f,
            0f,
            sdf.parse("2023-09-04T12:55:38.000Z").time,
            "native"
        )
        location16 = PreyLocation(
            38.9173817,
            -9.2112138,
            3.81f,
            0f,
            sdf.parse("2023-09-04T12:57:15.000Z").time,
            "native"
        )
        location17 = PreyLocation(
            38.9259691,
            -9.2169652,
            3.81f,
            0f,
            sdf.parse("2023-09-04T12:57:50.000Z").time,
            "native"
        )
        location18 = PreyLocation(
            38.9308599,
            -9.2179385,
            3.81f,
            0f,
            sdf.parse("2023-09-04T12:58:08.000Z").time,
            "native"
        )
        location19 = PreyLocation(
            38.9357081,
            -9.2190566,
            3.81f,
            0f,
            sdf.parse("2023-09-04T12:58:26.000Z").time,
            "native"
        )
        location20 = PreyLocation(
            38.9395969,
            -9.2150528,
            3.81f,
            0f,
            sdf.parse("2023-09-04T12:58:45.000Z").time,
            "native"
        )
        location21 = PreyLocation(
            38.6947511,
            -9.2131757,
            12.52f,
            0f,
            sdf.parse("2023-09-04T12:59:03.000Z").time,
            "native"
        )
        location22 = PreyLocation(
            38.7225368,
            -9.1696888,
            3.79f,
            0f,
            sdf.parse("2023-09-04T09:46:30.000Z").time,
            "native"
        )
        location23 = PreyLocation(
            38.7330589,
            -9.1536148,
            27.95f,
            0f,
            sdf.parse("2023-09-04T11:21:52.000Z").time,
            "native"
        )
        location24 = PreyLocation(
            38.8945217,
            -9.196622,
            3.79f,
            0f,
            sdf.parse("2023-09-04T12:55:38.000Z").time,
            "native"
        )
        location25 = PreyLocation(
            38.908392,
            -9.2059572,
            3.79f,
            0f,
            sdf.parse("2023-09-04T12:56:38.000Z").time,
            "native"
        )
        location26 = PreyLocation(
            38.9429847,
            -9.2105076,
            3.81f,
            0f,
            sdf.parse("2023-09-04T12:59:03.000Z").time,
            "native"
        )
        location27 = PreyLocation(
            39.0441557,
            -9.2378003,
            3.79f,
            0f,
            sdf.parse("2023-09-04T13:05:50.000Z").time,
            "native"
        )
        location28 = PreyLocation(
            39.9127509,
            -8.7990496,
            3f,
            0f,
            sdf.parse("2023-09-04T14:06:20.000Z").time,
            "native"
        )
        location29 = PreyLocation(
            40.1578164,
            -8.8578124,
            12.62f,
            0f,
            sdf.parse("2023-09-04T19:16:26.000Z").time,
            "native"
        )
        location30 = PreyLocation(
            40.1586107,
            -8.8488221,
            13.7f,
            0f,
            sdf.parse("2023-09-04T19:16:32.000Z").time,
            "native"
        )
    }

    @Test
    fun distance200() {
        val maxDistance = 200
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                locationNull,
                locationNull,
                maxDistance
            )
        )
        Assert.assertTrue(AwareController.getInstance().mustSendAware(ctx, locationNull, location01, maxDistance))
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location02,
                location01,
                maxDistance
            )
        ) //.561
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location03,
                location02,
                maxDistance
            )
        ) //.507
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location04,
                location03,
                maxDistance
            )
        ) //.551
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location05,
                location04,
                maxDistance
            )
        ) //.820
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location06,
                location05,
                maxDistance
            )
        ) //.557
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location07,
                location06,
                maxDistance
            )
        ) //.253
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location08,
                location07,
                maxDistance
            )
        ) //.508
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location09,
                location08,
                maxDistance
            )
        ) //.521
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location10,
                location09,
                maxDistance
            )
        ) //.520
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location11,
                location10,
                maxDistance
            )
        ) //.541
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location12,
                location11,
                maxDistance
            )
        ) //.21
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location13,
                location12,
                maxDistance
            )
        ) //.70
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location14,
                location13,
                maxDistance
            )
        ) //17.83
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location15,
                location14,
                maxDistance
            )
        ) //.52
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location16,
                location15,
                maxDistance
            )
        ) //2.84
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location17,
                location16,
                maxDistance
            )
        ) //1.08
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location18,
                location17,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location19,
                location18,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location20,
                location19,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location21,
                location20,
                maxDistance
            )
        ) //27.25
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location22,
                location21,
                maxDistance
            )
        ) //4.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location23,
                location22,
                maxDistance
            )
        ) //1.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location24,
                location23,
                maxDistance
            )
        ) //18.35
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location25,
                location24,
                maxDistance
            )
        ) //1.74
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location26,
                location25,
                maxDistance
            )
        ) //3.87
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location27,
                location26,
                maxDistance
            )
        ) //11.51
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location28,
                location27,
                maxDistance
            )
        ) //103.76
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location29,
                location28,
                maxDistance
            )
        ) //27.73
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location30,
                location29,
                maxDistance
            )
        ) //.77
    }

    @Test
    fun distance300() {
        val maxDistance = 300
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                locationNull,
                locationNull,
                maxDistance
            )
        )
        Assert.assertTrue(AwareController.getInstance().mustSendAware(ctx, locationNull, location01, maxDistance))
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location02,
                location01,
                maxDistance
            )
        ) //.561
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location03,
                location02,
                maxDistance
            )
        ) //.507
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location04,
                location03,
                maxDistance
            )
        ) //.551
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location05,
                location04,
                maxDistance
            )
        ) //.820
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location06,
                location05,
                maxDistance
            )
        ) //.557
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location07,
                location06,
                maxDistance
            )
        ) //.253
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location08,
                location07,
                maxDistance
            )
        ) //.508
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location09,
                location08,
                maxDistance
            )
        ) //.521
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location10,
                location09,
                maxDistance
            )
        ) //.520
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location11,
                location10,
                maxDistance
            )
        ) //.541
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location12,
                location11,
                maxDistance
            )
        ) //.21
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location13,
                location12,
                maxDistance
            )
        ) //.70
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location14,
                location13,
                maxDistance
            )
        ) //17.83
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location15,
                location14,
                maxDistance
            )
        ) //.52
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location16,
                location15,
                maxDistance
            )
        ) //2.84
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location17,
                location16,
                maxDistance
            )
        ) //1.08
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location18,
                location17,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location19,
                location18,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location20,
                location19,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location21,
                location20,
                maxDistance
            )
        ) //27.25
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location22,
                location21,
                maxDistance
            )
        ) //4.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location23,
                location22,
                maxDistance
            )
        ) //1.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location24,
                location23,
                maxDistance
            )
        ) //18.35
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location25,
                location24,
                maxDistance
            )
        ) //1.74
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location26,
                location25,
                maxDistance
            )
        ) //3.87
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location27,
                location26,
                maxDistance
            )
        ) //11.51
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location28,
                location27,
                maxDistance
            )
        ) //103.76
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location29,
                location28,
                maxDistance
            )
        ) //27.73
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location30,
                location29,
                maxDistance
            )
        ) //.77
    }

    @Test
    fun distance500() {
        val maxDistance = 500
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                locationNull,
                locationNull,
                maxDistance
            )
        )
        Assert.assertTrue(AwareController.getInstance().mustSendAware(ctx, locationNull, location01, maxDistance))
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location02,
                location01,
                maxDistance
            )
        ) //.561
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location03,
                location02,
                maxDistance
            )
        ) //.507
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location04,
                location03,
                maxDistance
            )
        ) //.551
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location05,
                location04,
                maxDistance
            )
        ) //.820
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location06,
                location05,
                maxDistance
            )
        ) //.557
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location07,
                location06,
                maxDistance
            )
        ) //.253
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location08,
                location07,
                maxDistance
            )
        ) //.508
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location09,
                location08,
                maxDistance
            )
        ) //.521
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location10,
                location09,
                maxDistance
            )
        ) //.520
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location11,
                location10,
                maxDistance
            )
        ) //.541
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location12,
                location11,
                maxDistance
            )
        ) //.21
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location13,
                location12,
                maxDistance
            )
        ) //.70
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location14,
                location13,
                maxDistance
            )
        ) //17.83
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location15,
                location14,
                maxDistance
            )
        ) //.52
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location16,
                location15,
                maxDistance
            )
        ) //2.84
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location17,
                location16,
                maxDistance
            )
        ) //1.08
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location18,
                location17,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location19,
                location18,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location20,
                location19,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location21,
                location20,
                maxDistance
            )
        ) //27.25
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location22,
                location21,
                maxDistance
            )
        ) //4.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location23,
                location22,
                maxDistance
            )
        ) //1.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location24,
                location23,
                maxDistance
            )
        ) //18.35
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location25,
                location24,
                maxDistance
            )
        ) //1.74
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location26,
                location25,
                maxDistance
            )
        ) //3.87
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location27,
                location26,
                maxDistance
            )
        ) //11.51
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location28,
                location27,
                maxDistance
            )
        ) //103.76
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location29,
                location28,
                maxDistance
            )
        ) //27.73
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location30,
                location29,
                maxDistance
            )
        ) //.77
    }

    @Test
    fun distance800() {
        val maxDistance = 800
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                locationNull,
                locationNull,
                maxDistance
            )
        )
        Assert.assertTrue(AwareController.getInstance().mustSendAware(ctx, locationNull, location01, maxDistance))
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location02,
                location01,
                maxDistance
            )
        ) //.561
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location03,
                location02,
                maxDistance
            )
        ) //.507
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location04,
                location03,
                maxDistance
            )
        ) //.551
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location05,
                location04,
                maxDistance
            )
        ) //.820
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location06,
                location05,
                maxDistance
            )
        ) //.557
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location07,
                location06,
                maxDistance
            )
        ) //.253
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location08,
                location07,
                maxDistance
            )
        ) //.508
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location09,
                location08,
                maxDistance
            )
        ) //.521
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location10,
                location09,
                maxDistance
            )
        ) //.520
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location11,
                location10,
                maxDistance
            )
        ) //.541
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location12,
                location11,
                maxDistance
            )
        ) //.21
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location13,
                location12,
                maxDistance
            )
        ) //.70
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location14,
                location13,
                maxDistance
            )
        ) //17.83
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location15,
                location14,
                maxDistance
            )
        ) //.52
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location16,
                location15,
                maxDistance
            )
        ) //2.84
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location17,
                location16,
                maxDistance
            )
        ) //1.08
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location18,
                location17,
                maxDistance
            )
        ) //.55
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location19,
                location18,
                maxDistance
            )
        ) //.55
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location20,
                location19,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location21,
                location20,
                maxDistance
            )
        ) //27.25
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location22,
                location21,
                maxDistance
            )
        ) //4.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location23,
                location22,
                maxDistance
            )
        ) //1.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location24,
                location23,
                maxDistance
            )
        ) //18.35
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location25,
                location24,
                maxDistance
            )
        ) //1.74
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location26,
                location25,
                maxDistance
            )
        ) //3.87
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location27,
                location26,
                maxDistance
            )
        ) //11.51
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location28,
                location27,
                maxDistance
            )
        ) //103.76
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location29,
                location28,
                maxDistance
            )
        ) //27.73
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location30,
                location29,
                maxDistance
            )
        ) //.77
    }

    @Test
    fun distance1000() {
        val maxDistance = 1000
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                locationNull,
                locationNull,
                maxDistance
            )
        )
        Assert.assertTrue(AwareController.getInstance().mustSendAware(ctx, locationNull, location01, maxDistance))
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location02,
                location01,
                maxDistance
            )
        ) //.561
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location03,
                location02,
                maxDistance
            )
        ) //.507
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location04,
                location03,
                maxDistance
            )
        ) //.551
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location05,
                location04,
                maxDistance
            )
        ) //.820
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location06,
                location05,
                maxDistance
            )
        ) //.557
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location07,
                location06,
                maxDistance
            )
        ) //.253
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location08,
                location07,
                maxDistance
            )
        ) //.508
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location09,
                location08,
                maxDistance
            )
        ) //.521
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location10,
                location09,
                maxDistance
            )
        ) //.520
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location11,
                location10,
                maxDistance
            )
        ) //.541
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location12,
                location11,
                maxDistance
            )
        ) //.21
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location13,
                location12,
                maxDistance
            )
        ) //.70
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location14,
                location13,
                maxDistance
            )
        ) //17.83
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location15,
                location14,
                maxDistance
            )
        ) //.52
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location16,
                location15,
                maxDistance
            )
        ) //2.84
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location17,
                location16,
                maxDistance
            )
        ) //1.08
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location18,
                location17,
                maxDistance
            )
        ) //.55
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location19,
                location18,
                maxDistance
            )
        ) //.55
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location20,
                location19,
                maxDistance
            )
        ) //.55
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location21,
                location20,
                maxDistance
            )
        ) //27.25
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location22,
                location21,
                maxDistance
            )
        ) //4.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location23,
                location22,
                maxDistance
            )
        ) //1.8
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location24,
                location23,
                maxDistance
            )
        ) //18.35
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location25,
                location24,
                maxDistance
            )
        ) //1.74
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location26,
                location25,
                maxDistance
            )
        ) //3.87
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location27,
                location26,
                maxDistance
            )
        ) //11.51
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location28,
                location27,
                maxDistance
            )
        ) //103.76
        Assert.assertTrue(
            AwareController.getInstance().mustSendAware(
                ctx,
                location29,
                location28,
                maxDistance
            )
        ) //27.73
        Assert.assertFalse(
            AwareController.getInstance().mustSendAware(
                ctx,
                location30,
                location29,
                maxDistance
            )
        ) //.77
    }

    @Test
    fun mustSendAware() {
        try {
            Assert.assertNull(AwareController.getInstance().sendNowAware(ctx!!, locationNull))
            Assert.assertNull(AwareController.getInstance().sendNowAware(ctx!!, locationZero))
            val response: PreyHttpResponse? = AwareController.getInstance().sendNowAware(ctx!!, location01)
            Assert.assertNotNull(response)
            assertEquals(response!!.getStatusCode(), HttpURLConnection.HTTP_OK)
        } catch (e: Exception) {
            PreyLogger.e("error mustSendAware:" + e.message, e)
        }
    }
}