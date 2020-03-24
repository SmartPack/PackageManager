/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smartpack.packagemanager.fragments.PackageTasksFragment;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.PagerAdapter;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.views.dialog.Dialog;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize Dark Theme & Google Ads
        Utils.initializeAppTheme(this);
        Utils.getInstance().initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.viewPagerID);
        AppCompatTextView copyRightText = findViewById(R.id.copyright_Text);
        copyRightText.setText(getString(R.string.copyright));
        if (Utils.getBoolean("allow_ads", true, this)) {
            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        }
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageTasksFragment(), "");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (!PackageTasks.mBatchApps.toString().isEmpty() && PackageTasks.mBatchApps.toString().contains(".")) {
            new Dialog(this)
                    .setMessage(R.string.batch_warning)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        super.onBackPressed();
                    })
                    .show();
        } else if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            Utils.toast(R.string.press_back, this);
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}