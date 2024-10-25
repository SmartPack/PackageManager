/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.ProgressBar;

import com.smartpack.packagemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */
public class PackageData {

    private static List<PackageItems> mRawData = null;

    public static int getSortingType(Context context) {
        return sCommonUtils.getInt("sort_apps", 0, context);
    }

    public static void makePackageFolder(Context context) {
        if (getPackageDir(context).exists() && getPackageDir(context).isFile()) {
            sFileUtils.delete(getPackageDir(context));
        }
        sFileUtils.mkdir(getPackageDir(context));
    }

    private static List<PackageItems> getRawData(ProgressBar progressBar, Context context) {
        List<PackageItems> mRawData = new ArrayList<>();
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo: packages) {
            mRawData.add(new PackageItems(
                    packageInfo.packageName,
                    getAppName(packageInfo.packageName, context),
                    sPackageUtils.getAppIcon(packageInfo.packageName, context),
                    new File(sPackageUtils.getSourceDir(packageInfo.packageName, context)).length(),
                    Objects.requireNonNull(getPackageInfo(packageInfo.packageName, context)).firstInstallTime,
                    Objects.requireNonNull(getPackageInfo(packageInfo.packageName, context)).lastUpdateTime)
            );
            if (progressBar != null) {
                if (progressBar.isIndeterminate()) {
                    progressBar.setIndeterminate(false);
                }
                progressBar.setMax(packages.size());
                if (progressBar.getProgress() < packages.size()) {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                } else {
                    progressBar.setProgress(0);
                }
            }
        }
        return mRawData;
    }

    public static List<PackageItems> getData(Context context) {
        boolean mAppType;
        List<PackageItems> mData = new ArrayList<>();
        for (PackageItems item : getRawData()) {
            if (sCommonUtils.getString("appTypes", "all", context).equals("system")) {
                mAppType = (sPackageUtils.isSystemApp(item.getPackageName(), context));
            } else if (sCommonUtils.getString("appTypes", "all", context).equals("user")) {
                mAppType = (!sPackageUtils.isSystemApp(item.getPackageName(), context));
            } else {
                mAppType = true;
            }
            if (mAppType && item.getPackageName().contains(".")) {
                if (Common.getSearchText() == null) {
                    mData.add(item);
                } else if (Common.isTextMatched(item.getAppName())
                        || Common.isTextMatched(item.getPackageName())) {
                    mData.add(item);
                }
            }
            if (PackageData.getSortingType(context) == 0) {
                mData.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAppName(), rhs.getAppName()));
            } else if (PackageData.getSortingType(context) == 4) {
                mData.sort(Comparator.comparingLong(PackageItems::getAPKSize));
            } else if (PackageData.getSortingType(context) == 2) {
                mData.sort(Comparator.comparingLong(PackageItems::getInstalledTime));
            } else if (PackageData.getSortingType(context) == 3) {
                mData.sort(Comparator.comparingLong(PackageItems::getUpdatedTime));
            } else {
                mData.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));
            }
        }
        if (sCommonUtils.getBoolean("reverse_order", false, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getAppName(String packageName, Context context) {
        return sPackageUtils.getAppName(packageName, context) + (sPackageUtils.isEnabled(packageName, context) ? "" : " (Disabled)");
    }

    public static String getFileName(String packageName, Context context) {
        if (sCommonUtils.getString("exportedAPKName", context.getString(R.string.package_id), context).equals(context.getString(R.string.name))) {
            return getAppName(packageName, context);
        } else {
            return packageName;
        }
    }

    public static File getPackageDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Utils.isPermissionDenied()) {
            return context.getExternalFilesDir("");
        } else {
            return new File(Environment.getExternalStorageDirectory(), "Package_Manager");
        }
    }

    public static void clearAppSettings(String packageID) {
        if (new RootShell().rootAccess()) {
            new RootShell().runCommand("pm clear " + packageID);
        } else {
            new ShizukuShell().runCommand("pm clear " + packageID);
        }
    }

    public static String getBundleSize(String path) {
        long size = 0;
        for (String mSplit : SplitAPKInstaller.splitApks(path)) {
            size += new File(path, mSplit).length() / 1024;
        }
        long decimal = (size - 1024) / 1024;
        if (size > 1024) {
            return size / 1024 + "." + decimal + " MB";
        } else {
            return size  + " KB";
        }
    }

    public static String showBatchList() {
        StringBuilder sb = new StringBuilder();
        for (String s : Common.getBatchList()) {
            if (s != null && !s.isEmpty())
                sb.append(" - ").append(s.replaceAll(","," ")).append("\n");
        }
        return "\n" + sb;
    }

    public static List<PackageItems> getRawData() {
        return mRawData;
    }

    public static void setRawData(ProgressBar progressBar, Context context) {
        mRawData = getRawData(progressBar, context);
    }

    public static void setSortingType(int value, Context context) {
        sCommonUtils.saveInt("sort_apps", value, context);
    }

}