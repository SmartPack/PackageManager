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
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PackageInfoAdapter;
import com.smartpack.packagemanager.adapters.PackageOptionsAdapter;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.SerializableItems.PackageInfoItems;
import com.smartpack.packagemanager.utils.SerializableItems.PackageOptionsItems;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
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

    private boolean mLaunchIntent = false, mRootOrShizuku = false;
    private String mAppName, mPackageName;

    public PackageInfoFragment() {
    }

    public static PackageInfoFragment newInstance(String appName, String packageName, boolean launchIntent) {
        PackageInfoFragment fragment = new PackageInfoFragment();

        Bundle args = new Bundle();
        args.putString("appNameIntent", appName);
        args.putString("packageNameIntent", packageName);
        args.putBoolean("launchIntent", launchIntent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mAppName = getArguments().getString("appNameIntent");
            mPackageName = getArguments().getString("packageNameIntent");
            mLaunchIntent = getArguments().getBoolean("launchIntent");
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packageinfo, container, false);

        RecyclerView mPackageOptions = mRootView.findViewById(R.id.package_options);
        RecyclerView mPackageInfo = mRootView.findViewById(R.id.recycler_view);

        PackageOptionsAdapter mPackageOptionsAdapter;
        PackageInfoAdapter mPackageInfoAdapter;
        mPackageOptions.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mPackageInfo.setLayoutManager(new LinearLayoutManager(requireActivity()));

        Drawable appIcon = sPackageUtils.getAppIcon(mPackageName, requireActivity());

        mRootOrShizuku = new RootShell().rootAccess() || new ShizukuShell().isReady();

        mPackageOptionsAdapter = new PackageOptionsAdapter(getPackageOptionsData());
        mPackageInfoAdapter = new PackageInfoAdapter(getPackageInfoData());
        mPackageOptions.setAdapter(mPackageOptionsAdapter);
        mPackageInfo.setAdapter(mPackageInfoAdapter);

        mPackageOptionsAdapter.setOnItemClickListener((position, v) -> {
            switch (getPackageOptionsData().get(position).getPosition()) {
                case 0:
                    if (mPackageName.equals(BuildConfig.APPLICATION_ID)) {
                        sCommonUtils.toast(getString(R.string.open_message), requireActivity()).show();
                    } else {
                        Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(mPackageName);
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        } else {
                            sCommonUtils.toast(getString(R.string.open_failed, mAppName), requireActivity()).show();
                        }
                    }
                    break;
                case 1:
                    new ExploreAPKTasks(mAppName, mPackageName, sPackageUtils.getSourceDir(mPackageName, requireActivity()), requireActivity()).execute();
                    break;
                case 2:
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(appIcon)
                            .setTitle(mAppName)
                            .setMessage(mAppName + " " + getString(R.string.disable_message,
                                    sPackageUtils.isEnabled(mPackageName, requireActivity()) ?
                                            getString(R.string.disabled) : getString(R.string.enabled)))
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(getString(R.string.yes), (dialog, id) -> new DisableAppTasks(mAppName, mPackageName, requireActivity()).execute())
                            .show();
                    break;
                case 3:
                    if (mPackageName.equals(BuildConfig.APPLICATION_ID)) {
                        sCommonUtils.toast(getString(R.string.uninstall_nope), requireActivity()).show();
                    } else if (!sPackageUtils.isSystemApp(mPackageName, requireActivity())) {
                        Intent intent = new Intent();
                        intent.putExtra("packageName", mPackageName);
                        requireActivity().setResult(Activity.RESULT_OK, intent);
                        requireActivity().finish();
                    } else {
                        PackageDetails.uninstallSystemApp(mPackageName, mAppName, appIcon, requireActivity());
                    }
                    break;
                default:
                    Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", mPackageName, null);
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
                            sCommonUtils.launchUrl("https://play.google.com/store/apps/details?id=" + mPackageName, requireActivity());
                            break;
                        case 1:
                            sCommonUtils.launchUrl("https://f-droid.org/packages/" + mPackageName, requireActivity());
                            break;
                        case 2:
                            if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            } else {
                                PackageData.makePackageFolder(requireActivity());
                                File mJSON = new File(PackageData.getPackageDir(requireActivity()), mPackageName + "_" + sAPKUtils.getVersionCode(
                                        sPackageUtils.getSourceDir(mPackageName, requireActivity()), requireActivity()) + ".json");
                                sFileUtils.create(Objects.requireNonNull(PackageDetails.getPackageDetails(mPackageName, requireActivity())).toString(), mJSON);

                                new MaterialAlertDialogBuilder(requireActivity())
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle(R.string.app_name)
                                        .setMessage(getString(R.string.export_details_message, mJSON.getAbsolutePath()))
                                        .setPositiveButton(R.string.cancel, (dialog, i) -> {
                                        }).show();
                            }
                            break;
                        case 3:
                            Intent shareLink = new Intent();
                            shareLink.setAction(Intent.ACTION_SEND);
                            shareLink.putExtra(Intent.EXTRA_SUBJECT, mAppName);
                            shareLink.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + mPackageName
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
                if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    PackageDetails.exportApp(mPackageName, sPackageUtils.getSourceDir(mPackageName, requireActivity()), appIcon, requireActivity());
                }
            } else if (position == 2) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(appIcon)
                        .setTitle(mAppName)
                        .setMessage(getString(R.string.reset_message, mAppName))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.yes, (dialog, id) ->
                                PackageData.clearAppSettings(mPackageName)
                        ).show();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });

        return mRootView;
    }

    @SuppressLint("StringFormatInvalid")
    private List<PackageInfoItems> getPackageInfoData() {
        List<PackageInfoItems> mPackageInfoItems = new ArrayList<>();
        boolean mAppBundle = new File(sPackageUtils.getSourceDir(mPackageName, requireActivity())).getName().equals("base.apk") && SplitAPKInstaller
                .splitApks(sPackageUtils.getParentDir(mPackageName, requireActivity())).size() > 1;
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.package_id), mPackageName, null, null,
                getString(R.string.more), sCommonUtils.getDrawable(R.drawable.ic_dots, requireActivity())));
        mPackageInfoItems.add(new PackageInfoItems(getString(mAppBundle ? R.string.bundle_path : R.string.apk_path), sPackageUtils.getParentDir(
                mPackageName, requireActivity()), null, mAppBundle ? getString(R.string.size_bundle, PackageData
                .getBundleSize(sPackageUtils.getParentDir(mPackageName, requireActivity()))) : getString(R.string.size_apk,
                sAPKUtils.getAPKSize(new File(sPackageUtils.getSourceDir(mPackageName, requireActivity())).length())), getString(R.string.export), sCommonUtils.getDrawable(
                R.drawable.ic_download, requireActivity())));
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.data_dir), sPackageUtils.getDataDir(mPackageName, requireActivity()), null, null,
                mRootOrShizuku ? getString(R.string.reset) : null, mRootOrShizuku ? sCommonUtils.getDrawable(R.drawable.ic_reset, requireActivity()) : null));
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.native_lib), null, sPackageUtils.getNativeLibDir(mPackageName, requireActivity()), null,
                null, null));
        mPackageInfoItems.add(new PackageInfoItems(getString(R.string.date_installation), null, getString(R.string.date_installed, sPackageUtils.getInstalledDate(
                mPackageName, requireActivity())) + "\n" + getString(R.string.date_updated, sPackageUtils.getUpdatedDate(mPackageName,
                requireActivity())), null, null, null));
        try {
            mPackageInfoItems.add(new PackageInfoItems(getString(R.string.certificate), null, APKParser.getCertificateDetails(sPackageUtils.getSourceDir(
                    mPackageName, requireActivity()), requireActivity()).trim(), null, null, null));
        } catch (Exception ignored) {
        }
        return mPackageInfoItems;
    }

    private List<PackageOptionsItems> getPackageOptionsData() {
        List<PackageOptionsItems> mPackageOptionsItems = new ArrayList<>();
        if (mLaunchIntent && sPackageUtils.isEnabled(mPackageName, requireActivity())) {
            mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_open, requireActivity()), getString(R.string.open), 0));
        }
        mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_explore, requireActivity()), getString(R.string.explore), 1));
        if (mRootOrShizuku) {
            mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_disable, requireActivity()), getString(sPackageUtils.isEnabled(
                    mPackageName, requireActivity()) ? R.string.disable : R.string.enable), 2));
        }
        mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_delete, requireActivity()), getString(R.string.uninstall), 3));
        mPackageOptionsItems.add(new PackageOptionsItems(sCommonUtils.getDrawable(R.drawable.ic_doubledots, requireActivity()), getString(R.string.app_info), 4));
        return mPackageOptionsItems;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    sCommonUtils.toast(getString(R.string.permission_granted_write_storage), requireActivity()).show();
                } else {
                    sCommonUtils.toast(getString(R.string.permission_denied_write_storage), requireActivity()).show();
                }
            }
    );

}