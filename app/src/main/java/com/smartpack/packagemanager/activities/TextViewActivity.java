/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewManifestAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class TextViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";
    private String mPath;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mExport = findViewById(R.id.export);
        MaterialTextView mTitle = findViewById(R.id.title);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mPath = getIntent().getStringExtra(PATH_INTENT);

        if (mPath != null) {
            mTitle.setText(new File(mPath).getName());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(new RecycleViewManifestAdapter(getData()));
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.export_storage_message, new File(mPath).getName()))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) ->
                        PackageExplorer.copyToStorage(mPath, PackageData.getPackageDir(this) + "/" +
                        Common.getApplicationID(), this)).show());

        mBack.setOnClickListener(v -> finish());
    }

    private List<String> getData() {
        List<String> mData = new ArrayList<>();
        String text;
        if (Common.getApplicationID() != null && PackageExplorer.isBinaryXML(mPath)) {
            text = PackageExplorer.readXMLFromAPK(Common.getSourceDir(), mPath.replace(getCacheDir().getPath() + "/apk/", ""));
        } else {
            text = sUtils.read(new File(mPath));
        }
        if (text != null) {
            mData.addAll(Arrays.asList(text.split("\\r?\\n")));
        }
        return mData;
    }

}