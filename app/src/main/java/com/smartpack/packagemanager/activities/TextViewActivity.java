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
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.ExportSuccessDialog;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class TextViewActivity extends AppCompatActivity {

    public static final String PACKAGE_INTENT = "package", PATH_INTENT = "path";
    private String mPackageName, mPath;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        MaterialButton mExport = findViewById(R.id.export);
        MaterialTextView mTitle = findViewById(R.id.title);
        MaterialTextView mText = findViewById(R.id.text);

        mPackageName = getIntent().getStringExtra(PACKAGE_INTENT);
        mPath = getIntent().getStringExtra(PATH_INTENT);

        if (mPath != null) {
            mTitle.setText(new File(mPath).getName());
            mText.setText(getText());
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.export_storage_message, new File(mPath).getName()))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, this)) {
                        sPermissionUtils.requestPermission(new String[] {
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                this);
                        sCommonUtils.toast(getString(R.string.permission_denied_write_storage), this).show();
                        return;
                    }
                    PackageData.makePackageFolder(this);
                    File parentFile = new File(PackageData.getPackageDir(this), mPackageName);
                    if (!parentFile.exists()) {
                        sFileUtils.mkdir(parentFile);
                    }
                    sFileUtils.create(mText.getText().toString().trim(), new File(parentFile, new File(mPath).getName()));

                    new ExportSuccessDialog(new File(parentFile, new File(mPath).getName()).getAbsolutePath(), this);
                }).show());
    }

    private String getText() {
        String text;
        if (mPackageName != null && PackageExplorer.isBinaryXML(mPath)) {
            text = PackageExplorer.readXMLFromAPK(mPath, this);
        } else {
            text = sFileUtils.read(new File(mPath));
        }
        return text;
    }

}