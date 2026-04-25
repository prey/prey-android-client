/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.prey.actions.HttpDataService;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;

public class PreyEmail {

    public static void sendDataMail(Context ctx, HttpDataService data) {
        try {
            if (data != null) {
                List<EntityFile> entityFiles = data.getEntityFiles();
                if (entityFiles != null && entityFiles.size() >= 0) {
                    String url = PreyWebServices.getInstance().getFileUrlJson(ctx);
                    PreyLogger.d("URL:" + url);
                    Map<String, String> parameters = new HashMap<String, String>();
                    PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
                    PreyHttpResponse preyHttpResponse = null;
                    preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, entityFiles);
                    PreyLogger.d("status line:" + preyHttpResponse.getStatusCode());
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
        }
    }

    // Account types searched, in preference order, to resolve the user's work email:
    //   - "com.google": Google work account (work profile under Android Enterprise,
    //     Google Endpoint Manager, managed Google Play).
    //   - "com.microsoft.workaccount": Microsoft work account used by Intune
    //     (Company Portal / Authenticator) on AAD-joined / Entra ID enrollments.
    private static final String[] WORK_ACCOUNT_TYPES = {
            "com.google",
            "com.microsoft.workaccount"
    };

    @SuppressLint("NewApi")
    public static String getEmail(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            AccountManager accountManager = AccountManager.get(context);
            Account account = getAccount(context,accountManager);
            if (account != null) {
                return account.name;
            }else{
                PreyLogger.d("account nulo");
            }
        }else{
            PreyLogger.d("sdk menor");
        }
        return null;
    }

    @SuppressLint("NewApi")
    private static Account getAccount(Context context,AccountManager accountManager) {
        if (PreyConfig.getPreyConfig(context).isEclairOrAbove()) {
            for (String type : WORK_ACCOUNT_TYPES) {
                Account[] accounts = accountManager.getAccountsByType(type);
                if (accounts.length > 0) {
                    return accounts[0];
                }
            }
            PreyLogger.d("account length 0");
        }else {
            PreyLogger.d("account bajo eckair");
        }
        return null;
    }

}