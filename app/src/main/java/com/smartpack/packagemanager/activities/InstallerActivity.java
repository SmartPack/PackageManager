/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RecycleViewItem;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sInstallerUtils;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 06, 2021
 */
public class InstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mIcon;
    private MaterialCardView mCancel, mOpen;
    private MaterialTextView mStatus, mTitle;
    private ProgressBar mProgress;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);

        mIcon = findViewById(R.id.icon);
        mProgress = findViewById(R.id.progress);
        mOpen = findViewById(R.id.open);
        mCancel = findViewById(R.id.cancel);
        mTitle = findViewById(R.id.title);
        mStatus = findViewById(R.id.status);

        if (getName() != null) {
            mTitle.setText(getName());
        } else {
            mTitle.setVisibility(View.GONE);
        }
        if (getIcon() != null) {
            mIcon.setImageDrawable(getIcon());
        } else {
            mIcon.setVisibility(View.GONE);
        }

        mOpen.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(Common.getApplicationID());
            if (launchIntent != null) {
                startActivity(launchIntent);
                finish();
            } else {
                sUtils.snackBar(findViewById(android.R.id.content), getString(R.string.open_failed, PackageData.getAppName(Common.getApplicationID(), this))).show();
            }
            PackageData.getRawData().add(new RecycleViewItem(Common.getApplicationID(),
                    sPackageUtils.getAppName(Common.getApplicationID(), this).toString(),
                    sPackageUtils.getAppIcon(Common.getApplicationID(), this),
                    new File(sPackageUtils.getSourceDir(Common.getApplicationID(), this)).length(),
                    Objects.requireNonNull(PackageData.getPackageInfo(Common.getApplicationID(), this)).firstInstallTime,
                    Objects.requireNonNull(PackageData.getPackageInfo(Common.getApplicationID(), this)).lastUpdateTime));
            Common.reloadPage(true);
        });

        mCancel.setOnClickListener(v -> onBackPressed());

        refreshStatus(this);
    }

    public void refreshStatus(Activity activity) {
        new Thread() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(() -> {
                            String installationStatus = sUtils.getString("installationStatus", "waiting", activity);
                            if (installationStatus.equals("waiting")) {
                                mStatus.setText(getString(R.string.installing_bundle));
                            } else {
                                mStatus.setText(getString(R.string.result, installationStatus));
                                if (installationStatus.equals(getString(R.string.installation_status_success))) {
                                    try {
                                        mTitle.setText(PackageData.getAppName(Common.getApplicationID(), activity));
                                        mIcon.setImageDrawable(sPackageUtils.getAppIcon(Common.getApplicationID(), activity));
                                        mOpen.setVisibility(View.VISIBLE);
                                    } catch (NullPointerException ignored) {}
                                }
                                mProgress.setVisibility(View.GONE);
                                mCancel.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    private CharSequence getName() {
        CharSequence name = null;
        for (String mAPKs : Common.getAppList()) {
            if (sAPKUtils.getAPKName(mAPKs, this) != null) {
                name = sAPKUtils.getAPKName(mAPKs, this);
            }
        }
        return name;
    }

    private Drawable getIcon() {
        Drawable icon = null;
        for (String mAPKs : Common.getAppList()) {
            if (sAPKUtils.getAPKIcon(mAPKs, this) != null) {
                icon = sAPKUtils.getAPKIcon(mAPKs, this);
            }
        }
        return icon;
    }

    @Override
    public void onBackPressed() {
        if (!sInstallerUtils.isPermissionDenied(this) && sUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        if (sUtils.getString("installationStatus", "waiting", this).equals(getString(R.string.installation_status_success))) {
            if (!Common.isUpdating()) {
                try {
                    PackageData.getRawData().add(new RecycleViewItem(Common.getApplicationID(),
                            PackageData.getAppName(Common.getApplicationID(), this),
                            sPackageUtils.getAppIcon(Common.getApplicationID(), this),
                            new File(sPackageUtils.getSourceDir(Common.getApplicationID(), this)).length(),
                            Objects.requireNonNull(PackageData.getPackageInfo(Common.getApplicationID(), this)).firstInstallTime,
                            Objects.requireNonNull(PackageData.getPackageInfo(Common.getApplicationID(), this)).lastUpdateTime));
                } catch (NullPointerException ignored) {}
                Common.reloadPage(true);
            } else {
                Common.isUpdating(false);
            }
        }
        super.onBackPressed();
    }

}