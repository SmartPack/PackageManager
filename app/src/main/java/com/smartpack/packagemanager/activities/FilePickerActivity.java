/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.FilePickerAdapter;
import com.smartpack.packagemanager.utils.APKData;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.tasks.AppBundleTasks;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class FilePickerActivity extends AppCompatActivity {

    private MaterialCardView mSelect;
    private MaterialTextView mTitle;
    private ProgressBar mProgress;
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

        if (!Common.getPath().contains(getCacheDir().getPath())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Utils.isPermissionDenied() || Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q &&
                    sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, this)) {
                LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
                MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
                MaterialTextView mPermissionText = findViewById(R.id.permission_text);
                mPermissionText.setText(getString(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? R.string.file_permission_request_message : R.string.permission_denied_write_storage));
                mPermissionLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mPermissionGrant.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Utils.requestPermission(this);
                        finish();
                    } else {
                        sPermissionUtils.requestPermission(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, this);
                    }
                });
                return;
            }
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new FilePickerAdapter(FilePicker.getData(this, true), this);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard) : new File(Common.getPath()).getName());

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            String mPath = FilePicker.getData(this, true).get(position);
            if (position == 0) {
                backPressedEvent();
            } else if (new File(mPath).isDirectory()) {
                Common.setPath(mPath);
                reload(this);
            } else if (mPath.endsWith("apks") || mPath.endsWith("apkm") || mPath.endsWith("xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.bundle_install_apks, new File(mPath).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> new AppBundleTasks(mProgress, mPath, true, this).execute()).show();
            } else if (mPath.endsWith("apk")) {
                if (Common.getAppList().contains(mPath)) {
                    Common.getAppList().remove(mPath);
                } else {
                    Common.getAppList().add(mPath);
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                mSelect.setVisibility(Common.getAppList().isEmpty() ? View.GONE : View.VISIBLE);
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
                            APKData.setAPKFile(new File(Common.getAppList().get(0)));
                            Intent apkDetails = new Intent(FilePickerActivity.this, APKPickerActivity.class);
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
                mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard)
                        : new File(Common.getPath()).getName());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }

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
        } else if (Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator)) {
            exitActivity();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            Common.getAppList().clear();
            reload(this);
        }
    }

}