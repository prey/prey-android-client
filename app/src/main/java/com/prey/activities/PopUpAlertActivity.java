package com.prey.activities;

/**
 * Created by oso on 24-08-15.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import com.prey.R;
public class PopUpAlertActivity extends PreyActivity {

    private static final int SHOW_POPUP = 0;
    private String message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            this.message = bundle.getString("alert_message");
        }

        showDialog(SHOW_POPUP);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog popup = null;
        switch (id) {

            case SHOW_POPUP:

                Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdaclean-regular.ttf");

                TextView start_dialog_desc = new TextView(this);
                start_dialog_desc.setText(this.message);
                start_dialog_desc.setPadding(10, 10, 10, 10);
                start_dialog_desc.setBackgroundResource(R.drawable.alert_v2);
                start_dialog_desc.setGravity(Gravity.CENTER);
                start_dialog_desc.setTextColor(Color.WHITE);
                start_dialog_desc.setTextSize(22);
                start_dialog_desc.setTypeface(myTypeface);

                AlertDialog.Builder start_dialog = new AlertDialog.Builder(this);
                start_dialog.setView(start_dialog_desc);

                popup = start_dialog.create();
                popup.setCancelable(true);
                popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
        }
        return popup;
    }

}

