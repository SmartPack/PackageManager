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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 17, 2025
 */
public class APKFile extends File {

    private Drawable drawable;
    private String appName,  pkgName, fileSize;

    public APKFile(String apkPath) {
        super(apkPath);
    }

    public APKFile(File apk) {
        super(apk.getAbsolutePath());
    }

    public void load(AppCompatImageButton icon, MaterialTextView name, MaterialTextView packageName, MaterialTextView size) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                PackageManager pm = icon.getContext().getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(getAbsolutePath(), 0);
                fileSize = sAPKUtils.getAPKSize(length());
                if (packageInfo != null) {
                    ApplicationInfo ai = packageInfo.applicationInfo;
                    Objects.requireNonNull(ai).sourceDir = getAbsolutePath();
                    ai.publicSourceDir = getAbsolutePath();
                    drawable = packageInfo.applicationInfo.loadIcon(pm);
                    pkgName = packageInfo.applicationInfo.packageName;
                    appName =  pm.getApplicationLabel(ai).toString();
                }
                handler.post(() -> {
                    if (drawable != null) {
                        icon.setImageDrawable(drawable);
                    }
                    name.setText(appName != null ? appName : getName());
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