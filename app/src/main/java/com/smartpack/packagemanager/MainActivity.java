/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager.widget.ViewPager;

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
        // Set App Language
        super.onCreate(savedInstanceState);
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.viewPagerID);
        AppCompatTextView copyRightText = findViewById(R.id.copyright_Text);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageTasksFragment(), "");

        copyRightText.setText(getString(R.string.about));
        viewPager.setAdapter(adapter);
    }

    public void creditsDialogue(View view) {
        new Dialog(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name) + "\nv" + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.about_summary))
                .setNegativeButton(getString(R.string.more_apps), (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/developer?id=sunilpaulmathew"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                })
                .setNeutralButton(getString(R.string.report_issue), (dialogInterface, i) -> {
                    Utils.launchUrl("https://github.com/SmartPack/PackageManager/issues/new", this);
                })
                .setPositiveButton(getString(R.string.support), (dialogInterface, i) -> {
                    Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (!PackageTasks.mBatchApps.toString().isEmpty()) {
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