/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;

import com.prey.PreyLogger;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHelper extends   FingerprintManager.AuthenticationCallback {

    private FingerprintHelperListener listener;

    public FingerprintHelper(FingerprintHelperListener listener) {
        this.listener = listener;
    }

    private CancellationSignal cancellationSignal;

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        try {
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        } catch (Exception ex) {
            PreyLogger.e("Error:"+ ex.getMessage(),ex);
            listener.authenticationFailed("An error occurred: " + ex.getMessage());
        }
    }

    public void cancel() {
        if (cancellationSignal != null)
            cancellationSignal.cancel();
    }

    interface FingerprintHelperListener {
        void authenticationFailed(String error);
        void authenticationSuccess(FingerprintManager.AuthenticationResult result);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        listener.authenticationFailed("AuthenticationError : "+errString);
        PreyLogger.d("onAuthenticationError");
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        listener.authenticationFailed("AuthenticationHelp : "+helpString);
        PreyLogger.d("onAuthenticationHelp");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        listener.authenticationSuccess(result);
        PreyLogger.d("onAuthenticationSucceeded");
    }

    @Override
    public void onAuthenticationFailed() {
        listener.authenticationFailed("Authentication Failed!");
        PreyLogger.d("onAuthenticationFailed");
    }
}