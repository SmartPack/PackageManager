/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class ImageViewActivity extends AppCompatActivity {

    public static final String APP_NAME_INTENT = "app_name", PACKAGE_INTENT = "package", PATH_INTENT = "path";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        AppCompatImageView mImage = findViewById(R.id.image);
        MaterialButton mExport = findViewById(R.id.export);
        MaterialTextView mTitle = findViewById(R.id.text);

        String appName = getIntent().getStringExtra(APP_NAME_INTENT);
        String packageName = getIntent().getStringExtra(PACKAGE_INTENT);
        String path = getIntent().getStringExtra(PATH_INTENT);

        if (path != null) {
            mTitle.setText(new File(path).getName());
            mImage.setImageURI(PackageExplorer.getIconFromPath(path));
        } else {
            mTitle.setText(appName);
            try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    Drawable drawable = sPackageUtils.getAppIcon(packageName, this);

                    handler.post(() -> mImage.setImageDrawable(drawable));
                });
            }
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.export_storage_message, path != null ? new File(path).getName() : appName + " icon"))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> {
                    if (path != null) {
                        PackageExplorer.copyToStorage(path, packageName, this);
                    } else {
                        PackageExplorer.saveIcon(PackageExplorer.drawableToBitmap(mImage.getDrawable()), appName + "_icon.png", packageName, this);
                    }
                }).show());
    }

}