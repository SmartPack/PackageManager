/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.MobileAds;
import com.smartpack.packagemanager.fragments.PackageTasksFragment;
import com.smartpack.packagemanager.utils.PagerAdapter;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.views.dialog.Dialog;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.viewPagerID);
        AppCompatTextView copyRightText = findViewById(R.id.copyright_Text);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageTasksFragment(), getString(R.string.apps));

        copyRightText.setText(getString(R.string.about));
        viewPager.setAdapter(adapter);

        // Initialize Google Ads
        MobileAds.initialize(this, "ca-app-pub-7791710838910455~4399535899");
    }

    public void creditsDialogue(View view) {
        new Dialog(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name) + "\n" + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.about_summary))
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setNegativeButton(getString(R.string.report_issue), (dialogInterface, i) -> {
                    Utils.launchUrl("https://github.com/SmartPack/PackageManager/issues/new", this);
                })
                .setPositiveButton(getString(R.string.support), (dialogInterface, i) -> {
                    Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                })
                .show();
    }

}