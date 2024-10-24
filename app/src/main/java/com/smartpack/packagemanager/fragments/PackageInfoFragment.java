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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PackageInfoAdapter;
import com.smartpack.packagemanager.adapters.PackageOptionsAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.Flavor;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.PackageInfoItems;
import com.smartpack.packagemanager.utils.PackageOptionsItems;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.tasks.DisableAppTasks;
import com.smartpack.packagemanager.utils.tasks.ExploreAPKTasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class PackageInfoFragment extends Fragment {

    private boolean mRootOrShizuku = false;

    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packageinfo, container, false);

        LinearLayout mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        MaterialTextView mProgressMessage = mRootView.findViewById(R.id.progress_message);
        ProgressBar mProgress = mRootView.findViewById(R.id.progress);
        RecyclerView mPackageOptions = mRootView.findViewById(R.id.package_options);
        RecyclerView mPackageInfo = mRootView.findViewById(R.id.recycler_view);

        PackageOptionsAdapter mPackageOptionsAdapter;
        PackageInfoAdapter mPackageInfoAdapter;
        mPackageOptions.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mPackageInfo.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mRootOrShizuku = new RootShell().rootAccess() || new ShizukuShell().isReady();

        mPackageOptionsAdapter = new PackageOptionsAdapter(getPackageOptionsData());
        mPackageInfoAdapter = new PackageInfoAdapter(getPackageInfoData());
        mPackageOptions.setAdapter(mPackageOptionsAdapter);
        mPackageInfo.setAdapter(mPackageInfoAdapter);

        mPackageOptionsAdapter.setOnItemClickListener((position, v) -> {
            switch (getPackageOptionsData().get(position).getPosition()) {
                case 0:
                    if (Common.getApplicationID().equals(BuildConfig.APPLICATION_ID)) {
                        sCommonUtils.snackBar(mRootView, getString(R.string.open_message)).show();
                    } else {
                        Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(Common.getApplicationID());
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        } else {
                            sCommonUtils.snackBar(mRootView, getString(R.string.open_failed, Common.getApplicationName())).show();
                        }
                    }
                    break;
                case 1:
                    if (sCommonUtils.getBoolean("firstExploreAttempt", true, requireActivity())) {
                        new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(getString(R.string.warning))
                                .setMessage(getString(R.string.file_picker_message))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                                    sCommonUtils.saveBoolean("firstExploreAttempt", false, requireActivity());
                                    new ExploreAPKTasks(mProgressLayout, mProgress, Common.getSourceDir(), requireActivity()).execute();
                                }).show();
                    } else {
                        new ExploreAPKTasks(mProgressLayout, mProgress, Common.getSourceDir(), requireActivity()).execute();
                    }
                    break;
                case 2:
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(Common.getApplicationIcon())
                            .setTitle(Common.getApplicationName())
                            .setMessage(Common.getApplicationName() + " " + getString(R.string.disable_message,
                                    sPackageUtils.isEnabled(Common.getApplicationID(), requireActivity()) ?
                                            getString(R.string.disabled) : getString(R.string.enabled)))
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(getString(R.string.yes), (dialog, id) -> new DisableAppTasks(mProgressLayout, mProgressMessage, requireActivity()).execute())
                            .show();
                    break;
                case 3:
                    if (Common.getApplicationID().equals(BuildConfig.APPLICATION_ID)) {
                        sCommonUtils.snackBar(mRootView, getString(R.string.uninstall_nope)).show();
                    } else if (!Common.isSystemApp()) {
                        Common.isUninstall(true);
                        requireActivity().finish();
                    } else {
                        PackageDetails.uninstallSystemApp(mProgressLayout, mProgressMessage, requireActivity());
                    }
                    break;
                default:
                    Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", Common.getApplicationID(), null);
                    settings.setData(uri);
                    startActivity(settings);
                    requireActivity().finish();
                    break;
            }
        });

        mPackageInfoAdapter.setOnItemClickListener((position, v) -> {
            if (position == 0) {
                PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
                Menu menu = popupMenu.getMenu();
                menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.search_market_message, getString(R.string.playstore)));
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.search_market_message, getString(R.string.fdroid)));
                menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.export_details));
                menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.share));
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 0:
                            sCommonUtils.launchUrl("https://play.google.com/store/apps/details?id=" + Common.getApplicationID(), requireActivity());
                            break;
                        case 1:
                            sCommonUtils.launchUrl("https://f-droid.org/packages/" + Common.getApplicationID(), requireActivity());
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
                                sCommonUtils.snackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.permission_denied_write_storage)).show();
                            } else {
                                if (!PackageData.getPackageDir(requireActivity()).exists()) {
                                    sFileUtils.mkdir(PackageData.getPackageDir(requireActivity()));
                                }
                                File mJSON = new File(PackageData.getPackageDir(requireActivity()), Common.getApplicationID() + "_" + sAPKUtils.getVersionCode(
                                        sPackageUtils.getSourceDir(Common.getApplicationID(), requireActivity()), requireActivity()) + ".json");
                                sFileUtils.create(Objects.requireNonNull(PackageDetails.getPackageDetails(Common.getApplicationID(), requireActivity())).toString(), mJSON);
                                sCommonUtils.snackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.export_details_message, mJSON.getName())).show();
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
            } else if (position == 1) {
                PackageDetails.exportApp(mProgressLayout, mProgressMessage, mProgress, requireActivity());
            } else if (position == 2) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(Common.getApplicationIcon())
                        .setTitle(Common.getApplicationName())
                        .setMessage(getString(R.string.reset_message, Common.getApplicationName()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.yes, (dialog, id) ->
                                PackageData.clearAppSettings(Common.getApplicationID())
                        ).show();
            }
        });

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

    @SuppressLint("StringFormatInvalid")
    private List<PackageInfoItems> getPackageInfoData() {
        List<PackageInfoItems> mPackageInfoItems = new ArrayList<>();
        boolean mAppBundle = new File(sPackageUtils.getSourceDir(Common.getApplicationID(), requireActivity())).getName().equals("base.apk") && SplitAPKInstaller
                .splitApks(sPackageUtils.getParentDir(Common.getApplicationID(), requireActivity())).size() > 1;
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.package_id), Common.getApplicationID(), null, null,
                getString(R.string.more), sCommonUtils.getDrawable(R.drawable.ic_dots, requireActivity())));
        mPackageInfoItems.add(new PackageInfoItems(getString(mAppBundle ? R.string.bundle_path : R.string.apk_path), sPackageUtils.getParentDir(
                Common.getApplicationID(), requireActivity()), null, mAppBundle ? getString(R.string.size_bundle, PackageData
                .getBundleSize(sPackageUtils.getParentDir(Common.getApplicationID(), requireActivity()))) : getString(R.string.size_apk,
                sAPKUtils.getAPKSize(new File(Common.getSourceDir()).length())), getString(R.string.export), sCommonUtils.getDrawable(
                R.drawable.ic_export, requireActivity())));
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.data_dir), Common.getDataDir(), null, null,
                mRootOrShizuku ? getString(R.string.reset) : null, mRootOrShizuku ? sCommonUtils.getDrawable(R.drawable.ic_reset, requireActivity()) : null));
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.native_lib), null, Common.getNativeLibsDir(), null,
                null, null));
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.date_installation), null, getString(R.string.date_installed, sPackageUtils.getInstalledDate(
                Common.getApplicationID(), requireActivity())) + "\n" + getString(R.string.date_updated, sPackageUtils.getUpdatedDate(Common.getApplicationID(),
                requireActivity())), null, null, null));
        try {
            mPackageInfoItems.add(new PackageInfoItems(getString(R.string.certificate), null, APKParser.getCertificateDetails(sPackageUtils.getSourceDir(
                    Common.getApplicationID(), requireActivity()), requireActivity()).trim(), null, null, null));
        } catch (Exception ignored) {
        }
        return mPackageInfoItems;
    }

    private List<PackageOptionsItems> getPackageOptionsData() {
        List<PackageOptionsItems> mPackageOptionsItems = new ArrayList<>();
        if (sPackageUtils.isEnabled(Common.getApplicationID(), requireActivity())) {
            mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_open, requireActivity()), getString(R.string.open), 0));
        }
        mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_explore, requireActivity()), getString(R.string.explore), 1));
        if (mRootOrShizuku) {
            mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_disable, requireActivity()), getString(sPackageUtils.isEnabled(
                    Common.getApplicationID(), requireActivity()) ? R.string.disable : R.string.enable), 2));
        }
        mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_delete, requireActivity()), getString(R.string.uninstall), 3));
        mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_doubledots, requireActivity()), getString(R.string.app_info), 4));
        return mPackageOptionsItems;
    }

}