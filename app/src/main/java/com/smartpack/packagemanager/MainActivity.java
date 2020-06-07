/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.smartpack.packagemanager.fragments.PackageTasksFragment;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.PagerAdapter;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.ViewUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private boolean mWelcomeDialog = true;
    private Handler mHandler = new Handler();
    private String copyright = Environment.getExternalStorageDirectory().toString() + "/Package_Manager/copyright";
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize Dark Theme & FaceBook Ads
        Utils.initializeAppTheme(this);
        Utils.initializeFaceBookAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewPagerID);
        AppCompatTextView copyRightText = findViewById(R.id.copyright_Text);

        Utils.mForegroundCard = findViewById(R.id.foreground_card);
        Utils.mBack = findViewById(R.id.back);
        Utils.mAppIcon = findViewById(R.id.app_image);
        Utils.mCardTitle = findViewById(R.id.card_title);
        Utils.mAppName = findViewById(R.id.app_title);
        Utils.mAboutApp = findViewById(R.id.about_app);
        Utils.mCreditsTitle = findViewById(R.id.credits_title);
        Utils.mCredits = findViewById(R.id.credits);
        Utils.mForegroundText = findViewById(R.id.foreground_text);
        Utils.mCancel = findViewById(R.id.cancel_button);
        Utils.mBack.setOnClickListener(v -> {
            Utils.closeForeground(this);
        });
        Utils.mCancel.setOnClickListener(v -> {
            Utils.closeForeground(this);
        });

        // Allow changing Copyright Text
        if (Utils.readFile(copyright) != null) {
            copyRightText.setText(Utils.readFile(copyright));
        } else {
            copyRightText.setText(R.string.copyright);
        }
        copyRightText.setOnLongClickListener(item -> {
            setCopyRightText();
            return false;
        });

        if (Utils.getBoolean("allow_ads", true, this)) {
            AdView mAdView = new AdView(this, "721915415222020_721917968555098", AdSize.BANNER_HEIGHT_50);
            LinearLayout adContainer = findViewById(R.id.banner_container);
            adContainer.addView(mAdView);
            mAdView.loadAd();
        }
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new PackageTasksFragment(), "");
        mViewPager.setAdapter(adapter);
    }

    public void setCopyRightText() {
        if (Utils.isStorageWritePermissionDenied(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.showSnackbar(mViewPager, getString(R.string.permission_denied_write_storage));
            return;
        }
        String PACKAGES = Environment.getExternalStorageDirectory().toString() + "/Package_Manager";
        File file = new File(PACKAGES);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
        ViewUtils.dialogEditText(Utils.readFile(copyright),
                (dialogInterface, i) -> {
                }, text -> {
                    if (text.equals(Utils.readFile(copyright))) return;
                    if (text.isEmpty()) {
                        new File(copyright).delete();
                        Utils.showSnackbar(mViewPager, getString(R.string.copyright_default, getString(R.string.copyright)));
                        return;
                    }
                    Utils.create(text, copyright);
                    Utils.showSnackbar(mViewPager, getString(R.string.copyright_message, text));
                }, this).setOnDismissListener(dialogInterface -> {
        }).show();
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
        if (Utils.mForegroundActive) {
            Utils.closeForeground(this);
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