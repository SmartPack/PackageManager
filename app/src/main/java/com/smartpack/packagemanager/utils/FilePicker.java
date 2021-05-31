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

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 30, 2021
 */
public class FilePicker {

    public static List<String> getData(Activity activity, boolean supported) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        try {
            // Add directories
            for (File mFile : getFilesList()) {
                if (mFile.isDirectory()) {
                    mDir.add(mFile.getAbsolutePath());
                }
            }
            Collections.sort(mDir, String.CASE_INSENSITIVE_ORDER);
            if (!Utils.getBoolean("az_order", true, activity)) {
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
            Collections.sort(mFiles, String.CASE_INSENSITIVE_ORDER);
            if (!Utils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mFiles);
            }
            mData.addAll(mFiles);
        } catch (NullPointerException ignored) {
            activity.finish();
        }
        return mData;
    }

    private static boolean isSupportedFile(String path) {
        return getExtFromPath(path).equals("apk") || getExtFromPath(path).equals("apks") || getExtFromPath(path)
                .equals("apkm") || getExtFromPath(path).equals("xapk");
    }

    private static File[] getFilesList() {
        if (!Common.getPath().endsWith(File.separator)) {
            Common.setPath(Common.getPath() + File.separator);
        }
        return new File(Common.getPath()).listFiles();
    }

    public static String getExtFromPath(String path) {
        return android.webkit.MimeTypeMap.getFileExtensionFromUrl(path);
    }

}