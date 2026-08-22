/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 13, 2026
 */
public abstract class RestoreAppDialog extends MaterialAlertDialogBuilder {

    private AlertDialog alertDialog;

    @SuppressLint("StringFormatInvalid")
    public RestoreAppDialog(Drawable drawable, String appName, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_restore_app, null);

        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialButton restore = rootView.findViewById(R.id.restore);
        MaterialTextView title = rootView.findViewById(R.id.title);

        icon.setImageDrawable(drawable);
        title.setText(context.getString(R.string.restore_message, appName));

        setView(rootView);

        cancel.setOnClickListener(v -> alertDialog.dismiss());

        restore.setOnClickListener(v -> {
            onRestoring();
            alertDialog.dismiss();
        });

        alertDialog = create();
        alertDialog.show();
    }

    public abstract void onRestoring();
}