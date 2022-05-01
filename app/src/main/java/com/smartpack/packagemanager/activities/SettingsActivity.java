/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewSettingsAdapter;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Billing;
import com.smartpack.packagemanager.utils.RecycleSettingsItem;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.Utils.sCreditsUtils;
import in.sunilpaulmathew.sCommon.Utils.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.Utils.sThemeUtils;
import in.sunilpaulmathew.sCommon.Utils.sTranslatorUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsActivity extends AppCompatActivity {

    private final ArrayList <RecycleSettingsItem> mData = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "StringFormatInvalid"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        LinearLayout mAppInfo = findViewById(R.id.app_info);
        MaterialTextView mAppTitle = findViewById(R.id.title);
        MaterialTextView mCopyright = findViewById(R.id.copyright);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mAppTitle.setText(getString(R.string.app_name) + (Utils.isProUser(this) ? " Pro " :  " ") + BuildConfig.VERSION_NAME);
        mAppTitle.setTextColor(sUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);
        mCopyright.setText(getString(R.string.copyright, "2022-2023, sunilpaulmathew"));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewSettingsAdapter mRecycleViewAdapter = new RecycleViewSettingsAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mAppInfo.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            settings.setData(uri);
            startActivity(settings);
            finish();
        });

        // User interface
        mData.add(new RecycleSettingsItem(getString(R.string.user_interface), null, null, null, sUtils.getColor(R.color.colorAccent, this), 15));
        mData.add(new RecycleSettingsItem(getString(R.string.language), AppSettings.getLanguage(this), sUtils.getDrawable(R.drawable.ic_language, this), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.app_theme), sThemeUtils.getAppTheme(this), sUtils.getDrawable(R.drawable.ic_theme, this), null, 0, 18));

        // General
        mData.add(new RecycleSettingsItem(getString(R.string.general), null, null, null, sUtils.getColor(R.color.colorAccent, this), 15));
        mData.add(new RecycleSettingsItem(getString(R.string.exported_apps_name), AppSettings.getExportedAPKName(this), sUtils.getDrawable(R.drawable.ic_pencil, this), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.installer_clicking), AppSettings.getInstallerStatus(this), sUtils.getDrawable(R.drawable.ic_install, this), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.exiting_app), AppSettings.getExitingStatus(this), sUtils.getDrawable(R.drawable.ic_exit, this), null, 0, 18));

        // Other
        mData.add(new RecycleSettingsItem(getString(R.string.other), null, null, null, sUtils.getColor(R.color.colorAccent, this), 15));
        mData.add(new RecycleSettingsItem(getString(R.string.source_code), getString(R.string.source_code_summary), sUtils.getDrawable(
                R.drawable.ic_github, this), "https://github.com/SmartPack/PackageManager", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.support), getString(R.string.support_summary), sUtils.getDrawable(R.drawable.ic_support, this),
                "https://t.me/smartpack_kmanager", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.report_issue), getString(R.string.report_issue_summary), sUtils.getDrawable(R.drawable.ic_issue, this),
                "https://github.com/SmartPack/PackageManager/issues/new/choose", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.support_development), null, sUtils.getDrawable(R.drawable.ic_donate, this), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.more_apps), getString(R.string.more_apps_summary), sUtils.getDrawable(
                R.drawable.ic_playstore, this), "https://play.google.com/store/apps/dev?id=5836199813143882901", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.documentation), getString(R.string.documentation_summary), sUtils.getDrawable(
                R.drawable.ic_book, this), "https://smartpack.github.io/PackageManager/general/", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.translations), getString(R.string.translations_summary), sUtils.getDrawable(
                R.drawable.ic_translate, this), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.change_logs), null, sUtils.getDrawable(R.drawable.ic_change_logs, this),
                null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.share_app), getString(R.string.share_app_Summary), sUtils.getDrawable(R.drawable.ic_share, this), null, 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.rate_us), getString(R.string.rate_us_Summary), sUtils.getDrawable(R.drawable.ic_rate, this),
                "https://play.google.com/store/apps/details?id=com.smartpack.packagemanager", 0, 18));
        mData.add(new RecycleSettingsItem(getString(R.string.credits), getString(R.string.credits_summary), sUtils.getDrawable(R.drawable.ic_credits, this),null, 0, 18));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getUrl() != null) {
                sUtils.launchUrl(mData.get(position).getUrl(), this);
            } else if (position == 1) {
                AppSettings.setLanguage(this);
            } else if (position == 2) {
                sThemeUtils.setAppTheme(this);
            } else if (position == 4) {
                new sSingleChoiceDialog(R.drawable.ic_pencil, getString(R.string.exported_apps_name),
                        AppSettings.getAPKNameOptionsMenu(this), AppSettings.getAPKNameOptionsPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (!sUtils.getString("exportedAPKName", getString(R.string.package_id), SettingsActivity.this).equals(getString(R.string.package_id))) {
                                    sUtils.saveString("exportedAPKName", getString(R.string.package_id), SettingsActivity.this);
                                    mData.set(position, new RecycleSettingsItem(getString(R.string.exported_apps_name), getString(R.string.package_id),
                                            sUtils.getDrawable(R.drawable.ic_pencil, SettingsActivity.this), null, 0, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                            case 1:
                                if (!sUtils.getString("exportedAPKName", getString(R.string.package_id), SettingsActivity.this).equals(getString(R.string.name))) {
                                    sUtils.saveString("exportedAPKName", getString(R.string.name), SettingsActivity.this);
                                    mData.set(position, new RecycleSettingsItem(getString(R.string.exported_apps_name), getString(R.string.name),
                                            sUtils.getDrawable(R.drawable.ic_pencil, SettingsActivity.this), null, 0, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                        }
                    }
                }.show();
            } else if (position == 5) {
                new sSingleChoiceDialog(R.drawable.ic_install, getString(R.string.installer_clicking),
                        AppSettings.getInstallerOptionsMenu(this), AppSettings.getInstallerOptionsPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (sUtils.getBoolean("neverShow", false, SettingsActivity.this)) {
                                    sUtils.saveBoolean("neverShow", false, SettingsActivity.this);
                                    mData.set(position, new RecycleSettingsItem(getString(R.string.installer_clicking), getString(R.string.installer_instructions),
                                            sUtils.getDrawable(R.drawable.ic_install, SettingsActivity.this), null, 0, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                            case 1:
                                if (!sUtils.getBoolean("neverShow", false, SettingsActivity.this)) {
                                    sUtils.saveBoolean("neverShow", true, SettingsActivity.this);
                                    mData.set(position, new RecycleSettingsItem(getString(R.string.installer_clicking), getString(R.string.installer_file_picker),
                                            sUtils.getDrawable(R.drawable.ic_install, SettingsActivity.this), null, 0, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                        }
                    }
                }.show();
            } else if (position == 6) {
                new sSingleChoiceDialog(R.drawable.ic_exit, getString(R.string.exiting_app),
                        AppSettings.getExitOptionsMenu(this), AppSettings.getExitMenuPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (sUtils.getBoolean("exit_confirmation", true, SettingsActivity.this)) {
                                    sUtils.saveBoolean("exit_confirmation", false, SettingsActivity.this);
                                    mData.set(position, new RecycleSettingsItem(getString(R.string.exiting_app), getString(R.string.exit_simple),
                                            sUtils.getDrawable(R.drawable.ic_exit, SettingsActivity.this), null, 0, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                            case 1:
                                if (!sUtils.getBoolean("exit_confirmation", true, SettingsActivity.this)) {
                                    sUtils.saveBoolean("exit_confirmation", true, SettingsActivity.this);
                                    mData.set(position, new RecycleSettingsItem(getString(R.string.exiting_app), getString(R.string.exit_confirmation),
                                            sUtils.getDrawable(R.drawable.ic_exit, SettingsActivity.this), null, 0, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                        }
                    }
                }.show();
            } else if (position == 11) {
                Billing.showDonateOption(this);
            } else if (position == 14) {
                new sTranslatorUtils(getString(R.string.app_name), "https://poeditor.com/join/project?hash=0CitpyI1Oc", this).show();
            } else if (position == 15) {
                Intent changeLogs = new Intent(this, ChangeLogsActivity.class);
                startActivity(changeLogs);
            } else if (position == 16) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            } else if (position == 18) {
                new sCreditsUtils(AppSettings.getCredits(),
                        sUtils.getDrawable(R.mipmap.ic_launcher, v.getContext()),
                        sUtils.getDrawable(R.drawable.ic_back, v.getContext()),
                        sUtils.getColor(R.color.colorAccent, v.getContext()),
                        20, v.getContext().getString(R.string.app_name), "2022-2023, sunilpaulmathew",
                        BuildConfig.VERSION_NAME).launchCredits(v.getContext());
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

}