/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 19, 2020
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class FilePicker {

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getAPKIcon(String apkPath, Context context) {
        PackageInfo pi = PackageData.getPackageManager(context).getPackageArchiveInfo(apkPath, 0);
        if (pi != null) {
            return pi.applicationInfo.loadIcon(PackageData.getPackageManager(context));
        } else {
            return null;
        }
    }
}