/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.SettingsAdapter;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Billing;
import com.smartpack.packagemanager.utils.SerializableItems.SettingsItems;
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
public class SettingsFragment extends Fragment {

    private SettingsAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_settings, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        requireActivity().findViewById(R.id.fab).setVisibility(View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.setAdapter(mRecycleViewAdapter = new SettingsAdapter(getData(), (settingsItems, position) -> {
            if (settingsItems.isWebLink()) {
                sCommonUtils.launchUrl(settingsItems.getUrl(), requireActivity());
            } else if (position == 1) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                startActivity(settings);
            } else if (position == 2) {
                AppSettings.setLanguage(requireActivity());
            } else if (position == 3) {
                new sSingleChoiceDialog(R.drawable.ic_theme, getString(R.string.app_theme),
                        new String[] {
                                getString(R.string.app_theme_auto),
                                getString(R.string.app_theme_dark),
                                getString(R.string.app_theme_light)
                        }, sCommonUtils.getInt("appTheme", 0, requireActivity()), requireActivity()) {

                    @Override
                    public void onItemSelected(int selectedPosition) {
                        if (selectedPosition == sCommonUtils.getInt("appTheme", 0, requireActivity())) {
                            return;
                        }
                        switch (selectedPosition) {
                            case 2:
                                sCommonUtils.saveInt("appTheme", 2, requireActivity());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                break;
                            case 1:
                                sCommonUtils.saveInt("appTheme", 1, requireActivity());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                break;
                            default:
                                sCommonUtils.saveInt("appTheme", 0, requireActivity());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                break;
                        }
                        settingsItems.setDescription(sThemeUtils.getAppTheme(requireActivity()));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 4) {
                new sSingleChoiceDialog(R.drawable.ic_pencil, getString(R.string.exported_apps_name),
                        AppSettings.getAPKNameOptionsMenu(requireActivity()), AppSettings.getAPKNameOptionsPosition(requireActivity()), requireActivity()) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (!sCommonUtils.getString("exportedAPKName", getString(R.string.package_id), requireActivity()).equals(getString(R.string.package_id))) {
                                    sCommonUtils.saveString("exportedAPKName", getString(R.string.package_id), requireActivity());
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getString("exportedAPKName", getString(R.string.package_id), requireActivity()).equals(getString(R.string.name))) {
                                    sCommonUtils.saveString("exportedAPKName", getString(R.string.name), requireActivity());
                                }
                                break;
                        }
                        settingsItems.setDescription(AppSettings.getExportedAPKName(requireActivity()));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 5) {
                new sSingleChoiceDialog(R.drawable.ic_install, getString(R.string.installer_clicking),
                        AppSettings.getInstallerOptionsMenu(requireActivity()), AppSettings.getInstallerOptionsPosition(requireActivity()), requireActivity()) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (sCommonUtils.getBoolean("neverShow", false, requireActivity())) {
                                    sCommonUtils.saveBoolean("neverShow", false, requireActivity());
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getBoolean("neverShow", false, requireActivity())) {
                                    sCommonUtils.saveBoolean("neverShow", true, requireActivity());
                                }
                                break;
                        }
                        settingsItems.setDescription(AppSettings.getInstallerStatus(requireActivity()));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 6) {
                new sSingleChoiceDialog(R.drawable.ic_exit, getString(R.string.exiting_app),
                        AppSettings.getExitOptionsMenu(requireActivity()), AppSettings.getExitMenuPosition(requireActivity()), requireActivity()) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (sCommonUtils.getBoolean("exit_confirmation", true, requireActivity())) {
                                    sCommonUtils.saveBoolean("exit_confirmation", false, requireActivity());
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getBoolean("exit_confirmation", true, requireActivity())) {
                                    sCommonUtils.saveBoolean("exit_confirmation", true, requireActivity());
                                }
                                break;
                        }
                        settingsItems.setDescription(AppSettings.getExitingStatus(requireActivity()));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 7) {
                Billing.showDonateOption(requireActivity());
            } else if (position == 8) {
                new sTranslatorUtils(getString(R.string.app_name), "https://poeditor.com/join/project?hash=0CitpyI1Oc", requireActivity()).show();
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
                        sCommonUtils.getDrawable(R.mipmap.ic_launcher, requireActivity()),
                        sCommonUtils.getDrawable(R.drawable.ic_back, requireActivity()),
                        Utils.getColor(R.attr.colorPrimary, requireActivity()),
                        25, getString(R.string.app_name), getString(R.string.copyright_text),
                        Billing.getAppVersion()).launchCredits(requireActivity()
                );
            }
        }));

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                AppSettings.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

    @SuppressLint("StringFormatInvalid")
    private List<SettingsItems> getData() {
        List <SettingsItems> mData = new CopyOnWriteArrayList<>();
        mData.add(new SettingsItems(getString(R.string.app_name) + " " + Billing.getAppVersion(), getString(R.string.copyright, getString(R.string.copyright_text)), sCommonUtils.getDrawable(R.mipmap.ic_launcher_round, requireActivity()), 1));

        // User interface
        mData.add(new SettingsItems(getString(R.string.user_interface)));
        mData.add(new SettingsItems(getString(R.string.language), AppSettings.getLanguageDescription(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_language, requireActivity()), 2));
        mData.add(new SettingsItems(getString(R.string.app_theme), sThemeUtils.getAppTheme(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_theme, requireActivity()), 3));

        // General
        mData.add(new SettingsItems(getString(R.string.general)));
        mData.add(new SettingsItems(getString(R.string.exported_apps_name), AppSettings.getExportedAPKName(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_pencil, requireActivity()), 4));
        mData.add(new SettingsItems(getString(R.string.installer_clicking), AppSettings.getInstallerStatus(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_install, requireActivity()), 5));
        mData.add(new SettingsItems(getString(R.string.exiting_app), AppSettings.getExitingStatus(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_exit, requireActivity()), 6));

        // Other
        mData.add(new SettingsItems(getString(R.string.other)));
        mData.add(new SettingsItems(getString(R.string.source_code), getString(R.string.source_code_summary), sCommonUtils.getDrawable(
                R.drawable.ic_github, requireActivity()), "https://github.com/SmartPack/PackageManager"));
        mData.add(new SettingsItems(getString(R.string.support), getString(R.string.support_summary), sCommonUtils.getDrawable(R.drawable.ic_support, requireActivity()),
                "https://t.me/smartpack_kmanager"));
        mData.add(new SettingsItems(getString(R.string.report_issue), getString(R.string.report_issue_summary), sCommonUtils.getDrawable(R.drawable.ic_issue, requireActivity()),
                "https://github.com/SmartPack/PackageManager/issues/new/choose"));
        mData.add(new SettingsItems(getString(R.string.support_development), "", sCommonUtils.getDrawable(R.drawable.ic_donate, requireActivity()), 7));
        mData.add(new SettingsItems(getString(R.string.more_apps), getString(R.string.more_apps_summary), sCommonUtils.getDrawable(
                R.drawable.ic_playstore, requireActivity()), "https://play.google.com/store/apps/dev?id=5836199813143882901"));
        mData.add(new SettingsItems(getString(R.string.documentation), getString(R.string.documentation_summary), sCommonUtils.getDrawable(
                R.drawable.ic_book, requireActivity()), "https://smartpack.github.io/PackageManager/general/"));
        mData.add(new SettingsItems(getString(R.string.translations), getString(R.string.translations_summary), sCommonUtils.getDrawable(
                R.drawable.ic_translate, requireActivity()), 8));
        mData.add(new SettingsItems(getString(R.string.change_logs), "", sCommonUtils.getDrawable(R.drawable.ic_change_logs, requireActivity()),
                "https://smartpack.github.io/PackageManager/changelog/"));
        mData.add(new SettingsItems(getString(R.string.share_app), getString(R.string.share_app_Summary), sCommonUtils.getDrawable(R.drawable.ic_share, requireActivity()), 9));
        mData.add(new SettingsItems(getString(R.string.rate_us), getString(R.string.rate_us_Summary), sCommonUtils.getDrawable(R.drawable.ic_rate, requireActivity()),
                "https://play.google.com/store/apps/details?id=com.smartpack.packagemanager" + Billing.getPackageExt()));
        mData.add(new SettingsItems(getString(R.string.credits), getString(R.string.credits_summary), sCommonUtils.getDrawable(R.drawable.ic_credits, requireActivity()), 10));

        return mData;
    }

}