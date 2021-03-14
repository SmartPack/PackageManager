/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */

public class Downloads {

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        mData.clear();
        for (File mFile : getDownloadList()) {
            if (Utils.getString("downloadTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.getName().endsWith(".apkm")) {
                    mData.add(mFile.getAbsolutePath());
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        }
        return mData;
    }

    private static File[] getDownloadList() {
        if (!Utils.exist(PackageData.getPackageDir())) {
            Utils.mkdir(PackageData.getPackageDir());
        }
        return new File(PackageData.getPackageDir()).listFiles();
    }

    public static String getAppName(String packageName, Context context) {
        return PackageData.getPackageManager(context).getApplicationLabel(Objects.requireNonNull(
                PackageData.getAppInfo(packageName, context))).toString();
    }

}