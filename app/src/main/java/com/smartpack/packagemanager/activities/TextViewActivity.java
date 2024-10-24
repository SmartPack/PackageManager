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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.ManifestAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

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

        MaterialButton mExport = findViewById(R.id.export);
        Toolbar mTitle = findViewById(R.id.toolbar);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mPath = getIntent().getStringExtra(PATH_INTENT);

        if (mPath != null) {
            mTitle.setTitle(new File(mPath).getName());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(new ManifestAdapter(getData()));
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.export_storage_message, new File(mPath).getName()))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) ->
                        PackageExplorer.copyToStorage(mPath, this)).show());
    }

    private List<String> getData() {
        List<String> mData = new ArrayList<>();
        String text;
        if (Common.getApplicationID() != null && PackageExplorer.isBinaryXML(mPath)) {
            text = PackageExplorer.readXMLFromAPK(mPath, this);
        } else {
            text = sFileUtils.read(new File(mPath));
        }
        if (text != null) {
            mData.addAll(Arrays.asList(text.split("\\r?\\n")));
        }
        return mData;
    }

}