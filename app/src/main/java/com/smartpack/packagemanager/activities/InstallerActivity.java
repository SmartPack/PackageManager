/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageItems;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 06, 2021
 */
public class InstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mIcon;
    private MaterialCardView mClose, mOpen;
    private MaterialTextView mStatus, mTitle;
    private ProgressBar mProgress;
    private Thread mRefreshThread = null;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);

        mIcon = findViewById(R.id.icon);
        mProgress = findViewById(R.id.progress);
        mOpen = findViewById(R.id.open);
        mClose = findViewById(R.id.close);
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
                sCommonUtils.snackBar(findViewById(android.R.id.content), getString(R.string.open_failed, PackageData.getAppName(Common.getApplicationID(), this))).show();
            }
            PackageData.getRawData().add(new PackageItems(Common.getApplicationID(),
                    sPackageUtils.getAppName(Common.getApplicationID(), this).toString(),
                    sPackageUtils.getAppIcon(Common.getApplicationID(), this),
                    new File(sPackageUtils.getSourceDir(Common.getApplicationID(), this)).length(),
                    Objects.requireNonNull(PackageData.getPackageInfo(Common.getApplicationID(), this)).firstInstallTime,
                    Objects.requireNonNull(PackageData.getPackageInfo(Common.getApplicationID(), this)).lastUpdateTime));
            Common.reloadPage(true);
        });

        mClose.setOnClickListener(v -> backPressedEvent());

        refreshStatus(this);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressedEvent();
            }
        });
    }

    public void refreshStatus(InstallerActivity activity) {
        mRefreshThread = new RefreshThread(activity);
        mRefreshThread.start();
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

    private void backPressedEvent() {
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals(getString(R.string.installation_status_success))) {
            if (!Common.isUpdating()) {
                try {
                    PackageData.getRawData().add(new PackageItems(Common.getApplicationID(),
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
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mRefreshThread != null) {
            try {
                mRefreshThread.interrupt();
            } catch(Exception ignored) {}
        }
        super.onDestroy();
    }

    private static class RefreshThread extends Thread {
        WeakReference<InstallerActivity> mInstallerActivityRef;
        RefreshThread(InstallerActivity activity) {
            mInstallerActivityRef = new WeakReference<>(activity);
        }
        @SuppressLint("StringFormatInvalid")
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(500);
                    final InstallerActivity activity = mInstallerActivityRef.get();
                    if(activity == null){
                        break;
                    }
                    activity.runOnUiThread(() -> {
                        String installationStatus = sCommonUtils.getString("installationStatus", "waiting", activity);
                        if (installationStatus.equals("waiting")) {
                            activity.mStatus.setText(activity.getString(R.string.installing_bundle));
                        } else {
                            activity.mStatus.setText(activity.getString(R.string.result, installationStatus));
                            if (installationStatus.equals(activity.getString(R.string.installation_status_success))) {
                                try {
                                    activity.mTitle.setText(PackageData.getAppName(Common.getApplicationID(), activity));
                                    activity.mIcon.setImageDrawable(sPackageUtils.getAppIcon(Common.getApplicationID(), activity));
                                    activity.mOpen.setVisibility(View.VISIBLE);
                                } catch (NullPointerException ignored) {}
                            }
                            activity.mProgress.setVisibility(View.GONE);
                            activity.mClose.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (InterruptedException ignored) {}
        }
    }

}