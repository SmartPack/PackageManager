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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Utils;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class PackageInfoFragment extends Fragment {

    @SuppressLint({"SetTextI18n", "StringFormatInvalid"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packageinfo, container, false);

        LinearLayout mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        MaterialTextView mProgressMessage = mRootView.findViewById(R.id.progress_message);
        AppCompatImageView mAppIcon = mRootView.findViewById(R.id.app_image);
        MaterialTextView mAppName = mRootView.findViewById(R.id.app_title);
        MaterialTextView mPackageID = mRootView.findViewById(R.id.package_id_text);
        MaterialTextView mVersion = mRootView.findViewById(R.id.version_text);
        MaterialTextView mDataDir = mRootView.findViewById(R.id.data_dir_text);
        MaterialTextView mNatLib = mRootView.findViewById(R.id.native_lib_text);
        MaterialTextView mAPKPath = mRootView.findViewById(R.id.apk_path_text);
        MaterialTextView mLastUpdated = mRootView.findViewById(R.id.updated_text);
        MaterialTextView mCertificate = mRootView.findViewById(R.id.certificate_text);
        MaterialTextView mDisableTitle = mRootView.findViewById(R.id.enable_title);
        LinearLayout mOpenApp = mRootView.findViewById(R.id.open_app);
        LinearLayout mClear = mRootView.findViewById(R.id.clear_app);
        LinearLayout mExplore = mRootView.findViewById(R.id.explore_app);
        LinearLayout mExport = mRootView.findViewById(R.id.export_app);
        LinearLayout mDisable = mRootView.findViewById(R.id.disable_app);
        LinearLayout mOpenStore = mRootView.findViewById(R.id.playstore_app);
        LinearLayout mUninstallApp = mRootView.findViewById(R.id.remove_app);
        LinearLayout mOpenSettings = mRootView.findViewById(R.id.info_app);
        mAppIcon.setImageDrawable(PackageData.mApplicationIcon);
        mAppName.setText(PackageData.mApplicationName);

        mProgressLayout.setBackgroundColor(Utils.isDarkTheme(requireActivity()) ? Color.BLACK : Color.WHITE);
        mLastUpdated.setText(getString(R.string.date_installed, PackageData.getInstalledDate(PackageData.mApplicationID, requireActivity())) +
                "\n" + getString(R.string.date_updated, PackageData.getUpdatedDate(PackageData.mApplicationID, requireActivity())));
        mCertificate.setText(PackageData.getCertificateDetails(PackageData.mDirSource));
        mPackageID.setText(PackageData.mApplicationID);
        mVersion.setText(getString(R.string.version, PackageData.getVersionName(PackageData.mDirSource, requireActivity())));
        mDisableTitle.setText(PackageData.isEnabled(PackageData.mApplicationID, requireActivity()) ? R.string.disable : R.string.enable);
        mDataDir.setText(PackageData.mDirData);
        mNatLib.setText(PackageData.mDirNatLib);
        mAPKPath.setText(PackageData.getParentDir(PackageData.mApplicationID, requireActivity()));
        mOpenApp.setVisibility(PackageData.isEnabled(PackageData.mApplicationID, requireActivity()) ? View.VISIBLE : View.GONE);
        mOpenApp.setOnClickListener(v -> {
            if (PackageData.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
                Utils.snackbar(mProgressLayout, getString(R.string.open_message));
            } else {
                Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(PackageData.mApplicationID);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    requireActivity().finish();
                } else {
                    Utils.snackbar(mProgressLayout, getString(R.string.open_failed, PackageData.mApplicationName));
                }
            }
        });
        mClear.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setIcon(PackageData.mApplicationIcon)
                .setTitle(PackageData.mApplicationName)
                .setMessage(getString(R.string.reset_message, PackageData.mApplicationName))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    PackageData.clearAppSettings(PackageData.mApplicationID);
                }).show());
        mExplore.setOnClickListener(v -> {
            if (Utils.getBoolean("firstExploreAttempt", true, requireActivity())) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.warning))
                        .setMessage(getString(R.string.file_picker_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                            Utils.saveBoolean("firstExploreAttempt", false, requireActivity());
                            PackageExplorer.exploreAPK(mProgressLayout, PackageData.mDirSource, requireActivity());
                        }).show();
            } else {
                PackageExplorer.exploreAPK(mProgressLayout, PackageData.mDirSource, requireActivity());
            }

        });
        mExport.setOnClickListener(v -> PackageDetails.exportApp(mProgressLayout, mProgressMessage, requireActivity()));
        mDisable.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setIcon(PackageData.mApplicationIcon)
                .setTitle(PackageData.mApplicationName)
                .setMessage(PackageData.mApplicationName + " " + getString(R.string.disable_message,
                        PackageData.isEnabled(PackageData.mApplicationID, requireActivity()) ?
                                getString(R.string.disabled) : getString(R.string.enabled)))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    PackageDetails.disableApp(mProgressLayout, mOpenApp, mProgressMessage, mDisableTitle, requireActivity());
                })
                .show());
        mOpenStore.setOnClickListener(v -> {
            Utils.launchUrl("https://play.google.com/store/apps/details?id=" + PackageData.mApplicationID, requireActivity());
        });
        mUninstallApp.setOnClickListener(v -> PackageDetails.uninstallApp(mProgressLayout, mProgressMessage, requireActivity()));
        mOpenSettings.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", PackageData.mApplicationID, null);
            settings.setData(uri);
            startActivity(settings);
            requireActivity().finish();
        });
        if (Utils.rootAccess()) {
            mClear.setVisibility(View.VISIBLE);
            mExport.setVisibility(View.VISIBLE);
            mDisable.setVisibility(View.VISIBLE);
        }

        return mRootView;
    }

}