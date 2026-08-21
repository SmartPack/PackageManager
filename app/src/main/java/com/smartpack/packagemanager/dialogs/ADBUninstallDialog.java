/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.dialogs;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 13, 2026
 */
public abstract class ADBUninstallDialog extends BottomSheetDialog {

    @SuppressLint("StringFormatInvalid")
    public ADBUninstallDialog(String appName, String packageName, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_adb_uninstall, null);
        MaterialButton mUninstallButton = rootView.findViewById(R.id.uninstall_button);
        MaterialButton mDocumentation = rootView.findViewById(R.id.documentation);
        MaterialButton mCopyButton = rootView.findViewById(R.id.copy_button);
        MaterialButton mGotIt = rootView.findViewById(R.id.got_it);
        MaterialCardView mUninstall = rootView.findViewById(R.id.uninstall);
        MaterialTextView mMainMessage = rootView.findViewById(R.id.uninstall_message);
        MaterialTextView mADBCommand = rootView.findViewById(R.id.adb_command);
        MaterialTextView mUninstallUpdates = rootView.findViewById(R.id.uninstall_updates);

        String command = "adb shell pm uninstall --user " + Utils.getUserID() + " " + packageName;

        mMainMessage.setText(context.getString(R.string.uninstall_adb_summary, appName));
        mADBCommand.setText(command);

        if (sPackageUtils.isUpdatedSystemApp(packageName, context)) {
            mUninstallUpdates.setText(context.getString(R.string.uninstall_updates_message, appName));
            mUninstall.setVisibility(View.VISIBLE);
        }

        mCopyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied to clipboard", mADBCommand.getText().toString().trim());
            clipboard.setPrimaryClip(clip);
            sCommonUtils.toast("Copied to clipboard", context).show();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mMainMessage.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            mUninstallUpdates.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        mADBCommand.setTextColor(Color.MAGENTA);

        mGotIt.setOnClickListener(v -> dismiss());

        mDocumentation.setOnClickListener(v -> onButtonClicked());

        mUninstallButton.setOnClickListener(v -> {
            Intent remove = new Intent(Intent.ACTION_DELETE);
            remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            remove.setData(Uri.parse("package:" + packageName));
            context.startActivity(remove);
            dismiss();
        });

        setContentView(rootView);

        show();
    }

    public abstract void onButtonClicked();

}