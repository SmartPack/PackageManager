/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PackageExploreAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2020
 */
public class PackageExploreActivity extends AppCompatActivity {

    private MaterialTextView mTitle;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private PackageExploreAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_explorer);

        MaterialButton mBack = findViewById(R.id.back);
        MaterialButton mSortButton = findViewById(R.id.sort);
        mTitle = findViewById(R.id.title);
        MaterialTextView mError = findViewById(R.id.error_status);
        mProgressBar = findViewById(R.id.progress);
        mRecyclerView = findViewById(R.id.recycler_view);

        mTitle.setText(Common.getApplicationName());

        mBack.setOnClickListener(v -> {
            sFileUtils.delete(new File(getCacheDir().getPath(), "apk"));
            finish();
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        try {
            mRecycleViewAdapter = new PackageExploreAdapter(FilePicker.getData(mProgressBar, this, false), this);
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(GONE);
            mError.setText(getString(R.string.explore_error_status, Common.getApplicationName()));
            mError.setVisibility(VISIBLE);
        }

        PackageExploreAdapter.setOnItemClickListener((position, v) -> {
            String mPath = FilePicker.getData(mProgressBar, this, false).get(position);
            if (position == 0) {
                backPressedEvent();
            } else if (new File(mPath).isDirectory()) {
                Common.setPath(mPath);
                reload(this);
            } else if (PackageExplorer.isTextFile(mPath)) {
                Intent textView = new Intent(this, TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, mPath);
                textView.putExtra(TextViewActivity.PACKAGE_INTENT, Common.getApplicationID());
                startActivity(textView);
            } else if (PackageExplorer.isImageFile(mPath)) {
                Intent imageView = new Intent(this, ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PATH_INTENT, mPath);
                imageView.putExtra(ImageViewActivity.PACKAGE_INTENT, Common.getApplicationID());
                startActivity(imageView);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(getString(R.string.open_failed_export_message, new File(mPath).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> PackageExplorer
                                .copyToStorage(mPath, Common.getApplicationID(), this)).show();
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
                mProgressBar.setVisibility(VISIBLE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new PackageExploreAdapter(FilePicker.getData(mProgressBar, activity, false), activity);
            }

            @Override
            public void onPostExecute() {
                mTitle.setText(Common.getPath().equals(getCacheDir().toString() + "/apk/") ? Common.getApplicationName()
                        : new File(Common.getPath()).getName());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressBar.setVisibility(GONE);
            }
        }.execute();
    }

    private void backPressedEvent() {
        if (new File(Common.getPath()).equals(new File(getCacheDir().toString(), "apk/"))) {
            sFileUtils.delete(new File(getCacheDir().getPath(),"apk"));
            finish();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            reload(this);
        }
    }

}