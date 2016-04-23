/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2016 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.barcode.BarcodeCaptureActivity;
import com.prey.net.PreyWebServices;

public class BarcodeActivity extends Activity implements View.OnClickListener {


    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        statusMessage = (TextView) findViewById(R.id.status_message);
        barcodeValue = (TextView) findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        findViewById(R.id.read_barcode).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {

            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    String barcodeValue = barcode.displayValue;
                    PreyLogger.d("Barcode read: " + barcodeValue);

                    if (barcodeValue.indexOf("prey") >= 0) {

                        barcodeValue = barcodeValue.substring(5);
                        String[] pairs = barcodeValue.split("&");
                        String apikey = "";
                        String mail = "batch@preyproject.com";
                        for (String pair : pairs) {
                            String[] llave = pair.split("=");
                            PreyLogger.i("key[" + llave[0] + "]" + llave[1]);
                            if (llave[0].equals("api_key")) {
                                apikey = llave[1];
                            }

                        }
                        if (!"".equals(apikey)) {
                            new AddDeviceToApiKeyBatch().execute(apikey, mail, PreyUtils.getDeviceType(this));
                        }


                    }

                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    PreyLogger.d("No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    String error = null;

    private static final int NO_MORE_DEVICES_WARNING = 0;
    private static final int ERROR = 3;

    private boolean noMoreDeviceError = false;

    private class AddDeviceToApiKeyBatch extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                error = null;
                Context ctx = getApplicationContext();

                PreyLogger.i("apikey:" + data[0] + " mail:" + data[1] + " device:" + data[2]);

                // if(!PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey()) {
                PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, data[0], data[1], data[2]);
                if (accountData != null) {
                    PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                    PreyConfig.getPreyConfig(getApplicationContext()).saveAccount(accountData);
                    PreyConfig.getPreyConfig(getApplication()).registerC2dm();
                    PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_SIGN_IN);
                }

            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            PreyLogger.i("error[" + error + "]");
            if (error == null) {
                String message = getString(R.string.device_added_congratulations_text);
                Bundle bundle = new Bundle();
                bundle.putString("message", message);
                PreyConfig.getPreyConfig(getApplicationContext()).setCamouflageSet(true);
                Intent intent = new Intent(getApplicationContext(), PermissionInformationActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

                finish();


            } else {
                showDialog(ERROR);
            }
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog pass = null;
        switch (id) {

            case ERROR:
                return new AlertDialog.Builder(BarcodeActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();

            case NO_MORE_DEVICES_WARNING:
                return new AlertDialog.Builder(BarcodeActivity.this).setIcon(R.drawable.info).setTitle(R.string.set_old_user_no_more_devices_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();
        }
        return pass;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        AlertDialog ad = null;
        switch (id) {

            case ERROR:
                ad = (AlertDialog) dialog;
                ad.setIcon(R.drawable.error);
                ad.setTitle(R.string.error_title);
                ad.setMessage(error);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handler code
                    }
                });

                ad.setCancelable(false);

                break;

            case NO_MORE_DEVICES_WARNING:
                ad = (AlertDialog) dialog;
                ad.setIcon(R.drawable.info);
                ad.setTitle(R.string.set_old_user_no_more_devices_title);
                ad.setMessage(error);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handler code
                    }
                });
                ad.setCancelable(false);

                break;
            default:
                super.onPrepareDialog(id, dialog);
        }
    }
}
