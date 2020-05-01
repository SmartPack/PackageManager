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
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.MainActivity;
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
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void loadInTo(List<RecyclerViewItem> items) {
        // TODO: 01/05/20 hardcode it outside recyclerview
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
                        } else if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                            Utils.toast(R.string.permission_denied_write_storage, getActivity());
                        } else if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog backup = new Dialog(requireActivity());
                            backup.setIcon(R.mipmap.ic_launcher);
                            backup.setTitle(R.string.sure_question);
                            backup.setMessage(getString(R.string.batch_list_backup) + "\n" + PackageTasks.mBatchApps.toString().replaceAll("\\s+","\n - "));
                            backup.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            backup.setPositiveButton(getString(R.string.backup), (dialogInterface, i) -> {
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
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_processing_initialized)).append("...\n\n");
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_list_summary)).append(PackageTasks
                                                .mBatchApps.toString().replaceAll("\\s+", "\n - ")).append("\n\n");
                                        Intent backupIntent = new Intent(getActivity(), PackageTasksActivity.class);
                                        backupIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.batch_processing));
                                        backupIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.batch_processing_finished));
                                        startActivity(backupIntent);
                                    }
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ","").replaceAll("\\s+"," ").split(" ");
                                        for(String packageID : batchApps) {
                                            if (packageID.contains(".")) {
                                                PackageTasks.mOutput.append("** ").append(getString(R.string.backing_summary, packageID));
                                                PackageTasks.backupApp(packageID, packageID + "_batch.tar.gz");
                                                PackageTasks.mOutput.append(": ").append(getString(R.string.done)).append(" *\n\n");
                                            }
                                        }
                                        reload();
                                        return null;
                                    }
                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.everything_done)).append(" ").append(getString(
                                                R.string.batch_backup_finished, PackageTasks.PACKAGES)).append(" *");
                                        PackageTasks.mRunning = false;
                                    }
                                }.execute();
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
                            turnoff.setIcon(R.mipmap.ic_launcher);
                            turnoff.setTitle(R.string.sure_question);
                            turnoff.setMessage(getString(R.string.batch_list_disable) + "\n" + PackageTasks.mBatchApps.toString().replaceAll("\\s+","\n - "));
                            turnoff.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            turnoff.setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) -> {
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
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_processing_initialized)).append("...\n\n");
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_list_summary)).append(PackageTasks
                                                .mBatchApps.toString().replaceAll("\\s+", "\n - ")).append("\n\n");
                                        Intent turnOffIntent = new Intent(getActivity(), PackageTasksActivity.class);
                                        turnOffIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.batch_processing));
                                        turnOffIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.batch_processing_finished));
                                        startActivity(turnOffIntent);
                                    }
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ","")
                                                .replaceAll("\\s+"," ").split(" ");
                                        for(String packageID : batchApps) {
                                            if (packageID.contains(".")) {
                                                PackageTasks.mOutput.append(PackageTasks.isEnabled(packageID, new WeakReference<>(requireActivity())) ? "** " +
                                                        getString(R.string.disabling, packageID) : "** " + getString(R.string.enabling, packageID));
                                                if (PackageTasks.isEnabled(packageID, new WeakReference<>(requireActivity()))) {
                                                    RootUtils.runCommand("pm disable " + packageID);
                                                } else {
                                                    RootUtils.runCommand("pm enable " + packageID);
                                                }
                                                PackageTasks.mOutput.append(": ").append(getString(R.string.done)).append(" *\n\n");
                                                Utils.sleep(1);
                                            }
                                        }
                                        reload();
                                        return null;
                                    }
                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.everything_done)).append(" *");
                                        PackageTasks.mRunning = false;
                                    }
                                }.execute();
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
                            uninstall.setIcon(R.mipmap.ic_launcher);
                            uninstall.setTitle(R.string.sure_question);
                            uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageTasks.mBatchApps.toString().replaceAll("\\s+","\n - "));
                            uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) -> {
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
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_processing_initialized)).append("...\n\n");
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_list_summary)).append(PackageTasks
                                                .mBatchApps.toString().replaceAll("\\s+", "\n - ")).append("\n\n");
                                        Intent removeIntent = new Intent(getActivity(), PackageTasksActivity.class);
                                        removeIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.batch_processing));
                                        removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.batch_processing_finished));
                                        startActivity(removeIntent);
                                    }
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ","")
                                                .replaceAll("\\s+"," ").split(" ");
                                        for(String packageID : batchApps) {
                                            if (packageID.contains(".") && Utils.isPackageInstalled(packageID, requireActivity())) {
                                                PackageTasks.mOutput.append("** ").append(getString(R.string.uninstall_summary, packageID));
                                                RootUtils.runCommand("pm uninstall --user 0 " + packageID);
                                                PackageTasks.mOutput.append(Utils.isPackageInstalled(packageID, requireActivity()) ? ": " +
                                                        getString(R.string.failed) + " *\n\n" : ": " + getString(R.string.done) + " *\n\n");
                                                Utils.sleep(1);
                                            }
                                        }
                                        reload();
                                        return null;
                                    }
                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.everything_done)).append(" *");
                                        PackageTasks.mRunning = false;
                                    }
                                }.execute();
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

        // TODO: 01/05/20 hardcode it outside recyclerview
        DescriptionView options = new DescriptionView();
        options.setTitle(getString(R.string.app_settings));
        options.setMenuIcon(getResources().getDrawable(R.drawable.ic_settings));
        options.setOnMenuListener((optionsMenu, popupMenu) -> {
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.system)).setCheckable(true)
                    .setChecked(Utils.getBoolean("system_apps", true, getActivity()));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.user)).setCheckable(true)
                    .setChecked(Utils.getBoolean("user_apps", true, getActivity()));
            SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));
            sort.add(Menu.NONE, 3, Menu.NONE, getString(R.string.name)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_name", true, getActivity()));
            sort.add(Menu.NONE, 4, Menu.NONE, getString(R.string.package_id)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_id", false, getActivity()));
            String lang;
            if (Utils.getBoolean("use_english", false, getActivity())) {
                lang = "en_US";
            } else if (Utils.getBoolean("use_korean", false, getActivity())) {
                lang = "ko";
            } else if (Utils.getBoolean("use_am", false, getActivity())) {
                lang = "am";
            } else if (Utils.getBoolean("use_el", false, getActivity())) {
                lang = "el";
            } else if (Utils.getBoolean("use_ml", false, getActivity())) {
                lang = "ml";
            } else if (Utils.getBoolean("use_pt", false, getActivity())) {
                lang = "pt";
            } else {
                lang = java.util.Locale.getDefault().getLanguage();
            }
            SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, lang));
            language.add(Menu.NONE, 12, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                    .setChecked(Utils.languageDefault(getActivity()));
            language.add(Menu.NONE, 13, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_english", false, getActivity()));
            language.add(Menu.NONE, 14, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_korean", false, getActivity()));
            language.add(Menu.NONE, 15, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_am", false, getActivity()));
            language.add(Menu.NONE, 16, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_el", false, getActivity()));
            language.add(Menu.NONE, 17, Menu.NONE, getString(R.string.language_ml)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_ml", false, getActivity()));
            language.add(Menu.NONE, 18, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_pt", false, getActivity()));
            if (!Utils.isNotDonated(requireActivity())) {
                menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true)
                        .setChecked(Utils.getBoolean("allow_ads", true, getActivity()));
            }
            menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true)
                    .setChecked(Utils.getBoolean("dark_theme", true, getActivity()));
            SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
            about.add(Menu.NONE, 7, Menu.NONE, getString(R.string.support));
            about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.more_apps));
            about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.report_issue));
            about.add(Menu.NONE, 10, Menu.NONE, getString(R.string.source_code));
            about.add(Menu.NONE, 11, Menu.NONE, getString(R.string.about));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        if (Utils.getBoolean("system_apps", true, getActivity())) {
                            Utils.saveBoolean("system_apps", false, getActivity());
                        } else {
                            Utils.saveBoolean("system_apps", true, getActivity());
                        }
                        reload();
                        break;
                    case 2:
                        if (Utils.getBoolean("user_apps", true, getActivity())) {
                            Utils.saveBoolean("user_apps", false, getActivity());
                        } else {
                            Utils.saveBoolean("user_apps", true, getActivity());
                        }
                        reload();
                        break;
                    case 3:
                        if (Utils.getBoolean("sort_name", true, getActivity())) {
                            Utils.saveBoolean("sort_name", false, getActivity());
                            Utils.saveBoolean("sort_id", true, getActivity());
                        } else {
                            Utils.saveBoolean("sort_name", true, getActivity());
                            Utils.saveBoolean("sort_id", false, getActivity());
                        }
                        reload();
                        break;
                    case 4:
                        if (Utils.getBoolean("sort_id", false, getActivity())) {
                            Utils.saveBoolean("sort_id", false, getActivity());
                            Utils.saveBoolean("sort_name", true, getActivity());
                        } else {
                            Utils.saveBoolean("sort_id", true, getActivity());
                            Utils.saveBoolean("sort_name", false, getActivity());
                        }
                        reload();
                        break;
                    case 5:
                        if (Utils.getBoolean("allow_ads", true, getActivity())) {
                            Utils.saveBoolean("allow_ads", false, getActivity());
                        } else {
                            Utils.saveBoolean("allow_ads", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 6:
                        if (Utils.getBoolean("dark_theme", true, getActivity())) {
                            Utils.saveBoolean("dark_theme", false, getActivity());
                        } else {
                            Utils.saveBoolean("dark_theme", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 7:
                        Utils.launchUrl("https://t.me/smartpack_kmanager", getActivity());
                        break;
                    case 8:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(
                                "https://play.google.com/store/apps/developer?id=sunilpaulmathew"));
                        startActivity(intent);
                        break;
                    case 9:
                        Utils.launchUrl("https://github.com/SmartPack/PackageManager/issues/new", getActivity());
                        break;
                    case 10:
                        Utils.launchUrl("https://github.com/SmartPack/PackageManager/", getActivity());
                        break;
                    case 11:
                        aboutDialogue();
                        break;
                    case 12:
                        if (!Utils.languageDefault(getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 13:
                        if (!Utils.getBoolean("use_english", false, getActivity())) {
                            Utils.saveBoolean("use_english", true, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 14:
                        if (!Utils.getBoolean("use_korean", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", true, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 15:
                        if (!Utils.getBoolean("use_am", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", true, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 16:
                        if (!Utils.getBoolean("use_el", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", true, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 17:
                        if (!Utils.getBoolean("use_ml", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", true, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 18:
                        if (!Utils.getBoolean("use_pt", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", true, getActivity());
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
        if (Utils.getBoolean("sort_name", true, getActivity())) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));
        }
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
            if (mAppType && packageInfo.packageName.contains(".")) {
                DescriptionView apps = new DescriptionView();
                apps.setDrawable(requireActivity().getPackageManager().getApplicationIcon(packageInfo));
                apps.setTitle(pm.getApplicationLabel(packageInfo) + (PackageTasks.isEnabled(
                        packageInfo.packageName, new WeakReference<>(requireActivity())) ? "" : " (Disabled)"));
                if (mAppName != null && !mAppName.isEmpty()) {
                    apps.setSummary(Utils.htmlFrom(packageInfo.packageName.replace(mAppName,
                            "<b><font color=\"" + ViewUtils.getThemeAccentColor(requireActivity()) + "\">" + mAppName + "</font></b>")));
                } else {
                    apps.setSummary(packageInfo.packageName);
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
                                        if (!PackageTasks.isEnabled(packageInfo.packageName, new WeakReference<>(requireActivity()))) {
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
                                                        requireActivity().getPackageManager().getApplicationIcon(packageInfo), new WeakReference<>(getActivity()));
                                                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                return;
                                            }
                                        }
                                        PackageTasks.exportingTask(packageInfo.sourceDir, packageInfo.packageName,
                                                requireActivity().getPackageManager().getApplicationIcon(packageInfo), new WeakReference<>(getActivity()));
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
                                                        PackageTasks.isEnabled(packageInfo.packageName, new WeakReference<>(requireActivity())) ?
                                                                getString(R.string.disabled) : getString(R.string.enabled)))
                                                .setCancelable(false)
                                                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                                })
                                                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                                                    PackageTasks.disableApp(packageInfo.packageName, pm.getApplicationLabel(packageInfo).toString(), new WeakReference<>(getActivity()));
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
                                                        PackageTasks.removeSystemApp(packageInfo.packageName, pm.getApplicationLabel(packageInfo).toString(), new WeakReference<>(getActivity()));
                                                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                                        reload();
                                                    })
                                                    .show();
                                        }
                                        break;
                                }
                            }
                        }).setOnDismissListener(dialogInterface -> mOptionsDialog = null);
                        mOptionsDialog.show();
                    }
                });
                apps.setChecked(PackageTasks.mBatchApps.toString().contains(packageInfo.packageName));
                apps.setOnCheckBoxListener((descriptionView, isChecked) -> {
                    PackageTasks.batchOption(packageInfo.packageName);
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

    private void aboutDialogue() {
        new Dialog(requireActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name) + "\nv" + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.about_summary))
                .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .show();
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
                    Utils.toast(getString(R.string.wrong_extension, ".apk"), getActivity());
                    return;
                }
                Utils.getInstance().showInterstitialAd(getActivity());
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
        mAppName = null;
        PackageTasks.mBatchApps.setLength(0);
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

}