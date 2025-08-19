plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.koverAndroidReport)
}

android {
    namespace = "com.prey"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.prey"
        minSdk = 21
        targetSdk = 35
        versionCode = 381
        versionName = "2.7.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/LICENSE-notice.md")
    }

    lint {
        // Turns off checks for the issue IDs you specify.
        disable += "TypographyFractions" + "TypographyQuotes"
        // Turns on checks for the issue IDs you specify. These checks are in
        // addition to the default lint checks.
        enable += "RtlHardcoded" + "RtlCompat" + "RtlEnabled"
        // To enable checks for only a subset of issue IDs and ignore all others,
        // list the issue IDs with the 'check' property instead. This property overrides
        // any issue IDs you enable or disable using the properties above.
        checkOnly += "NewApi" + "InlinedApi"
        // If set to true, turns off analysis progress reporting by lint.
        quiet = true
        // If set to true (default), stops the build if errors are found.
        abortOnError = false
        // If set to true, lint only reports errors.
        ignoreWarnings = true
        // If set to true, lint also checks all dependencies as part of its analysis.
        // Recommended for projects consisting of an app with library dependencies.
        checkDependencies = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.gms:play-services-gcm:17.0.0")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.google.android.gms:play-services-maps:19.0.0")

    implementation(platform(libs.firebase.bom)) // Use the latest version
    implementation (libs.google.firebase.messaging)
    implementation (libs.google.firebase.analytics)

    implementation (libs.firebase.core)
    implementation (libs.firebase.iid)
    implementation (libs.firebase.crashlytics)
    implementation (libs.firebase.database)

    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("com.android.installreferrer:installreferrer:2.2")
    implementation ("com.android.support:multidex:1.0.3")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.biometric:biometric:1.4.0-alpha02")
    implementation ("androidx.work:work-runtime:2.9.1")
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation ("androidx.camera:camera-mlkit-vision:1.2.0-beta02")
    implementation ("androidx.camera:camera-core:1.2.0-rc01")
    implementation ("androidx.camera:camera-camera2:1.2.0-rc01")
    implementation ("androidx.camera:camera-lifecycle:1.2.0-rc01")
    implementation ("androidx.camera:camera-view:1.2.0-rc01")

    testImplementation(libs.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation (libs.core)
    androidTestImplementation (libs.core.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.ktx)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation (libs.kotlin.test.junit)
    androidTestImplementation (libs.test.rules)
    androidTestImplementation (libs.test.runner)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation (libs.mockito.core)
    androidTestImplementation(libs.androidx.espresso.web)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation (libs.mockito.android)
    androidTestImplementation (libs.io.mockk.mockk.android)
    androidTestImplementation (libs.io.mockk.mockk.agent)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kover {
    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
                androidGeneratedClasses()
            }
        }

        variant("release") {
            // filters for all report types only of 'release' build type
            filters {
                excludes {
                    androidGeneratedClasses()

                    classes(
                        // excludes debug classes
                        "*.DebugUtil"
                    )
                }
            }
        }

    }
}