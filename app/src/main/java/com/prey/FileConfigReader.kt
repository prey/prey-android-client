/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.content.Context
import java.io.InputStream
import java.util.Properties

/**
 * A class representing a file-based configuration reader.
 *
 * This class loads configuration properties from a file named "config" in the raw resources.
 * It provides methods to retrieve specific configuration properties.
 */
class FileConfigReader private constructor(context: Context) {
    private val properties = Properties()

    /**
     * Initializes the FileConfigReader instance by loading the configuration properties from the file.
     */
    init {
        try {
            PreyLogger.d("Loading config properties from file...")
            val file = context.resources.openRawResource(R.raw.config)
            properties.load(file)
            file.close()
            PreyLogger.d("Config: $properties")
        } catch (e: Exception) {
            try {
                val input: InputStream = ClassLoader.getSystemResourceAsStream("config")
                properties.load(input)
            } catch (e: Exception) {
                PreyLogger.e("Config file wasn't found", e)
            }
        }
    }

    fun getPreyCampaign(): String {
        return properties.getProperty("prey-campaign")
    }

    fun getPreyPanel(): String {
        return properties.getProperty("prey-panel")
    }

    fun getAgreementId(): String {
        return properties.getProperty("agreement-id")
    }

    fun getGcmId(): String {
        return properties.getProperty("gcm-id")
    }

    fun getGcmIdPrefix(): String {
        return properties.getProperty("gcm-id-prefix")
    }

    fun getc2dmAction(): String {
        return properties.getProperty("c2dm-action")
    }

    fun getc2dmMessageSync(): String {
        return properties.getProperty("c2dm-message-sync")
    }

    fun getPreyDomain(): String {
        return properties.getProperty("prey-domain")
    }

    fun getPreySubdomain(): String {
        return properties.getProperty("prey-subdomain")
    }

    fun getPreyUninstall(): String {
        return properties.getProperty("prey-uninstall")
    }

    fun getPreyUninstallEs(): String {
        return properties.getProperty("prey-uninstall-es")
    }

    fun getPreyMinorVersion(): String {
        return properties.getProperty("prey-minor-version")
    }

    fun isAskForPassword(): Boolean {
        return properties.getProperty("ask-for-password").toBoolean()
    }

    fun isLogEnabled(): Boolean {
        return properties.getProperty("log-enabled").toBoolean()
    }

    fun getEmailFeedback(): String {
        return properties.getProperty("email-feedback")
    }

    fun getSubjectFeedback(): String {
        return properties.getProperty("subject-feedback")
    }

    fun getApiV2(): String {
        return properties.getProperty("api-v2")
    }

    fun isScheduled(): Boolean {
        return properties.getProperty("scheduled").toBoolean()
    }

    fun isOverOtherApps(): Boolean {
        return properties.getProperty("over-other-apps").toBoolean()
    }

    fun getMinuteScheduled(): Int {
        return properties.getProperty("minute-scheduled").toInt()
    }

    fun getTimeoutReport(): Int {
        return properties.getProperty("timeout-report").toInt()
    }

    fun getGeofenceMaximumAccuracy(): Int {
        return properties.getProperty("geofence-maximum-accuracy").toInt()
    }

    fun getPreyJwt(): String {
        return properties.getProperty("prey-jwt")
    }

    fun getPreyGooglePlay(): String {
        return properties.getProperty("prey-google-play")
    }

    fun getGeofenceLoiteringDelay(): Int {
        return properties.getProperty("geofence-loitering-delay").toInt()
    }

    fun getPreyEventsLogs(): String {
        return properties.getProperty("prey-events-logs")
    }

    fun getDistanceLocation(): Int {
        return properties.getProperty("distance-location").toInt()
    }

    fun getGeofenceNotificationResponsiveness(): Int {
        return properties.getProperty("geofence-notification-responsiveness").toInt()
    }

    fun getFlyerKey(): String {
        return properties.getProperty("flyer-key")
    }

    fun getDistanceAware(): Int {
        return properties.getProperty("distance-aware").toInt()
    }

    fun getRadiusAware(): Int {
        return properties.getProperty("radius-aware").toInt()
    }

    fun getPreyTerms(): String {
        return properties.getProperty("prey-terms")
    }

    fun getPreyTermsEs(): String {
        return properties.getProperty("prey-terms-es")
    }

    fun getPreyForgot(): String {
        return properties.getProperty("prey-forgot")
    }

    /**
     * Method if it should show pin
     * @return true o false
     */
    fun getOpenPin(): Boolean {
        return properties.getProperty("open-pin").toBoolean()
    }

    companion object {
        private var instance: FileConfigReader? = null
        fun getInstance(context: Context): FileConfigReader {
            return instance ?: FileConfigReader(context).also { instance = it }
        }
    }
}