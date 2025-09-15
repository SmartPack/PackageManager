/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.SerializableItems;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class PackageItems implements Serializable {

    private final long mAPKSize, mInstalledTime, mUpdatedTime;
    private final String mPackageName, mAppName;

    public PackageItems(String packageName, String appName, long apkSize, long installedTime, long updatedTime) {
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mAPKSize = apkSize;
        this.mInstalledTime = installedTime;
        this.mUpdatedTime = updatedTime;
    }

    public Intent launchIntent(Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(mPackageName);
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public long getAPKSize() {
        return mAPKSize;
    }

    public long getInstalledTime() {
        return mInstalledTime;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    private Drawable getAppIcon(Context context) {
        return sPackageUtils.getAppIcon(mPackageName, context);
    }

    public void loadAppIcon(ImageView view) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                Drawable drawable = getAppIcon(view.getContext());

                handler.post(() -> view.setImageDrawable(drawable));
            });
        }
    }

}