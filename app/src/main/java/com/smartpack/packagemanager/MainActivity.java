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
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.smartpack.packagemanager.fragments.PackageTasksFragment;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.PagerAdapter;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private boolean mWelcomeDialog = true;
    private Handler mHandler = new Handler();
    private ViewPager mViewPager;
    private ViewGroup.MarginLayoutParams mLayoutParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize App Theme & Google Ads
        Utils.initializeAppTheme(this);
        Utils.initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewPagerID);
        mLayoutParams = (ViewGroup.MarginLayoutParams) mViewPager.getLayoutParams();

        if (Utils.getBoolean("allow_ads", true, this)) {
            AdView mAdView = findViewById(R.id.adView);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    mLayoutParams.bottomMargin = 0;
                }
            });
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mLayoutParams.bottomMargin = 0;
        }
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageTasksFragment(), "");
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.getBoolean("welcomeMessage", true, this)) {
            WelcomeDialog();
        }
        if (PackageTasks.mBatchApps == null) {
            PackageTasks.mBatchApps = new StringBuilder();
        }
    }

    /*
     * Taken and used almost as such from https://github.com/morogoku/MTweaks-KernelAdiutorMOD/
     * Ref: https://github.com/morogoku/MTweaks-KernelAdiutorMOD/blob/dd5a4c3242d5e1697d55c4cc6412a9b76c8b8e2e/app/src/main/java/com/moro/mtweaks/fragments/kernel/BoefflaWakelockFragment.java#L133
     */
    private void WelcomeDialog() {
        View checkBoxView = View.inflate(this, R.layout.rv_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        checkBox.setText(getString(R.string.always_show));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mWelcomeDialog = isChecked;
        });

        Dialog alert = new Dialog(Objects.requireNonNull(this));
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(getText(R.string.welcome_message));
        alert.setView(checkBoxView);
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
            Utils.saveBoolean("welcomeMessage", mWelcomeDialog, this);
        });

        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (PackageTasks.mAppName != null) {
            Utils.mKeyEdit.setText(null);
            PackageTasks.mAppName = null;
        } else if (!PackageTasks.mBatchApps.toString().isEmpty() && PackageTasks.mBatchApps.toString().contains(".")) {
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
            Utils.showSnackbar(mViewPager, getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}