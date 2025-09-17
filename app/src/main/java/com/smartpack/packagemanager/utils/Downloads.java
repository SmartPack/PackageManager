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

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class Downloads {

    public static List<String> getData(String searchText, Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : getDownloadList(context)) {
            if (sCommonUtils.getString("downloadTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.getName().endsWith(".apkm")) {
                    if (searchText == null || PackageData.isTextMatched(mFile.getName(), searchText)) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    if (searchText == null || PackageData.isTextMatched(mFile.getName(), searchText)) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            }
        }
        if (sCommonUtils.getBoolean("reverse_order_exports", false, context)) {
            mData.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(rhs, lhs));
        } else {
            mData.sort(String.CASE_INSENSITIVE_ORDER);
        }
        return mData;
    }

    private static File[] getDownloadList(Context context) {
        PackageData.makePackageFolder(context);
        return PackageData.getPackageDir(context).listFiles();
    }

}