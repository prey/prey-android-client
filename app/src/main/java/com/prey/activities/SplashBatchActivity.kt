/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.prey.PreyBatch
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.R

/**
 * This activity verifies that the installer has a valid token.
 */
class SplashBatchActivity : FragmentActivity() {

    // Error message to be displayed if token validation fails
    private var error: String? = null

    // TextView to display error messages or loading text
    private var textSplash: TextView? = null

    /**
     * Called when the activity is resumed.
     */
    public override fun onResume() {
        // Log debug message to indicate onResume was called
        PreyLogger.d("onResume of SplashBatchActivity")
        super.onResume()
        // Call tokenBatch function to validate token
        tokenBatch()
    }

    /**
     * Called when the activity is paused.
     */
    public override fun onPause() {
        // Log debug message to indicate onPause was called
        PreyLogger.d("onPause of SplashBatchActivity")
        super.onPause()
    }

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState The saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Log debug message to indicate onCreate was called
        PreyLogger.d("onCreate of SplashBatchActivity")
        super.onCreate(savedInstanceState)
        // Request no title for the window
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Set the content view to splash_batch layout
        setContentView(R.layout.splash_batch)
        // Find the text_splash TextView and assign it to textSplash
        textSplash = findViewById<View>(R.id.text_splash) as TextView
    }

    /**
     * Validates the token and displays a progress dialog while doing so.
     */
    fun tokenBatch() {
        // Progress dialog to display while validating token
        var progressDialog: ProgressDialog? = null
        // Get the application context
        val context = applicationContext

        try {
            // Clear any previous error messages from textSplash
            textSplash!!.text = ""
            // Create a new progress dialog
            progressDialog = ProgressDialog(this@SplashBatchActivity)
            // Set the progress dialog message to "Loading..."
            progressDialog!!.setMessage(getText(R.string.loading).toString())
            // Set the progress dialog to indeterminate
            progressDialog!!.isIndeterminate = true
            // Make the progress dialog non-cancelable
            progressDialog!!.setCancelable(false)
            // Show the progress dialog
            progressDialog!!.show()
        } catch (e: Exception) {
            // Log any exceptions that occur while creating the progress dialog
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            // Reset the error message
            error = null
            // Get the token from PreyBatch
            val token: String? = PreyConfig.getInstance(context).getTokenBatch()
            // Check if the token is null or empty
            if (token == null || "" == token) {
                // Set the error message to "Error: Invalid token"
                error = context.getString(R.string.error_token)
            } else {
                // Validate the token using PreyWebServices
                val validToken = PreyConfig.getInstance(context).getWebServices()
                    .validToken(context, PreyBatch.getInstance(context).getToken())
                // Check if the token is invalid
                if (!validToken) {
                    // Set the error message to "Error: Invalid token"
                    error = context.getString(R.string.error_token)
                }
            }
        } catch (e: Exception) {
            // Set the error message to the exception message
            error = e.message
        }
        // Dismiss the progress dialog if it was created
        if (progressDialog != null) progressDialog!!.dismiss()
        // Check if an error occurred during token validation
        if (error == null) {
            // Create an intent to start WelcomeBatchActivity
            val intentPermission = Intent(
                this@SplashBatchActivity,
                WelcomeBatchActivity::class.java
            )
            PreyConfig.getInstance(context).setActivityView(WELCOME_BATCH_ACTIVITY)
            // Start the WelcomeBatchActivity
            startActivity(intentPermission)
            // Finish the current activity
            finish()
        } else {
            // Display the error message in textSplash
            textSplash!!.text = error
            PreyConfig.getInstance(context).setActivityView(SPLASH_BATCH_ACTIVITY_ERROR)
        }
    }

    companion object {
        const val WELCOME_BATCH_ACTIVITY = "WelcomeBatchActivity"
        const val SPLASH_BATCH_ACTIVITY_ERROR = "SplashBatchActivityError"
    }

}