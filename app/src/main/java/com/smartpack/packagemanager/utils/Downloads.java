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
import java.util.Collections;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */

public class Downloads {

    private static String mSearchText;

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : getDownloadList(context)) {
            if (Utils.getString("downloadTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.getName().endsWith(".apkm")) {
                    if (mSearchText == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (isTextMatched(mFile.getName())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    if (mSearchText == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (isTextMatched(mFile.getName())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            }
        }
        if (Utils.getBoolean("reverse_order_exports", false, context)) {
            Collections.reverse(mData);
        } else {
            Collections.sort(mData);
        }
        return mData;
    }

    private static boolean isTextMatched(String searchText) {
        for (int a = 0; a < searchText.length() - mSearchText.length() + 1; a++) {
            if (mSearchText.equalsIgnoreCase(searchText.substring(a, a + mSearchText.length()))) {
                return true;
            }
        }
        return false;
    }

    private static File[] getDownloadList(Context context) {
        if (!PackageData.getPackageDir(context).exists()) {
            PackageData.getPackageDir(context).mkdirs();
        }
        return PackageData.getPackageDir(context).listFiles();
    }

    public static String getSearchText() {
        return mSearchText;
    }

    public static void setSearchText(String searchText) {
        mSearchText = searchText;
    }

}