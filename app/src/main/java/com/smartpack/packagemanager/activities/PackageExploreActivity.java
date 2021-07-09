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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewExploreAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2020
 */
public class PackageExploreActivity extends AppCompatActivity {

    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewExploreAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packageexplorer);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSortButton = findViewById(R.id.sort);
        mTitle = findViewById(R.id.title);
        MaterialTextView mError = findViewById(R.id.error_status);
        mRecyclerView = findViewById(R.id.recycler_view);

        mTitle.setText(Common.getApplicationName());

        mBack.setOnClickListener(v -> {
            Utils.delete(getCacheDir().getPath() + "/apk");
            super.onBackPressed();
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        try {
            mRecycleViewAdapter = new RecycleViewExploreAdapter(FilePicker.getData(this, false));
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, Common.getApplicationName()));
            mError.setVisibility(View.VISIBLE);
        }

        RecycleViewExploreAdapter.setOnItemClickListener((position, v) -> {
            String mPath = FilePicker.getData(this, false).get(position);
            if (new File(mPath).isDirectory()) {
                Common.setPath(mPath);
                reload(this);
            } else if (PackageExplorer.isTextFile(mPath)) {
                Intent textView = new Intent(this, TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, mPath);
                startActivity(textView);
            } else if (PackageExplorer.isImageFile(mPath)) {
                Intent imageView = new Intent(this, ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PATH_INTENT, mPath);
                startActivity(imageView);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.open_failed_export_message, new File(mPath).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> PackageExplorer
                                .copyToStorage(mPath, PackageData.getPackageDir(this) + "/" +
                                        Common.getApplicationID(),this)).show();
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
    }

    @SuppressLint("StaticFieldLeak")
    private void reload(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewExploreAdapter(FilePicker.getData(activity, false));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mTitle.setText(Common.getPath().equals(getCacheDir().toString() + "/apk/") ? Common.getApplicationName()
                        : new File(Common.getPath()).getName());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if (Common.getPath().equals(getCacheDir().toString() + "/apk/")) {
            Utils.delete(getCacheDir().getPath() + "/apk");
            finish();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            reload(this);
        }
    }

}