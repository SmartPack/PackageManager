/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 17, 2025
 */
public class APKFile extends File {

    private Drawable drawable;
    private String fileName,  pkgName, fileSize;

    public APKFile(String apkPath) {
        super(apkPath);
    }

    public void load(AppCompatImageButton icon, MaterialTextView name, MaterialTextView packageName, MaterialTextView size) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                PackageInfo packageInfo = icon.getContext().getPackageManager().getPackageArchiveInfo(getAbsolutePath(), 0);
                fileName = getName();
                fileSize = sAPKUtils.getAPKSize(length());
                if (packageInfo != null) {
                    drawable = packageInfo.applicationInfo.loadIcon(icon.getContext().getPackageManager());
                    pkgName = packageInfo.applicationInfo.packageName;
                }
                handler.post(() -> {
                    if (drawable != null) {
                        icon.setImageDrawable(drawable);
                    }
                    name.setText(fileName);
                    if (pkgName != null) {
                        packageName.setText(pkgName);
                        packageName.setVisibility(VISIBLE);
                    } else {
                        packageName.setVisibility(GONE);
                    }
                    size.setText(fileSize);
                });
            });
        }
    }

}