/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.ViewUtils;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;
import com.smartpack.packagemanager.views.recyclerview.DescriptionView;
import com.smartpack.packagemanager.views.recyclerview.RecyclerViewItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class PackageTasksFragment extends RecyclerViewFragment {

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private boolean mWelcomeDialog = true;

    private Dialog mOptionsDialog;

    private String mAppName;
    private String mPath;

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(new SearchFragment());

        if (mOptionsDialog != null) {
            mOptionsDialog.show();
        }
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount() + 1;
    }

    @Override
    protected Drawable getBottomFabDrawable() {
        return getResources().getDrawable(R.drawable.ic_apps);
    }

    @Override
    protected boolean showBottomFab() {
        return true;
    }

    @Override
    protected void onBottomFabClick() {
        super.onBottomFabClick();

        if (RootUtils.rootAccessDenied()) {
            Utils.toast(R.string.no_root, getActivity());
            return;
        }

        if (Utils.isStorageWritePermissionDenied(requireActivity())) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            return;
        }

        mOptionsDialog = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                R.array.fab_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Intent restore = new Intent(Intent.ACTION_GET_CONTENT);
                            restore.setType("*/*");
                            startActivityForResult(restore, 0);
                            break;
                        case 1:
                            Intent install = new Intent(Intent.ACTION_GET_CONTENT);
                            install.setType("*/*");
                            startActivityForResult(install, 1);
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> mOptionsDialog = null);
        mOptionsDialog.show();
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        reload();
    }

    private void reload() {
        if (mLoader == null) {
            getHandler().postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    clearItems();
                    mLoader = new AsyncTask<Void, Void, List<RecyclerViewItem>>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgress();
                        }

                        @Override
                        protected List<RecyclerViewItem> doInBackground(Void... voids) {
                            List<RecyclerViewItem> items = new ArrayList<>();
                            load(items);
                            return items;
                        }

                        @Override
                        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            for (RecyclerViewItem item : recyclerViewItems) {
                                addItem(item);
                            }
                            hideProgress();
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void load(List<RecyclerViewItem> items) {
        DescriptionView batch = new DescriptionView();
        batch.setTitle(getString(R.string.batch_options));
        batch.setMenuIcon(getResources().getDrawable(R.drawable.ic_queue));
        batch.setOnMenuListener((script, popupMenu) -> {
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.backup));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.turn_on_off));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.uninstall));
            menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.batch_list_clear));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        if (RootUtils.rootAccessDenied()) {
                            Utils.toast(R.string.no_root, getActivity());
                        } else if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog backup = new Dialog(requireActivity());
                            backup.setMessage(getString(R.string.batch_list_backup) + "\n" + PackageTasks.mBatchApps.toString().replaceAll(
                                    " ", "\n"));
                            backup.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            backup.setPositiveButton(getString(R.string.backup), (dialogInterface, i) -> {
                                String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ","").split(" ");
                                for(String packageID : batchApps) {
                                    new AsyncTask<Void, Void, Void>() {
                                        private ProgressDialog mProgressDialog;
                                        @Override
                                        protected void onPreExecute() {
                                            super.onPreExecute();
                                            mProgressDialog = new ProgressDialog(getActivity());
                                            mProgressDialog.setMessage(getString(R.string.backing_up, PackageTasks.mBatchApps.toString().replaceFirst(" ",
                                                    "").replace(" ", ", ")) + "...");
                                            mProgressDialog.setCancelable(false);
                                            mProgressDialog.show();
                                        }
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                            if (packageID.contains(".")) {
                                                PackageTasks.backupApp(packageID, packageID + "_batch.tar.gz");
                                            }
                                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                            return null;
                                        }
                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            super.onPostExecute(aVoid);
                                            try {
                                                mProgressDialog.dismiss();
                                            } catch (IllegalArgumentException ignored) {
                                            }
                                        }
                                    }.execute();
                                }
                                reload();
                            });
                            backup.show();
                        }
                        break;
                    case 1:
                        if (RootUtils.rootAccessDenied()) {
                            Utils.toast(R.string.no_root, getActivity());
                        } else if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog turnoff = new Dialog(requireActivity());
                            turnoff.setMessage(getString(R.string.batch_list_disable) + "\n" + PackageTasks.mBatchApps.toString().replaceAll(
                                    " ", "\n"));
                            turnoff.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            turnoff.setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) -> {
                                String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ","").split(" ");
                                for(String packageID : batchApps) {
                                    if (packageID.contains(".")) {
                                        PackageTasks.disableApp(packageID, PackageTasks.mBatchApps.toString().replaceFirst(" ",
                                                "").replace(" ", ", "), getActivity());
                                    }
                                }
                                reload();
                            });
                            turnoff.show();
                        }
                        break;
                    case 2:
                        if (RootUtils.rootAccessDenied()) {
                            Utils.toast(R.string.no_root, getActivity());
                        } else if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog uninstall = new Dialog(requireActivity());
                            uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageTasks.mBatchApps.toString().replaceAll(
                                    " ", "\n"));
                            uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) -> {
                                String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ","").split(" ");
                                for(String packageID : batchApps) {
                                    if (packageID.contains(".")) {
                                        PackageTasks.removeSystemApp(packageID, PackageTasks.mBatchApps.toString().replaceFirst(" ",
                                                "").replace(" ", ", "), getActivity());
                                    }
                                }
                                reload();
                            });
                            uninstall.show();
                        }
                        break;
                    case 3:
                        if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            PackageTasks.mBatchApps.setLength(0);
                            reload();
                        }
                        break;
                }
                return false;
            });
        });

        items.add(batch);

        DescriptionView options = new DescriptionView();
        options.setTitle(getString(R.string.app_settings));
        options.setMenuIcon(getResources().getDrawable(R.drawable.ic_settings));
        options.setOnMenuListener((optionsMenu, popupMenu) -> {
            Menu menu = popupMenu.getMenu();
            MenuItem system = menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.system)).setCheckable(true);
            system.setChecked(Utils.getBoolean("system_apps", true, getActivity()));
            MenuItem user = menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.user)).setCheckable(true);
            user.setChecked(Utils.getBoolean("user_apps", true, getActivity()));
            if (!Utils.isNotDonated(requireActivity())) {
                MenuItem allowAds = menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true);
                allowAds.setChecked(Utils.getBoolean("allow_ads", true, getActivity()));
            }
            MenuItem darkTheme = menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true);
            darkTheme.setChecked(Utils.getBoolean("dark_theme", true, getActivity()));
            String lang;
            if (Utils.getBoolean("use_english", false, getActivity())) {
                lang = "en_US";
            } else if (Utils.getBoolean("use_korean", false, getActivity())) {
                lang = "ko";
            } else {
                lang = java.util.Locale.getDefault().getLanguage();
            }
            SubMenu subMenu = menu.addSubMenu(Menu.NONE, 4, Menu.NONE, getString(R.string.language, lang));
            subMenu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.language_default));
            subMenu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.language_en));
            subMenu.add(Menu.NONE, 7, Menu.NONE, getString(R.string.language_ko));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        if (Utils.getBoolean("system_apps", true, getActivity())) {
                            Utils.saveBoolean("system_apps", false, getActivity());
                        } else {
                            Utils.saveBoolean("system_apps", true, getActivity());
                        }
                        reload();
                        break;
                    case 1:
                        if (Utils.getBoolean("user_apps", true, getActivity())) {
                            Utils.saveBoolean("user_apps", false, getActivity());
                        } else {
                            Utils.saveBoolean("user_apps", true, getActivity());
                        }
                        reload();
                        break;
                    case 2:
                        if (Utils.getBoolean("allow_ads", true, getActivity())) {
                            Utils.saveBoolean("allow_ads", false, getActivity());
                        } else {
                            Utils.saveBoolean("allow_ads", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 3:
                        if (Utils.getBoolean("dark_theme", true, getActivity())) {
                            Utils.saveBoolean("dark_theme", false, getActivity());
                        } else {
                            Utils.saveBoolean("dark_theme", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 4:
                        break;
                    case 5:
                        if (!Utils.languageDefault(getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 6:
                        if (!Utils.getBoolean("use_english", false, getActivity())) {
                            Utils.saveBoolean("use_english", true, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 7:
                        if (!Utils.getBoolean("use_korean", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", true, getActivity());
                            restartApp();
                        }
                        break;
                }
                return false;
            });
        });
        items.add(options);
        
        final PackageManager pm = requireActivity().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if ((mAppName != null && (!packageInfo.packageName.contains(mAppName.toLowerCase())))) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                continue;
            }
            boolean mAppType;
            if (Utils.getBoolean("system_apps", true, getActivity())
                    && Utils.getBoolean("user_apps", true, getActivity())) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                        || (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else if (Utils.getBoolean("system_apps", true, getActivity())
                    && !Utils.getBoolean("user_apps", true, getActivity())) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            } else if (!Utils.getBoolean("system_apps", true, getActivity())
                    && Utils.getBoolean("user_apps", true, getActivity())) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else {
                mAppType = false;
            }
            if (mAppType) {
                DescriptionView apps = new DescriptionView();
                apps.setDrawable(requireActivity().getPackageManager().getApplicationIcon(packageInfo));
                apps.setTitle(pm.getApplicationLabel(packageInfo) + (PackageTasks.isEnabled(
                        packageInfo.packageName, requireActivity()) ? "" : " (Disabled)"));
                apps.setSummary(packageInfo.packageName);
                if (PackageTasks.mBatchApps.toString().contains(packageInfo.packageName)) {
                    apps.setMenuIcon(getResources().getDrawable(R.drawable.ic_check_box_marked));
                } else {
                    apps.setMenuIcon(getResources().getDrawable(R.drawable.ic_check_box_empty));
                }
                apps.setFullSpan(true);
                apps.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                    @Override
                    public void onClick(RecyclerViewItem item) {
                        mOptionsDialog = new Dialog(requireActivity()).setItems(getResources().getStringArray(
                                R.array.app_options), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        if (packageInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                                            Utils.toast(R.string.open_message, getActivity());
                                            return;
                                        }
                                        if (!PackageTasks.isEnabled(packageInfo.packageName, requireActivity())) {
                                            Utils.toast(getString(R.string.disabled_message, pm.getApplicationLabel(packageInfo)), getActivity());
                                            return;
                                        }
                                        Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(packageInfo.packageName);
                                        if (launchIntent != null) {
                                            startActivity(launchIntent);
                                        } else {
                                            Utils.toast(getString(R.string.open_failed, pm.getApplicationLabel(packageInfo)), getActivity());
                                        }
                                        break;
                                    case 1:
                                        Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        Uri uri = Uri.fromParts("package", packageInfo.packageName, null);
                                        settings.setData(uri);
                                        startActivity(settings);
                                        break;
                                    case 2:
                                        if (RootUtils.rootAccessDenied()) {
                                            Utils.toast(R.string.no_root, getActivity());
                                            return;
                                        }
                                        if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                                            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                                            Utils.toast(R.string.permission_denied_write_storage, getActivity());
                                            return;
                                        }
                                        Utils.getInstance().showInterstitialAd(getActivity());
                                        ViewUtils.dialogEditText(pm.getApplicationLabel(packageInfo).toString(),
                                                (dialogInterface1, i1) -> {
                                                }, new ViewUtils.OnDialogEditTextListener() {
                                                    @SuppressLint("StaticFieldLeak")
                                                    @Override
                                                    public void onClick(String text) {
                                                        if (text.isEmpty()) {
                                                            Utils.toast(R.string.name_empty, getActivity());
                                                            return;
                                                        }
                                                        if (!text.endsWith(".tar.gz")) {
                                                            text += ".tar.gz";
                                                        }
                                                        if (text.contains(" ")) {
                                                            text = text.replace(" ", "_");
                                                        }
                                                        if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager" + "/" + text)) {
                                                            Utils.toast(getString(R.string.already_exists, text), getActivity());
                                                            return;
                                                        }
                                                        final String path = text;
                                                        new AsyncTask<Void, Void, Void>() {
                                                            private ProgressDialog mProgressDialog;
                                                            @Override
                                                            protected void onPreExecute() {
                                                                super.onPreExecute();
                                                                mProgressDialog = new ProgressDialog(getActivity());
                                                                mProgressDialog.setMessage(getString(R.string.backing_up, pm.getApplicationLabel(packageInfo).toString()) + "...");
                                                                mProgressDialog.setCancelable(false);
                                                                mProgressDialog.show();
                                                            }
                                                            @Override
                                                            protected Void doInBackground(Void... voids) {
                                                                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                                                PackageTasks.backupApp(packageInfo.packageName, path);
                                                                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                                return null;
                                                            }
                                                            @Override
                                                            protected void onPostExecute(Void aVoid) {
                                                                super.onPostExecute(aVoid);
                                                                try {
                                                                    mProgressDialog.dismiss();
                                                                } catch (IllegalArgumentException ignored) {
                                                                }
                                                            }
                                                        }.execute();
                                                    }
                                                }, getActivity()).setOnDismissListener(dialogInterface12 -> {
                                                }).show();
                                        break;
                                    case 3:
                                        if (RootUtils.rootAccessDenied()) {
                                            Utils.toast(R.string.no_root, getActivity());
                                            return;
                                        }
                                        if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                                            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                                            Utils.toast(R.string.permission_denied_write_storage, getActivity());
                                            return;
                                        }
                                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                        for (final String splitApps : PackageTasks.splitApks(packageInfo.sourceDir.replace("base.apk", ""))) {
                                            if (splitApps.contains("split_")) {
                                                if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager/" + packageInfo.packageName)) {
                                                    Utils.toast(getString(R.string.already_exists, packageInfo.packageName), getActivity());
                                                    return;
                                                }
                                                PackageTasks.exportingBundleTask(packageInfo.sourceDir.replace("base.apk", ""), packageInfo.packageName,
                                                        requireActivity().getPackageManager().getApplicationIcon(packageInfo), getActivity());
                                                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                return;
                                            }
                                        }
                                        PackageTasks.exportingTask(packageInfo.sourceDir, packageInfo.packageName,
                                                requireActivity().getPackageManager().getApplicationIcon(packageInfo), getActivity());
                                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                        break;
                                    case 4:
                                        if (RootUtils.rootAccessDenied()) {
                                            Utils.toast(R.string.no_root, getActivity());
                                            return;
                                        }
                                        Utils.getInstance().showInterstitialAd(getActivity());
                                        new Dialog(requireActivity())
                                                .setIcon(requireActivity().getPackageManager().getApplicationIcon(packageInfo))
                                                .setTitle(pm.getApplicationLabel(packageInfo))
                                                .setMessage(pm.getApplicationLabel(packageInfo) + " " + getString(R.string.disable_message,
                                                        PackageTasks.isEnabled(packageInfo.packageName, requireActivity()) ?
                                                                getString(R.string.disabled) : getString(R.string.enabled)))
                                                .setCancelable(false)
                                                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                                                    PackageTasks.disableApp(packageInfo.packageName, pm.getApplicationLabel(packageInfo).toString(), getActivity());
                                                    reload();
                                                })
                                                .show();
                                        break;
                                    case 5:
                                        Utils.getInstance().showInterstitialAd(getActivity());
                                        Intent ps = new Intent(Intent.ACTION_VIEW);
                                        ps.setData(Uri.parse(
                                                "https://play.google.com/store/apps/details?id=" + packageInfo.packageName));
                                        startActivity(ps);
                                        break;
                                    case 6:
                                        if (packageInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                                            Utils.toast(R.string.uninstall_nope, getActivity());
                                            return;
                                        }
                                        if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ||
                                                (packageInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {

                                            Intent remove = new Intent(Intent.ACTION_DELETE);
                                            remove.setData(Uri.parse("package:" + packageInfo.packageName));
                                            startActivity(remove);
                                        } else {
                                            if (RootUtils.rootAccessDenied()) {
                                                Utils.toast(R.string.no_root, getActivity());
                                                return;
                                            }
                                            Utils.getInstance().showInterstitialAd(getActivity());
                                            new Dialog(requireActivity())
                                                    .setIcon(requireActivity().getPackageManager().getApplicationIcon(packageInfo))
                                                    .setTitle(getString(R.string.uninstall_title, pm.getApplicationLabel(packageInfo)))
                                                    .setMessage(getString(R.string.uninstall_warning))
                                                    .setCancelable(false)
                                                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                                    })
                                                    .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                                                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                                        PackageTasks.removeSystemApp(packageInfo.packageName, pm.getApplicationLabel(packageInfo).toString(), getActivity());
                                                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                    })
                                                    .show();
                                        }
                                        reload();
                                        break;
                                }
                            }
                        }).setOnDismissListener(dialogInterface -> mOptionsDialog = null);
                        mOptionsDialog.show();
                    }
                });
                apps.setOnMenuListener((script, popupMenu) -> {
                    Menu menu = popupMenu.getMenu();
                    MenuItem batch_add = menu.add(Menu.NONE, 0, Menu.NONE, PackageTasks.mBatchApps.toString().contains(packageInfo.packageName) ?
                            getString(R.string.batch_remove) : getString(R.string.batch_add)).setCheckable(true);
                    batch_add.setChecked(PackageTasks.mBatchApps.toString().contains(packageInfo.packageName));
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 0) {
                            if (PackageTasks.mBatchApps.toString().contains(packageInfo.packageName)) {
                                int appID = PackageTasks.mBatchApps.indexOf(packageInfo.packageName);
                                PackageTasks.mBatchApps.delete(appID, appID + packageInfo.packageName.length());
                            } else {
                                PackageTasks.mBatchApps.append(" ").append(packageInfo.packageName);
                            }
                        }
                        if (PackageTasks.mBatchApps.toString().contains(packageInfo.packageName)) {
                            apps.setMenuIcon(getResources().getDrawable(R.drawable.ic_check_box_marked));
                        } else {
                            apps.setMenuIcon(getResources().getDrawable(R.drawable.ic_check_box_empty));
                        }
                        batch_add.setChecked(PackageTasks.mBatchApps.toString().contains(packageInfo.packageName));
                        return false;
                    });
                });

                items.add(apps);
            }
        }
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            assert uri != null;
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (Utils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle") Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mPath = Environment.getExternalStorageDirectory().toString() + "/Package_Manager/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mPath = Utils.getPath(file);
            }
            String fileName = new File(mPath).getName();
            if (requestCode == 0) {
                if (!mPath.endsWith(".tar.gz")) {
                    Utils.toast(getString(R.string.wrong_extension, ".tar.gz"), getActivity());
                    return;
                }
                Utils.getInstance().showInterstitialAd(getActivity());
                Dialog restoreApp = new Dialog(requireActivity());
                restoreApp.setIcon(R.mipmap.ic_launcher);
                restoreApp.setTitle(getString(R.string.restore_message, fileName));
                restoreApp.setMessage(getString(R.string.restore_summary));
                restoreApp.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                restoreApp.setPositiveButton(getString(R.string.restore), (dialogInterface, i) -> {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    PackageTasks.restoreApp(mPath, getActivity());
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                });
                restoreApp.show();
            } else if (requestCode == 1) {
                if (!mPath.endsWith(".apk")) {
                    Utils.toast(getString(R.string.wrong_extension, ".apk"), getActivity());
                    return;
                }
                Utils.getInstance().showInterstitialAd(getActivity());
                Dialog installApp = new Dialog(requireActivity());
                installApp.setIcon(R.mipmap.ic_launcher);
                installApp.setTitle(getString(R.string.sure_question));
                installApp.setMessage(getString(R.string.bundle_install, mPath.replace(fileName, "")));
                installApp.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                installApp.setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    PackageTasks.installSplitAPKs(mPath.replace(fileName, ""), getActivity());
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                });
                installApp.show();
            }
        }
    }

    public static class SearchFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Fragment fragment = getParentFragment();
            if (!(fragment instanceof PackageTasksFragment)) {
                assert fragment != null;
                fragment = fragment.getParentFragment();
            }
            final PackageTasksFragment systemAppsFragment = (PackageTasksFragment) fragment;

            View rootView = inflater.inflate(R.layout.fragment_search, container, false);

            AppCompatEditText keyEdit = rootView.findViewById(R.id.key_edittext);

            keyEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    assert systemAppsFragment != null;
                    systemAppsFragment.mAppName = s.toString();
                    systemAppsFragment.reload();
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                }
            });
            assert systemAppsFragment != null;
            if (systemAppsFragment.mAppName != null) {
                keyEdit.append(systemAppsFragment.mAppName);
            }

            return rootView;
        }
    }

    /*
     * Taken and used almost as such from https://github.com/morogoku/MTweaks-KernelAdiutorMOD/
     * Ref: https://github.com/morogoku/MTweaks-KernelAdiutorMOD/blob/dd5a4c3242d5e1697d55c4cc6412a9b76c8b8e2e/app/src/main/java/com/moro/mtweaks/fragments/kernel/BoefflaWakelockFragment.java#L133
     */
    private void WelcomeDialog() {
        View checkBoxView = View.inflate(getActivity(), R.layout.rv_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        checkBox.setText(getString(R.string.always_show));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mWelcomeDialog = isChecked;
        });

        Dialog alert = new Dialog(Objects.requireNonNull(getActivity()));
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(getText(R.string.welcome_message));
        alert.setView(checkBoxView);
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
            Utils.saveBoolean("welcomeMessage", mWelcomeDialog, getActivity());
        });

        alert.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.getBoolean("welcomeMessage", true, getActivity())) {
            WelcomeDialog();
        }
        if (PackageTasks.mBatchApps == null) {
            PackageTasks.mBatchApps = new StringBuilder();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoader != null) {
            mLoader.cancel(true);
        }
        mAppName = null;
        PackageTasks.mBatchApps.setLength(0);
    }

}