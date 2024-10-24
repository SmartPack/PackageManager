/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 30, 2021
 */
public class FilePicker {

    public static List<String> getData(Activity activity, boolean supported) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        mData.add("");
        try {
            // Add directories
            for (File mFile : getFilesList()) {
                if (mFile.isDirectory()) {
                    mDir.add(mFile.getAbsolutePath());
                }
            }
            mDir.sort(String.CASE_INSENSITIVE_ORDER);
            if (!sCommonUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mDir);
            }
            mData.addAll(mDir);
            // Add files
            for (File mFile : getFilesList()) {
                if (supported && mFile.isFile() && isSupportedFile(mFile.getAbsolutePath())) {
                    mFiles.add(mFile.getAbsolutePath());
                }
                if (!supported && mFile.isFile()) {
                    mFiles.add(mFile.getAbsolutePath());
                }
            }
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

    private static boolean isSupportedFile(String path) {
        return path.endsWith("apk") || path.endsWith("apks") || path.endsWith("apkm") || path.endsWith("xapk");
    }

    private static File[] getFilesList() {
        if (!Common.getPath().endsWith(File.separator)) {
            Common.setPath(Common.getPath() + File.separator);
        }
        return new File(Common.getPath()).listFiles();
    }

    public static String getLastDirPath(Activity activity) {
        String mDir = sCommonUtils.getString("lastDirPath", Environment.getExternalStorageDirectory().toString(), activity);
        if (sFileUtils.exist(new File(mDir)) && mDir.contains(Environment.getExternalStorageDirectory().toString())) {
            return mDir;
        } else {
            return Environment.getExternalStorageDirectory().toString();
        }
    }

}