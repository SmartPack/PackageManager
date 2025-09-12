/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 30, 2021
 */
public class FilePicker {

    public static boolean isSelectedAPK(File file, Activity activity) {
        return file.getName().endsWith(".apk") && file.getName().contains(Build.SUPPORTED_ABIS[0].replace("-","_"))
                || file.getName().contains(Locale.getDefault().getLanguage()) || file.getName().contains("base.apk")
                || file.getName().contains(FilePicker.getScreenDensity(activity));
    }

    public static List<String> getData(Activity activity, boolean splits) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        mData.add("");
        try {
            for (File mFile : Objects.requireNonNull(new File(Common.getPath()).listFiles())) {
                if (mFile.isDirectory()) {
                    // Add directories
                    mDir.add(mFile.getAbsolutePath());
                } else if (!splits || mFile.getName().endsWith(".apk")) {
                    // Add files
                    mFiles.add(mFile.getAbsolutePath());
                }
            }
            mDir.sort(String.CASE_INSENSITIVE_ORDER);
            if (!sCommonUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mDir);
            }
            mData.addAll(mDir);
            mFiles.sort(String.CASE_INSENSITIVE_ORDER);
            if (!sCommonUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mFiles);
            }
            mData.addAll(mFiles);
        } catch (NullPointerException ignored) {
            activity.finish();
        }
        return mData;
    }

    private static String getScreenDensity(Context context) {
        int screenDPI = context.getResources().getDisplayMetrics().densityDpi;
        if (screenDPI <= 140) {
            return "ldpi";
        } else if (screenDPI <= 200) {
            return "mdpi";
        } else if (screenDPI <= 280) {
            return "hdpi";
        } else if (screenDPI <= 400) {
            return "xhdpi";
        } else if (screenDPI <= 560) {
            return "xxhdpi";
        } else {
            return "xxxhdpi";
        }
    }

}