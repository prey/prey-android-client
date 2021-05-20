/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import com.prey.FileConfigReader;
import com.prey.PreyUtils;
import com.prey.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FormFeedbackActivity extends PreyActivity {

    private static final int SHOW_POPUP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        showDialog(SHOW_POPUP);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog popup = null;
        switch (id) {
            case SHOW_POPUP:
                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.dialog_signin, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setIcon(R.drawable.info);
                alert.setTitle(R.string.feedback_form_title);
                alert.setMessage(R.string.feedback_form_message);
                alert.setView(textEntryView);
                final EditText input = (EditText) textEntryView.findViewById(R.id.feedback_form_field_comment);
                alert.setPositiveButton(R.string.feedback_form_button2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                alert.setNegativeButton(R.string.feedback_form_button1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (input != null) {
                            Context ctx = getApplicationContext();
                            String emailFeedback = FileConfigReader.getInstance(getApplicationContext()).getEmailFeedback();
                            StringBuffer subject = new StringBuffer();
                            subject.append(FileConfigReader.getInstance(ctx).getSubjectFeedback()).append(" ");
                            subject.append(PreyUtils.randomAlphaNumeric(7).toUpperCase());
                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailFeedback});
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject.toString());
                            intent.putExtra(Intent.EXTRA_TEXT, input.getText().toString());
                            Intent chooser = Intent.createChooser(intent, ctx.getText(R.string.feedback_form_send_email));
                            startActivity(chooser);
                        }
                        finish();
                    }
                });
                popup = alert.create();
        }
        return popup;
    }

}