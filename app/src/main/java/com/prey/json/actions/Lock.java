/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.CloseActivity;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.services.CheckLockActivated;
import com.prey.services.PreyLockHtmlService;
import com.prey.services.PreyLockService;

public class Lock extends JsonAction {

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String messageId = null;
            try {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID);
                PreyLogger.d("messageId:"+messageId);
            } catch (Exception e) {
            }
            String reason=null;
            String jobId =null;
            try {
                jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                reason="{\"device_job_id\":\""+jobId+"\"}";
                PreyConfig.getPreyConfig(ctx).setJobIdLock(jobId);
            } catch (Exception e) {
            }
            String unlock = null;
            try {
                unlock = parameters.getString(PreyConfig.UNLOCK_PASS);
                PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
            } catch (Exception e) {
            }
            lock(ctx, unlock, messageId, reason,jobId);
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String messageId = null;
            try {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID);
                PreyLogger.d("messageId:"+messageId);
            } catch (Exception e) {
            }
            String reason="{\"origin\":\"panel\"}";;
            try {
                String jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                reason="{\"device_job_id\":\""+jobId+"\",\"origin\":\"panel\"}";

            } catch (Exception e) {
            }
            String jobIdLock=PreyConfig.getPreyConfig(ctx).getJobIdLock();
            if(jobIdLock!=null&&!"".equals(jobIdLock)){
                reason="{\"device_job_id\":\""+jobIdLock+"\",\"origin\":\"panel\"}";
                PreyConfig.getPreyConfig(ctx).setJobIdLock("");
            }
            PreyConfig.getPreyConfig(ctx).setLock(false);
            PreyConfig.getPreyConfig(ctx).deleteUnlockPass();
            if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove()){
                Thread.sleep(1000);
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed",messageId,UtilJson.makeMapParam("start", "lock", "stopped",reason));
                Thread.sleep(2000);
                if(   PreyPermission.canDrawOverlays(ctx)) {
                    try {
                        View view = PreyConfig.getPreyConfig(ctx).viewLock;
                        WindowManager wm = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
                        if (wm != null && view != null) {
                            wm.removeView(view);
                            PreyConfig.getPreyConfig(ctx).viewLock = null;
                        } else {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }catch (Exception e){
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }else{
                    boolean canAccessibility = PreyPermission.isAccessibilityServiceEnabled(ctx);
                    if(   canAccessibility) {
                        Intent intent = new Intent(ctx, CloseActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    }else{
                        try{

                                FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
                                WakeLock screenLock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, PreyConfig.TAG);
                                screenLock.acquire();
                                screenLock.release();

                            Thread.sleep(2000);
                            reason="{\"origin\":\"panel\"}";
                            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped",reason));
                        }catch(Exception e){
                            throw new PreyException(e);
                        }
                    }
                }
            }else{
                try{
                    if(!PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() ) {
                        FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
                        WakeLock screenLock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, PreyConfig.TAG);
                        screenLock.acquire();
                        screenLock.release();
                    }
                    Thread.sleep(2000);
                    reason="{\"origin\":\"panel\"}";
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped",reason));
                }catch(Exception e){
                    throw new PreyException(e);
                }
            }

        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String unlock = parameters.getString("parameter");
            lock(ctx, unlock, null, null,null);
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(),e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void lock(final Context ctx, String unlock,final String messageId,final String reason,String device_job_id) {
        PreyLogger.d("lock unlock:"+unlock+" messageId:"+ messageId+" reason:"+reason);
        PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
        PreyConfig.getPreyConfig(ctx).setLock(true);
        PreyLogger.d("lock 1");
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //TODO:ACCESS
            //boolean accessibility=PreyPermission.isAccessibilityServiceEnabled(ctx);
            boolean canDrawOverlays=PreyPermission.canDrawOverlays(ctx);
            if(canDrawOverlays) {
                Intent intent = null;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    PreyLogger.d("lock 2");
                    intent = new Intent(ctx, PreyLockHtmlService.class);
                }else {
                    PreyLogger.d("lock 3");
                    intent = new Intent(ctx, PreyLockService.class);
                }
                ctx.startService(intent);
                Intent intent3 = new Intent(ctx, CheckLockActivated.class);
                ctx.startService(intent3);
            }else{
                //TODO:ACCESS
                /*
                if(accessibility) {
                    PreyLogger.d("lock 4");
                    PreyConfig.getPreyConfig(ctx).setOverLock(false);
                    Intent intent4 = new Intent(ctx, AppAccessibilityService.class);
                    ctx.startService(intent4);
                    Intent intent = null;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent = new Intent(ctx, PasswordHtmlActivity.class);
                    }else{
                        intent = new Intent(ctx, PasswordNativeActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                }else {
                    PreyLogger.d("lock 5");
                    Lock.lockWhenYouNocantDrawOverlays(ctx);
                }
                */
                PreyLogger.d("lock 5");
                Lock.lockWhenYouNocantDrawOverlays(ctx);
            }
        }else{
            PreyLogger.d("lock 6");
            Lock.lockOld(ctx);
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "lock", "started", reason));
                }catch(Exception e){}
            }
        }).start();
    }

    public static void sendUnLock(final Context context){
        new Thread(new Runnable() {
            public void run() {
                String unlockPass=PreyConfig.getPreyConfig(context).getUnlockPass();
                PreyLogger.d("sendUnLock unlockPass:" + unlockPass);
                if (unlockPass!=null && !"".equals(unlockPass)) {
                    if (PreyConfig.getPreyConfig(context).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(context)) {
                        PreyLogger.d("sendUnLock nothing");
                    } else {
                        PreyLogger.d("sendUnLock deleteUnlockPass");
                        PreyConfig.getPreyConfig(context).setUnlockPass("");
                        Intent intent = new Intent(context, CloseActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        //TODO:ACCESS
                        /*
                        Intent intent4 = new Intent(context, AppAccessibilityService.class);
                        context.stopService(intent4);
                        */
                        final Context ctx = context;
                        new Thread() {
                            public void run() {
                                String jobIdLock = PreyConfig.getPreyConfig(ctx).getJobIdLock();
                                String reason = "{\"origin\":\"user\"}";
                                if (jobIdLock != null && !"".equals(jobIdLock)) {
                                    reason = "{\"origin\":\"user\",\"device_job_id\":\"" + jobIdLock + "\"}";
                                    PreyConfig.getPreyConfig(ctx).setJobIdLock("");
                                }
                                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped", reason));
                            }
                        }.start();
                    }
                }
            }
        }).start();
    }

    public static void lockWhenYouNocantDrawOverlays(Context ctx) {
        boolean accessibility=PreyPermission.isAccessibilityServiceEnabled(ctx);
        boolean canDrawOverlays=PreyPermission.canDrawOverlays(ctx);
        String unlockPass=PreyConfig.getPreyConfig(ctx).getUnlockPass();
        PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass2:" + unlockPass+" accessibility:" + accessibility+" canDrawOverlays:"+canDrawOverlays);
        boolean isAccessibilityServiceEnabled=PreyPermission.isAccessibilityServiceEnabled(ctx);
        if (unlockPass!=null && !"".equals(unlockPass)) {
            if (!canDrawOverlays(ctx) &&!isAccessibilityServiceEnabled) {
                    boolean isPatternSet = isPatternSet(ctx);
                    boolean isPassOrPinSet = isPassOrPinSet(ctx);
                    PreyLogger.d("CheckLockActivated isPatternSet:" + isPatternSet);
                    PreyLogger.d("CheckLockActivated  isPassOrPinSet:" + isPassOrPinSet);
                    if (isPatternSet || isPassOrPinSet) {
                        FroyoSupport.getInstance(ctx).lockNow();
                        new Thread(new EventManagerRunner(ctx, new Event(Event.NATIVE_LOCK))).start();
                    } else {
                        try {
                            FroyoSupport.getInstance(ctx).changePasswordAndLock(PreyConfig.getPreyConfig(ctx).getUnlockPass(), true);
                            new Thread(new EventManagerRunner(ctx, new Event(Event.NATIVE_LOCK))).start();
                        } catch (Exception e) {
                        }
                    }

            }
        }
    }

    public static void lockOld(Context ctx) {
        boolean accessibility=PreyPermission.isAccessibilityServiceEnabled(ctx);
        boolean canDrawOverlays=PreyPermission.canDrawOverlays(ctx);
        String unlockPass=PreyConfig.getPreyConfig(ctx).getUnlockPass();
        PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass1:" + unlockPass+" accessibility:" + accessibility+" canDrawOverlays:"+canDrawOverlays);
        if (unlockPass!=null && !"".equals(unlockPass)) {
            boolean isPatternSet = isPatternSet(ctx);
            boolean isPassOrPinSet = isPassOrPinSet(ctx);
            PreyLogger.d("CheckLockActivated isPatternSet:" + isPatternSet);
            PreyLogger.d("CheckLockActivated  isPassOrPinSet:" + isPassOrPinSet);
            if (isPatternSet || isPassOrPinSet) {
                FroyoSupport.getInstance(ctx).lockNow();
            } else {
                try {
                    FroyoSupport.getInstance(ctx).changePasswordAndLock(PreyConfig.getPreyConfig(ctx).getUnlockPass(), true);
                } catch (Exception e) {
                    PreyLogger.e("error lockold:"+e.getMessage(),e);
                }
            }
        }
    }

    public static boolean canDrawOverlays(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(ctx);
    }

    /**
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    public static boolean isPatternSet(Context ctx) {
        ContentResolver cr = ctx.getContentResolver();
        try {
            int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
            return lockPatternEnable == 1;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /**
     * @return true if pass or pin set
     */
    @TargetApi(16)
    public static boolean isPassOrPinSet(Context ctx) {
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
        return keyguardManager.isKeyguardSecure();
    }

    /**
     * @return true if pass or pin or pattern locks screen
     */
    @TargetApi(23)
    private boolean isDeviceLocked(Context ctx) {
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE); //api 23+
        return keyguardManager.isDeviceSecure();
    }

}

