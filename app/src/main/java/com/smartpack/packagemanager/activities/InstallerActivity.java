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
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.SerializableItems.PackageItems;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 06, 2021
 */
public class InstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mIcon;
    private MaterialButton mClose, mOpen;
    private MaterialTextView mStatus, mTitle;
    private ContentLoadingProgressBar mProgress;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static ArrayList<String> mAppList = null;
    private static boolean mUpdating = false;
    private static Drawable mAppIcon;
    private static String mApkPath = null, mAppName, mPackageName;
    public static final String APP_LIST_INTENT = "app_list", APK_PATH_INTENT = "app_list";

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

        new sExecutor() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                mAppList = getIntent().getStringArrayListExtra(APP_LIST_INTENT);
                mApkPath = getIntent().getStringExtra(APK_PATH_INTENT);
                mAppName = getAppName();
                mPackageName = getPackageID();
                mAppIcon = getAppIcon();
                mUpdating = sPackageUtils.isPackageInstalled(mPackageName, InstallerActivity.this);
            }

            @Override
            public void onPostExecute() {
                if (mAppName != null) {
                    mTitle.setText(getAppName());
                } else {
                    mTitle.setVisibility(View.GONE);
                }
                if (mAppIcon != null) {
                    mIcon.setImageDrawable(getAppIcon());
                } else {
                    mIcon.setVisibility(View.GONE);
                }

                mHandler.post(refreshRunnable);
            }
        }.execute();

        mOpen.setOnClickListener(v -> {
            startActivity(getPackageManager().getLaunchIntentForPackage(mPackageName));
            finish();
        });

        mClose.setOnClickListener(v -> backPressedEvent());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressedEvent();
            }
        });
    }

    private String getAppName() {
        String name = null;
        if (mAppList != null) {
            for (String apkPath : mAppList) {
                if (sFileUtils.exist(apkPath) && sAPKUtils.getAPKName(apkPath, this) != null) {
                    name = sAPKUtils.getAPKName(apkPath, this).toString();
                }
            }
        } else if (mApkPath != null) {
            name = sAPKUtils.getAPKName(mApkPath, this).toString();
        }
        return name;
    }

    private String getPackageID() {
        String name = null;
        if (mAppList != null) {
            for (String apkPath : mAppList) {
                if (sFileUtils.exist(apkPath) && sAPKUtils.getAPKName(apkPath, this) != null) {
                    name = sAPKUtils.getPackageName(apkPath, this);
                }
            }
        } else if (mApkPath != null) {
            name = sAPKUtils.getPackageName(mApkPath, this);
        }
        return name;
    }

    private Drawable getAppIcon() {
        Drawable icon = null;
        if (mAppList != null) {
            for (String apkPath : mAppList) {
                if (sFileUtils.exist(apkPath) && sAPKUtils.getAPKIcon(apkPath, this) != null) {
                    icon = sAPKUtils.getAPKIcon(apkPath, this);
                }
            }
        } else if (mApkPath != null) {
            icon = sAPKUtils.getAPKIcon(mApkPath, this);
        }
        return icon;
    }

    private void backPressedEvent() {
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(refreshRunnable);
        super.onDestroy();
    }


    private final Runnable refreshRunnable = new Runnable() {
        @SuppressLint("StringFormatInvalid")
        @Override
        public void run() {
            String installationStatus = sCommonUtils.getString("installationStatus", "waiting", InstallerActivity.this);

            if (installationStatus.equals("waiting")) {
                mStatus.setText(getString(R.string.installing_bundle));
                mHandler.postDelayed(this, 500);
            } else {
                mStatus.setText(getString(R.string.result, installationStatus));
                if (installationStatus.equals(getString(R.string.installation_status_success))) {
                    mTitle.setText(PackageData.getAppName(mPackageName, InstallerActivity.this));
                    mIcon.setImageDrawable(sPackageUtils.getAppIcon(mPackageName, InstallerActivity.this));
                    if (getPackageManager().getLaunchIntentForPackage(mPackageName) != null) {
                        mOpen.setVisibility(View.VISIBLE);
                    }

                    Intent result = new Intent();
                    if (!mUpdating) {
                        PackageData.getRawData().add(new PackageItems(
                                mPackageName,
                                sPackageUtils.getAppName(mPackageName, InstallerActivity.this).toString(),
                                new File(sPackageUtils.getSourceDir(mPackageName, InstallerActivity.this)).length(),
                                Objects.requireNonNull(PackageData.getPackageInfo(mPackageName, InstallerActivity.this)).firstInstallTime,
                                Objects.requireNonNull(PackageData.getPackageInfo(mPackageName, InstallerActivity.this)).lastUpdateTime)
                        );
                        result.putExtra("INSTALL_STATUS_UPDATE", false);
                    } else {
                        result.putExtra("INSTALL_STATUS_UPDATE", true);
                    }
                    setResult(Activity.RESULT_OK, result);
                }
                mProgress.setVisibility(View.GONE);
                mClose.setVisibility(View.VISIBLE);
            }
        }
    };

}