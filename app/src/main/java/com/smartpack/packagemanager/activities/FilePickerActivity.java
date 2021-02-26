/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.smartpack.packagemanager.adapters.RecycleViewFilePickerAdapter;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.SplitAPKInstallerNoRoot;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class FilePickerActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private LinearLayout mProgressLayout;
    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewFilePickerAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        PackageExplorer.mSelect = findViewById(R.id.select);
        mRecyclerView = findViewById(R.id.recycler_view);
        mProgressLayout = findViewById(R.id.progress_layout);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewFilePickerAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(PackageData.mPath.equals("/storage/emulated/0/") ? getString(R.string.sdcard) : new File(PackageData.mPath).getName());

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                PackageData.mPath = mData.get(position);
                reload();
            } else if (mData.get(position).endsWith(".apks") || mData.get(position).endsWith(".apkm") || mData.get(position).endsWith(".xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.bundle_install_apks, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                            if (Utils.rootAccess()) {
                                SplitAPKInstaller.handleAppBundle(mProgressLayout, mData.get(position), this);
                            } else {
                                SplitAPKInstallerNoRoot.handleAppBundle(mProgressLayout, mData.get(position), this);
                            }
                            finish();
                        }).show();
            } else if (mData.get(position).endsWith(".apk")) {
                if (PackageExplorer.mAPKList.contains(mData.get(position))) {
                    PackageExplorer.mAPKList.remove(mData.get(position));
                } else {
                    PackageExplorer.mAPKList.add(mData.get(position));
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                PackageExplorer.mSelect.setVisibility(PackageExplorer.mAPKList.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                Utils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apks/.apkm/.xapk"));
            }
        });

        PackageExplorer.mSelect.setOnClickListener(v -> {
            if (Utils.rootAccess()) {
                SplitAPKInstaller.installSplitAPKs(null, this);
            } else {
                SplitAPKInstallerNoRoot.installSplitAPKs(this);
            }
            finish();
        });

        mBack.setOnClickListener(v -> {
            super.onBackPressed();
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
                if (mFile.isFile() && isSupportedFile(mFile.getAbsolutePath())) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        } catch (NullPointerException ignored) {
            finish();
        }
        return mData;
    }

    private boolean isSupportedFile(String path) {
        return path.endsWith(".apk") || path.endsWith(".apks") || path.endsWith(".apkm") || path.endsWith(".xapk");
    }

    private File[] getFilesList() {
        if (!PackageData.mPath.endsWith(File.separator)) {
            PackageData.mPath = PackageData.mPath + File.separator;
        }
        return new File(PackageData.mPath).listFiles();
    }

    private void reload() {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mData.clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewFilePickerAdapter(getData());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mTitle.setText(PackageData.mPath.equals("/storage/emulated/0/") ? getString(R.string.sdcard)
                                    : new File(PackageData.mPath).getName());
                            if (PackageExplorer.mAPKList.isEmpty()) {
                                PackageExplorer.mSelect.setVisibility(View.GONE);
                            } else {
                                PackageExplorer.mSelect.setVisibility(View.VISIBLE);
                            }
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onBackPressed() {
        if (PackageData.mPath.equals(getCacheDir().getPath() + "/splits/")) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        finish();
                    }).show();
        } else if (PackageData.mPath.equals("/storage/emulated/0/")) {
            super.onBackPressed();
        } else {
            PackageData.mPath = Objects.requireNonNull(new File(PackageData.mPath).getParentFile()).getPath();
            PackageExplorer.mAPKList.clear();
            reload();
        }
    }

}