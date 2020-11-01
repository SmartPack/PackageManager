/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.packagemanager.R;

import java.io.File;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */

public class PackageTasksFragment extends Fragment {

    private AppCompatImageButton mBatch;
    private AppCompatImageButton mSettings;
    private AppCompatTextView mProgressMessage;
    private AsyncTask<Void, Void, List<String>> mLoader;
    private FloatingActionButton mFAB;
    private Handler mHandler = new Handler();
    private LinearLayout mProgressLayout;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;
    private String mPath;

    @SuppressLint("StaticFieldLeak")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packagetasks, container, false);

        mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        mProgressMessage = mRootView.findViewById(R.id.progress_message);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        loadUI(requireActivity());

        Utils.mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatch = mRootView.findViewById(R.id.batch_icon);
        mSettings = mRootView.findViewById(R.id.settings_icon);
        mFAB = mRootView.findViewById(R.id.fab);

        Utils.mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Utils.mSearchText = s.toString().toLowerCase();
                reload(requireActivity());
            }
        });

        mBatch.setOnClickListener(v -> {
            if (!Utils.rootAccess()) {
                Utils.snackbar(mRecyclerView, getString(R.string.no_root));
                return;
            }
            batchMenu(requireActivity());
        });

        mSettings.setOnClickListener(v -> settingsMenu(requireActivity()));

        mFAB.setOnClickListener(v -> {
            if (!Utils.rootAccess()) {
                Utils.snackbar(mRecyclerView, getString(R.string.no_root));
                return;
            }
            if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                Utils.snackbar(mRecyclerView, getString(R.string.permission_denied_write_storage));
                return;
            }
            fabMenu(requireActivity());
        });

        return mRootView;
    }

    private void batchMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mBatch);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.backup));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.turn_on_off));
        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.uninstall));
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.reset));
        menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.batch_list_clear));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (Utils.isStorageWritePermissionDenied(activity)) {
                        ActivityCompat.requestPermissions(activity, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        Utils.snackbar(mRecyclerView, getString(R.string.permission_denied_write_storage));
                    } else if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        new AlertDialog.Builder(activity)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.sure_question)
                                .setMessage(getString(R.string.batch_list_backup) + "\n" + PackageTasks.showBatchList())
                                .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                })
                                .setPositiveButton(getString(R.string.backup), (dialogInterface, i) -> {
                                    PackageTasks.batchBackupTask(activity);
                                })
                                .show();
                    }
                    break;
                case 1:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        new AlertDialog.Builder(activity)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.sure_question)
                                .setMessage(getString(R.string.batch_list_disable) + "\n" + PackageTasks.showBatchList())
                                .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                })
                                .setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) -> {
                                    PackageTasks.batchDisableTask(activity);
                                })
                                .show();
                    }
                    break;
                case 2:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        AlertDialog.Builder uninstall = new AlertDialog.Builder(activity);
                        uninstall.setIcon(R.mipmap.ic_launcher);
                        uninstall.setTitle(R.string.sure_question);
                        uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageTasks.showBatchList());
                        uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) -> {
                            PackageTasks.batchUninstallTask(activity);
                        });
                        uninstall.show();
                    }
                    break;
                case 3:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        AlertDialog.Builder reset = new AlertDialog.Builder(activity);
                        reset.setIcon(R.mipmap.ic_launcher);
                        reset.setTitle(R.string.sure_question);
                        reset.setMessage(getString(R.string.batch_list_reset) + "\n" + PackageTasks.showBatchList());
                        reset.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        reset.setPositiveButton(getString(R.string.reset), (dialogInterface, i) -> {
                            PackageTasks.batchResetTask(activity);
                        });
                        reset.show();
                    }
                    break;
                case 4:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        PackageTasks.mBatchList.clear();
                        reload(activity);
                    }
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void settingsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSettings);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.system)).setCheckable(true)
                .setChecked(Utils.getBoolean("system_apps", true, activity));
        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.user)).setCheckable(true)
                .setChecked(Utils.getBoolean("user_apps", true, activity));
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));
        sort.add(Menu.NONE, 3, Menu.NONE, getString(R.string.name)).setCheckable(true)
                .setChecked(Utils.getBoolean("sort_name", false, activity));
        sort.add(Menu.NONE, 4, Menu.NONE, getString(R.string.package_id)).setCheckable(true)
                .setChecked(Utils.getBoolean("sort_id", true, activity));
        SubMenu oem = sort.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.oem));
        oem.add(Menu.NONE, 29, Menu.NONE, getString(R.string.oem_asus)).setCheckable(true)
                .setChecked(Utils.getBoolean("asus_apps", false, activity));
        oem.add(Menu.NONE, 21, Menu.NONE, getString(R.string.oem_google)).setCheckable(true)
                .setChecked(Utils.getBoolean("google_apps", false, activity));
        oem.add(Menu.NONE, 22, Menu.NONE, getString(R.string.oem_samsung)).setCheckable(true)
                .setChecked(Utils.getBoolean("samsung_apps", false, activity));
        oem.add(Menu.NONE, 23, Menu.NONE, getString(R.string.oem_moto)).setCheckable(true)
                .setChecked(Utils.getBoolean("moto_apps", false, activity));
        oem.add(Menu.NONE, 24, Menu.NONE, getString(R.string.oem_oneplus)).setCheckable(true)
                .setChecked(Utils.getBoolean("oneplus_apps", false, activity));
        oem.add(Menu.NONE, 27, Menu.NONE, getString(R.string.oem_huawei)).setCheckable(true)
                .setChecked(Utils.getBoolean("huawei_apps", false, activity));
        oem.add(Menu.NONE, 25, Menu.NONE, getString(R.string.oem_sony)).setCheckable(true)
                .setChecked(Utils.getBoolean("sony_apps", false, activity));
        oem.add(Menu.NONE, 28, Menu.NONE, getString(R.string.oem_lg)).setCheckable(true)
                .setChecked(Utils.getBoolean("lg_apps", false, activity));
        oem.add(Menu.NONE, 26, Menu.NONE, getString(R.string.oem_mi)).setCheckable(true)
                .setChecked(Utils.getBoolean("mi_apps", false, activity));
        SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, Utils.getLanguage(activity)));
        language.add(Menu.NONE, 12, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                .setChecked(Utils.languageDefault(activity));
        language.add(Menu.NONE, 13, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_english", false, activity));
        language.add(Menu.NONE, 14, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_korean", false, activity));
        language.add(Menu.NONE, 15, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_am", false, activity));
        language.add(Menu.NONE, 16, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_el", false, activity));
        language.add(Menu.NONE, 17, Menu.NONE, getString(R.string.language_ml)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_ml", false, activity));
        language.add(Menu.NONE, 18, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_pt", false, activity));
        language.add(Menu.NONE, 19, Menu.NONE, getString(R.string.language_ru)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_ru", false, activity));
        language.add(Menu.NONE, 20, Menu.NONE, getString(R.string.language_uk)).setCheckable(true)
                .setChecked(Utils.getBoolean("use_uk", false, activity));
        if (!Utils.isNotDonated(activity)) {
            menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true)
                    .setChecked(Utils.getBoolean("allow_ads", true, activity));
        }
        SubMenu appTheme = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.dark_theme));
        appTheme.add(Menu.NONE, 31, Menu.NONE, getString(R.string.dark_theme_auto)).setCheckable(true)
                .setChecked(Utils.getBoolean("theme_auto", true, activity));
        appTheme.add(Menu.NONE, 6, Menu.NONE, getString(R.string.dark_theme_enable)).setCheckable(true)
                .setChecked(Utils.getBoolean("dark_theme", false, activity));
        appTheme.add(Menu.NONE, 30, Menu.NONE, getString(R.string.dark_theme_disable)).setCheckable(true)
                .setChecked(Utils.getBoolean("light_theme", false, activity));
        SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
        about.add(Menu.NONE, 10, Menu.NONE, getString(R.string.source_code));
        about.add(Menu.NONE, 7, Menu.NONE, getString(R.string.support));
        about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.more_apps));
        about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.report_issue));
        about.add(Menu.NONE, 11, Menu.NONE, getString(R.string.about));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (Utils.getBoolean("system_apps", true, activity)) {
                        Utils.saveBoolean("system_apps", false, activity);
                    } else {
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("system_apps", true, activity);
                    }
                    reload(activity);
                    break;
                case 2:
                    if (Utils.getBoolean("user_apps", true, activity)) {
                        Utils.saveBoolean("user_apps", false, activity);
                    } else {
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("user_apps", true, activity);
                    }
                    reload(activity);
                    break;
                case 3:
                    if (!Utils.getBoolean("sort_name", false, activity)) {
                        Utils.saveBoolean("sort_name", true, activity);
                        Utils.saveBoolean("sort_id", false, activity);
                        reload(activity);
                    }
                    break;
                case 4:
                    if (!Utils.getBoolean("sort_id", true, activity)) {
                        Utils.saveBoolean("sort_id", true, activity);
                        Utils.saveBoolean("sort_name", false, activity);
                        reload(activity);
                    }
                    break;
                case 5:
                    if (Utils.getBoolean("allow_ads", true, activity)) {
                        Utils.saveBoolean("allow_ads", false, activity);
                    } else {
                        Utils.saveBoolean("allow_ads", true, activity);
                    }
                    Utils.restartApp(activity);
                    break;
                case 6:
                    if (!Utils.getBoolean("dark_theme", false, activity)) {
                        Utils.saveBoolean("dark_theme", true, activity);
                        Utils.saveBoolean("light_theme", false, activity);
                        Utils.saveBoolean("theme_auto", false, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 7:
                    Utils.launchUrl("https://t.me/smartpack_kmanager", mRecyclerView, activity);
                    break;
                case 8:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/dev?id=5836199813143882901"));
                    startActivity(intent);
                    break;
                case 9:
                    Utils.launchUrl("https://github.com/SmartPack/PackageManager/issues/new", mRecyclerView, activity);
                    break;
                case 10:
                    Utils.launchUrl("https://github.com/SmartPack/PackageManager/", mRecyclerView, activity);
                    break;
                case 11:
                    Intent aboutView = new Intent(activity, AboutActivity.class);
                    startActivity(aboutView);
                    break;
                case 12:
                    if (!Utils.languageDefault(activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 13:
                    if (!Utils.getBoolean("use_english", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_english", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 14:
                    if (!Utils.getBoolean("use_korean", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_korean", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 15:
                    if (!Utils.getBoolean("use_am", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_am", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 16:
                    if (!Utils.getBoolean("use_el", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_el", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 17:
                    if (!Utils.getBoolean("use_ml", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_ml", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 18:
                    if (!Utils.getBoolean("use_pt", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_pt", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 19:
                    if (!Utils.getBoolean("use_ru", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_ru", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 20:
                    if (!Utils.getBoolean("use_uk", false, activity)) {
                        Utils.setDefaultLanguage(activity);
                        Utils.saveBoolean("use_uk", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 21:
                    if (!Utils.getBoolean("google_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("google_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 22:
                    if (!Utils.getBoolean("samsung_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("samsung_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 23:
                    if (!Utils.getBoolean("moto_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("moto_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 24:
                    if (!Utils.getBoolean("oneplus_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("oneplus_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 25:
                    if (!Utils.getBoolean("sony_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("sony_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 26:
                    if (!Utils.getBoolean("mi_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("mi_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 27:
                    if (!Utils.getBoolean("huawei_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("huawei_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 28:
                    if (!Utils.getBoolean("lg_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("lg_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 29:
                    if (!Utils.getBoolean("asus_apps", false, activity)) {
                        Utils.mSortByOEM = true;
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("asus_apps", true, activity);
                        reload(activity);
                    }
                    break;
                case 30:
                    if (!Utils.getBoolean("light_theme", false, activity)) {
                        Utils.saveBoolean("dark_theme", false, activity);
                        Utils.saveBoolean("light_theme", true, activity);
                        Utils.saveBoolean("theme_auto", false, activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 31:
                    if (!Utils.getBoolean("theme_auto", true, activity)) {
                        Utils.saveBoolean("dark_theme", false, activity);
                        Utils.saveBoolean("light_theme", false, activity);
                        Utils.saveBoolean("theme_auto", true, activity);
                        Utils.restartApp(activity);
                    }
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void fabMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mFAB);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.restore_data));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.install_bundle));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
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
            return false;
        });
        popupMenu.show();
    }

    private void showProgress(String message) {
        mProgressMessage.setText(message);
        mProgressMessage.setVisibility(View.VISIBLE);
        mProgressLayout.setBackgroundColor(Utils.isDarkTheme(requireActivity()) ? Color.BLACK : Color.WHITE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressMessage.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.GONE);
    }

    @SuppressLint("StaticFieldLeak")
    private void restore(String path, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(activity.getString(R.string.restoring, path) + "...");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(2);
                Utils.runCommand("tar -zxvf " + path + " -C /");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadUI(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewAdapter(PackageTasks.getData(activity));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private void reload(Activity activity) {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mProgressLayout.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                            if (!PackageTasks.getBatchList().isEmpty() && PackageTasks.getBatchList().contains(".")) {
                                PackageTasks.mBatchList.clear();
                            }
                            mRecyclerView.removeAllViews();
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAdapter(PackageTasks.getData(activity));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mProgressLayout.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
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
                    Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".tar.gz"));
                    return;
                }
                AlertDialog.Builder restoreApp = new AlertDialog.Builder(requireActivity());
                restoreApp.setIcon(R.mipmap.ic_launcher);
                restoreApp.setTitle(getString(R.string.restore_message, fileName));
                restoreApp.setMessage(getString(R.string.restore_summary));
                restoreApp.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                restoreApp.setPositiveButton(getString(R.string.restore), (dialogInterface, i) -> {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    restore(mPath, requireActivity());
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                });
                restoreApp.show();
            } else if (requestCode == 1) {
                if (!mPath.endsWith(".apk")) {
                    Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apk"));
                    return;
                }
                AlertDialog.Builder installApp = new AlertDialog.Builder(requireActivity());
                installApp.setIcon(R.mipmap.ic_launcher);
                installApp.setTitle(getString(R.string.sure_question));
                installApp.setMessage(getString(R.string.bundle_install, mPath.replace(fileName, "")));
                installApp.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });
                installApp.setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    PackageTasks.installSplitAPKs(mPath.replace(fileName, ""), requireActivity());
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                });
                installApp.show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.getBoolean("welcomeMessage", true, getActivity())) {
            Utils.WelcomeDialog(getActivity());
        }
        if (Utils.mReloadPage) {
            Utils.mReloadPage = false;
            reload(requireActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Utils.mSearchText != null) {
            Utils.mSearchWord.setText(null);
            Utils.mSearchText = null;
        }
    }

}