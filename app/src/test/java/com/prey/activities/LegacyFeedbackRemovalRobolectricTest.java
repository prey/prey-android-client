/*******************************************************************************
 * Created by Prey
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class LegacyFeedbackRemovalRobolectricTest {

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void feedbackActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.FeedbackActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void formFeedbackActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.FormFeedbackActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void signUpActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.SignUpActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void signInActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.SignInActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void setupActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.SetupActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void passwordActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.PasswordActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void simpleVideoActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.SimpleVideoActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void videoActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.VideoActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void welcomeActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.WelcomeActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void deviceReadyActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.DeviceReadyActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void checkPasswordActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.CheckPasswordActivity"),
                PackageManager.GET_META_DATA
        );
    }

    @Test(expected = PackageManager.NameNotFoundException.class)
    public void preyConfigurationActivityIsNotDeclaredInManifest() throws PackageManager.NameNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        context.getPackageManager().getActivityInfo(
                new ComponentName(context.getPackageName(), "com.prey.activities.PreyConfigurationActivity"),
                PackageManager.GET_META_DATA
        );
    }
}
