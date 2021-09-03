/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.CertificateData;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.RecycleViewItem;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class PackageInfoFragment extends Fragment {

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packageinfo, container, false);

        LinearLayout mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        MaterialTextView mProgressMessage = mRootView.findViewById(R.id.progress_message);
        MaterialTextView mPackageID = mRootView.findViewById(R.id.package_id_text);
        MaterialTextView mDataDir = mRootView.findViewById(R.id.data_dir_text);
        MaterialTextView mNatLib = mRootView.findViewById(R.id.native_lib_text);
        MaterialTextView mAPKPathTitle = mRootView.findViewById(R.id.apk_path);
        MaterialTextView mAPKPath = mRootView.findViewById(R.id.apk_path_text);
        MaterialTextView mAPKSize = mRootView.findViewById(R.id.apk_size);
        MaterialTextView mLastUpdated = mRootView.findViewById(R.id.updated_text);
        MaterialTextView mCertificateTitle = mRootView.findViewById(R.id.certificate_title);
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

        mProgressLayout.setBackgroundColor(Utils.isDarkTheme(requireActivity()) ? Color.BLACK : Color.WHITE);
        mLastUpdated.setText(getString(R.string.date_installed, PackageData.getInstalledDate(Common.getApplicationID(), requireActivity())) +
                "\n" + getString(R.string.date_updated, PackageData.getUpdatedDate(Common.getApplicationID(), requireActivity())));

        String certificate = CertificateData.getCertificateDetails(Common.getApplicationID(), requireActivity());
        if (certificate == null) {
            mCertificateTitle.setVisibility(View.GONE);
            mCertificate.setVisibility(View.GONE);
        } else {
            mCertificate.setText(certificate);
        }

        mPackageID.setText(Common.getApplicationID());
        mDisableTitle.setText(PackageData.isEnabled(Common.getApplicationID(), requireActivity()) ? R.string.disable : R.string.enable);
        mDataDir.setText(Common.getDataDir());
        mNatLib.setText(Common.getNativeLibsDir());
        if (new File(PackageData.getSourceDir(Common.getApplicationID(), requireActivity())).getName().equals("base.apk") && SplitAPKInstaller
                .splitApks(PackageData.getParentDir(Common.getApplicationID(), requireActivity())).size() > 1) {
            mAPKPathTitle.setText(getString(R.string.bundle_path));
            mAPKSize.setText(getString(R.string.size_bundle, PackageData.getBundleSize(PackageData.getParentDir(Common.getApplicationID(), requireActivity()))));
        } else {
            mAPKPathTitle.setText(getString(R.string.apk_path));
            mAPKSize.setText(getString(R.string.size_apk, PackageData.getAPKSize(Common.getSourceDir())));
        }
        mAPKPath.setText(PackageData.getParentDir(Common.getApplicationID(), requireActivity()));
        mOpenApp.setVisibility(PackageData.isEnabled(Common.getApplicationID(), requireActivity()) ? View.VISIBLE : View.GONE);
        mOpenApp.setOnClickListener(v -> {
            if (Common.getApplicationID().equals(BuildConfig.APPLICATION_ID)) {
                Utils.snackbar(mRootView, getString(R.string.open_message));
            } else {
                Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(Common.getApplicationID());
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    requireActivity().finish();
                } else {
                    Utils.snackbar(mRootView, getString(R.string.open_failed, Common.getApplicationName()));
                }
            }
        });
        mClear.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setIcon(Common.getApplicationIcon())
                .setTitle(Common.getApplicationName())
                .setMessage(getString(R.string.reset_message, Common.getApplicationName()))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.yes, (dialog, id) -> PackageData.clearAppSettings(Common.getApplicationID())).show());
        mExplore.setOnClickListener(v -> {
            if (Utils.getBoolean("firstExploreAttempt", true, requireActivity())) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.warning))
                        .setMessage(getString(R.string.file_picker_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                            Utils.saveBoolean("firstExploreAttempt", false, requireActivity());
                            PackageExplorer.exploreAPK(mProgressLayout, Common.getSourceDir(), requireActivity());
                        }).show();
            } else {
                PackageExplorer.exploreAPK(mProgressLayout, Common.getSourceDir(), requireActivity());
            }

        });
        mExport.setOnClickListener(v -> PackageDetails.exportApp(mProgressLayout, mProgressMessage, requireActivity()));
        mDisable.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setIcon(Common.getApplicationIcon())
                .setTitle(Common.getApplicationName())
                .setMessage(Common.getApplicationName() + " " + getString(R.string.disable_message,
                        PackageData.isEnabled(Common.getApplicationID(), requireActivity()) ?
                                getString(R.string.disabled) : getString(R.string.enabled)))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> PackageDetails.disableApp(mProgressLayout,
                        mOpenApp, mProgressMessage, mDisableTitle, requireActivity()))
                .show());
        mOpenStore.setOnClickListener(v -> Utils.launchUrl("https://play.google.com/store/apps/details?id=" +
                Common.getApplicationID(), requireActivity()));
        mUninstallApp.setOnClickListener(v -> {
            if (Common.getApplicationID().equals(BuildConfig.APPLICATION_ID)) {
                Utils.snackbar(mRootView, getString(R.string.uninstall_nope));
            } else if (!Common.isSystemApp()) {
                Intent remove = new Intent(Intent.ACTION_DELETE);
                remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                remove.setData(Uri.parse("package:" + Common.getApplicationID()));
                startActivityForResult(remove, 0);
            } else {
                PackageDetails.uninstallSystemApp(mProgressLayout, mProgressMessage, requireActivity());
            }
        });
        mOpenSettings.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", Common.getApplicationID(), null);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && data != null && resultCode == Activity.RESULT_OK) {
            for (RecycleViewItem item : PackageData.getRawData()) {
                if (item.getPackageName().equals(Common.getApplicationID())) {
                    PackageData.getRawData().remove(item);
                }
            }
            Common.reloadPage(true);
            requireActivity().finish();
        }
    }

}