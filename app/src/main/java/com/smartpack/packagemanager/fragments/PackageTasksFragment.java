/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import static android.view.View.GONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PackageTasksAdapter;
import com.smartpack.packagemanager.dialogs.BatchOptionsDialog;
import com.smartpack.packagemanager.dialogs.BatchResultsDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.SerializableItems.BatchOptionsItems;
import com.smartpack.packagemanager.utils.SerializableItems.PackageItems;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class PackageTasksFragment extends Fragment {

    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mSort, mBatchOptions;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private List<PackageItems> mData;
    private List<String> mBatchList = null;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private PackageTasksAdapter mRecycleViewAdapter;
    private RootShell mRootShell = null;
    private ShizukuShell mShizukuShell = null;
    private String mPackageNameRemoved = null, mSearchText = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packagetasks, container, false);

        mProgress = mRootView.findViewById(R.id.progress);
        mBatchOptions = mRootView.findViewById(R.id.batch);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        MaterialButton mSearch = mRootView.findViewById(R.id.search_icon);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mSort = mRootView.findViewById(R.id.sort_icon);
        MaterialButton mReload = mRootView.findViewById(R.id.reload_icon);
        FloatingActionButton mFAB = requireActivity().findViewById(R.id.fab);

        mFAB.setVisibility(View.VISIBLE);

        mSearchWord.setHintTextColor(Color.GRAY);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        mRootShell = new RootShell();
        mShizukuShell = new ShizukuShell();

        if (!mRootShell.rootAccess() && mShizukuShell.isSupported() && sCommonUtils.getBoolean("request_shizuku", true, requireActivity())) {
            if (mShizukuShell.isPermissionDenied()) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setCancelable(false)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.shizuku_integration_message))
                        .setNegativeButton(getString(R.string.never_show), (dialogInterface, i) -> sCommonUtils.saveBoolean(
                                "request_shizuku", false, requireActivity()))
                        .setPositiveButton(getString(R.string.request), (dialogInterface, i) -> mShizukuShell.requestPermission()
                        ).show();
            } else {
                // Activate Shizuku on app launch for supported and enabled devices;
                mShizukuShell.ensureUserService();
            }
        }

        loadUI(mSearchText, requireActivity());

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.show_apps_user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            sCommonUtils.saveString("appTypes", "all", requireActivity());
                            loadUI(mSearchText, requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            sCommonUtils.saveString("appTypes", "system", requireActivity());
                            loadUI(mSearchText, requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            sCommonUtils.saveString("appTypes", "user", requireActivity());
                            loadUI(mSearchText, requireActivity());
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
                Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                Utils.toggleKeyboard(1, mSearchWord, requireActivity());
            }
            if (mSearchText != null) {
                mSearchText = null;
                mSearchWord.setText(null);
            }
        });

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            Utils.toggleKeyboard(0, mSearchWord, requireActivity());
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
                loadUI(s.toString().trim().toLowerCase(), requireActivity());
            }
        });

        mReload.setOnClickListener(v ->
                new sExecutor() {

                    @Override
                    public void onPreExecute() {
                        mProgress.setVisibility(View.VISIBLE);
                        mBatchOptions.setVisibility(GONE);
                        mRecyclerView.setVisibility(GONE);

                        mData = new CopyOnWriteArrayList<>();

                        if (mSearchText != null) {
                            mSearchWord.setText(null);
                        }
                        if (mSearchWord.getVisibility() == View.VISIBLE) {
                            mSearchWord.setVisibility(GONE);
                            Utils.toggleKeyboard(0, mSearchWord, requireActivity());
                        }

                        mBatchList.clear();
                    }

                    @Override
                    public void doInBackground() {
                        PackageData.setRawData(mProgress, requireActivity());
                        mData = PackageData.getData(mSearchText, requireActivity());
                        mRecycleViewAdapter = new PackageTasksAdapter(mData, mSearchText, mBatchList, diableOrUninstall, requireActivity());
                    }

                    @Override
                    public void onPostExecute() {
                        mBatchOptions.setVisibility(GONE);
                        mRecyclerView.setAdapter(mRecycleViewAdapter);
                        mProgress.setVisibility(GONE);
                        mProgress.setIndeterminate(true);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }.execute()
        );

        mSort.setOnClickListener(v -> sortMenu(requireActivity()));

        mBatchOptions.setOnClickListener(v -> batchOptionsMenu(requireActivity()));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                mFAB.setVisibility(newState == RecyclerView.SCROLL_STATE_IDLE ? View.VISIBLE : View.GONE);
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    if (mSearchText != null) {
                        mSearchText = null;
                        mSearchWord.setText(null);
                    }
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (!mBatchList.isEmpty()) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.batch_warning)
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                                mBatchList.clear();
                                loadUI(mSearchText, requireActivity());
                            }).show();
                } else if (sCommonUtils.getBoolean("exit_confirmation", true, requireActivity())) {
                    if (mExit) {
                        mExit = false;
                        exit(requireActivity());
                    } else {
                        sCommonUtils.toast(getString(R.string.press_back), requireActivity()).show();
                        mExit = true;
                        mHandler.postDelayed(() -> mExit = false, 2000);
                    }
                } else {
                    exit(requireActivity());
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = sCommonUtils.getString("appTypes", "all", activity);
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void exit(Activity activity) {
        if (mRootShell.rootAccess() && mRootShell != null) mRootShell.closeSU();
        activity.finish();
    }

    private void uninstallUserApp(String packageName) {
        Intent remove = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName));
        remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        if (mBatchList.isEmpty()) {
            mPackageNameRemoved = packageName;
            diableOrUninstall.launch(remove);
        } else {
            uninstallApps.launch(remove);
        }
    }

    private void handleUninstallEvent() {
        if (!mBatchList.isEmpty()) {
            uninstallUserApp(mBatchList.get(0));
        } else {
            mBatchOptions.setVisibility(GONE);
        }
    }

    private void sortMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSort);
        Menu menu = popupMenu.getMenu();
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));
        sort.add(0, 1, Menu.NONE, getString(R.string.name)).setCheckable(true)
                .setChecked(PackageData.getSortingType(activity) == 0);
        sort.add(0, 2, Menu.NONE, getString(R.string.package_id)).setCheckable(true)
                .setChecked(PackageData.getSortingType(activity) == 1);
        sort.add(0, 3, Menu.NONE, getString(R.string.time_installed)).setCheckable(true)
                .setChecked(PackageData.getSortingType(activity) == 2);
        sort.add(0, 4, Menu.NONE, getString(R.string.time_updated)).setCheckable(true)
                .setChecked(PackageData.getSortingType(activity) == 3);
        sort.add(0, 5, Menu.NONE, getString(R.string.size)).setCheckable(true)
                .setChecked(PackageData.getSortingType(activity) == 4);
        menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                .setChecked(sCommonUtils.getBoolean("reverse_order", false, activity));
        sort.setGroupCheckable(0, true, true);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (PackageData.getSortingType(activity) != 0) {
                        PackageData.setSortingType(0, activity);
                        loadUI(mSearchText, activity);
                    }
                    break;
                case 2:
                    if (PackageData.getSortingType(activity) != 1) {
                        PackageData.setSortingType(1, activity);
                        loadUI(mSearchText, activity);
                    }
                    break;
                case 3:
                    if (PackageData.getSortingType(activity) != 2) {
                        PackageData.setSortingType(2, activity);
                        loadUI(mSearchText, activity);
                    }
                    break;
                case 4:
                    if (PackageData.getSortingType(activity) != 3) {
                        PackageData.setSortingType(3, activity);
                        loadUI(mSearchText, activity);
                    }
                    break;
                case 5:
                    if (PackageData.getSortingType(activity) != 4) {
                        PackageData.setSortingType(4, activity);
                        loadUI(mSearchText, activity);
                    }
                    break;
                case 6:
                    sCommonUtils.saveBoolean("reverse_order", !sCommonUtils.getBoolean("reverse_order", false, activity), activity);
                    loadUI(mSearchText, activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    @SuppressLint("StringFormatInvalid")
    private void batchOptionsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mBatchOptions);
        Menu menu = popupMenu.getMenu();
        if (mRootShell.rootAccess() || mShizukuShell.isReady()) {
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.turn_on_off));
        }
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.uninstall));
        if (mRootShell.rootAccess() || mShizukuShell.isReady()) {
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.reset));
        }
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.export));
        menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.export_details));
        menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.select_all)).setCheckable(true)
                .setChecked(mData.size() == mBatchList.size());
        if (mData.size() != mBatchList.size()) {
            menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.batch_list_clear));
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new BatchOptionsDialog(getString(R.string.turn_on_off_selected_question), getString(R.string.turn_on_off), mBatchList, requireActivity()) {
                        @Override
                        public void apply(List<BatchOptionsItems> data) {
                            new sExecutor() {
                                private final List<BatchOptionsItems> newData = new CopyOnWriteArrayList<>();
                                private ProgressDialog mProgressDialog;
                                @Override
                                public void onPreExecute() {
                                    mProgressDialog = new ProgressDialog(activity);
                                    mProgressDialog.setIcon(R.mipmap.ic_launcher);
                                    mProgressDialog.setTitle(getString(R.string.disabling, "..."));
                                    mProgressDialog.show();

                                    if (mRootShell == null) {
                                        mRootShell = new RootShell();
                                    }
                                    if (mShizukuShell == null) {
                                        mShizukuShell = new ShizukuShell();
                                    }
                                }

                                @Override
                                public void doInBackground() {
                                    mProgressDialog.setMax(data.size());
                                    for (BatchOptionsItems batchOptionsItems : data) {
                                        if (batchOptionsItems.isChecked()) {
                                            if (batchOptionsItems.getPackageName().equals(activity.getPackageName())) {
                                                newData.add(new BatchOptionsItems(batchOptionsItems.getName(), batchOptionsItems.getPackageName(), batchOptionsItems.getIcon(), false, 0));
                                            } else {
                                                String result;
                                                if (mRootShell.rootAccess()) {
                                                    result = mRootShell.runAndGetError((sPackageUtils.isEnabled(batchOptionsItems.getPackageName(), activity) ? "pm disable " : "pm enable ") + batchOptionsItems.getPackageName());
                                                } else {
                                                    result = mShizukuShell.runAndGetOutput((sPackageUtils.isEnabled(batchOptionsItems.getPackageName(), activity) ? "pm disable " : "pm enable ") + batchOptionsItems.getPackageName());
                                                }
                                                if (result != null && (!result.contains("new state: disabled") && !result.contains("new state: enabled"))) {
                                                    newData.add(new BatchOptionsItems(batchOptionsItems.getName(), batchOptionsItems.getPackageName(), batchOptionsItems.getIcon(), false, 1));
                                                }
                                                for (int i = 0; i < PackageData.getRawData().size(); i++) {
                                                    if (PackageData.getRawData().get(i).getPackageName().equals(batchOptionsItems.getPackageName())) {
                                                        PackageItems itemOld = PackageData.getRawData().get(i);
                                                        PackageItems itemNew = new PackageItems(
                                                                itemOld.getPackageName(),
                                                                sPackageUtils.getAppName(batchOptionsItems.getPackageName(), requireActivity()).toString() + (sPackageUtils.isEnabled(batchOptionsItems.getPackageName(), requireActivity()) ? "" : " (Disabled)"),
                                                                itemOld.getAPKSize(),
                                                                requireActivity()
                                                        );
                                                        PackageData.getRawData().set(i, itemNew);
                                                        int index = mData.indexOf(itemOld);
                                                        if (index != -1) {
                                                            mData.set(index, itemNew);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        mProgressDialog.updateProgress(1);
                                    }
                                }

                                @Override
                                public void onPostExecute() {
                                    mProgressDialog.dismiss();
                                    mRecycleViewAdapter.notifyItemRangeChanged(0, mRecycleViewAdapter.getItemCount());
                                    if (!newData.isEmpty()) {
                                        new BatchResultsDialog(newData, activity);
                                    } else {
                                        sCommonUtils.toast(R.string.batch_processing_success_message, activity).show();
                                    }
                                }
                            }.execute();
                        }
                    };
                    break;
                case 1:
                    if (mRootShell.rootAccess() || mShizukuShell.isReady()) {
                        new BatchOptionsDialog(getString(R.string.uninstall_selected_question), getString(R.string.uninstall), mBatchList, requireActivity()) {
                            @Override
                            public void apply(List<BatchOptionsItems> data) {
                                new sExecutor() {
                                    private final List<Integer> mPositionsRemoved = new CopyOnWriteArrayList<>();
                                    private final List<BatchOptionsItems> mNewData = new CopyOnWriteArrayList<>();
                                    private ProgressDialog mProgressDialog;
                                    @Override
                                    public void onPreExecute() {
                                        mProgressDialog = new ProgressDialog(activity);
                                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                                        mProgressDialog.setTitle(getString(R.string.uninstall_summary, "..."));
                                        mProgressDialog.show();

                                        if (mRootShell == null) {
                                            mRootShell = new RootShell();
                                        }
                                        if (mShizukuShell == null) {
                                            mShizukuShell = new ShizukuShell();
                                        }
                                    }

                                    @Override
                                    public void doInBackground() {
                                        mProgressDialog.setMax(data.size());
                                        for (BatchOptionsItems batchOptionsItems : data) {
                                            if (batchOptionsItems.isChecked()) {
                                                if (batchOptionsItems.getPackageName().equals(activity.getPackageName())) {
                                                    mNewData.add(new BatchOptionsItems(batchOptionsItems.getName(), batchOptionsItems.getPackageName(), batchOptionsItems.getIcon(), false, 0));
                                                } else {
                                                    String result;
                                                    if (mRootShell.rootAccess()) {
                                                        result = mRootShell.runAndGetError("pm uninstall --user 0 " + batchOptionsItems.getPackageName());
                                                    } else {
                                                        result = mShizukuShell.runAndGetOutput("pm uninstall --user 0 " + batchOptionsItems.getPackageName());
                                                    }
                                                    if (result != null && result.trim().equals("Success")) {
                                                        for (int i = 0; i < PackageData.getRawData().size(); i++) {
                                                            if (PackageData.getRawData().get(i).getPackageName().equals(batchOptionsItems.getPackageName())) {
                                                                PackageItems packageItems = PackageData.getRawData().get(i);
                                                                PackageData.getRawData().remove(packageItems);
                                                                int index = mData.indexOf(packageItems);
                                                                if (index != -1) {
                                                                    mData.remove(packageItems);
                                                                    mPositionsRemoved.add(index);
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        mNewData.add(new BatchOptionsItems(batchOptionsItems.getName(), batchOptionsItems.getPackageName(), batchOptionsItems.getIcon(), false, 1));
                                                    }
                                                }
                                            }
                                            mProgressDialog.updateProgress(1);
                                        }
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        for (Integer positions : mPositionsRemoved) {
                                            mRecycleViewAdapter.notifyItemRemoved(positions);
                                        }
                                        mRecycleViewAdapter.notifyItemRangeChanged(0, mRecycleViewAdapter.getItemCount());
                                        mBatchList.clear();
                                        mBatchOptions.setVisibility(GONE);
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                        if (!mNewData.isEmpty()) {
                                            new BatchResultsDialog(mNewData, activity);
                                        } else {
                                            sCommonUtils.toast(R.string.batch_processing_success_message, activity).show();
                                        }
                                    }
                                }.execute();
                            }
                        };
                        break;
                    } else {
                        uninstallUserApp(mBatchList.get(0));
                    }
                    break;
                case 2:
                    new BatchOptionsDialog(getString(R.string.reset_selected_question), getString(R.string.reset), mBatchList, requireActivity()) {
                        @Override
                        public void apply(List<BatchOptionsItems> data) {
                            new sExecutor() {
                                private boolean mEmpty = true;
                                private final List<BatchOptionsItems> newData = new CopyOnWriteArrayList<>();
                                private ProgressDialog mProgressDialog;
                                @Override
                                public void onPreExecute() {
                                    mProgressDialog = new ProgressDialog(activity);
                                    mProgressDialog.setIcon(R.mipmap.ic_launcher);
                                    mProgressDialog.setTitle(getString(R.string.reset_summary, "..."));
                                    mProgressDialog.show();

                                    if (mRootShell == null) {
                                        mRootShell = new RootShell();
                                    }
                                    if (mShizukuShell == null) {
                                        mShizukuShell = new ShizukuShell();
                                    }
                                }

                                @Override
                                public void doInBackground() {
                                    mProgressDialog.setMax(data.size());
                                    for (BatchOptionsItems batchOptionsItems : data) {
                                        if (batchOptionsItems.isChecked()) {
                                            if (batchOptionsItems.getPackageName().equals(activity.getPackageName())) {
                                                newData.add(new BatchOptionsItems(batchOptionsItems.getName(), batchOptionsItems.getPackageName(), batchOptionsItems.getIcon(), false, 0));
                                            } else {
                                                if (mRootShell.rootAccess()) {
                                                    mRootShell.runCommand("pm clear " + batchOptionsItems.getPackageName());
                                                } else {
                                                    mShizukuShell.runCommand("pm clear " + batchOptionsItems.getPackageName());
                                                }
                                                newData.add(new BatchOptionsItems(batchOptionsItems.getName(), batchOptionsItems.getPackageName(), batchOptionsItems.getIcon(), false, 1));
                                            }
                                            mEmpty = false;
                                        }
                                        mProgressDialog.updateProgress(1);
                                    }
                                }

                                @Override
                                public void onPostExecute() {
                                    mProgressDialog.dismiss();
                                    if (!newData.isEmpty()) {
                                        new BatchResultsDialog(newData, activity);
                                    }
                                    if (!mEmpty) {
                                        if (newData.isEmpty()) {
                                            sCommonUtils.toast(R.string.batch_processing_success_message, activity).show();
                                        }
                                    }
                                }
                            }.execute();
                        }
                    };
                    break;
                case 3:
                    if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
                        sPermissionUtils.requestPermission(new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                activity);
                        sCommonUtils.toast(activity.getString(R.string.permission_denied_write_storage), requireActivity()).show();
                    } else {
                        new BatchOptionsDialog(getString(R.string.export_selected_question), getString(R.string.export), mBatchList, requireActivity()) {
                            @Override
                            public void apply(List<BatchOptionsItems> data) {
                                new sExecutor() {
                                    private boolean mEmpty = true;
                                    private ProgressDialog mProgressDialog;
                                    @Override
                                    public void onPreExecute() {
                                        mProgressDialog = new ProgressDialog(activity);
                                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                                        mProgressDialog.setTitle(activity.getString(R.string.exporting, "..."));
                                        mProgressDialog.show();
                                    }

                                    @Override
                                    public void doInBackground() {
                                        PackageData.makePackageFolder(activity);
                                        mProgressDialog.setMax(data.size());
                                        for (BatchOptionsItems batchOptionsItems : data) {
                                            if (batchOptionsItems.isChecked()) {
                                                if (SplitAPKInstaller.isAppBundle(sPackageUtils.getParentDir(batchOptionsItems.getPackageName(), activity))) {
                                                    List<File> mFiles = new ArrayList<>();
                                                    for (final String splitApps : SplitAPKInstaller.splitApks(sPackageUtils.getParentDir(batchOptionsItems.getPackageName(), activity))) {
                                                        mFiles.add(new File(sPackageUtils.getParentDir(batchOptionsItems.getPackageName(), activity) + "/" + splitApps));
                                                    }
                                                    try (ZipFileUtils zipFileUtils = new ZipFileUtils(PackageData.getPackageDir(activity) + "/" + PackageData.getFileName(batchOptionsItems.getPackageName(), activity) + "_" +
                                                            sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(batchOptionsItems.getPackageName(), activity), activity) + ".apkm")) {
                                                        zipFileUtils.zip(mFiles);
                                                    } catch (IOException ignored) {
                                                    }
                                                } else {
                                                    sFileUtils.copy(new File(sPackageUtils.getSourceDir(batchOptionsItems.getPackageName(), activity)), new File(PackageData.getPackageDir(activity), PackageData.getFileName(batchOptionsItems.getPackageName(), activity) + "_" +
                                                            sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(batchOptionsItems.getPackageName(), activity), activity) + ".apk"));
                                                }
                                                mEmpty = false;
                                            }
                                            mProgressDialog.updateProgress(1);
                                        }
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        mProgressDialog.dismiss();
                                        if (!mEmpty) {
                                            new MaterialAlertDialogBuilder(activity)
                                                    .setIcon(R.mipmap.ic_launcher)
                                                    .setTitle(R.string.app_name)
                                                    .setMessage(getString(R.string.export_message_summary, PackageData.getPackageDir(activity)))
                                                    .setPositiveButton(R.string.cancel, (dialog, id) -> {
                                                    }).show();
                                        }
                                    }
                                }.execute();
                            }
                        };
                    }
                    break;
                case 4:
                    if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
                        sPermissionUtils.requestPermission(new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                activity);
                        sCommonUtils.toast(getString(R.string.permission_denied_write_storage), activity).show();
                    } else {
                        new BatchOptionsDialog(getString(R.string.export_details_selected_question), getString(R.string.export), mBatchList, requireActivity()) {
                            @Override
                            public void apply(List<BatchOptionsItems> data) {
                                new sExecutor() {
                                    private boolean mEmpty = true;
                                    private File mJSON;
                                    private ProgressDialog mProgressDialog;
                                    @Override
                                    public void onPreExecute() {
                                        mProgressDialog = new ProgressDialog(activity);
                                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                                        mProgressDialog.setTitle(activity.getString(R.string.exporting, "..."));
                                        mProgressDialog.show();
                                    }

                                    @Override
                                    public void doInBackground() {
                                        PackageData.makePackageFolder(activity);
                                        mProgressDialog.setMax(data.size());
                                        mJSON = new File(PackageData.getPackageDir(activity), "package_details" + System.currentTimeMillis() + " .json");
                                        JSONObject obj = new JSONObject();
                                        JSONArray apps = new JSONArray();
                                        for (BatchOptionsItems batchOptionsItems : data) {
                                            if (batchOptionsItems.isChecked() && sPackageUtils.isPackageInstalled(batchOptionsItems.getPackageName(), activity)) {
                                                try {
                                                    apps.put(PackageDetails.getPackageDetails(batchOptionsItems.getPackageName(), activity));
                                                    obj.put("applications", apps);
                                                } catch (JSONException ignored) {
                                                }
                                                mEmpty = false;
                                            }
                                            mProgressDialog.updateProgress(1);
                                        }
                                        sFileUtils.create(obj.toString(), mJSON);
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        mProgressDialog.dismiss();

                                        if (!mEmpty) {
                                            new MaterialAlertDialogBuilder(requireActivity())
                                                    .setIcon(R.mipmap.ic_launcher)
                                                    .setTitle(R.string.app_name)
                                                    .setMessage(getString(R.string.export_details_message, mJSON.getAbsolutePath()))
                                                    .setPositiveButton(R.string.cancel, (dialog, i) -> {
                                                    }).show();
                                        }
                                    }
                                }.execute();
                            }
                        };
                    }
                    break;
                case 5:
                    if (mData.size() != mBatchList.size()) {
                        mBatchList.clear();
                        for (PackageItems mPackage : mData) {
                            mBatchList.add(mPackage.getPackageName());
                        }
                    } else {
                        mBatchList.clear();
                    }
                    loadUI(mSearchText, activity);
                    break;
                case 6:
                    mBatchList.clear();
                    loadUI(mSearchText, activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void loadUI(String searchTxt, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(GONE);
                //mData = new CopyOnWriteArrayList<>();
                if (mBatchList == null) {
                    mBatchList = new ArrayList<>();
                }
            }

            @Override
            public void doInBackground() {
                mData = PackageData.getData(searchTxt, activity);
                mRecycleViewAdapter = new PackageTasksAdapter(mData, searchTxt, mBatchList, diableOrUninstall, activity);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }
                mSearchText = searchTxt;
                mSearchWord.setHint(getString(R.string.search_market_message, mRecycleViewAdapter.getItemCount() + " " + getString(R.string.applications)));
                mBatchOptions.setVisibility(!mBatchList.isEmpty() ? View.VISIBLE : GONE);
                mBatchOptions.setText(activity.getString(R.string.batch_options, mBatchList.size()));
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private sExecutor removeItem(String packageName) {
        return new sExecutor() {
            private int position;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                for (int i=0; i<mData.size(); i++) {
                    if (mData.get(i).getPackageName().equals(packageName)) {
                        PackageData.getRawData().remove(mData.get(i));
                        mData.remove(i);
                        position = i;
                        return;
                    }
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (mPackageNameRemoved != null) {
                    mPackageNameRemoved = null;
                }
                mRecycleViewAdapter.notifyItemRemoved(position);
                mRecycleViewAdapter.notifyItemRangeChanged(position, mRecycleViewAdapter.getItemCount());
                mProgress.setVisibility(GONE);
            }
        };
    }

    @SuppressLint("StringFormatInvalid")
    private final ActivityResultLauncher<Intent> diableOrUninstall = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String packageName = Objects.requireNonNull(data).getStringExtra("packageName");
                    String packageNameDisabled = Objects.requireNonNull(data).getStringExtra("packageNameDisabled");
                    if (packageName != null) {
                        if (sPackageUtils.isPackageInstalled(packageName, requireActivity())) {
                            uninstallUserApp(packageName);
                        } else {
                            removeItem(packageName).execute();
                        }
                    } else if (packageNameDisabled != null) {
                        new sExecutor() {
                            private int position;

                            @Override
                            public void onPreExecute() {
                                mProgress.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void doInBackground() {
                                for (int i=0; i<mData.size(); i++) {
                                    if (mData.get(i).getPackageName().equals(packageNameDisabled)) {
                                        PackageItems itemOld = mData.get(i);
                                        PackageItems itemNew = new PackageItems(
                                                itemOld.getPackageName(),
                                                sPackageUtils.getAppName(itemOld.getPackageName(), requireActivity()).toString() + (sPackageUtils.isEnabled(itemOld.getPackageName(), requireActivity()) ? "" : " (Disabled)"),
                                                itemOld.getAPKSize(),
                                                requireActivity()
                                        );
                                        int index = PackageData.getRawData().indexOf(itemOld);
                                        if (index != -1) {
                                            PackageData.getRawData().set(index, itemNew);
                                        }
                                        mData.set(i, itemNew);
                                        position = i;
                                        return;
                                    }
                                }
                            }

                            @SuppressLint("StringFormatInvalid")
                            @Override
                            public void onPostExecute() {
                                mRecycleViewAdapter.notifyItemChanged(position);
                                mProgress.setVisibility(GONE);
                            }
                        }.execute();
                    } else {
                        removeItem(mPackageNameRemoved).execute();
                    }
                }
            }
    );

    @SuppressLint("StringFormatInvalid")
    private final ActivityResultLauncher<Intent> uninstallApps = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // If uninstallation succeed
                    try {
                        for (PackageItems item : PackageData.getRawData()) {
                            if (item.getPackageName().equals(mBatchList.get(0))) {
                                PackageData.getRawData().remove(item);
                                mBatchList.remove(0);

                                int index = mData.indexOf(item);
                                if (index != -1) {
                                    mData.remove(index);
                                    mRecycleViewAdapter.notifyItemRangeChanged(0, mRecycleViewAdapter.getItemCount());
                                }
                            }
                        }
                    } catch (ConcurrentModificationException ignored) {}
                    handleUninstallEvent();
                } else {
                    sCommonUtils.toast(getString(R.string.uninstall_status_failed, PackageData.getAppName(mBatchList.get(0), requireActivity())), requireActivity()).show();
                    mBatchList.remove(0);
                    mRecycleViewAdapter.notifyItemRangeChanged(0, mRecycleViewAdapter.getItemCount());
                    handleUninstallEvent();
                }
            }
    );

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSearchText != null) {
            mSearchWord.setText(null);
        }
        mBatchList.clear();
        if (mRootShell.rootAccess() && mRootShell != null) mRootShell.closeSU();
    }

}