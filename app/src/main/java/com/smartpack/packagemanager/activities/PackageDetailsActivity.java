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
import com.smartpack.packagemanager.adapters.PagerAdapter;
import com.smartpack.packagemanager.fragments.ActivitiesFragment;
import com.smartpack.packagemanager.fragments.AppOpsFragment;
import com.smartpack.packagemanager.fragments.ManifestFragment;
import com.smartpack.packagemanager.fragments.PackageInfoFragment;
import com.smartpack.packagemanager.fragments.PermissionsFragment;
import com.smartpack.packagemanager.fragments.SplitApksFragment;
import com.smartpack.packagemanager.utils.AppOps;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */

public class PackageDetailsActivity extends AppCompatActivity {

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

        mAppIcon.setImageDrawable(Common.getApplicationIcon());
        mAppName.setText(Common.getApplicationName());
        mVersion.setText(getString(R.string.version, PackageData.getVersionName(Common.getSourceDir(), this)));

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageInfoFragment(), getString(R.string.app_info));
        if (new File(PackageData.getSourceDir(Common.getApplicationID(), this)).getName().equals("base.apk") && SplitAPKInstaller.splitApks(PackageData.getParentDir(Common.getApplicationID(), this)).size() > 1) {
            adapter.AddFragment(new SplitApksFragment(), getString(R.string.split_apk));
        }
        if (PackageDetails.getPermissions(Common.getApplicationID(), this).size() > 0) {
            adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
        }
        if (Utils.rootAccess() && AppOps.getOps(this).size() > 0) {
            adapter.AddFragment(new AppOpsFragment(), getString(R.string.operations));
        }
        if (PackageDetails.getActivities(Common.getApplicationID(), this).size() > 0) {
            adapter.AddFragment(new ActivitiesFragment(), getString(R.string.activities));
        }
        adapter.AddFragment(new ManifestFragment(), getString(R.string.manifest));

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

}