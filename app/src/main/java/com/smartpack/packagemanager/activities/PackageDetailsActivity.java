/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PagerAdapter;
import com.smartpack.packagemanager.fragments.ActivitiesFragment;
import com.smartpack.packagemanager.fragments.PackageInfoFragment;
import com.smartpack.packagemanager.fragments.PermissionsFragment;
import com.smartpack.packagemanager.fragments.SplitApksFragment;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */

public class PackageDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagedetails);

        TabLayout mTabLayout = findViewById(R.id.tab_Layout);
        ViewPager mViewPager = findViewById(R.id.view_pager);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageInfoFragment(), getString(R.string.app_info));
        if (new File(PackageData.getSourceDir(PackageData.mApplicationID, this)).getName().equals("base.apk") && SplitAPKInstaller.splitApks(PackageData.getParentDir(PackageData.mApplicationID, this)).size() > 1) {
            adapter.AddFragment(new SplitApksFragment(), getString(R.string.split_apk));
        }
        if (PackageDetails.getPermissions(PackageData.mApplicationID, this).size() > 0) {
            adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
        }
        if (PackageDetails.getActivities(PackageData.mApplicationID, this).size() > 0) {
            adapter.AddFragment(new ActivitiesFragment(), getString(R.string.activities));
        }

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

}