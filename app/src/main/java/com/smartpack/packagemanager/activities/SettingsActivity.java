/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Billing;
import com.smartpack.packagemanager.utils.RecycleViewItem;
import com.smartpack.packagemanager.adapters.RecycleViewSettingsAdapter;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsActivity extends AppCompatActivity {

    private ArrayList <RecycleViewItem> mData = new ArrayList<>();
    private String mPath;

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        LinearLayout mAppInfo = findViewById(R.id.app_info);
        MaterialTextView mAppTitle = findViewById(R.id.title);
        MaterialTextView mAppDescription = findViewById(R.id.description);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mAppTitle.setText(getString(R.string.app_name) + (Utils.isProUser(this) ? " Pro " :  " ") + BuildConfig.VERSION_NAME);
        mAppTitle.setTextColor(Utils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);
        mAppDescription.setText(BuildConfig.APPLICATION_ID);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
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

        mData.add(new RecycleViewItem(getString(R.string.sort_by), getString(Utils.getBoolean("sort_id", true, this) ?
                R.string.package_id : R.string.name), getResources().getDrawable(R.drawable.ic_sort), null));
        mData.add(new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this), getResources().getDrawable(R.drawable.ic_show), null));
        mData.add(new RecycleViewItem(getString(R.string.dark_theme), AppSettings.getAppThemeDescription(this), getResources().getDrawable(R.drawable.ic_theme), null));
        mData.add(new RecycleViewItem(getString(R.string.language), AppSettings.getLanguage(this), getResources().getDrawable(R.drawable.ic_language), null));
        mData.add(new RecycleViewItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(
                R.drawable.ic_github), "https://github.com/SmartPack/PackageManager"));
        mData.add(new RecycleViewItem(getString(R.string.support), getString(R.string.support_summary), getResources().getDrawable(R.drawable.ic_support),
                "https://t.me/smartpack_kmanager"));
        mData.add(new RecycleViewItem(getString(R.string.report_issue), getString(R.string.report_issue_summary), getResources().getDrawable(R.drawable.ic_issue),
                "https://github.com/SmartPack/PackageManager/issues/new/choose"));
        mData.add(new RecycleViewItem(getString(R.string.support_development), null, getResources().getDrawable(R.drawable.ic_donate), null));
        mData.add(new RecycleViewItem(getString(R.string.more_apps), getString(R.string.more_apps_summary), getResources().getDrawable(
                R.drawable.ic_playstore), "https://play.google.com/store/apps/dev?id=5836199813143882901"));
        mData.add(new RecycleViewItem(getString(R.string.translations), getString(R.string.translations_summary), getResources().getDrawable(
                R.drawable.ic_translate), "https://poeditor.com/join/project?hash=0CitpyI1Oc"));
        mData.add(new RecycleViewItem(getString(R.string.share_app), getString(R.string.share_app_Summary), getResources().getDrawable(R.drawable.ic_share), null));
        mData.add(new RecycleViewItem(getString(R.string.rate_us), getString(R.string.rate_us_Summary), getResources().getDrawable(R.drawable.ic_rate),
                "https://play.google.com/store/apps/details?id=com.smartpack.packagemanager"));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getUrl() != null) {
                Utils.launchUrl(mData.get(position).getUrl(), this);
            } else if (position == 0) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.sort_apps), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (!Utils.getBoolean("sort_name", false, this)) {
                                Utils.saveBoolean("sort_name", true, this);
                                Utils.saveBoolean("sort_id", false, this);
                                Utils.mReloadPage = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.sort_by), getString(Utils.getBoolean("sort_id", true, this) ?
                                        R.string.package_id : R.string.name), getResources().getDrawable(R.drawable.ic_sort), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 1:
                            if (!Utils.getBoolean("sort_id", true, this)) {
                                Utils.saveBoolean("sort_id", true, this);
                                Utils.saveBoolean("sort_name", false, this);
                                Utils.mReloadPage = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.sort_by), getString(Utils.getBoolean("sort_id", true, this) ?
                                        R.string.package_id : R.string.name), getResources().getDrawable(R.drawable.ic_sort), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 1) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.show_oem), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.resetDefault(this);
                            Utils.saveBoolean("system_apps", true, this);
                            Utils.saveBoolean("user_apps", true, this);
                            Utils.mReloadPage = true;
                            Utils.mSortByOEM = false;
                            mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                    getResources().getDrawable(R.drawable.ic_show), null));
                            mRecycleViewAdapter.notifyItemChanged(position);
                            break;
                        case 1:
                            if (!Utils.getBoolean("asus_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("asus_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 2:
                            if (!Utils.getBoolean("google_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("google_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 3:
                            if (!Utils.getBoolean("samsung_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("samsung_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 4:
                            if (!Utils.getBoolean("moto_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("moto_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 5:
                            if (!Utils.getBoolean("oneplus_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("oneplus_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 6:
                            if (!Utils.getBoolean("huawei_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("huawei_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 7:
                            if (!Utils.getBoolean("sony_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("sony_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 8:
                            if (!Utils.getBoolean("lg_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("lg_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                        case 9:
                            if (!Utils.getBoolean("mi_apps", false, this)) {
                                Utils.resetDefault(this);
                                Utils.saveBoolean("mi_apps", true, this);
                                Utils.mReloadPage = true;
                                Utils.mSortByOEM = true;
                                mData.set(position, new RecycleViewItem(getString(R.string.show_oem), AppSettings.getOEMDescription(this),
                                        getResources().getDrawable(R.drawable.ic_show), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 2) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.app_theme), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (!Utils.getBoolean("theme_auto", true, this)) {
                                Utils.saveBoolean("dark_theme", false, this);
                                Utils.saveBoolean("light_theme", false, this);
                                Utils.saveBoolean("theme_auto", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 1:
                            if (!Utils.getBoolean("dark_theme", false, this)) {
                                Utils.saveBoolean("dark_theme", true, this);
                                Utils.saveBoolean("light_theme", false, this);
                                Utils.saveBoolean("theme_auto", false, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 2:
                            if (!Utils.getBoolean("light_theme", false, this)) {
                                Utils.saveBoolean("dark_theme", false, this);
                                Utils.saveBoolean("light_theme", true, this);
                                Utils.saveBoolean("theme_auto", false, this);
                                Utils.restartApp(this);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 3) {
                new MaterialAlertDialogBuilder(this).setItems(getResources().getStringArray(
                        R.array.app_language), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (!Utils.languageDefault(this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 1:
                            if (!Utils.getBoolean("use_english", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_english", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 2:
                            if (!Utils.getBoolean("use_korean", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_korean", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 3:
                            if (!Utils.getBoolean("use_am", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_am", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 4:
                            if (!Utils.getBoolean("use_el", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_el", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 5:
                            if (!Utils.getBoolean("use_ml", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_ml", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 6:
                            if (!Utils.getBoolean("use_pt", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_pt", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 7:
                            if (!Utils.getBoolean("use_ru", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_ru", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                        case 8:
                            if (!Utils.getBoolean("use_uk", false, this)) {
                                Utils.setDefaultLanguage(this);
                                Utils.saveBoolean("use_uk", true, this);
                                Utils.restartApp(this);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (position == 7) {
                Billing.showDonateOption(this);
            } else if (position == 10) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

}