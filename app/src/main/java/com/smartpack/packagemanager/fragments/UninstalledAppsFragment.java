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
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.SerializableItems.PackageItems;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 10, 2021
 */
public class UninstalledAppsFragment extends Fragment {

    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mBatch, mSearch, mSort;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private UninstalledAppsAdapter mRecycleViewAdapter;
    private List<PackageItems> mData;
    private List<String> mRestoreList = null;

    private RootShell mRootShell = null;
    private ShizukuShell mShizukuShell = null;
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

        mRootShell = new RootShell();
        mShizukuShell = new ShizukuShell();

        loadUI(mSearchText);

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
                loadUI(s.toString().trim());
            }
        });

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSort);
            Menu menu = popupMenu.getMenu();
            if (!mData.isEmpty()) {
                menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                        .setChecked(sCommonUtils.getBoolean("reverse_order", false, requireActivity()));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("reverse_order", !sCommonUtils.getBoolean("reverse_order", false, requireActivity()), requireActivity());
                    loadUI(mSearchText);
                }
                return false;
            });
            popupMenu.show();
        });

        mBatch.setOnClickListener(v -> {
            if (!mRootShell.rootAccess() && !mShizukuShell.isReady()) {
                sCommonUtils.toast(v.getContext().getString(R.string.feature_unavailable_message), v.getContext()).show();
                return;
            }
            restore(v.getContext()).execute();
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

                AppSettings.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

    private void loadUI(String searchTxt) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(VISIBLE);
                if (mRestoreList == null) {
                    mRestoreList = new ArrayList<>();
                }
            }

            @Override
            public void doInBackground() {
                mData = getData(searchTxt, requireActivity());
                mRecycleViewAdapter = new UninstalledAppsAdapter(mData, mRestoreList, mBatch);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }
                mSearchText = searchTxt;
                mSearchWord.setHint(getString(R.string.search_market_message, mRecycleViewAdapter.getItemCount() + " " + getString(R.string.uninstalled_apps)));
                mSearch.setEnabled(mRecycleViewAdapter.getItemCount() >= 5);
                mSort.setEnabled(mRecycleViewAdapter.getItemCount() >= 5);
                mBatch.setVisibility(mRestoreList.isEmpty() ? GONE : VISIBLE);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(GONE);
            }
        }.execute();
    }

    private List<PackageItems> getData(String searchTxt, Context context) {
        mData = new ArrayList<>();
        for (PackageItems packageItems : PackageData.getRemovedPackagesData()) {
            if (searchTxt == null || packageItems.getPackageName().contains(searchTxt)) {
                mData.add(packageItems);
            }
        }
        if (sCommonUtils.getBoolean("reverse_order", false, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private sExecutor restore(Context context) {
        return new sExecutor() {
            private final List<Integer> positions = new ArrayList<>();

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                for (String packageName : mRestoreList) {
                    PackageItems packageItems = PackageData.getRemovedPackagesData().stream()
                            .filter(item -> packageName.equals(item.getPackageName()))
                            .findFirst()
                            .orElse(null);
                    int index = IntStream.range(0, mData.size())
                            .filter(i -> packageName.equals(mData.get(i).getPackageName()))
                            .findFirst()
                            .orElse(-1);
                    if (index != -1) {
                        positions.add(index);
                        mData.remove(packageItems);
                    }
                    if (mRootShell.rootAccess()) {
                        mRootShell.runCommand("cmd package install-existing " + packageName);
                    } else {
                        mShizukuShell.runCommand("cmd package install-existing " + packageName);
                    }
                    if (packageItems != null) {
                        PackageData.getRemovedPackagesData().remove(packageItems);
                        PackageData.getRawData().add(new PackageItems(packageItems.getPackageName(), packageItems.getAppName(), packageItems.getSourceDir(), false, context));
                    }
                }
            }

            @Override
            public void onPostExecute() {
                mRestoreList.clear();
                for (int position : positions) {
                    mRecycleViewAdapter.notifyItemRemoved(position);
                }
                mRecycleViewAdapter.notifyItemRangeChanged(0, mRecycleViewAdapter.getItemCount());
                mProgress.setVisibility(GONE);
                new MaterialAlertDialogBuilder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.restore_success_message))
                        .setPositiveButton(R.string.cancel, (dialog, id) -> {
                        }).show();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRestoreList.clear();
    }

}