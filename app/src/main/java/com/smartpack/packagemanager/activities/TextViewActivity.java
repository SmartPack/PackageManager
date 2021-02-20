/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class TextViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mExport = findViewById(R.id.export);
        MaterialTextView mTitle = findViewById(R.id.title);
        MaterialTextView mText = findViewById(R.id.text);

        String path = getIntent().getStringExtra(PATH_INTENT);

        assert path != null;
        mTitle.setText(new  File(path).getName());
        mText.setText(Utils.read(path));

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.export_storage_message, new File(path).getName()))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> {
                    PackageExplorer.copyToStorage(path, PackageData.getPackageDir(this) + "/" +
                            PackageData.mApplicationID, this);
                }).show());

        mBack.setOnClickListener(v -> finish());
    }

}