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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class PackageItems implements Serializable {

    private boolean mSystemApp, mUpdatedSystemApp, mUserApp;
    private Drawable mAppIcon;
    private final boolean removed;
    private final String mPackageName, mAppName, mAPKPath;
    private final Context mContext;

    public PackageItems(String packageName, String appName, String apkPath, boolean removed, Context context) {
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mAPKPath = apkPath;
        this.removed = removed;
        this.mContext = context;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean isSystemApp() {
        return mSystemApp;
    }

    public boolean isUserApp() {
        return mUserApp;
    }

    public Intent launchIntent() {
        return mContext.getPackageManager().getLaunchIntentForPackage(mPackageName);
    }

    public String getSourceDir() {
        return mAPKPath;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public long getAPKSize() {
        return new File(mAPKPath).length();
    }

    public long getInstalledTime() {
        return Objects.requireNonNull(getPackageInfo(getPackageName(), mContext)).firstInstallTime;
    }

    public long getUpdatedTime() {
        return Objects.requireNonNull(getPackageInfo(getPackageName(), mContext)).lastUpdateTime;
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

                PackageManager pm = view.getContext().getPackageManager();
                ApplicationInfo ai = null;

                try {
                    if (!removed) {
                        ai = pm.getApplicationInfo(mPackageName, 0);
                    } else {
                        PackageInfo pi = pm.getPackageArchiveInfo(mAPKPath, 0);
                        if (pi != null) {
                            ai = pi.applicationInfo;
                            Objects.requireNonNull(ai).sourceDir = mAPKPath;
                            ai.publicSourceDir = mAPKPath;
                        }
                    }

                    if (ai != null) {
                        mAppIcon = pm.getApplicationIcon(ai);
                        mSystemApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                        mUpdatedSystemApp = (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
                        mUserApp = !mSystemApp && !mUpdatedSystemApp;
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }

                handler.post(() -> view.setImageDrawable(mAppIcon));
            });
        }
    }

}