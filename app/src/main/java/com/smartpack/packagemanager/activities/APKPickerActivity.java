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
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import com.apk.axml.APKParser;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.fragments.APKDetailsFragment;
import com.smartpack.packagemanager.fragments.CertificateFragment;
import com.smartpack.packagemanager.fragments.ManifestFragment;
import com.smartpack.packagemanager.fragments.PermissionsFragment;
import com.smartpack.packagemanager.utils.APKData;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FileUtils;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKPickerActivity extends AppCompatActivity {

    private APKParser mAPKParser;
    private AppCompatImageView mAppIcon;
    private File mFile = null;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialCardView mCancel, mInstall;
    private MaterialTextView mAppName, mInstallTitle, mPackageID;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize App Theme
        sThemeUtils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkpicker);

        mAppIcon = findViewById(R.id.app_image);
        mAppName = findViewById(R.id.app_title);
        mInstallTitle = findViewById(R.id.install_title);
        mPackageID = findViewById(R.id.package_id);
        mMainLayout = findViewById(R.id.main_layout);
        mIconsLayout = findViewById(R.id.icons_layout);
        mInstall = findViewById(R.id.install);
        mCancel = findViewById(R.id.cancel);
        mTabLayout = findViewById(R.id.tab_Layout);
        mViewPager = findViewById(R.id.view_pager);

        if (APKData.getAPKFile() != null) {
            manageInstallation(null, this).execute();
        } else if (getIntent().getData() != null) {
            manageInstallation(getIntent().getData(), this).execute();
        }
    }

    private sExecutor manageInstallation(Uri uri, Activity activity) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setMessage("\n" + activity.getString(R.string.initializing));
                mProgressDialog.setIndeterminate(APKData.getAPKFile() != null);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                if (APKData.getAPKFile() != null) {
                    mFile = APKData.getAPKFile();
                } else if (uri != null) {
                    sFileUtils.delete(getExternalFilesDir("APK"));
                    String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(activity, uri)).getName();
                    mFile = new File(getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
                }
                Common.isAPKPicker(true);
            }

            @Override
            public void doInBackground() {
                if (uri != null) {
                    try {
                        FileUtils FileUtils = new FileUtils(mFile.getAbsolutePath());
                        FileUtils.setProgress(mProgressDialog);
                        FileUtils.copy(uri, activity);
                    } catch (IOException ignored) {}
                }
                if (mFile.getName().endsWith(".apk")) {
                    try {
                        mAPKParser = new APKParser();
                        mAPKParser.parse(mFile.getAbsolutePath(), activity);
                    } catch (Exception ignored) {
                    }
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (mFile.exists()) {
                    if (mAPKParser != null && mAPKParser.isParsed()) {
                        loadAPKDetails(activity);
                    } else {
                        sCommonUtils.toast(getString(R.string.wrong_extension, ".apk"), activity).show();
                        activity.finish();
                    }
                }
            }
        };
    }

    private void loadAPKDetails(Activity activity) {
        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());
        if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
            mAppIcon.setImageDrawable(sPackageUtils.getAppIcon(mAPKParser.getPackageName(), activity));
            mAppName.setText(sPackageUtils.getAppName(mAPKParser.getPackageName(), activity));
            mPackageID.setText(mAPKParser.getPackageName());
            mPackageID.setVisibility(View.VISIBLE);
            mAppName.setVisibility(View.VISIBLE);
        } else {
            mAppName.setText(mAPKParser.getPackageName());
            mAppIcon.setImageDrawable(mAPKParser.getAppIcon());
            mAppName.setVisibility(View.VISIBLE);
        }
        if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
            mInstallTitle.setText(getString(R.string.update));
        }

        adapter.AddFragment(new APKDetailsFragment(), getString(R.string.app_info));
        if (mAPKParser.getPermissions() != null) {
            adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
        }
        if (mAPKParser.getManifest() != null) {
            adapter.AddFragment(new ManifestFragment(), getString(R.string.manifest));
        }
        if (mAPKParser.getCertificate() != null) {
            adapter.AddFragment(new CertificateFragment(), getString(R.string.certificate));
        }

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mMainLayout.setVisibility(View.VISIBLE);
        mIconsLayout.setVisibility(View.VISIBLE);

        mCancel.setOnClickListener(v -> finish());
        mInstall.setOnClickListener(v -> {
            Common.getAppList().add(mFile.getAbsolutePath());
            Common.setApplicationID(mAPKParser.getPackageName());
            Common.isUpdating(sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity));
            if (Common.getApplicationID() != null) {
                new SplitAPKsInstallationTasks(activity).execute();
            } else {
                sCommonUtils.snackBar(activity.findViewById(android.R.id.content), activity.getString(R.string.installation_status_bad_apks)).show();
            }
            finish();
        });
    }

}