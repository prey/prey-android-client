/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyUtils;
import com.prey.R;

import com.prey.activities.js.CustomWebView;
import com.prey.activities.js.WebAppInterface;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.factories.EventFactory;
import com.prey.services.PreyAccessibilityService;
import com.prey.services.PreyOverlayService;
import com.prey.services.PreyStorageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CheckPasswordHtmlActivity extends AppCompatActivity {

    public static String JS_ALIAS="Android";
    public static String URL_ONB = "file:///android_asset/html/index.html";

    public static final String CLOSE_PREY = "close_prey";
    private final BroadcastReceiver close_prey_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreyLogger.d("CheckPasswordHtmlActivity BroadcastReceiver: finish");
            finish();
        }
    };

    private WebView myWebView = null;

    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;
    public static int FILE_CHOOSER_RESULT_CODE = 6969;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            PreyLogger.e(String.format("Error ActionBar().hide:%s", e.getMessage()), e);
        }
        setContentView(R.layout.webview);
        PreyLogger.d("CheckPasswordHtmlActivity: onCreate");
        registerReceiver(close_prey_receiver, new IntentFilter(CLOSE_PREY));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        Bundle extras = getIntent().getExtras();
        String nexturl = "";
        try {
            nexturl = extras.getString("nexturl");
        } catch (Exception e) {
            PreyLogger.d("not extra nexturl");
        }
        PreyLogger.d(String.format("CheckPasswordHtmlActivity nexturl: %s", nexturl));
        if ("tryReport".equals(nexturl)) {
            tryReport();
        } else {
            loadUrl();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreyConfig.getPreyConfig(this).setCapsLockOn(false);
        PreyLogger.d("CheckPasswordHtmlActivity: onResume");
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(close_prey_receiver);
    }

    public void settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings");
        myWebView = (WebView) findViewById(R.id.install_browser);
        myWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                CustomWebView.callDispatchKeyEvent(getApplicationContext(), keyEvent);
                return false;
            }
        });
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                myWebView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                myWebView.getSettings().setSavePassword(false);
                myWebView.clearFormData();
            } catch (Exception e) {
                PreyLogger.e(String.format("Error autofill:%s", e.getMessage()), e);
            }
        }
    }

    public void tryReport() {
        PreyLogger.d("CheckPasswordHtmlActivity: tryReport");
        String lng = PreyUtils.getLanguage();
        StringBuffer url = new StringBuffer("");
        url.append(URL_ONB).append("#/").append(lng).append("/activation");
        settings();
        PreyLogger.d(String.format("_url:%s", url.toString()));
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(url.toString());
        myWebView.loadUrl("javascript:window.location.reload(true)");
    }

    public void loadUrl() {
        PreyLogger.d("CheckPasswordHtmlActivity: loadUrl");
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
        myWebView.loadUrl("javascript:window.location.reload(true)");
    }

    public void reload() {
        PreyLogger.d("CheckPasswordHtmlActivity: reload");
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
        myWebView.reload();
    }

    public String getUrl(Context ctx) {
        String lng = PreyUtils.getLanguage();
        StringBuffer url = new StringBuffer("");
        String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();
        boolean registered = PreyConfig.getPreyConfig(this).isThisDeviceAlreadyRegisteredWithPrey();
        PreyLogger.d(String.format("CheckPasswordHtmlActivity deviceKey:%s", deviceKey));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES >=M");
            boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
            boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
            boolean canAccessCamera = PreyPermission.canAccessCamera(this);
            boolean canAccessStorage = PreyPermission.canAccessStorage(this);
            boolean canAccessBackgroundLocation = PreyPermission.canAccessBackgroundLocationView(this);
            boolean verifyNotification = EventFactory.verifyNotification(ctx);
            if (verifyNotification) {
                EventFactory.notification(ctx);
            } else {
                NotificationManager manager = (NotificationManager) ctx.getSystemService(Service.NOTIFICATION_SERVICE);
                manager.cancel(EventFactory.NOTIFICATION_ID);
            }
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canAccessFineLocation:%s", canAccessFineLocation));
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canAccessCoarseLocation:%s", canAccessCoarseLocation));
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canAccessCamera:%s", canAccessCamera));
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canAccessStorage:%s", canAccessStorage));
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canAccessBackgroundLocation:%s", canAccessBackgroundLocation));
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canDrawOverlays:%s", canDrawOverlays));
            boolean canAccessibility = PreyPermission.isAccessibilityServiceView(this);
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: canAccessibility:%s", canAccessibility));
            boolean isAdminActive = FroyoSupport.getInstance(this).isAdminActive();
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: isAdminActive:%s", isAdminActive));
            boolean isStorage = PreyPermission.isExternalStorageManagerView(this);
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: isStorage:%s", isStorage));
            boolean configurated = (canAccessFineLocation || canAccessCoarseLocation) && canAccessBackgroundLocation && canAccessCamera
                    && canAccessStorage && isAdminActive && canDrawOverlays && canAccessibility && isStorage;
            String installationStatus = PreyConfig.getPreyConfig(this).getInstallationStatus();
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: configurated:%s installationStatus:%s", configurated, installationStatus));
            if (configurated) {
                if (registered) {
                    if ("".equals(installationStatus)) {
                        url.append(URL_ONB).append("#/").append(lng).append("/");
                    } else {
                        if ("OK".equals(installationStatus)) {
                            PreyConfig.getPreyConfig(ctx).setInstallationStatus("");
                            url.append(URL_ONB).append("#/").append(lng).append("/emailok");
                        } else {
                            url.append(URL_ONB).append("#/").append(lng).append("/emailsent");
                        }
                    }
                } else {
                    if ("DEL".equals(installationStatus)) {
                        PreyConfig.getPreyConfig(ctx).setInstallationStatus("");
                        url.append(URL_ONB).append("#/").append(lng).append("/emailretry");
                    } else {
                        url.append(URL_ONB).append("#/").append(lng).append("/signin");
                    }
                }
            } else {
                boolean permissionsAndBasic = (canAccessFineLocation || canAccessCoarseLocation) && canAccessCamera
                        && canAccessStorage && isAdminActive && canDrawOverlays ;
                boolean permissionsOrBasic = canAccessFineLocation || canAccessCoarseLocation || canAccessCamera
                        || canAccessStorage || isAdminActive || canDrawOverlays ;
                if (!permissionsOrBasic) {
                    url.append(URL_ONB).append("#/").append(lng).append("/start");
                } else {
                    if (permissionsAndBasic) {
                        if (!canAccessibility) {
                            PreyLogger.d(String.format("CheckPasswordHtmlActivity !canAccessibility"));
                            url.append(URL_ONB).append("#/").append(lng).append("/accessibility");
                        } else {
                            if (!canAccessBackgroundLocation) {
                                PreyLogger.d(String.format("CheckPasswordHtmlActivity !canAccessBackgroundLocation"));
                                url.append(URL_ONB).append("#/").append(lng).append("/bgloc");
                            } else {
                               url.append(URL_ONB).append("#/").append(lng).append("/permissions");
                            }
                        }
                    }else{
                        url.append(URL_ONB).append("#/").append(lng).append("/permissions");
                    }
                }
            }
        } else {
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES <M");
            if (registered) {
                url.append(URL_ONB).append("#/").append(lng).append("/");
            } else {
                url.append(URL_ONB).append("#/").append(lng).append("/signin");
            }
        }
        PreyLogger.d(String.format("_url:%s", url.toString()));
        return url.toString();
    }

    private static final int REQUEST_PERMISSIONS = 5;
    private static final int REQUEST_PERMISSIONS_LOCATION = 6;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private static final String[] INITIAL_PERMS_TIRAMISU = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermissionAndroid7() {
        PreyLogger.d("CheckPasswordHtmlActivity: askForPermissionAndroid7");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        startOverlayService();
    }

    private void startOverlayService() {
        PreyLogger.d("CheckPasswordHtmlActivity: startOverlayService");
        Intent intentOverlay = new Intent(getApplicationContext(), PreyOverlayService.class);
        startService(intentOverlay);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermission() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermission");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
        }else {
            ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, INITIAL_PERMS_TIRAMISU, REQUEST_PERMISSIONS);
        }
    }

    public void deniedPermission() {
        PreyLogger.d("deniedPermission");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String permission = "";
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
        if (!canAccessFineLocation || !canAccessCoarseLocation) {
            permission = this.getString(R.string.permission_location);
        } else {
            boolean canAccessBackgroundLocation = PreyPermission.canAccessBackgroundLocation(this);
            if (!canAccessBackgroundLocation) {
                permission = this.getString(R.string.permission_location);
            }
        }
        boolean canAccessCamera = PreyPermission.canAccessCamera(this);
        if (!canAccessCamera) {
            if (!"".equals(permission))
                permission += ", ";
            permission += this.getString(R.string.permission_camera);
        }
        boolean canAccessWriteExternalStorage = PreyPermission.canAccessStorage(this);
        if (!canAccessWriteExternalStorage) {
            if (!"".equals(permission))
                permission += ", ";
            permission += this.getString(R.string.permission_storage);
        }
        PreyLogger.d("permission:" + permission);
        String message = "";
        try {
            message = String.format(getResources().getString(R.string.permission_message_popup), permission);
        } catch (Exception e) {
            PreyLogger.e("Error format:" + e.getMessage(), e);
        }
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.permission_manually, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intentSetting = new Intent();
                intentSetting.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intentSetting.setData(uri);
                startActivity(intentSetting);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PreyLogger.d(String.format("CheckPasswordHtmlActivity onRequestPermissionsResult:%s", requestCode));
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; permissions != null && i < permissions.length; i++) {
                PreyLogger.d(String.format("CheckPasswordHtmlActivity onRequestPermissionsResult:%s %s", permissions[i], grantResults[i]));
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(false);
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[i] == 0) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(true);
                }
            }
        }
        if (requestCode == REQUEST_PERMISSIONS_LOCATION) {
            for (int i = 0; permissions != null && i < permissions.length; i++) {
                PreyLogger.d(String.format("CheckPasswordHtmlActivity onRequestPermissionsResult[%d]: %s", i, grantResults[i]));
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(false);
                }
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(true);
                }
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(true);
                }
            }
        }
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
        boolean canAccessCamera = PreyPermission.canAccessCamera(this);
        boolean canAccessStorage = PreyPermission.canAccessStorage(this);
        if (canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
                && canAccessStorage) {
            PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 1");
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
            if (!canDrawOverlays) {
                PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 2");
                askForPermissionAndroid7();
                startOverlayService();
            } else {
                PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 3");
                if (!canDrawOverlays) {
                    PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 4");
                    askForAdminActive();
                } else {
                    PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 5");
                    Intent intentLogin = new Intent(this, LoginActivity.class);
                    intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentLogin);
                    finish();
                }
            }
        }
    }

    private static final int SECURITY_PRIVILEGES = 10;
    public void askForAdminActive() {
        Intent intentAskForAdmin = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
        startActivityForResult(intentAskForAdmin, SECURITY_PRIVILEGES);
    }

    public void accessibility() {
        PreyLogger.d("CheckPasswordHtmlActivity accessibility");
        Intent intentService = new Intent(getApplicationContext(), PreyAccessibilityService.class);
        startService(intentService);
        Intent intentSetting = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentSetting);
    }

    public void askForPermissionLocation() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermissionLocation");
        ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSIONS_LOCATION);
    }

    /**
     * Method that open the image Chooser
     */
    public void openImageChooserActivity() {
        PreyLogger.d("CheckPasswordHtmlActivity openImageChooserActivity");
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * Method activity result
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = intent.getData();
                if (uri != null && uri.toString().startsWith("content:")) {
                    String fileName = getFileNameHelp(getApplicationContext(), uri);
                    PreyConfig.getPreyConfig(this).setFileHelp(fileName);
                }
            }
        }
    }

    /**
     * Method get selected image
     *
     * @param ctx
     * @param uri
     * @return image
     */
    @SuppressLint("Range")
    public String getFileNameHelp(Context ctx, Uri uri) {
        String fileNameHelp = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String displayName = cursor.getString(columnIndex);
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), PreyConfig.HELP_DIRECTORY);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File newFile = new File(dir, displayName);
                if (newFile.exists()) {
                    newFile.delete();
                    newFile = new File(dir, displayName);
                }
                FileOutputStream out = null;
                InputStream in = null;
                try {
                    out = new FileOutputStream(newFile);
                    in = getApplicationContext().getContentResolver().openInputStream(uri);
                    PreyUtils.copyFile(in, out);
                    fileNameHelp = displayName;
                } catch (Exception e) {
                    PreyLogger.d(String.format("Error getFileNameHelp:%s", e.getMessage()));
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Exception e) {
                            PreyLogger.d(String.format("Error getFileNameHelp:%s", e.getMessage()));
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                            PreyLogger.d(String.format("Error getFileNameHelp:%s", e.getMessage()));
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return fileNameHelp;
    }

    /**
     * Method for requesting storage permission
     */
    public void allFiles() {
    }

}
