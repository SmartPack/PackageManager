/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.fragments.ActivitiesFragment;
import com.smartpack.packagemanager.fragments.AppOpsFragment;
import com.smartpack.packagemanager.fragments.ManifestFragment;
import com.smartpack.packagemanager.fragments.PackageInfoFragment;
import com.smartpack.packagemanager.fragments.PermissionsFragment;
import com.smartpack.packagemanager.fragments.SplitApksFragment;
import com.smartpack.packagemanager.utils.AppOps;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.SerializableItems.PackageItems;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;

import java.io.File;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */
public class PackageDetailsActivity extends AppCompatActivity {

    public static final String DATA_INTENT = "data", APK_PICKED = "apk_picked";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagedetails);

        AppCompatImageView mAppIcon = findViewById(R.id.app_image);
        MaterialTextView mAppName = findViewById(R.id.app_title);
        MaterialTextView mVersion = findViewById(R.id.version_text);
        TabLayout mTabLayout = findViewById(R.id.tab_Layout);
        ViewPager mViewPager = findViewById(R.id.view_pager);

        PackageItems packageItems = (PackageItems) getIntent().getSerializableExtra(DATA_INTENT);
        boolean apkPicked = getIntent().getBooleanExtra(APK_PICKED, false);

        packageItems.loadAppIcon(mAppIcon);
        mAppName.setText(packageItems.getAppName());
        mVersion.setText(getString(R.string.version, sAPKUtils.getVersionName(sPackageUtils.getSourceDir(packageItems.getPackageName(), this), this)));

        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(PackageInfoFragment.newInstance(packageItems), getString(R.string.app_info));
        if (new File(sPackageUtils.getSourceDir(packageItems.getPackageName(), this)).getName().equals("base.apk") && SplitAPKInstaller.splitApks(sPackageUtils.getParentDir(packageItems.getPackageName(), this)).size() > 1) {
            adapter.AddFragment(SplitApksFragment.newInstance(packageItems.getPackageName()), getString(R.string.split_apk));
        }
        if (!PackageDetails.getPermissions(packageItems.getPackageName(), this).isEmpty()) {
            adapter.AddFragment(PermissionsFragment.newInstance(packageItems.getPackageName(), apkPicked), getString(R.string.permissions));
        }
        if ((new RootShell().rootAccess() || new ShizukuShell().isReady()) && !AppOps.getOps(packageItems.getPackageName(), this).isEmpty()) {
            adapter.AddFragment(AppOpsFragment.newInstance(packageItems.getPackageName()), getString(R.string.operations));
        }
        if (!PackageDetails.getActivities(packageItems.getPackageName(), this).isEmpty()) {
            adapter.AddFragment(ActivitiesFragment.newInstance(packageItems.getPackageName()), getString(R.string.activities));
        }
        adapter.AddFragment(ManifestFragment.newInstance(packageItems.getPackageName(), apkPicked), getString(R.string.manifest));

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

}