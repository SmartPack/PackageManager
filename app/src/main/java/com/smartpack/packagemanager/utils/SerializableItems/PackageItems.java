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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class PackageItems implements Serializable {

    private final long mAPKSize;
    private final String mPackageName, mAppName;
    private final Context mContext;

    public PackageItems(String packageName, String appName, long apkSize, Context context) {
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mAPKSize = apkSize;
        this.mContext = context;
    }

    public Intent launchIntent() {
        return mContext.getPackageManager().getLaunchIntentForPackage(mPackageName);
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
        return Objects.requireNonNull(getPackageInfo(getPackageName(), mContext)).firstInstallTime;
    }

    public long getUpdatedTime() {
        return Objects.requireNonNull(getPackageInfo(getPackageName(), mContext)).firstInstallTime;
    }

    private Drawable getAppIcon() {
        return sPackageUtils.getAppIcon(mPackageName, mContext);
    }

    private static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    public void loadAppIcon(ImageView view) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                Drawable drawable = getAppIcon();

                handler.post(() -> view.setImageDrawable(drawable));
            });
        }
    }

}