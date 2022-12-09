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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.fragments.APKDetailsFragment;
import com.smartpack.packagemanager.fragments.CertificateFragment;
import com.smartpack.packagemanager.fragments.ManifestFragment;
import com.smartpack.packagemanager.fragments.PermissionsFragment;
import com.smartpack.packagemanager.utils.APKData;
import com.smartpack.packagemanager.utils.APKItems;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PermissionsItems;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.Utils.sAPKCertificateUtils;
import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.Utils.sThemeUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKPickerActivity extends AppCompatActivity {

    private AppCompatImageView mAppIcon;
    private Drawable mIcon = null;
    private File mFile = null;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialCardView mCancel, mInstall;
    private MaterialTextView mAppName, mInstallTitle, mPackageID;
    private String mName = null, mPackageName = null;
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
                mProgressDialog.setMessage("\n" + activity.getString(R.string.initializing));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                // Nullify previously acquired certificates, if any
                APKData.setCertificate(null);

                if (APKData.getAPKFile() != null) {
                    mFile = APKData.getAPKFile();
                } else if (uri != null) {
                    sUtils.delete(getExternalFilesDir("APK"));
                    mFile = new File(getExternalFilesDir("APK"), "APK.apk");
                }
                Common.isAPKPicker(true);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void doInBackground() {
                if (uri != null) {
                    sUtils.copy(uri, mFile, activity);
                }
                try {
                    APKItems mAPKData = APKData.getAPKData(mFile.getAbsolutePath(), activity);
                    if (mAPKData != null) {
                        if (mAPKData.getAppName() != null) {
                            mName = mAPKData.getAppName();
                        }
                        if (mAPKData.getPackageName() != null) {
                            mPackageName = mAPKData.getPackageName();
                        }
                        if (mAPKData.getIcon() != null) {
                            mIcon = mAPKData.getIcon();
                        }
                        if (mAPKData.getPermissions() != null) {
                            List<PermissionsItems> mPerms = new ArrayList<>();
                            for (int i = 0; i < mAPKData.getPermissions().size(); i++) {
                                mPerms.add(new PermissionsItems(false, mAPKData.getPermissions().get(i), sPermissionUtils.getDescription(
                                        mAPKData.getPermissions().get(i).replace("android.permission.",""), activity)));
                            }
                            APKData.setPermissions(mPerms);
                        }
                        if (mAPKData.getManifest() != null) {
                            APKData.setManifest(mAPKData.getManifest());
                        }
                        if (new sAPKCertificateUtils(mFile,null, activity).getCertificateDetails() != null) {
                            APKData.setCertificate(new sAPKCertificateUtils(mFile,null, activity).getCertificateDetails());
                        }
                        if (mAPKData.getVersionName() != null) {
                            APKData.setVersionInfo(getString(R.string.version, mAPKData.getVersionName() + " (" + mAPKData.getVersionCode() + ")"));
                        }
                        if (mAPKData.getSDKVersion() != null) {
                            APKData.setSDKVersion(mAPKData.getSDKVersion(), activity);
                        }
                        if (mAPKData.getMinSDKVersion() != null) {
                            APKData.setMinSDKVersion(mAPKData.getMinSDKVersion(), activity);
                        }
                        APKData.setSize(getString(R.string.size, sAPKUtils.getAPKSize(mFile.getAbsolutePath())) + " (" + mFile.length() + " bytes)");
                    }
                } catch (Exception ignored) {
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
                    if (mName != null || mPackageName != null || mIcon != null) {
                        loadAPKDetails(activity);
                    } else {
                        sUtils.toast(getString(R.string.wrong_extension, ".apk"), activity).show();
                        activity.finish();
                    }
                }
            }
        };
    }

    private void loadAPKDetails(Activity activity) {
        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());
        try {
            if (mName != null) {
                mAppName.setText(mName);
                mAppName.setVisibility(View.VISIBLE);
            }
            if (mPackageName != null) {
                mPackageID.setText(mPackageName);
                mPackageID.setVisibility(View.VISIBLE);
            }
            if (mIcon != null) {
                mAppIcon.setImageDrawable(mIcon);
            }
            if (sPackageUtils.isPackageInstalled(mPackageName, activity)) {
                mInstallTitle.setText(getString(R.string.update));
            }

            adapter.AddFragment(new APKDetailsFragment(), getString(R.string.app_info));
            if (APKData.getPermissions() != null) {
                adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
            }
            if (APKData.getManifest() != null) {
                adapter.AddFragment(new ManifestFragment(), getString(R.string.manifest));
            }
            if (APKData.getCertificate() != null) {
                adapter.AddFragment(new CertificateFragment(), getString(R.string.certificate));
            }
        } catch (Exception ignored) {}

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mMainLayout.setVisibility(View.VISIBLE);
        mIconsLayout.setVisibility(View.VISIBLE);

        mCancel.setOnClickListener(v -> finish());
        mInstall.setOnClickListener(v -> {
            Common.getAppList().add(mFile.getAbsolutePath());
            Common.setApplicationID(mPackageName);
            Common.isUpdating(sPackageUtils.isPackageInstalled(mPackageName, activity));
            if (Common.getApplicationID() != null) {
                SplitAPKInstaller.installSplitAPKs(activity);
            } else {
                sUtils.snackBar(activity.findViewById(android.R.id.content), activity.getString(R.string.installation_status_bad_apks)).show();
            }
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}