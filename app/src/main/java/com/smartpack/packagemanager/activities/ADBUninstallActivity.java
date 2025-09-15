/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 28, 2021
 */
public class ADBUninstallActivity extends AppCompatActivity {

    public static final String PACKAGE_INTENT = "package";

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_uninstall);

        MaterialButton mUninstallButton = findViewById(R.id.uninstall_button);
        MaterialButton mDocumentation = findViewById(R.id.documentation);
        MaterialButton mCopyButton = findViewById(R.id.copy_button);
        MaterialButton mGotIt = findViewById(R.id.got_it);
        MaterialCardView mUninstall = findViewById(R.id.uninstall);
        MaterialTextView mMainMessage = findViewById(R.id.uninstall_message);
        MaterialTextView mADBCommand = findViewById(R.id.adb_command);
        MaterialTextView mUninstallUpdates = findViewById(R.id.uninstall_updates);

        String packageName = getIntent().getStringExtra(PACKAGE_INTENT);

        mMainMessage.setText(getString(R.string.uninstall_adb_summary, packageName));
        mADBCommand.setText("adb shell pm uninstall -k --user 0 " + packageName);
        if (sPackageUtils.isUpdatedSystemApp(packageName, this)) {
            mUninstallUpdates.setText(getString(R.string.uninstall_updates_message, packageName));
            mUninstall.setVisibility(View.VISIBLE);
        }

        mCopyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied to clipboard", mADBCommand.getText().toString().trim());
            clipboard.setPrimaryClip(clip);
            sCommonUtils.toast("Copied to clipboard", this).show();
        });

        mADBCommand.setTextColor(Color.MAGENTA);

        mGotIt.setOnClickListener(v -> finish());

        mDocumentation.setOnClickListener(v -> sCommonUtils.launchUrl("https://smartpack.github.io/adb-debloating/", this));

        mUninstallButton.setOnClickListener(v -> {
            Intent remove = new Intent(Intent.ACTION_DELETE);
            remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            remove.setData(Uri.parse("package:" + packageName));
            startActivity(remove);
            finish();
        });
    }

}