/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.Flavor;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sAPKCertificateUtils;
import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

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
        LinearLayout mMore = mRootView.findViewById(R.id.more);
        LinearLayout mUninstallApp = mRootView.findViewById(R.id.remove_app);
        LinearLayout mOpenSettings = mRootView.findViewById(R.id.info_app);

        mProgressLayout.setBackgroundColor(sUtils.isDarkTheme(requireActivity()) ? Color.BLACK : Color.WHITE);
        mLastUpdated.setText(getString(R.string.date_installed, sPackageUtils.getInstalledDate(Common.getApplicationID(), requireActivity())) +
                "\n" + getString(R.string.date_updated, sPackageUtils.getUpdatedDate(Common.getApplicationID(), requireActivity())));

        String certificate = new sAPKCertificateUtils(null, Common.getApplicationID(), requireActivity()).getCertificateDetails();
        if (certificate == null) {
            mCertificateTitle.setVisibility(View.GONE);
            mCertificate.setVisibility(View.GONE);
        } else {
            mCertificate.setText(certificate);
        }

        mPackageID.setText(Common.getApplicationID());
        mDisableTitle.setText(sPackageUtils.isEnabled(Common.getApplicationID(), requireActivity()) ? R.string.disable : R.string.enable);
        mDataDir.setText(Common.getDataDir());
        mNatLib.setText(Common.getNativeLibsDir());
        if (new File(sPackageUtils.getSourceDir(Common.getApplicationID(), requireActivity())).getName().equals("base.apk") && SplitAPKInstaller
                .splitApks(sPackageUtils.getParentDir(Common.getApplicationID(), requireActivity())).size() > 1) {
            mAPKPathTitle.setText(getString(R.string.bundle_path));
            mAPKSize.setText(getString(R.string.size_bundle, PackageData.getBundleSize(sPackageUtils.getParentDir(Common.getApplicationID(), requireActivity()))));
        } else {
            mAPKPathTitle.setText(getString(R.string.apk_path));
            mAPKSize.setText(getString(R.string.size_apk, sAPKUtils.getAPKSize(Common.getSourceDir())));
        }
        mAPKPath.setText(sPackageUtils.getParentDir(Common.getApplicationID(), requireActivity()));
        mOpenApp.setVisibility(sPackageUtils.isEnabled(Common.getApplicationID(), requireActivity()) ? View.VISIBLE : View.GONE);
        mOpenApp.setOnClickListener(v -> {
            if (Common.getApplicationID().equals(BuildConfig.APPLICATION_ID)) {
                sUtils.snackBar(mRootView, getString(R.string.open_message)).show();
            } else {
                Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(Common.getApplicationID());
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    sUtils.snackBar(mRootView, getString(R.string.open_failed, Common.getApplicationName())).show();
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
            if (sUtils.getBoolean("firstExploreAttempt", true, requireActivity())) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.warning))
                        .setMessage(getString(R.string.file_picker_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                            sUtils.saveBoolean("firstExploreAttempt", false, requireActivity());
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
                        sPackageUtils.isEnabled(Common.getApplicationID(), requireActivity()) ?
                                getString(R.string.disabled) : getString(R.string.enabled)))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> PackageDetails.disableApp(mProgressLayout, mProgressMessage, requireActivity()))
                .show());
        mMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mMore);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.search_market_message, getString(R.string.playstore)));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.search_market_message, getString(R.string.fdroid)));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.export_details));
            menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.share));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        sUtils.launchUrl("https://play.google.com/store/apps/details?id=" + Common.getApplicationID(), requireActivity());
                        break;
                    case 1:
                        sUtils.launchUrl("https://f-droid.org/packages/" + Common.getApplicationID(), requireActivity());
                        break;
                    case 2:
                        if (Flavor.isFullVersion() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Utils.isPermissionDenied() ||
                                Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                new MaterialAlertDialogBuilder(requireActivity())
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle(R.string.app_name)
                                        .setMessage(getString(R.string.file_permission_request_message))
                                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                        })
                                        .setPositiveButton(getString(R.string.grant), (dialogInterface, i) ->
                                                Utils.requestPermission(requireActivity()))
                                        .show();
                            } else {
                                sPermissionUtils.requestPermission(new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        requireActivity());
                            }
                            sUtils.snackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.permission_denied_write_storage)).show();
                        } else {
                            if (!PackageData.getPackageDir(requireActivity()).exists()) {
                                PackageData.getPackageDir(requireActivity()).mkdirs();
                            }
                            File mJSON = new File(PackageData.getPackageDir(requireActivity()), Common.getApplicationID() + "_" + sAPKUtils.getVersionCode(
                                    sPackageUtils.getSourceDir(Common.getApplicationID(), requireActivity()), requireActivity()) + ".json");
                            sUtils.create(Objects.requireNonNull(PackageDetails.getPackageDetails(Common.getApplicationID(), requireActivity())).toString(), mJSON);
                            sUtils.snackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.export_details_message, mJSON.getName())).show();
                        }
                        break;
                    case 3:
                        Intent shareLink = new Intent();
                        shareLink.setAction(Intent.ACTION_SEND);
                        shareLink.putExtra(Intent.EXTRA_SUBJECT, Common.getApplicationName());
                        shareLink.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + Common.getApplicationID()
                                + "\n\n" + getString(R.string.share_message, BuildConfig.VERSION_NAME));
                        shareLink.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(shareLink, getString(R.string.share_with));
                        startActivity(shareIntent);
                        break;
                }
                return false;
            });
            popupMenu.show();
        });
        mUninstallApp.setOnClickListener(v -> {
            if (Common.getApplicationID().equals(BuildConfig.APPLICATION_ID)) {
                sUtils.snackBar(mRootView, getString(R.string.uninstall_nope)).show();
            } else if (!Common.isSystemApp()) {
                Common.isUninstall(true);
                requireActivity().finish();
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
        if (new RootShell().rootAccess() || new ShizukuShell().isReady()) {
            mClear.setVisibility(View.VISIBLE);
            mDisable.setVisibility(View.VISIBLE);
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgressLayout.getVisibility() == View.GONE) {
                    requireActivity().finish();
                }
            }
        });

        return mRootView;
    }

}