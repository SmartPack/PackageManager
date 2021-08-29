/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */

public class RecycleViewItem implements Serializable {

    private final Drawable mIcon;
    private final long mAPKSize;
    private final String mPackageName, mAppName;

    public RecycleViewItem(String packageName, String appName, Drawable icon, long apkSize) {
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mIcon = icon;
        this.mAPKSize = apkSize;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public long getAPKSize() {
        return mAPKSize;
    }

}