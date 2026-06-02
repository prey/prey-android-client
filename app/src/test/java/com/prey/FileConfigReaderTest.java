package com.prey;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileConfigReaderTest {

    @Test
    public void returnsStringValuesFromBuildConfig() {
        FileConfigReader reader = FileConfigReader.getInstance(null);

        assertEquals(BuildConfig.PREY_CAMPAIGN, reader.getPreyCampaign());
        assertEquals(BuildConfig.PREY_DOMAIN, reader.getPreyDomain());
        assertEquals(BuildConfig.API_V2, reader.getApiV2());
    }

    @Test
    public void returnsNumericAndBooleanValuesFromBuildConfig() {
        FileConfigReader reader = FileConfigReader.getInstance(null);

        assertEquals(BuildConfig.DISTANCE_AWARE, reader.getDistanceAware());
        assertEquals(BuildConfig.GEOFENCE_MAXIMUM_ACCURACY, reader.getGeofenceMaximumAccuracy());
        assertEquals(BuildConfig.SCHEDULED, reader.isScheduled());
    }
}
