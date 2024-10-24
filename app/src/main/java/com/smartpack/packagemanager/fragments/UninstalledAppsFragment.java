/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

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
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private AppCompatAutoCompleteTextView mSearchWord;
    private MaterialCardView mRestore;
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
        MaterialButton mSearch = mRootView.findViewById(R.id.search_icon);
        MaterialButton mSort = mRootView.findViewById(R.id.sort_icon);
        mRestore = mRootView.findViewById(R.id.restore);
        mProgress = mRootView.findViewById(R.id.progress);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        Common.getView(requireActivity(), R.id.fab).setVisibility(View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecycleViewAdapter = new UninstalledAppsAdapter(getData(requireActivity()), requireActivity());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) ->
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.sure_question)
                        .setMessage(getString(R.string.restore_message, getData(requireActivity()).get(position)))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.restore, (dialog, id) -> restore(position, false,requireActivity())).show()
        );

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
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("reverse_order", false, requireActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("reverse_order", !sCommonUtils.getBoolean("reverse_order", false, requireActivity()), requireActivity());
                    loadUI();
                }
                return false;
            });
            popupMenu.show();
        });

        mRestore.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (String packageName : Common.getRestoreList()) {
                sb.append("* ").append(packageName).append("\n");
            }
            new MaterialAlertDialogBuilder(requireActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.sure_question)
                    .setMessage(getString(R.string.restore_message_batch, sb.toString()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.restore, (dialog, id) -> restore(0, true, requireActivity())).show();
        });

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
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new UninstalledAppsAdapter(getData(requireActivity()), requireActivity());
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(View.GONE);
            }
        }.execute();
    }

    private List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo packageInfo : packages) {
            if (!sPackageUtils.isPackageInstalled(packageInfo.packageName, context)) {
                if (mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (packageInfo.packageName.contains(mSearchText)) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        if (sCommonUtils.getBoolean("reverse_order", false, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private void restore(int position, boolean batch, Context context) {
        new sExecutor() {
            private RootShell mRootShell = null;
            private ShizukuShell mShizukuShell = null;
            private String mOutput = null;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mRootShell = new RootShell();
                mShizukuShell = new ShizukuShell();
            }

            @Override
            public void doInBackground() {
                if (batch) {
                    for (String packageName : Common.getRestoreList()) {
                        if (mRootShell.rootAccess()) {
                            mRootShell.runCommand("cmd package install-existing " + packageName);
                        } else {
                            mShizukuShell.runCommand("cmd package install-existing " + packageName);
                        }
                    }
                } else {
                    if (mRootShell.rootAccess()) {
                        mOutput = mRootShell.runAndGetError("cmd package install-existing " + getData(context).get(position));
                    } else {
                        mOutput = mShizukuShell.runAndGetOutput("cmd package install-existing " + getData(context).get(position));
                    }
                }
            }

            @Override
            public void onPostExecute() {
                PackageData.setRawData(null, context);
                if (batch) {
                    mRestore.setVisibility(View.GONE);
                    Common.getRestoreList().clear();
                    new MaterialAlertDialogBuilder(context)
                            .setMessage(getString(R.string.restore_success_message))
                            .setPositiveButton(R.string.cancel, (dialog, id) -> {
                            }).show();
                    loadUI();
                } else {
                    new MaterialAlertDialogBuilder(context)
                            .setMessage(mOutput.endsWith("installed for user: 0") ? getString(R.string.restore_success_message) : mOutput)
                            .setPositiveButton(R.string.cancel, (dialog, id) -> {
                            }).show();
                    mRecycleViewAdapter.notifyItemRemoved(position);
                    mProgress.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

}