/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
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
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class ImageViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mExport = findViewById(R.id.export);
        AppCompatImageView mImage = findViewById(R.id.image);
        MaterialTextView mTitle = findViewById(R.id.title);

        String path = getIntent().getStringExtra(PATH_INTENT);

        if (path != null) {
            mTitle.setText(new File(path).getName());
            mImage.setImageURI(PackageExplorer.getIconFromPath(path));
        } else {
            mTitle.setText(PackageData.mApplicationName);
            mImage.setImageDrawable(PackageData.mApplicationIcon);
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.export_storage_message, path != null ? new File(path).getName() : PackageData.mApplicationName + " icon"))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> {
                    if (path != null) {
                        PackageExplorer.copyToStorage(path, PackageData.getPackageDir() + "/" +
                                PackageData.mApplicationID, this);
                    } else {
                        PackageExplorer.saveIcon(PackageExplorer.drawableToBitmap(mImage.getDrawable()), PackageData.getPackageDir()
                                + "/" + PackageData.mApplicationName.toString().replace(" ","_") + "_icon.png", this);
                    }
                }).show());

        mBack.setOnClickListener(v -> finish());
    }

}