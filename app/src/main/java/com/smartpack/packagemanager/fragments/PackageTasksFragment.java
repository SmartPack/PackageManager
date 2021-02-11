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
import android.content.pm.ActivityInfo;
import android.database.Cursor;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.AboutActivity;
import com.smartpack.packagemanager.activities.SettingsActivity;
import com.smartpack.packagemanager.adapters.RecycleViewAdapter;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class PackageTasksFragment extends Fragment {

    private AppCompatImageButton mBatch;
    private AppCompatImageButton mSettings;
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
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.turn_on_off));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.uninstall));
        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.reset));
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.batch_list_clear));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        new MaterialAlertDialogBuilder(activity)
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
                case 1:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        MaterialAlertDialogBuilder uninstall = new MaterialAlertDialogBuilder(activity);
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
                case 2:
                    if (PackageTasks.getBatchList().isEmpty() || !PackageTasks.getBatchList().contains(".")) {
                        Utils.snackbar(mRecyclerView, getString(R.string.batch_list_empty));
                    } else {
                        MaterialAlertDialogBuilder reset = new MaterialAlertDialogBuilder(activity);
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
                case 3:
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
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.system)).setCheckable(true)
                .setChecked(Utils.getBoolean("system_apps", true, activity));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.user)).setCheckable(true)
                .setChecked(Utils.getBoolean("user_apps", true, activity));
        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.settings));
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.about));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    if (Utils.getBoolean("system_apps", true, activity)) {
                        Utils.saveBoolean("system_apps", false, activity);
                    } else {
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("system_apps", true, activity);
                    }
                    reload(activity);
                    break;
                case 1:
                    if (Utils.getBoolean("user_apps", true, activity)) {
                        Utils.saveBoolean("user_apps", false, activity);
                    } else {
                        Utils.resetDefault(activity);
                        Utils.saveBoolean("user_apps", true, activity);
                    }
                    reload(activity);
                    break;
                case 2:
                    Intent settingsPage = new Intent(activity, SettingsActivity.class);
                    startActivity(settingsPage);
                    break;
                case 3:
                    Intent aboutView = new Intent(activity, AboutActivity.class);
                    startActivity(aboutView);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void fabMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mFAB);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.install_bundle));
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                Intent install = new Intent(Intent.ACTION_GET_CONTENT);
                install.setType("*/*");
                startActivityForResult(install, 0);
            }
            return false;
        });
        popupMenu.show();
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
                    mPath = Environment.getExternalStorageDirectory().toString() + "/Download/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mPath = Utils.getPath(file);
            }
            if (requestCode == 0) {
                if (mPath.endsWith(".apk") || mPath.endsWith(".apks") || mPath.endsWith(".xapk")) {
                    MaterialAlertDialogBuilder installApp = new MaterialAlertDialogBuilder(requireActivity());
                    if (mPath.endsWith(".apks") || mPath.endsWith(".xapk")) {
                        installApp.setMessage(getString(R.string.bundle_install_apks, new File(mPath).getName()));
                    } else {
                        installApp.setIcon(R.mipmap.ic_launcher);
                        installApp.setTitle(getString(R.string.sure_question));
                        installApp.setMessage(getString(R.string.bundle_install, Objects.requireNonNull(new File(mPath)
                                .getParentFile()).toString()));
                    }
                    installApp.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    installApp.setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                        if (mPath.endsWith(".apk")) {
                            PackageTasks.installSplitAPKs(Objects.requireNonNull(new File(mPath).getParentFile())
                                    .toString(), requireActivity());
                        } else {
                            PackageTasks.installSplitAPKs(mPath, requireActivity());
                        }
                        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    });
                    installApp.show();
                } else {
                    Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apk/.apks/.xapk"));
                }
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