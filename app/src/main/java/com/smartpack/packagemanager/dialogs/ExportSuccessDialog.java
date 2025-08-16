/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.dialogs;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 15, 2025
 */
public class ExportSuccessDialog extends MaterialAlertDialogBuilder {

    public ExportSuccessDialog(String path, Context context) {
        super(context);

        setIcon(R.mipmap.ic_launcher);
        setTitle(R.string.app_name);
        setMessage(context.getString(R.string.export_success_message, path));
        setPositiveButton(R.string.cancel, (dialog, i) -> {
        });
        show();
    }

}