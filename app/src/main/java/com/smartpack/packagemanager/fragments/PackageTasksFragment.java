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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PackageTasksAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.Flavor;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.PackageItems;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.tasks.BatchDisableTask;
import com.smartpack.packagemanager.utils.tasks.BatchExportTask;
import com.smartpack.packagemanager.utils.tasks.BatchResetTask;
import com.smartpack.packagemanager.utils.tasks.BatchUninstallTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class PackageTasksFragment extends Fragment {

    private AppCompatAutoCompleteTextView mSearchWord;
    private MaterialButton mSort;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private MaterialCardView mBatchOptions;
    private MaterialTextView mBatchOptionTitle;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private PackageTasksAdapter mRecycleViewAdapter;
    private RootShell mRootShell = null;
    private ShizukuShell mShizukuShell = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packagetasks, container, false);

        mBatchOptionTitle = mRootView.findViewById(R.id.batch_option_title);
        mProgress = mRootView.findViewById(R.id.progress);
        mBatchOptions = mRootView.findViewById(R.id.batch_options);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        MaterialButton mSearch = mRootView.findViewById(R.id.search_icon);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mSort = mRootView.findViewById(R.id.sort_icon);
        MaterialButton mReload = mRootView.findViewById(R.id.reload_icon);

        Common.getView(requireActivity(), R.id.fab).setVisibility(View.VISIBLE);

        mSearchWord.setHintTextColor(Color.GRAY);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        mRootShell = new RootShell();
        mShizukuShell = new ShizukuShell();

        if (!mRootShell.rootAccess() && mShizukuShell.isSupported()) {
            if (mShizukuShell.isPermissionDenied() && sCommonUtils.getBoolean("request_shizuku", true, requireActivity())) {
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

        loadUI(requireActivity());

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
                            loadUI(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            sCommonUtils.saveString("appTypes", "system", requireActivity());
                            loadUI(requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            sCommonUtils.saveString("appTypes", "user", requireActivity());
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
                Utils.toggleKeyboard(1, mSearchWord, requireActivity());
                return;
            }
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                Utils.toggleKeyboard(1, mSearchWord, requireActivity());
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
                Common.setSearchText(s.toString().toLowerCase());
                loadUI(requireActivity());
            }
        });

        mReload.setOnClickListener(v -> new sExecutor() {

                    @Override
                    public void onPreExecute() {
                        mProgress.setVisibility(View.VISIBLE);
                        mBatchOptions.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);

                        if (Common.getSearchText() != null) {
                            mSearchWord.setText(null);
                            Common.setSearchText(null);
                        }
                        if (mSearchWord.getVisibility() == View.VISIBLE) {
                            mSearchWord.setVisibility(View.GONE);
                            Utils.toggleKeyboard(0, mSearchWord, requireActivity());
                        }

                        Common.getBatchList().clear();
                        mRecyclerView.removeAllViews();
                    }

                    @Override
                    public void doInBackground() {
                        PackageData.setRawData(mProgress, requireActivity());
                        mRecycleViewAdapter = new PackageTasksAdapter(PackageData.getData(requireActivity()), requireActivity());
                    }

                    @Override
                    public void onPostExecute() {
                        mBatchOptions.setVisibility(View.GONE);
                        mRecyclerView.setAdapter(mRecycleViewAdapter);
                        mProgress.setVisibility(View.GONE);
                        mProgress.setIndeterminate(true);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }.execute()
        );

        mSort.setOnClickListener(v -> sortMenu(requireActivity()));

        mBatchOptions.setOnClickListener(v -> batchOptionsMenu(requireActivity()));

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (Common.getSearchText() != null) {
                    mSearchWord.setText(null);
                    Common.setSearchText(null);
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (!Common.getBatchList().isEmpty()) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(R.string.batch_warning)
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> exit(requireActivity())
                            ).show();
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

    private void selectAll(boolean b) {
        sCommonUtils.saveBoolean("select_all", b, requireActivity());
        loadUI(requireActivity());
    }

    private void uninstallUserApp(String packageName) {
        Intent remove = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName));
        remove.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        uninstallApps.launch(remove);
    }

    private void handleUninstallEvent() {
        if (!Common.getBatchList().isEmpty()) {
            uninstallUserApp(Common.getBatchList().get(0));
        } else {
            loadUI(requireActivity());
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
                        loadUI(activity);
                    }
                    break;
                case 2:
                    if (PackageData.getSortingType(activity) != 1) {
                        PackageData.setSortingType(1, activity);
                        loadUI(activity);
                    }
                    break;
                case 3:
                    if (PackageData.getSortingType(activity) != 2) {
                        PackageData.setSortingType(2, activity);
                        loadUI(activity);
                    }
                    break;
                case 4:
                    if (PackageData.getSortingType(activity) != 3) {
                        PackageData.setSortingType(3, activity);
                        loadUI(activity);
                    }
                    break;
                case 5:
                    if (PackageData.getSortingType(activity) != 4) {
                        PackageData.setSortingType(4, activity);
                        loadUI(activity);
                    }
                    break;
                case 6:
                    sCommonUtils.saveBoolean("reverse_order", !sCommonUtils.getBoolean("reverse_order", false, activity), activity);
                    loadUI(activity);
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
                .setChecked(PackageData.getData(activity).size() == Common.getBatchList().size());
        if (PackageData.getData(activity).size() != Common.getBatchList().size()) {
            menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.batch_list_clear));
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.sure_question)
                            .setMessage(getString(R.string.batch_list_disable) + "\n" + PackageData.showBatchList())
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) ->
                                    new BatchDisableTask(activity).execute())
                            .show();
                    break;
                case 1:
                    if (mRootShell.rootAccess() || mShizukuShell.isReady()) {
                        MaterialAlertDialogBuilder uninstall = new MaterialAlertDialogBuilder(activity);
                        uninstall.setIcon(R.mipmap.ic_launcher);
                        uninstall.setTitle(R.string.sure_question);
                        uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageData.showBatchList());
                        uninstall.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) ->
                                new BatchUninstallTask(activity).execute());
                        uninstall.show();
                    } else {
                        uninstallUserApp(Common.getBatchList().get(0));
                    }
                    break;
                case 2:
                    MaterialAlertDialogBuilder reset = new MaterialAlertDialogBuilder(activity);
                    reset.setIcon(R.mipmap.ic_launcher);
                    reset.setTitle(R.string.sure_question);
                    reset.setMessage(getString(R.string.batch_list_reset) + "\n" + PackageData.showBatchList());
                    reset.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reset.setPositiveButton(getString(R.string.reset), (dialogInterface, i) ->
                            new BatchResetTask(activity).execute());
                    reset.show();
                    break;
                case 3:
                    if (Flavor.isFullVersion() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Utils.isPermissionDenied() ||
                            Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            new MaterialAlertDialogBuilder(activity)
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
                                    activity);
                        }
                        sCommonUtils.toast(activity.getString(R.string.permission_denied_write_storage), requireActivity()).show();
                    } else {
                        MaterialAlertDialogBuilder export = new MaterialAlertDialogBuilder(activity);
                        export.setIcon(R.mipmap.ic_launcher);
                        export.setTitle(R.string.sure_question);
                        export.setMessage(getString(R.string.batch_list_export) + "\n" + PackageData.showBatchList());
                        export.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        });
                        export.setPositiveButton(getString(R.string.export), (dialogInterface, i) ->
                                new BatchExportTask(activity).execute());
                        export.show();
                    }
                    break;
                case 4:
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
                        sCommonUtils.toast(getString(R.string.permission_denied_write_storage), requireActivity()).show();
                    } else {
                        if (!PackageData.getPackageDir(activity).exists()) {
                            sFileUtils.mkdir(PackageData.getPackageDir(activity));
                        }
                        File mJSON = new File(PackageData.getPackageDir(requireActivity()), "package_details.json");
                        try {
                            JSONObject obj = new JSONObject();
                            JSONArray apps = new JSONArray();
                            for (String packageID : Common.getBatchList()) {
                                if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, activity)) {
                                    apps.put(PackageDetails.getPackageDetails(packageID, activity));
                                }
                            }
                            obj.put("applications", apps);
                            sFileUtils.create(obj.toString(), mJSON);
                            sCommonUtils.toast(getString(R.string.export_details_message, mJSON.getName()), requireActivity()).show();
                        } catch (JSONException ignored) {
                        }
                    }
                    break;
                case 5:
                    if (PackageData.getData(activity).size() == Common.getBatchList().size()) {
                        selectAll(false);
                    } else {
                        if (sCommonUtils.getBoolean("select_all_firstAttempt", true, requireActivity())) {
                            new MaterialAlertDialogBuilder(Objects.requireNonNull(requireActivity()))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(getString(R.string.sure_question))
                                    .setMessage(getString(R.string.select_all_summary))
                                    .setCancelable(false)
                                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                    })
                                    .setPositiveButton(getString(R.string.select_all), (dialog, id) -> {
                                        selectAll(true);
                                        sCommonUtils.saveBoolean("select_all_firstAttempt", false, requireActivity());
                                    }).show();
                        } else {
                            selectAll(true);
                        }
                    }
                    break;
                case 6:
                    Common.getBatchList().clear();
                    loadUI(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void loadUI(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mBatchOptions.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                if (sCommonUtils.getBoolean("select_all", false, activity)) {
                    Common.getBatchList().clear();
                    for (PackageItems mPackage : PackageData.getData(activity)) {
                        Common.getBatchList().add(mPackage.getPackageName());
                    }
                } else {
                    Common.getBatchList().clear();
                }
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new PackageTasksAdapter(PackageData.getData(activity), activity);
            }

            @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
            @Override
            public void onPostExecute() {
                if (sCommonUtils.getBoolean("select_all", false, activity)) {
                    sCommonUtils.saveBoolean("select_all", false, activity);
                    mBatchOptions.setVisibility(View.VISIBLE);
                } else {
                    mBatchOptions.setVisibility(View.GONE);
                }
                mBatchOptions.setVisibility(!Common.getBatchList().isEmpty() ? View.VISIBLE : View.GONE);
                if (!Common.getBatchList().isEmpty()) {
                    mBatchOptionTitle.setText(getString(R.string.batch_options, Common.getBatchList().size()));
                }
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    @SuppressLint("StringFormatInvalid")
    ActivityResultLauncher<Intent> uninstallApps = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // If uninstallation succeed
                    try {
                        for (PackageItems item : PackageData.getRawData()) {
                            if (item.getPackageName().equals(Common.isUninstall() ? Common.getApplicationID() : Common.getBatchList().get(0))) {
                                PackageData.getRawData().remove(item);
                                if (!Common.isUninstall()) {
                                    Common.getBatchList().remove(0);
                                }
                                if (!Common.reloadPage()) Common.reloadPage(true);
                            }
                        }
                    } catch (ConcurrentModificationException ignored) {}
                    if (Common.isUninstall()) {
                        Common.isUninstall(false);
                        loadUI(requireActivity());
                    } else {
                        handleUninstallEvent();
                    }
                } else {
                    if (Common.isUninstall()) {
                        Common.isUninstall(false);
                    } else {
                        // If uninstallation cancelled or failed
                        sCommonUtils.toast(getString(R.string.uninstall_status_failed, PackageData.getAppName(Common.getBatchList().get(0), requireActivity())), requireActivity()).show();
                        Common.getBatchList().remove(0);
                        handleUninstallEvent();
                    }
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();
        if (Common.isUninstall()) {
            uninstallUserApp(Common.getApplicationID());
        } else if (Common.reloadPage()) {
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
        if (mRootShell.rootAccess() && mRootShell != null) mRootShell.closeSU();
    }

}