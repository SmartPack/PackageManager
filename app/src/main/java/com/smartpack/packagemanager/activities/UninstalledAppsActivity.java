/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewUninstalledAppsAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 10, 2021
 */
public class UninstalledAppsActivity extends AppCompatActivity {

    private AppCompatEditText mSearchWord;
    private MaterialTextView mTitle;
    private ProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private RecycleViewUninstalledAppsAdapter mRecycleViewAdapter;
    private String mSearchText = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstalled_apps);

        mSearchWord = findViewById(R.id.search_word);
        AppCompatImageButton mSearch = findViewById(R.id.search_icon);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        AppCompatImageButton mSort = findViewById(R.id.sort_icon);
        MaterialCardView mRestore = Common.initializeRestoreCard(findViewById(android.R.id.content), R.id.restore);
        mProgress = findViewById(R.id.progress);
        mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleViewAdapter = new RecycleViewUninstalledAppsAdapter(getData(this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) ->
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.sure_question)
                        .setMessage(getString(R.string.restore_message, getData(this).get(position)))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.restore, (dialog, id) -> restore(position, false,this)).show()
        );

        mSearch.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mTitle.setVisibility(View.VISIBLE);
                Utils.toggleKeyboard(0, mSearchWord, this);
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mTitle.setVisibility(View.GONE);
                Utils.toggleKeyboard(1, mSearchWord, this);
            }
        });

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            Utils.toggleKeyboard(0, mSearchWord, this);
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

        mBack.setOnClickListener(v -> {
            if (mProgress.getVisibility() == View.GONE) {
                Common.getRestoreList().clear();
                finish();
            }
        });

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSort);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                    .setChecked(sUtils.getBoolean("reverse_order", false, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sUtils.saveBoolean("reverse_order", !sUtils.getBoolean("reverse_order", false, this), this);
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
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.sure_question)
                    .setMessage(getString(R.string.restore_message_batch, sb.toString()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.restore, (dialog, id) -> restore(0, true, this)).show();
        });
    }

    private void loadUI() {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new RecycleViewUninstalledAppsAdapter(getData(UninstalledAppsActivity.this));
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
        if (sUtils.getBoolean("reverse_order", false, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private void restore(int position, boolean batch, Context context) {
        new sExecutor() {
            String mOutput = null;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                if (batch) {
                    for (String packageName : Common.getRestoreList()) {
                        Utils.runCommand("cmd package install-existing " + packageName);
                    }
                } else {
                    mOutput = Utils.runAndGetError("cmd package install-existing " + getData(context).get(position));
                }
            }

            @Override
            public void onPostExecute() {
                PackageData.setRawData(context);
                if (batch) {
                    Common.getRestoreCard().setVisibility(View.GONE);
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

    @Override
    public void onBackPressed() {
        if (mSearchText != null) {
            mSearchWord.setText(null);
            mSearchText = null;
            return;
        }
        if (mSearchWord.getVisibility() == View.VISIBLE) {
            mSearchWord.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
            return;
        }
        if (mProgress.getVisibility() == View.VISIBLE) {
            return;
        }
        Common.getRestoreList().clear();
        finish();
    }

}