/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 06, 2021
 */
public class SplitAPKInstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mIcon;
    private MaterialCardView mCancel;
    private MaterialTextView mStatus, mTitle;
    private ProgressBar mProgress;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);

        mIcon = findViewById(R.id.icon);
        mProgress = findViewById(R.id.progress);
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
                            String installationStatus = Utils.getString("installationStatus", "waiting", activity);
                            if (installationStatus.equals("waiting")) {
                                mStatus.setText(getString(R.string.installing_bundle));
                            } else {
                                mStatus.setText(getString(R.string.result, installationStatus));
                                if (installationStatus.equals(getString(R.string.installation_status_success))) {
                                    try {
                                        mTitle.setText(PackageData.getAppName(getPackageId(), activity));
                                        mIcon.setImageDrawable(PackageData.getAppIcon(getPackageId(), activity));
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
        for (String mAPKs : PackageExplorer.mAPKList) {
            if (PackageData.getAPKName(mAPKs, this) != null) {
                name = PackageData.getAPKName(mAPKs, this);
            }
        }
        return name;
    }

    private String getPackageId() {
        String name = null;
        for (String mAPKs : PackageExplorer.mAPKList) {
            if (PackageData.getAPKId(mAPKs, this) != null) {
                name = PackageData.getAPKId(mAPKs, this);
            }
        }
        return name;
    }

    private Drawable getIcon() {
        Drawable icon = null;
        for (String mAPKs : PackageExplorer.mAPKList) {
            if (PackageData.getAPKIcon(mAPKs, this) != null) {
                icon = PackageData.getAPKIcon(mAPKs, this);
            }
        }
        return icon;
    }

    @Override
    public void onBackPressed() {
        if (Utils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        if (Utils.getString("installationStatus", "waiting", this).equals(getString(R.string.installation_status_success))) {
            PackageTasks.mReloadPage = true;
        }
        super.onBackPressed();
    }

}