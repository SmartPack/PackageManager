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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewFilePickerAdapter;
import com.smartpack.packagemanager.utils.AsyncTasks;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class FilePickerActivity extends AppCompatActivity {

    private LinearLayout mProgressLayout;
    private MaterialCardView mSelect;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewFilePickerAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSortButton = findViewById(R.id.sort);
        mProgressLayout = findViewById(R.id.progress_layout);
        mTitle = findViewById(R.id.title);
        mSelect = Common.initializeSelectCard(findViewById(android.R.id.content), R.id.select);
        mRecyclerView = findViewById(R.id.recycler_view);

        mBack.setOnClickListener(v -> super.onBackPressed());

        if (Build.VERSION.SDK_INT >= 30 && Utils.isPermissionDenied() || Build.VERSION.SDK_INT < 30 && Utils.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(getString(Build.VERSION.SDK_INT >= 30 ? R.string.file_permission_request_message : R.string.permission_denied_write_storage));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    Utils.requestPermission(this);
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
            });
            return;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewFilePickerAdapter(FilePicker.getData(this, true));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard) : new File(Common.getPath()).getName());

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            String mPath = FilePicker.getData(this, true).get(position);
            if (new File(mPath).isDirectory()) {
                Common.setPath(mPath);
                reload(this);
            } else if (FilePicker.getExtFromPath(mPath).equals("apks") || FilePicker.getExtFromPath(mPath).equals("apkm")
                    || FilePicker.getExtFromPath(mPath).equals("xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.bundle_install_apks, new File(mPath).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                            SplitAPKInstaller.handleAppBundle(mProgressLayout, mPath, this);
                            finish();
                        }).show();
            } else if (FilePicker.getExtFromPath(mPath).equals("apk")) {
                if (Common.getAppList().contains(mPath)) {
                    Common.getAppList().remove(mPath);
                } else {
                    Common.getAppList().add(mPath);
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                mSelect.setVisibility(Common.getAppList().isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apks/.apkm/.xapk"));
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, "A-Z").setCheckable(true)
                    .setChecked(Utils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                Utils.saveBoolean("az_order", !Utils.getBoolean("az_order", true, this), this);
                reload(this);
                return false;
            });
            popupMenu.show();
        });

        mSelect.setOnClickListener(v -> {
            SplitAPKInstaller.installSplitAPKs(this);
            finish();
        });
    }

    private void reload(Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                FilePicker.getData(activity, true).clear();
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new RecycleViewFilePickerAdapter(FilePicker.getData(activity, true));
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
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }

    }

    @Override
    public void onBackPressed() {
        if (Common.getPath().equals(getCacheDir().getPath() + "/splits/")) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> finish()).show();
        } else if (Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator)) {
            super.onBackPressed();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            Common.getAppList().clear();
            reload(this);
        }
    }

}