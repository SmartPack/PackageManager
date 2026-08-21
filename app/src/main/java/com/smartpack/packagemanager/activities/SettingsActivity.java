/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.SettingsAdapter;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Billing;
import com.smartpack.packagemanager.utils.RecyclerViewSwipeListener;
import com.smartpack.packagemanager.utils.Serializables.SettingsItems;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Credits.sCreditsUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;
import in.sunilpaulmathew.sCommon.TranslatorUtils.sTranslatorUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsActivity extends BaseActivity {

    private RecyclerViewSwipeListener mRecyclerViewSwipeListener;
    private SettingsAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithInsets(R.layout.activity_settings, R.id.layout_root);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecycleViewAdapter = new SettingsAdapter(getData(), (settingsItems, position) -> {
            Context context = getApplicationContext();
            if (settingsItems.isWebLink()) {
                sCommonUtils.launchUrl(settingsItems.getUrl(), this);
            } else if (position == 1) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                startActivity(settings);
            } else if (position == 2) {
                AppSettings.setLanguage(this);
            } else if (position == 3) {
                new sSingleChoiceDialog(R.drawable.ic_theme, getString(R.string.app_theme),
                        new String[] {
                                getString(R.string.app_theme_auto),
                                getString(R.string.app_theme_dark),
                                getString(R.string.app_theme_light)
                        }, sCommonUtils.getInt("appTheme", 0, this), this) {

                    @Override
                    public void onItemSelected(int selectedPosition) {
                        if (selectedPosition == sCommonUtils.getInt("appTheme", 0, context)) {
                            return;
                        }
                        sCommonUtils.saveInt("appTheme", selectedPosition, context);
                        switch (selectedPosition) {
                            case 2:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                break;
                            case 1:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                break;
                            default:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                break;
                        }
                        settingsItems.setDescription(sThemeUtils.getAppTheme(context));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 4) {
                new sSingleChoiceDialog(R.drawable.ic_pencil, getString(R.string.exported_apps_name),
                        AppSettings.getAPKNameOptionsMenu(this), AppSettings.getAPKNameOptionsPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (!sCommonUtils.getString("exportedAPKName", getString(R.string.package_id), context).equals(getString(R.string.package_id))) {
                                    sCommonUtils.saveString("exportedAPKName", getString(R.string.package_id), context);
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getString("exportedAPKName", getString(R.string.package_id), context).equals(getString(R.string.name))) {
                                    sCommonUtils.saveString("exportedAPKName", getString(R.string.name), context);
                                }
                                break;
                        }
                        settingsItems.setDescription(AppSettings.getExportedAPKName(context));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 5) {
                new sSingleChoiceDialog(R.drawable.ic_install, getString(R.string.installer_clicking),
                        AppSettings.getInstallerOptionsMenu(this), AppSettings.getInstallerOptionsPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (sCommonUtils.getBoolean("neverShow", false, context)) {
                                    sCommonUtils.saveBoolean("neverShow", false, context);
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getBoolean("neverShow", false, context)) {
                                    sCommonUtils.saveBoolean("neverShow", true, context);
                                }
                                break;
                        }
                        settingsItems.setDescription(AppSettings.getInstallerStatus(context));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 6) {
                new sSingleChoiceDialog(R.drawable.ic_exit, getString(R.string.exiting_app),
                        AppSettings.getExitOptionsMenu(this), AppSettings.getExitMenuPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (sCommonUtils.getBoolean("exit_confirmation", true, context)) {
                                    sCommonUtils.saveBoolean("exit_confirmation", false, context);
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getBoolean("exit_confirmation", true, context)) {
                                    sCommonUtils.saveBoolean("exit_confirmation", true, context);
                                }
                                break;
                        }
                        settingsItems.setDescription(AppSettings.getExitingStatus(context));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 7) {
                Billing.showDonateOption(this);
            } else if (position == 8) {
                new sTranslatorUtils(getString(R.string.app_name), "https://poeditor.com/join/project?hash=0CitpyI1Oc", this).show();
            } else if (position == 9) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, Billing.getAppVersion()) + Billing.getPackageExt());
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            } else if (position == 10) {
                new sCreditsUtils(AppSettings.getCredits(),
                        sCommonUtils.getDrawable(R.mipmap.ic_launcher, this),
                        sCommonUtils.getDrawable(R.drawable.ic_back, this),
                        Utils.getColor(R.attr.colorPrimary, this),
                        25, getString(R.string.app_name), getString(R.string.copyright_text),
                        Billing.getAppVersion()).launchCredits(this
                );
            }
        }));

        mRecyclerViewSwipeListener = new RecyclerViewSwipeListener(mRecyclerView) {
            @Override
            public void onSwipeLeft() {
                finish();
            }

            @Override
            public void onSwipeRight() {
            }
        };
    }

    @SuppressLint("StringFormatInvalid")
    private List<SettingsItems> getData() {
        List <SettingsItems> mData = new CopyOnWriteArrayList<>();
        mData.add(new SettingsItems(getString(R.string.app_name) + " " + Billing.getAppVersion(), getString(R.string.copyright, getString(R.string.copyright_text)), sCommonUtils.getDrawable(R.mipmap.ic_launcher_round, this), 1));

        // User interface
        mData.add(new SettingsItems(getString(R.string.user_interface)));
        mData.add(new SettingsItems(getString(R.string.language), AppSettings.getLanguageDescription(this), sCommonUtils.getDrawable(R.drawable.ic_language, this), 2));
        mData.add(new SettingsItems(getString(R.string.app_theme), sThemeUtils.getAppTheme(this), sCommonUtils.getDrawable(R.drawable.ic_theme, this), 3));

        // General
        mData.add(new SettingsItems(getString(R.string.general)));
        mData.add(new SettingsItems(getString(R.string.exported_apps_name), AppSettings.getExportedAPKName(this), sCommonUtils.getDrawable(R.drawable.ic_pencil, this), 4));
        mData.add(new SettingsItems(getString(R.string.installer_clicking), AppSettings.getInstallerStatus(this), sCommonUtils.getDrawable(R.drawable.ic_install, this), 5));
        mData.add(new SettingsItems(getString(R.string.exiting_app), AppSettings.getExitingStatus(this), sCommonUtils.getDrawable(R.drawable.ic_exit, this), 6));

        // Other
        mData.add(new SettingsItems(getString(R.string.other)));
        mData.add(new SettingsItems(getString(R.string.source_code), getString(R.string.source_code_summary), sCommonUtils.getDrawable(
                R.drawable.ic_github, this), "https://github.com/SmartPack/PackageManager"));
        mData.add(new SettingsItems(getString(R.string.support), getString(R.string.support_summary), sCommonUtils.getDrawable(R.drawable.ic_support, this),
                "https://t.me/smartpack_kmanager"));
        mData.add(new SettingsItems(getString(R.string.report_issue), getString(R.string.report_issue_summary), sCommonUtils.getDrawable(R.drawable.ic_issue, this),
                "https://github.com/SmartPack/PackageManager/issues/new/choose"));
        mData.add(new SettingsItems(getString(R.string.support_development), "", sCommonUtils.getDrawable(R.drawable.ic_donate, this), 7));
        mData.add(new SettingsItems(getString(R.string.more_apps), getString(R.string.more_apps_summary), sCommonUtils.getDrawable(
                R.drawable.ic_playstore, this), "https://play.google.com/store/apps/dev?id=5836199813143882901"));
        mData.add(new SettingsItems(getString(R.string.documentation), getString(R.string.documentation_summary), sCommonUtils.getDrawable(
                R.drawable.ic_book, this), "https://smartpack.github.io/PackageManager/general/"));
        mData.add(new SettingsItems(getString(R.string.translations), getString(R.string.translations_summary), sCommonUtils.getDrawable(
                R.drawable.ic_translate, this), 8));
        mData.add(new SettingsItems(getString(R.string.change_logs), "", sCommonUtils.getDrawable(R.drawable.ic_change_logs, this),
                "https://smartpack.github.io/PackageManager/changelog/"));
        mData.add(new SettingsItems(getString(R.string.share_app), getString(R.string.share_app_Summary), sCommonUtils.getDrawable(R.drawable.ic_share, this), 9));
        mData.add(new SettingsItems(getString(R.string.rate_us), getString(R.string.rate_us_Summary), sCommonUtils.getDrawable(R.drawable.ic_rate, this),
                "https://play.google.com/store/apps/details?id=com.smartpack.packagemanager" + Billing.getPackageExt()));
        mData.add(new SettingsItems(getString(R.string.credits), getString(R.string.credits_summary), sCommonUtils.getDrawable(R.drawable.ic_credits, this), 10));

        return mData;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRecyclerViewSwipeListener != null) {
            mRecyclerViewSwipeListener.detach();
            mRecyclerViewSwipeListener = null;
        }
    }

}