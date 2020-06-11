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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.Menu;

import androidx.core.app.ActivityCompat;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.PackageTasksActivity;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.ViewUtils;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;
import com.smartpack.packagemanager.views.recyclerview.DescriptionView;
import com.smartpack.packagemanager.views.recyclerview.RecyclerViewItem;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class PackageTasksFragment extends RecyclerViewFragment {

    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;

    private Dialog mOptionsDialog;

    private String mPath;

    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(new DescriptionFragment());

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
            Utils.showSnackbar(getRootView(), getString(R.string.no_root));
            return;
        }

        if (Utils.isStorageWritePermissionDenied(requireActivity())) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.showSnackbar(getRootView(), getString(R.string.permission_denied_write_storage));
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

    void reload() {
        if (mLoader == null) {
            getHandler().postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<RecyclerViewItem>>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgress();
                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                        }

                        @Override
                        protected List<RecyclerViewItem> doInBackground(Void... voids) {
                            List<RecyclerViewItem> items = new ArrayList<>();
                            loadInTo(items);
                            return items;
                        }

                        @Override
                        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);

                            if (isAdded()) {
                                clearItems();
                                for (RecyclerViewItem item : recyclerViewItems) {
                                    addItem(item);
                                }

                                hideProgress();
                                mLoader = null;
                            }
                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    private void loadInTo(List<RecyclerViewItem> items) {
        final PackageManager pm = requireActivity().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, getActivity())) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));
        }
        for (ApplicationInfo packageInfo : packages) {
            if ((PackageTasks.mAppName != null && (!packageInfo.packageName.contains(PackageTasks.mAppName.toLowerCase())))) {
                continue;
            }
            boolean mAppType;
            if (Utils.getBoolean("google_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.google.android.");
            } else if (Utils.getBoolean("samsung_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.samsung.")
                        || packageInfo.packageName.startsWith("com.sec.android.");
            } else if (Utils.getBoolean("asus_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.asus.");
            } else if (Utils.getBoolean("moto_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.motorola.");
            } else if (Utils.getBoolean("oneplus_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.oneplus.");
            } else if (Utils.getBoolean("huawei_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.huawei.") || packageInfo.packageName.startsWith("com.huaweioverseas.")
                        || packageInfo.packageName.startsWith("com.bitaxon.app.");
            } else if (Utils.getBoolean("sony_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.sony.")
                        || packageInfo.packageName.startsWith("jp.sony.")
                        || packageInfo.packageName.startsWith("jp.co.sony.");
            } else if (Utils.getBoolean("lg_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.lge.") || packageInfo.packageName.startsWith("com.lgeha.")
                        || packageInfo.packageName.startsWith("ru.lgerp.");
            } else if (Utils.getBoolean("mi_apps", true, getActivity())) {
                mAppType = packageInfo.packageName.startsWith("com.mi.") || packageInfo.packageName.startsWith("com.xiaomi.");
            } else if (Utils.getBoolean("system_apps", true, getActivity())
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
            if (mAppType && packageInfo.packageName.contains(".")) {
                DescriptionView apps = new DescriptionView();
                apps.setDrawable(requireActivity().getPackageManager().getApplicationIcon(packageInfo));
                apps.setTitle(pm.getApplicationLabel(packageInfo) + (PackageTasks.isEnabled(
                        packageInfo.packageName, new WeakReference<>(requireActivity())) ? "" : " (Disabled)"));
                if (PackageTasks.mAppName != null && !PackageTasks.mAppName.isEmpty()) {
                    apps.setSummary(Utils.htmlFrom(packageInfo.packageName.replace(PackageTasks.mAppName,
                            "<b><font color=\"" + ViewUtils.getThemeAccentColor(requireActivity()) +
                                    "\">" + PackageTasks.mAppName + "</font></b>")));
                } else {
                    apps.setSummary(packageInfo.packageName);
                }
                apps.setFullSpan(true);
                apps.setOnMenuListener((itemslist1, popupMenu) -> {
                    Menu menu = popupMenu.getMenu();
                    menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.open));
                    menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.app_info));
                    menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.backup));
                    menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.export));
                    menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.turn_on_off));
                    menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.playstore));
                    menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.uninstall));
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case 0:
                                if (packageInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                                    Utils.showSnackbar(getRootView(), getString(R.string.open_message));
                                } else if (!PackageTasks.isEnabled(packageInfo.packageName, new WeakReference<>(requireActivity()))) {
                                    Utils.showSnackbar(getRootView(), getString(R.string.disabled_message, pm.getApplicationLabel(packageInfo)));
                                } else {
                                    Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(packageInfo.packageName);
                                    if (launchIntent != null) {
                                        startActivity(launchIntent);
                                    } else {
                                        Utils.showSnackbar(getRootView(), getString(R.string.open_failed, pm.getApplicationLabel(packageInfo)));
                                    }
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
                                    Utils.showSnackbar(getRootView(), getString(R.string.no_root));
                                } else if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    Utils.showSnackbar(getRootView(), getString(R.string.permission_denied_write_storage));
                                } else {
                                    ViewUtils.dialogEditText(pm.getApplicationLabel(packageInfo).toString(),
                                            (dialogInterface1, i1) -> {
                                            }, new ViewUtils.OnDialogEditTextListener() {
                                                @SuppressLint("StaticFieldLeak")
                                                @Override
                                                public void onClick(String text) {
                                                    if (text.isEmpty()) {
                                                        Utils.showSnackbar(getRootView(), getString(R.string.name_empty));
                                                        return;
                                                    }
                                                    if (!text.endsWith(".tar.gz")) {
                                                        text += ".tar.gz";
                                                    }
                                                    if (text.contains(" ")) {
                                                        text = text.replaceAll(" ", "_");
                                                    }
                                                    if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager" + "/" + text)) {
                                                        Utils.showSnackbar(getRootView(), getString(R.string.already_exists, text));
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
                                }
                                break;
                            case 3:
                                if (RootUtils.rootAccessDenied()) {
                                    Utils.showSnackbar(getRootView(), getString(R.string.no_root));
                                } else if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                                    ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    Utils.showSnackbar(getRootView(), getString(R.string.permission_denied_write_storage));
                                } else {
                                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                    for (final String splitApps : PackageTasks.splitApks(packageInfo.sourceDir.replace("base.apk", ""))) {
                                        if (splitApps.contains("split_")) {
                                            if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager/" + packageInfo.packageName)) {
                                                Utils.showSnackbar(getRootView(), getString(R.string.already_exists, packageInfo.packageName));
                                            } else {
                                                PackageTasks.exportingBundleTask(packageInfo.sourceDir.replace("base.apk", ""), packageInfo.packageName,
                                                        requireActivity().getPackageManager().getApplicationIcon(packageInfo), new WeakReference<>(getActivity()));
                                                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                            }
                                        }
                                    }
                                    PackageTasks.exportingTask(packageInfo.sourceDir, packageInfo.packageName,
                                            requireActivity().getPackageManager().getApplicationIcon(packageInfo), new WeakReference<>(getActivity()));
                                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                }
                                break;
                            case 4:
                                if (RootUtils.rootAccessDenied()) {
                                    Utils.showSnackbar(getRootView(), getString(R.string.no_root));
                                } else {
                                    new Dialog(requireActivity())
                                            .setIcon(requireActivity().getPackageManager().getApplicationIcon(packageInfo))
                                            .setTitle(pm.getApplicationLabel(packageInfo))
                                            .setMessage(pm.getApplicationLabel(packageInfo) + " " + getString(R.string.disable_message,
                                                    PackageTasks.isEnabled(packageInfo.packageName, new WeakReference<>(requireActivity())) ?
                                                            getString(R.string.disabled) : getString(R.string.enabled)))
                                            .setCancelable(false)
                                            .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                                            })
                                            .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                                                PackageTasks.disableApp(packageInfo.packageName, pm.getApplicationLabel(packageInfo).toString(), new WeakReference<>(getActivity()));
                                                reload();
                                            })
                                            .show();
                                }
                                break;
                            case 5:
                                Intent ps = new Intent(Intent.ACTION_VIEW);
                                ps.setData(Uri.parse(
                                        "https://play.google.com/store/apps/details?id=" + packageInfo.packageName));
                                startActivity(ps);
                                break;
                            case 6:
                                if (packageInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                                    Utils.showSnackbar(getRootView(), getString(R.string.uninstall_nope));
                                } else if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ||
                                        (packageInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                                    Intent remove = new Intent(Intent.ACTION_DELETE);
                                    remove.setData(Uri.parse("package:" + packageInfo.packageName));
                                    startActivity(remove);
                                } else {
                                    if (RootUtils.rootAccessDenied()) {
                                        Utils.showSnackbar(getRootView(), getString(R.string.no_root));
                                    } else {
                                        new Dialog(requireActivity())
                                                .setIcon(requireActivity().getPackageManager().getApplicationIcon(packageInfo))
                                                .setTitle(getString(R.string.uninstall_title, pm.getApplicationLabel(packageInfo)))
                                                .setMessage(getString(R.string.uninstall_warning))
                                                .setCancelable(false)
                                                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                                                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                                    PackageTasks.removeSystemApp(packageInfo.packageName, pm.getApplicationLabel(packageInfo).toString(), new WeakReference<>(getActivity()));
                                                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                    reload();
                                                })
                                                .show();
                                    }
                                }
                                break;
                        }
                        return false;
                    });
                });
                apps.setChecked(PackageTasks.mBatchApps.toString().contains(packageInfo.packageName));
                apps.setOnCheckBoxListener((descriptionView, isChecked) -> {
                    PackageTasks.batchOption(packageInfo.packageName);
                });
                items.add(apps);
            }
        }
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
                    Utils.showSnackbar(getRootView(), getString(R.string.wrong_extension, ".tar.gz"));
                    return;
                }
                Dialog restoreApp = new Dialog(requireActivity());
                restoreApp.setIcon(R.mipmap.ic_launcher);
                restoreApp.setTitle(getString(R.string.restore_message, fileName));
                restoreApp.setMessage(getString(R.string.restore_summary));
                restoreApp.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                restoreApp.setPositiveButton(getString(R.string.restore), (dialogInterface, i) -> {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    PackageTasks.restoreApp(mPath, new WeakReference<>(getActivity()));
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                });
                restoreApp.show();
            } else if (requestCode == 1) {
                if (!mPath.endsWith(".apk")) {
                    Utils.showSnackbar(getRootView(), getString(R.string.wrong_extension, ".apk"));
                    return;
                }
                Dialog installApp = new Dialog(requireActivity());
                installApp.setIcon(R.mipmap.ic_launcher);
                installApp.setTitle(getString(R.string.sure_question));
                installApp.setMessage(getString(R.string.bundle_install, mPath.replace(fileName, "")));
                installApp.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                installApp.setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    installSplitAPKs(mPath.replace(fileName, ""));
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                });
                installApp.show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void installSplitAPKs(String dir){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                PackageTasks.mRunning = true;
                if (PackageTasks.mOutput == null) {
                    PackageTasks.mOutput = new StringBuilder();
                } else {
                    PackageTasks.mOutput.setLength(0);
                }
                PackageTasks.mOutput.append("** ").append(getString(R.string.install_bundle_initialized)).append("...\n\n");
                Intent installIntent = new Intent(getActivity(), PackageTasksActivity.class);
                installIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.installing_bundle));
                installIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.installing_bundle_finished));
                startActivity(installIntent);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                PackageTasks.installSplitAPKs(dir, new WeakReference<>(requireActivity()));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                PackageTasks.mRunning = false;
            }
        }.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        PackageTasks.mAppName = null;
        PackageTasks.mBatchApps.setLength(0);
    }

}