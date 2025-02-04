/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.content.Context
import android.content.res.Resources
import java.io.IOException
import java.util.Properties

class FileConfigReader private constructor(ctx: Context) {
    private val properties = Properties()

    init {
        try {
            PreyLogger.d("Loading config properties from file...")
            val file = ctx.resources.openRawResource(R.raw.config)
            properties.load(file)
            file.close()
            PreyLogger.d("Config: $properties")
        } catch (e: Resources.NotFoundException) {
            PreyLogger.e("Config file wasn't found", e)
        } catch (e: IOException) {
            PreyLogger.e("Couldn't read config file", e)
        }
    }

    val preyCampaign: String
        get() = properties.getProperty("prey-campaign")

    val preyPanel: String
        get() = properties.getProperty("prey-panel")

    val agreementId: String
        get() = properties.getProperty("agreement-id")

    val gcmId: String
        get() = properties.getProperty("gcm-id")

    val gcmIdPrefix: String
        get() = properties.getProperty("gcm-id-prefix")

    fun getc2dmAction(): String {
        return properties.getProperty("c2dm-action")
    }

    fun getc2dmMessageSync(): String {
        return properties.getProperty("c2dm-message-sync")
    }

    val preyDomain: String
        get() = properties.getProperty("prey-domain")

    val preySubdomain: String
        get() = properties.getProperty("prey-subdomain")

    val preyUninstall: String
        get() = properties.getProperty("prey-uninstall")

    val preyUninstallEs: String
        get() = properties.getProperty("prey-uninstall-es")

    val preyMinorVersion: String
        get() = properties.getProperty("prey-minor-version")

    val isAskForPassword: Boolean
        get() = properties.getProperty("ask-for-password").toBoolean()

    val isLogEnabled: Boolean
        get() = properties.getProperty("log-enabled").toBoolean()

    val emailFeedback: String
        get() = properties.getProperty("email-feedback")

    val subjectFeedback: String
        get() = properties.getProperty("subject-feedback")

    val apiV2: String
        get() = properties.getProperty("api-v2")

    val isScheduled: Boolean
        get() = properties.getProperty("scheduled").toBoolean()

    val isOverOtherApps: Boolean
        get() = properties.getProperty("over-other-apps").toBoolean()

    val minuteScheduled: Int
        get() = properties.getProperty("minute-scheduled").toInt()

    val timeoutReport: Int
        get() = properties.getProperty("timeout-report").toInt()

    val geofenceMaximumAccuracy: Int
        get() = properties.getProperty("geofence-maximum-accuracy").toInt()

    val preyJwt: String
        get() = properties.getProperty("prey-jwt")

    val preyGooglePlay: String
        get() = properties.getProperty("prey-google-play")

    val geofenceLoiteringDelay: Int
        get() = properties.getProperty("geofence-loitering-delay").toInt()

    val preyEventsLogs: String
        get() = properties.getProperty("prey-events-logs")

    val distanceLocation: Int
        get() = properties.getProperty("distance-location").toInt()

    val geofenceNotificationResponsiveness: Int
        get() = properties.getProperty("geofence-notification-responsiveness").toInt()

    val flyerKey: String
        get() = properties.getProperty("flyer-key")

    val distanceAware: Int
        get() = properties.getProperty("distance-aware").toInt()

    val radiusAware: Int
        get() = properties.getProperty("radius-aware").toInt()

    val preyTerms: String
        get() = properties.getProperty("prey-terms")

    val preyTermsEs: String
        get() = properties.getProperty("prey-terms-es")

    val preyForgot: String
        get() = properties.getProperty("prey-forgot")

    val openPin: Boolean
        /**
         * Method if it should show pin
         * @return true o false
         */
        get() = properties.getProperty("open-pin").toBoolean()

    companion object {
        private var _instance: FileConfigReader? = null
        fun getInstance(ctx: Context): FileConfigReader? {
            if (_instance == null) _instance = FileConfigReader(ctx)
            return _instance
        }
    }
}