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
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.AboutActivity;
import com.smartpack.packagemanager.activities.ExportedAppsActivity;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.SettingsActivity;
import com.smartpack.packagemanager.adapters.RecycleViewAdapter;
import com.smartpack.packagemanager.utils.AsyncTasks;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.RecycleViewItem;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class PackageTasksFragment extends Fragment {

    private AppCompatEditText mSearchWord;
    private AppCompatImageButton mSettings, mSort;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private MaterialCardView mBatchOptions;
    private MaterialTextView mAppTitle;
    private LinearLayout mProgressLayout;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;
    private String mPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packagetasks, container, false);

        mAppTitle = mRootView.findViewById(R.id.app_title);
        mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        mBatchOptions = Common.initializeBatchOptionsCard(mRootView, R.id.batch_options);
        MaterialCheckBox mSelectAll = Common.initializeSelectAllCheckBox(mRootView, R.id.checkbox);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        AppCompatImageButton mSearch = mRootView.findViewById(R.id.search_icon);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mSort = mRootView.findViewById(R.id.sort_icon);
        mSettings = mRootView.findViewById(R.id.settings_icon);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        loadUI(requireActivity());

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = Utils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            Utils.saveString("appTypes", "all", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            Utils.saveString("appTypes", "system", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            Utils.saveString("appTypes", "user", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSearch.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mAppTitle.setVisibility(View.VISIBLE);
                toggleKeyboard(0);
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mAppTitle.setVisibility(View.GONE);
                toggleKeyboard(1);
            }
        });

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            toggleKeyboard(0);
            mSearchWord.clearFocus();
            return true;
        });

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.setSearchText(s.toString().toLowerCase());
                loadUI(requireActivity());
            }
        });

        mSettings.setOnClickListener(v -> settingsMenu(requireActivity()));

        mSort.setOnClickListener(v -> sortMenu(requireActivity()));

        mBatchOptions.setOnClickListener(v -> batchOptionsMenu(requireActivity()));

        mSelectAll.setChecked(Common.getBatchList().size() == PackageData.getData(requireActivity()).size());

        mSelectAll.setOnClickListener(v -> {
            if (Utils.getBoolean("select_all_firstAttempt", true, requireActivity())) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.sure_question))
                        .setMessage(getString(R.string.select_all_summary))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                            mSelectAll.setChecked(Common.getBatchList().size() == PackageData.getData(requireActivity()).size());
                        })
                        .setPositiveButton(getString(R.string.select_all), (dialog, id) -> {
                            selectAll(mSelectAll.isChecked());
                            Utils.saveBoolean("select_all_firstAttempt", false, requireActivity());
                        }).show();
            } else {
                selectAll(mSelectAll.isChecked());
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Common.getSearchText() != null) {
                    mSearchWord.setText(null);
                    Common.setSearchText(null);
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    mAppTitle.setVisibility(View.VISIBLE);
                    return;
                }
                if (!PackageData.getBatchList().isEmpty() && PackageData.getBatchList().contains(".")) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(R.string.batch_warning)
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> requireActivity().finish())
                            .show();
                } else if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    Utils.snackbar(mRootView, getString(R.string.press_back));
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    public void toggleKeyboard(int mode) {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (mode == 1) {
            if (mSearchWord.requestFocus()) {
                imm.showSoftInput(mSearchWord, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            imm.hideSoftInputFromWindow(mSearchWord.getWindowToken(), 0);
        }
    }

    private int getTabPosition(Activity activity) {
        String mStatus = Utils.getString("appTypes", "all", activity);
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void selectAll(boolean b) {
        Utils.saveBoolean("select_all", b, requireActivity());
        loadUI(requireActivity());
    }

    private void uninstallUserApp() {
        Intent remove = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + Common.getBatchList().get(0)));
        remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(remove, 1);
        Common.reloadPage(true);
    }

    private void handleUninstallEvent() {
        if (Common.getBatchList().size() > 0) {
            uninstallUserApp();
        } else {
            loadUI(requireActivity());
        }
    }

    private void sortMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSort);
        Menu menu = popupMenu.getMenu();
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));
        sort.add(Menu.NONE, 1, Menu.NONE, getString(R.string.name)).setCheckable(true)
                .setChecked(Utils.getBoolean("sort_name", false, activity));
        sort.add(Menu.NONE, 2, Menu.NONE, getString(R.string.package_id)).setCheckable(true)
                .setChecked(Utils.getBoolean("sort_id", true, activity));
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                .setChecked(Utils.getBoolean("reverse_order", false, activity));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (!Utils.getBoolean("sort_name", false, activity)) {
                        Utils.saveBoolean("sort_name", true, activity);
                        Utils.saveBoolean("sort_id", false, activity);
                        loadUI(activity);
                    }
                    break;
                case 2:
                    if (!Utils.getBoolean("sort_id", true, activity)) {
                        Utils.saveBoolean("sort_id", true, activity);
                        Utils.saveBoolean("sort_name", false, activity);
                        loadUI(activity);
                    }
                    break;
                case 3:
                    Utils.saveBoolean("reverse_order", !Utils.getBoolean("reverse_order", false, activity), activity);
                    loadUI(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void settingsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSettings);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.install_bundle));
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.exported_apps));
        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.settings));
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.about));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    Common.getAppList().clear();
                    if (Utils.getBoolean("filePicker", true, requireActivity())) {
                        Common.setPath(Environment.getExternalStorageDirectory().toString());
                        Intent filePicker = new Intent(activity, FilePickerActivity.class);
                        startActivity(filePicker);
                    } else {
                        if (Build.VERSION.SDK_INT >= 30 && Utils.isPermissionDenied()) {
                            new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(getString(R.string.important))
                                    .setMessage(getString(R.string.file_permission_request_message))
                                    .setCancelable(false)
                                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                    })
                                    .setPositiveButton(getString(R.string.grant), (dialog, id) -> Utils.requestPermission(requireActivity())).show();
                        } else if (Utils.getBoolean("firstAttempt", true, requireActivity())) {
                            new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(getString(R.string.install_bundle))
                                    .setMessage(getString(R.string.bundle_install_message))
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                                        Utils.saveBoolean("firstAttempt", false, requireActivity());
                                        initializeSplitAPKInstallation();
                                    }).show();
                        } else {
                            initializeSplitAPKInstallation();
                        }
                    }
                    break;
                case 1:
                    Intent exportedApps = new Intent(activity, ExportedAppsActivity.class);
                    startActivity(exportedApps);
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

    private void batchOptionsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mBatchOptions);
        Menu menu = popupMenu.getMenu();
        if (Utils.rootAccess()) {
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.turn_on_off));
        }
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.uninstall));
        if (Utils.rootAccess()) {
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.reset));
        }
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.export));
        menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.batch_list_clear));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.sure_question)
                            .setMessage(getString(R.string.batch_list_disable) + "\n" + PackageData.showBatchList())
                            .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) ->
                                    PackageTasks.batchDisableTask(activity))
                            .show();
                    break;
                case 1:
                    if (Utils.rootAccess()) {
                        MaterialAlertDialogBuilder uninstall = new MaterialAlertDialogBuilder(activity);
                        uninstall.setIcon(R.mipmap.ic_launcher);
                        uninstall.setTitle(R.string.sure_question);
                        uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageData.showBatchList());
                        uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) ->
                                PackageTasks.batchUninstallTask(activity));
                        uninstall.show();
                    } else {
                        uninstallUserApp();
                    }
                    break;
                case 2:
                    MaterialAlertDialogBuilder reset = new MaterialAlertDialogBuilder(activity);
                    reset.setIcon(R.mipmap.ic_launcher);
                    reset.setTitle(R.string.sure_question);
                    reset.setMessage(getString(R.string.batch_list_reset) + "\n" + PackageData.showBatchList());
                    reset.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reset.setPositiveButton(getString(R.string.reset), (dialogInterface, i) ->
                            PackageTasks.batchResetTask(activity));
                    reset.show();
                    break;
                case 3:
                    MaterialAlertDialogBuilder export = new MaterialAlertDialogBuilder(activity);
                    export.setIcon(R.mipmap.ic_launcher);
                    export.setTitle(R.string.sure_question);
                    export.setMessage(getString(R.string.batch_list_export) + "\n" + PackageData.showBatchList());
                    export.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    export.setPositiveButton(getString(R.string.export), (dialogInterface, i) ->
                            PackageTasks.batchExportTask(activity));
                    export.show();
                    break;
                case 4:
                    Common.getBatchList().clear();
                    loadUI(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void initializeSplitAPKInstallation() {
        Intent install = new Intent(Intent.ACTION_GET_CONTENT);
        install.setType("*/*");
        startActivityForResult(install, 0);
    }

    private void loadUI(Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                mBatchOptions.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                if (Utils.getBoolean("select_all", false, activity)) {
                    Common.getBatchList().clear();
                    for (RecycleViewItem mPackage : PackageData.getData(activity)) {
                        Common.getBatchList().add(mPackage.getTitle());
                    }
                } else {
                    Common.getBatchList().clear();
                }
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new RecycleViewAdapter(PackageData.getData(activity));
            }

            @Override
            public void onPostExecute() {
                if (Utils.getBoolean("select_all", false, activity)) {
                    Utils.saveBoolean("select_all", false, activity);
                    mBatchOptions.setVisibility(View.VISIBLE);
                } else {
                    mBatchOptions.setVisibility(View.GONE);
                }
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == 0) {
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
                if (mPath.endsWith(".apk") || mPath.endsWith(".apks") || mPath.endsWith(".apkm") || mPath.endsWith(".xapk")) {
                    MaterialAlertDialogBuilder installApp = new MaterialAlertDialogBuilder(requireActivity());
                    if (mPath.endsWith(".apks") || mPath.endsWith(".apkm") || mPath.endsWith(".xapk")) {
                        installApp.setMessage(getString(R.string.bundle_install_apks, new File(mPath).getName()));
                    } else {
                        installApp.setIcon(R.mipmap.ic_launcher);
                        installApp.setTitle(getString(R.string.sure_question));
                        installApp.setMessage(getString(R.string.bundle_install, Objects.requireNonNull(new File(mPath)
                                .getParentFile()).toString()));
                        installApp.setNeutralButton(getString(R.string.list), (dialogInterface, i) ->
                                new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle(R.string.split_apk_list)
                                        .setMessage(SplitAPKInstaller.listSplitAPKs(Objects.requireNonNull(new File(mPath).getParentFile()).toString()))
                                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                        })
                                        .setPositiveButton(getString(R.string.install), (dialog, id) -> {
                                            Common.getAppList().clear();
                                            if (new File(mPath).exists()) {
                                                for (File mFile : Objects.requireNonNull(new File(Objects.requireNonNull(new File(mPath).getParentFile())
                                                        .toString()).listFiles())) {
                                                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                                        Common.getAppList().add(mFile.getAbsolutePath());
                                                    }
                                                }
                                            }
                                            SplitAPKInstaller.installSplitAPKs(requireActivity());
                                        }).show());
                    }
                    installApp.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    installApp.setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                        if (mPath.endsWith(".apk")) {
                            Common.getAppList().clear();
                            if (new File(mPath).exists()) {
                                for (File mFile : Objects.requireNonNull(new File(Objects.requireNonNull(new File(mPath).getParentFile())
                                        .toString()).listFiles())) {
                                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                        Common.getAppList().add(mFile.getAbsolutePath());
                                    }
                                }
                            }
                            SplitAPKInstaller.installSplitAPKs(requireActivity());
                        } else {
                            SplitAPKInstaller.handleAppBundle(mProgressLayout, mPath, requireActivity());
                        }
                    });
                    installApp.show();
                } else {
                    Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apk/.apks/.apkm/.xapk"));
                }
            } else {
                // If uninstallation succeed
                try {
                    for (RecycleViewItem item : PackageData.getRawData()) {
                        if (item.getTitle().equals(Common.getBatchList().get(0))) {
                            PackageData.getRawData().remove(item);
                            Common.getBatchList().remove(0);
                            if (!Common.reloadPage()) Common.reloadPage(true);
                        }
                    }
                } catch (ConcurrentModificationException ignored) {}
                handleUninstallEvent();
            }
        } else if (requestCode == 1) {
            // If uninstallation cancelled or failed
            Utils.snackbar(mRecyclerView, getString(R.string.uninstall_status_failed, PackageData.getAppName(Common.getBatchList().get(0), requireActivity())));
            Common.getBatchList().remove(0);
            handleUninstallEvent();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.getBoolean("welcomeMessage", true, getActivity())) {
            Utils.WelcomeDialog(getActivity());
        }
        if (Common.reloadPage()) {
            Common.reloadPage(false);
            loadUI(requireActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Common.getSearchText() != null) {
            mSearchWord.setText(null);
            Common.setSearchText(null);
        }
    }

}