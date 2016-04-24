/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prey.barcodereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.PermissionInformationActivity;
import com.prey.net.PreyWebServices;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class BarcodeActivity extends Activity   {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "PREY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        statusMessage = (TextView) findViewById(R.id.status_message);
        barcodeValue = (TextView) findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        autoFocus.setChecked(true);

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
        intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

        startActivityForResult(intent, RC_BARCODE_CAPTURE);




    }

    @Override
    protected void onResume() {
        super.onResume();

        Button readBarcodeButton=(Button)findViewById(R.id.read_barcode);
        readBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreyLogger.i("click");
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });
    }



    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    //barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    String barcodeValue = barcode.displayValue;

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
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    String error = null;

    private static final int NO_MORE_DEVICES_WARNING = 0;
    private static final int ERROR = 3;

    private boolean noMoreDeviceError = false;

    private class AddDeviceToApiKeyBatch extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(BarcodeActivity.this);
            progressDialog.setMessage(BarcodeActivity.this.getText(R.string.set_old_user_loading).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                error = null;
                Context ctx = getApplicationContext();

                PreyLogger.i("apikey:" + data[0] + " mail:" + data[1] + " device:" + data[2]);

                if(!PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey()) {
                    PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, data[0], data[1], data[2]);
                    if (accountData != null) {
                        PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                        PreyConfig.getPreyConfig(getApplicationContext()).saveAccount(accountData);
                        PreyConfig.getPreyConfig(getApplication()).registerC2dm();
                        PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_SIGN_IN);
                    }
                }

            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
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
