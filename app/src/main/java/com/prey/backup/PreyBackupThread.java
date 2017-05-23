/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.backup;

import android.content.Context;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.PreyUtils;
import com.prey.net.PreyWebServices;

public class PreyBackupThread extends Thread {

    private Context ctx;

    public PreyBackupThread(Context ctx) {
        this.ctx = ctx;
        PreyLogger.d("PreyBackupThread");
    }

    public void run() {
        try {
            PreyLogger.d("PreyBackupThread run");
            String deviceId = PreyConfig.getPreyConfig(ctx).getDeviceId();
            String apiKey = PreyConfig.getPreyConfig(ctx).getApiKey();
            String email = PreyConfig.getPreyConfig(ctx).getEmail();
            PreyLogger.d("deviceId:" + deviceId);
            PreyLogger.d("apikey:" + apiKey);
            PreyPhone phone = new PreyPhone(ctx);
            PreyPhone.Hardware hardware = phone.getHardware();
            String uuid = hardware.getUuid();
            if (uuid != null && !"".equals(uuid)) {
                String uuidOld = PreyWebServices.getInstance().getUuidDevice(ctx);
                if (uuidOld != null && !"".equals(uuidOld) && !uuid.equals(uuidOld)) {
                    PreyLogger.i("uuid:" + uuid);
                    PreyLogger.i("uuidOld:" + uuidOld);
                    PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, apiKey, email, PreyUtils.getDeviceType(ctx));
                    PreyLogger.i("new deviceId:" + accountData.getDeviceId());
                    PreyLogger.i("new apikey:" + accountData.getApiKey());
                    PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                    PreyConfig.getPreyConfig(ctx).registerC2dm();
                    PreyWebServices.getInstance().sendEvent(ctx, PreyConfig.ANDROID_SIGN_UP);
                    PreyLogger.i("get deviceId:" + PreyConfig.getPreyConfig(ctx).getDeviceId());
                    PreyLogger.i("get apikey:" + PreyConfig.getPreyConfig(ctx).getApiKey());
                }
            }
        }catch (Exception e){
        }
    }

}
