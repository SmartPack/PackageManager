/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.smartpack.packagemanager.adapters.ExportedAppsAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.Downloads;
import com.smartpack.packagemanager.utils.Flavor;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.tasks.AppBundleTasks;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsFragment extends Fragment {

    private AppCompatAutoCompleteTextView mSearchWord;
    private boolean mRefresh = false;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private ExportedAppsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_exported_apps, container, false);

        mSearchWord = mRootView.findViewById(R.id.search_word);
        MaterialButton mSearch = mRootView.findViewById(R.id.search_icon);
        MaterialButton mSort = mRootView.findViewById(R.id.sort_icon);
        mProgress = mRootView.findViewById(R.id.progress);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);

        Common.getView(requireActivity(), R.id.fab).setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSort);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("reverse_order_exports", false, requireActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("reverse_order_exports", !sCommonUtils.getBoolean("reverse_order_exports", false, requireActivity()), requireActivity());
                    loadUI();
                }
                return false;
            });
            popupMenu.show();
        });

        if (Flavor.isFullVersion() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Utils.isPermissionDenied() || Build.VERSION.SDK_INT <=
                Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
            LinearLayout mPermissionLayout = mRootView.findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = mRootView.findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = mRootView.findViewById(R.id.permission_text);
            mPermissionText.setText(getString(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
                    R.string.file_permission_request_message : R.string.permission_denied_write_storage));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Utils.requestPermission(requireActivity());
                    mRefresh = true;
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            });
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        loadUI();

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("downloadTypes", "apks", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            sCommonUtils.saveString("downloadTypes", "apks", requireActivity());
                            loadUI();
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            sCommonUtils.saveString("downloadTypes", "bundles", requireActivity());
                            loadUI();
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
                Downloads.setSearchText(s.toString());
                loadUI();
            }
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> new MaterialAlertDialogBuilder(requireActivity())
                .setMessage(getString(Downloads.getData(requireActivity()).get(position).endsWith(".apkm") ? R.string.bundle_install_apks
                        : R.string.install_question, new File(Downloads.getData(requireActivity()).get(position)).getName()))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.install, (dialog, id) -> {
                    if (Downloads.getData(requireActivity()).get(position).endsWith(".apkm")) {
                        new AppBundleTasks(mProgress, Downloads.getData(requireActivity()).get(position), false, requireActivity()).execute();
                    } else {
                        Common.getAppList().clear();
                        Common.getAppList().add(Downloads.getData(requireActivity()).get(position));
                        Common.isUpdating(sPackageUtils.isPackageInstalled(sAPKUtils.getPackageName(Downloads.getData(requireActivity()).get(position), requireActivity()), requireActivity()));
                        new SplitAPKsInstallationTasks(requireActivity()).execute();
                    }
                }).show());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Downloads.getSearchText() != null) {
                    mSearchWord.setText(null);
                    Downloads.setSearchText(null);
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }

                Common.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

    private void loadUI() {
        mRecycleViewAdapter = new ExportedAppsAdapter(Downloads.getData(requireActivity()));
        mRecyclerView.setAdapter(mRecycleViewAdapter);
    }

    private int getTabPosition(Activity activity) {
        if (sCommonUtils.getString("downloadTypes", "apks", activity).equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    requireActivity().recreate();
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();
        if (mRefresh) {
            requireActivity().recreate();
            mRefresh = false;
        }
    }

}