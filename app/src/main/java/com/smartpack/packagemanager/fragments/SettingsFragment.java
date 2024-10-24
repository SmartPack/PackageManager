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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.SettingsAdapter;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Billing;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.SettingsItems;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Credits.sCreditsUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;
import in.sunilpaulmathew.sCommon.TranslatorUtils.sTranslatorUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsFragment extends Fragment {

    private final ArrayList <SettingsItems> mData = new ArrayList<>();

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_settings, container, false);

        LinearLayout mAppInfo = mRootView.findViewById(R.id.app_info);
        MaterialTextView mAppTitle = mRootView.findViewById(R.id.title);
        MaterialTextView mCopyright = mRootView.findViewById(R.id.copyright);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        Common.getView(requireActivity(), R.id.fab).setVisibility(View.GONE);

        mAppTitle.setText(getString(R.string.app_name) + (Utils.isProUser(requireActivity()) ? " Pro " :  " ") + BuildConfig.VERSION_NAME);
        mAppTitle.setTextColor(sThemeUtils.isDarkTheme(requireActivity()) ? Color.WHITE : Color.BLACK);
        mCopyright.setText(getString(R.string.copyright, "2024-2025, sunilpaulmathew"));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        SettingsAdapter mRecycleViewAdapter = new SettingsAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mAppInfo.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            settings.setData(uri);
            startActivity(settings);
        });

        // User interface
        mData.add(new SettingsItems(getString(R.string.user_interface), null, null, null, true, 15));
        mData.add(new SettingsItems(getString(R.string.language), AppSettings.getLanguage(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_language, requireActivity()), null, false, 18));
        mData.add(new SettingsItems(getString(R.string.app_theme), sThemeUtils.getAppTheme(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_theme, requireActivity()), null, false, 18));

        // General
        mData.add(new SettingsItems(getString(R.string.general), null, null, null, true, 15));
        mData.add(new SettingsItems(getString(R.string.exported_apps_name), AppSettings.getExportedAPKName(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_pencil, requireActivity()), null, false, 18));
        mData.add(new SettingsItems(getString(R.string.installer_clicking), AppSettings.getInstallerStatus(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_install, requireActivity()), null, false, 18));
        mData.add(new SettingsItems(getString(R.string.exiting_app), AppSettings.getExitingStatus(requireActivity()), sCommonUtils.getDrawable(R.drawable.ic_exit, requireActivity()), null, false, 18));

        // Other
        mData.add(new SettingsItems(getString(R.string.other), null, null, null, true, 15));
        mData.add(new SettingsItems(getString(R.string.source_code), getString(R.string.source_code_summary), sCommonUtils.getDrawable(
                R.drawable.ic_github, requireActivity()), "https://github.com/SmartPack/PackageManager", false, 18));
        mData.add(new SettingsItems(getString(R.string.support), getString(R.string.support_summary), sCommonUtils.getDrawable(R.drawable.ic_support, requireActivity()),
                "https://t.me/smartpack_kmanager", false, 18));
        mData.add(new SettingsItems(getString(R.string.report_issue), getString(R.string.report_issue_summary), sCommonUtils.getDrawable(R.drawable.ic_issue, requireActivity()),
                "https://github.com/SmartPack/PackageManager/issues/new/choose", false, 18));
        mData.add(new SettingsItems(getString(R.string.support_development), null, sCommonUtils.getDrawable(R.drawable.ic_donate, requireActivity()), null, false, 18));
        mData.add(new SettingsItems(getString(R.string.more_apps), getString(R.string.more_apps_summary), sCommonUtils.getDrawable(
                R.drawable.ic_playstore, requireActivity()), "https://play.google.com/store/apps/dev?id=5836199813143882901", false, 18));
        mData.add(new SettingsItems(getString(R.string.documentation), getString(R.string.documentation_summary), sCommonUtils.getDrawable(
                R.drawable.ic_book, requireActivity()), "https://smartpack.github.io/PackageManager/general/", false, 18));
        mData.add(new SettingsItems(getString(R.string.translations), getString(R.string.translations_summary), sCommonUtils.getDrawable(
                R.drawable.ic_translate, requireActivity()), null, false, 18));
        mData.add(new SettingsItems(getString(R.string.change_logs), null, sCommonUtils.getDrawable(R.drawable.ic_change_logs, requireActivity()),
                "https://smartpack.github.io/PackageManager/change-logs/", false, 18));
        mData.add(new SettingsItems(getString(R.string.share_app), getString(R.string.share_app_Summary), sCommonUtils.getDrawable(R.drawable.ic_share, requireActivity()), null, false, 18));
        mData.add(new SettingsItems(getString(R.string.rate_us), getString(R.string.rate_us_Summary), sCommonUtils.getDrawable(R.drawable.ic_rate, requireActivity()),
                "https://play.google.com/store/apps/details?id=com.smartpack.packagemanager", false, 18));
        mData.add(new SettingsItems(getString(R.string.credits), getString(R.string.credits_summary), sCommonUtils.getDrawable(R.drawable.ic_credits, requireActivity()),null, false, 18));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getUrl() != null) {
                sCommonUtils.launchUrl(mData.get(position).getUrl(), requireActivity());
            } else if (position == 1) {
                AppSettings.setLanguage(requireActivity());
            } else if (position == 2) {
                sThemeUtils.setAppTheme(requireActivity());
            } else if (position == 4) {
                new sSingleChoiceDialog(R.drawable.ic_pencil, getString(R.string.exported_apps_name),
                        AppSettings.getAPKNameOptionsMenu(requireActivity()), AppSettings.getAPKNameOptionsPosition(requireActivity()), requireActivity()) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        switch (itemPosition) {
                            case 0:
                                if (!sCommonUtils.getString("exportedAPKName", getString(R.string.package_id), requireActivity()).equals(getString(R.string.package_id))) {
                                    sCommonUtils.saveString("exportedAPKName", getString(R.string.package_id), requireActivity());
                                    mData.set(position, new SettingsItems(getString(R.string.exported_apps_name), getString(R.string.package_id),
                                            sCommonUtils.getDrawable(R.drawable.ic_pencil, requireActivity()), null, false, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getString("exportedAPKName", getString(R.string.package_id), requireActivity()).equals(getString(R.string.name))) {
                                    sCommonUtils.saveString("exportedAPKName", getString(R.string.name), requireActivity());
                                    mData.set(position, new SettingsItems(getString(R.string.exported_apps_name), getString(R.string.name),
                                            sCommonUtils.getDrawable(R.drawable.ic_pencil, requireActivity()), null, false, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                        }
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
                                    mData.set(position, new SettingsItems(getString(R.string.installer_clicking), getString(R.string.installer_instructions),
                                            sCommonUtils.getDrawable(R.drawable.ic_install, requireActivity()), null, false, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getBoolean("neverShow", false, requireActivity())) {
                                    sCommonUtils.saveBoolean("neverShow", true, requireActivity());
                                    mData.set(position, new SettingsItems(getString(R.string.installer_clicking), getString(R.string.installer_file_picker),
                                            sCommonUtils.getDrawable(R.drawable.ic_install, requireActivity()), null, false, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                        }
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
                                    mData.set(position, new SettingsItems(getString(R.string.exiting_app), getString(R.string.exit_simple),
                                            sCommonUtils.getDrawable(R.drawable.ic_exit, requireActivity()), null, false, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                            case 1:
                                if (!sCommonUtils.getBoolean("exit_confirmation", true, requireActivity())) {
                                    sCommonUtils.saveBoolean("exit_confirmation", true, requireActivity());
                                    mData.set(position, new SettingsItems(getString(R.string.exiting_app), getString(R.string.exit_confirmation),
                                            sCommonUtils.getDrawable(R.drawable.ic_exit, requireActivity()), null, false, 18));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                                break;
                        }
                    }
                }.show();
            } else if (position == 11) {
                Billing.showDonateOption(requireActivity());
            } else if (position == 14) {
                new sTranslatorUtils(getString(R.string.app_name), "https://poeditor.com/join/project?hash=0CitpyI1Oc", requireActivity()).show();
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
                        sCommonUtils.getDrawable(R.mipmap.ic_launcher, requireActivity()),
                        sCommonUtils.getDrawable(R.drawable.ic_back, requireActivity()),
                        sCommonUtils.getColor(sThemeUtils.isDarkTheme(requireActivity()) ?
                                R.color.colorWhite : R.color.colorBlack, requireActivity()),
                        20, getString(R.string.app_name), "2024-2025, sunilpaulmathew",
                        BuildConfig.VERSION_NAME).launchCredits(v.getContext()
                );
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Common.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

}