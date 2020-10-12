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
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.PackageTasksFragment;
import com.smartpack.packagemanager.utils.PagerAdapter;
import com.smartpack.packagemanager.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme & Google Ads
        Utils.initializeAppTheme(this);
        Utils.initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewPagerID);
        AdView mAdView = findViewById(R.id.adView);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mViewPager.getLayoutParams();

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageTasksFragment(), null);
        mViewPager.setAdapter(adapter);

        if (Utils.isNotDonated(this) || Utils.getBoolean("allow_ads", true, this)) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    layoutParams.setMargins(0,0,0,0);
                }
            });
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            layoutParams.setMargins(0,0,0,0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PackageTasks.mBatchApps == null) {
            PackageTasks.mBatchApps = new StringBuilder();
        }
    }

    @Override
    public void onBackPressed() {
        if (Utils.mSearchText != null) {
            Utils.mSearchWord.setText(null);
            Utils.mSearchText = null;
            return;
        }
        if (!PackageTasks.mBatchApps.toString().isEmpty() && PackageTasks.mBatchApps.toString().contains(".")) {
            new AlertDialog.Builder(this)
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
            Utils.snackbar(mViewPager, getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}