/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 30, 2021
 */
public class FilePicker {

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

}