/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.FilePickerAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class FilePickerActivity extends AppCompatActivity {

    private ContentLoadingProgressBar mProgress;
    private MaterialButton mSelect;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private FilePickerAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        MaterialButton mBack = findViewById(R.id.back);
        MaterialButton mSortButton = findViewById(R.id.sort);
        mProgress = findViewById(R.id.progress);
        mTitle = findViewById(R.id.title);
        mSelect = findViewById(R.id.select);
        mRecyclerView = findViewById(R.id.recycler_view);

        mBack.setOnClickListener(v -> exitActivity());

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new FilePickerAdapter(FilePicker.getData(this, true), this);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(new File(Common.getPath()).equals(getCacheDir()) ? getString(R.string.split_apk) : new File(Common.getPath()).getName());

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            String mPath = FilePicker.getData(this, true).get(position);
            if (position == 0) {
                backPressedEvent();
            } else if (new File(mPath).isDirectory()) {
                Common.setPath(mPath);
                reload(this);
            } else {
                sCommonUtils.snackBar(mRecyclerView, getString(R.string.wrong_extension, ".apks/.apkm/.xapk")).show();
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, "A-Z").setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, this), this);
                reload(this);
                return false;
            });
            popupMenu.show();
        });

        mSelect.setOnClickListener(v ->
                new sExecutor() {

                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        for (String mAPKs : Common.getAppList()) {
                            if (sAPKUtils.getPackageName(mAPKs, FilePickerActivity.this) != null) {
                                Common.setApplicationID(Objects.requireNonNull(sAPKUtils.getPackageName(mAPKs, FilePickerActivity.this)));
                            }
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        if (Common.getAppList().size() == 1) {
                            Intent apkDetails = new Intent(FilePickerActivity.this, APKPickerActivity.class);
                            apkDetails.putExtra(APKPickerActivity.PATH_INTENT, Common.getAppList().get(0));
                            apkDetails.putExtra(APKPickerActivity.NAME_INTENT, new File(Common.getAppList().get(0)).getName());
                            startActivity(apkDetails);
                            exitActivity();
                        } else {
                            Common.isUpdating(sPackageUtils.isPackageInstalled(Common.getApplicationID(), FilePickerActivity.this));
                            if (Common.getApplicationID() != null) {
                                new SplitAPKsInstallationTasks(FilePickerActivity.this).execute();
                                exitActivity();
                            } else {
                                sCommonUtils.snackBar(mRecyclerView, getString(R.string.installation_status_bad_apks)).show();
                            }
                        }
                    }
                }.execute()
        );

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressedEvent();
            }
        });
    }

    private void reload(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                FilePicker.getData(activity, true).clear();
                mProgress.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new FilePickerAdapter(FilePicker.getData(activity, true), activity);
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mTitle.setText(new File(Common.getPath()).equals(getCacheDir()) ? getString(R.string.split_apk) : new File(Common.getPath()).getName());
                if (Common.getAppList().isEmpty()) {
                    mSelect.setVisibility(View.GONE);
                } else {
                    mSelect.setVisibility(View.VISIBLE);
                }
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private void exitActivity() {
        if (!Common.getPath().contains(getCacheDir().getPath())) {
            sCommonUtils.saveString("lastDirPath", Common.getPath(), this);
        }
        finish();
    }

    private void backPressedEvent() {
        if (mProgress.getVisibility() == View.VISIBLE) return;
        if (new File(Common.getPath()).equals(getCacheDir())) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> finish()).show();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            Common.getAppList().clear();
            reload(this);
        }
    }

}