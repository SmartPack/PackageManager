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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
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

        MaterialButton mExport = findViewById(R.id.export);
        AppCompatImageView mImage = findViewById(R.id.image);
        Toolbar mTitle = findViewById(R.id.toolbar);

        String path = getIntent().getStringExtra(PATH_INTENT);

        if (path != null) {
            mTitle.setTitle(new File(path).getName());
            mImage.setImageURI(PackageExplorer.getIconFromPath(path));
        } else {
            mTitle.setTitle(Common.getApplicationName());
            mImage.setImageDrawable(Common.getApplicationIcon());
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.export_storage_message, path != null ? new File(path).getName() : Common.getApplicationName() + " icon"))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> {
                    if (path != null) {
                        PackageExplorer.copyToStorage(path, this);
                    } else {
                        PackageExplorer.saveIcon(PackageExplorer.drawableToBitmap(mImage.getDrawable()), Common.getApplicationName() + "_icon.png", this);
                    }
                }).show());
    }

}