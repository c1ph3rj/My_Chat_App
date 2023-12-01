package com.codinginflow.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(Context context) {
        super(context, R.style.CustomProgressDialog);
        setContentView(R.layout.custom_progress_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView messageTextView = findViewById(R.id.messageTextView);

        // You can customize the message or other properties here
    }
}

