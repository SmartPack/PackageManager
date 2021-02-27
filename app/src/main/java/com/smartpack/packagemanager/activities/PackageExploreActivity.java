/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewExploreAdapter;
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

    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewExploreAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packageexplorer);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        MaterialTextView mError = findViewById(R.id.error_status);
        LinearLayout mProgressLayout = findViewById(R.id.progress_layout);
        mRecyclerView = findViewById(R.id.recycler_view);

        mTitle.setText(PackageData.mApplicationName);

        mBack.setOnClickListener(v -> {
            Utils.delete(getCacheDir().getPath() + "/apk");
            super.onBackPressed();
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        try {
            mRecycleViewAdapter = new RecycleViewExploreAdapter(getData());
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, PackageData.mApplicationName));
            mError.setVisibility(View.VISIBLE);
        }

        RecycleViewExploreAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                PackageData.mPath = mData.get(position);
                reload();
            } else if (PackageExplorer.isTextFile(mData.get(position))) {
                Intent textView = new Intent(this, TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                startActivity(textView);
            } else if (PackageExplorer.isImageFile(mData.get(position))) {
                Intent imageView = new Intent(this, ImageViewActivity.class);
                imageView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                startActivity(imageView);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.open_failed_export_message, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> PackageExplorer
                                .copyToStorage(mData.get(position), PackageData.getPackageDir() + "/" +
                                        PackageData.mApplicationID,this)).show();
            }
        });
    }

    private List<String> getData() {
        try {
            mData.clear();
            // Add directories
            for (File mFile : getFilesList()) {
                if (mFile.isDirectory()) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
            // Add files
            for (File mFile : getFilesList()) {
                if (mFile.isFile()) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        } catch (NullPointerException ignored) {
            finish();
        }
        return mData;
    }

    private File[] getFilesList() {
        if (!PackageData.mPath.endsWith(File.separator)) {
            PackageData.mPath = PackageData.mPath + File.separator;
        }
        return new File(PackageData.mPath).listFiles();
    }

    @SuppressLint("StaticFieldLeak")
    private void reload() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewExploreAdapter(getData());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mTitle.setText(PackageData.mPath.equals(getCacheDir().toString() + "/apk/") ? PackageData.mApplicationName
                        : new File(PackageData.mPath).getName());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if (PackageData.mPath.equals(getCacheDir().toString() + "/apk/")) {
            Utils.delete(getCacheDir().getPath() + "/apk");
            finish();
        } else {
            PackageData.mPath = Objects.requireNonNull(new File(PackageData.mPath).getParentFile()).getPath();
            reload();
        }
    }

}