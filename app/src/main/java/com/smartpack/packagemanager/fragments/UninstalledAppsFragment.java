/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.UninstalledAppsAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 10, 2021
 */
public class UninstalledAppsFragment extends Fragment {

    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mBatch, mSearch, mSort;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private UninstalledAppsAdapter mRecycleViewAdapter;
    private String mSearchText = null;

    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_uninstalled_apps, container, false);

        mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatch = mRootView.findViewById(R.id.batch);
        mSearch = mRootView.findViewById(R.id.search_icon);
        mSort = mRootView.findViewById(R.id.sort_icon);
        mProgress = mRootView.findViewById(R.id.progress);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        requireActivity().findViewById(R.id.fab).setVisibility(View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        loadUI();

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
                mSearchText = s.toString();
                loadUI();
            }
        });

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSort);
            Menu menu = popupMenu.getMenu();
            if (!getData(requireActivity()).isEmpty()) {
                menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                        .setChecked(sCommonUtils.getBoolean("reverse_order", false, requireActivity()));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("reverse_order", !sCommonUtils.getBoolean("reverse_order", false, requireActivity()), requireActivity());
                    loadUI();
                }
                return false;
            });
            popupMenu.show();
        });

        mBatch.setOnClickListener(v -> restore(v.getContext()).execute());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mSearchText != null) {
                    mSearchWord.setText(null);
                    mSearchText = null;
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                Common.getRestoreList().clear();

                Common.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

    private void loadUI() {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(VISIBLE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new UninstalledAppsAdapter(getData(requireActivity()), requireActivity());
            }

            @Override
            public void onPostExecute() {
                mSearch.setEnabled(mRecycleViewAdapter.getItemCount() >= 5);
                mSort.setEnabled(mRecycleViewAdapter.getItemCount() >= 5);
                mBatch.setVisibility(Common.getRestoreList().isEmpty() ? GONE : VISIBLE);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(GONE);
            }
        }.execute();
    }

    private List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        if (mProgress != null) {
            if (mProgress.isIndeterminate()) {
                mProgress.setIndeterminate(false);
            }
            mProgress.setMax(packages.size());
        }
        for (ApplicationInfo packageInfo : packages) {
            if (!sPackageUtils.isPackageInstalled(packageInfo.packageName, context)) {
                if (mSearchText == null || packageInfo.packageName.contains(mSearchText)) {
                    mData.add(packageInfo.packageName);
                }
            }
            if (mProgress != null) {
                if (mProgress.getProgress() < packages.size()) {
                    mProgress.setProgress(mProgress.getProgress() + 1);
                } else {
                    mProgress.setProgress(0);
                }
            }
        }
        if (sCommonUtils.getBoolean("reverse_order", false, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private sExecutor restore(Context context) {
        return new sExecutor() {
            private RootShell mRootShell = null;
            private ShizukuShell mShizukuShell = null;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mRootShell = new RootShell();
                mShizukuShell = new ShizukuShell();
            }

            @Override
            public void doInBackground() {
                for (String packageName : Common.getRestoreList()) {
                    if (mRootShell.rootAccess()) {
                        mRootShell.runCommand("cmd package install-existing " + packageName);
                    } else {
                        mShizukuShell.runCommand("cmd package install-existing " + packageName);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                PackageData.setRawData(null, context);
                Common.getRestoreList().clear();
                new MaterialAlertDialogBuilder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.restore_success_message))
                        .setPositiveButton(R.string.cancel, (dialog, id) -> {
                        }).show();
                loadUI();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Common.getRestoreList().clear();
    }

}