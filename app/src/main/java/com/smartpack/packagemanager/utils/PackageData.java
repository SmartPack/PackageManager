/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;

import com.smartpack.packagemanager.R;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageData {

    private static List<RecycleViewItem> mRawData = null;

    public static void makePackageFolder(Context context) {
        if (getPackageDir(context).exists() && getPackageDir(context).isFile()) {
            getPackageDir(context).delete();
        }
        getPackageDir(context).mkdirs();
    }

    private static List<RecycleViewItem> getRawData(Context context) {
        List<RecycleViewItem> mRawData = new ArrayList<>();
        List<ApplicationInfo> packages = getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo: packages) {
            mRawData.add(new RecycleViewItem(
                    packageInfo.packageName,
                    getAppName(packageInfo.packageName, context),
                    getAppIcon(packageInfo.packageName, context),
                    new File(getSourceDir(packageInfo.packageName, context)).length(),
                    Objects.requireNonNull(getPackageInfo(packageInfo.packageName, context)).firstInstallTime,
                    Objects.requireNonNull(getPackageInfo(packageInfo.packageName, context)).lastUpdateTime));
        }
        return mRawData;
    }

    public static List<RecycleViewItem> getData(Context context) {
        boolean mAppType;
        List<RecycleViewItem> mData = new ArrayList<>();
        for (RecycleViewItem item : getRawData()) {
            if (Utils.getString("appTypes", "all", context).equals("system")) {
                mAppType = (isSystemApp(item.getPackageName(), context));
            } else if (Utils.getString("appTypes", "all", context).equals("user")) {
                mAppType = (!isSystemApp(item.getPackageName(), context));
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
            if (Utils.getBoolean("sort_name", false, context)) {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAppName(), rhs.getAppName()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Utils.getBoolean("sort_size", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(RecycleViewItem::getAPKSize));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Utils.getBoolean("sort_installed", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(RecycleViewItem::getInstalledTime));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Utils.getBoolean("sort_updated", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(RecycleViewItem::getUpdatedTime));
            } else {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));
            }
        }
        if (Utils.getBoolean("reverse_order", false, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    public static PackageManager getPackageManager(Context context) {
        return context.getApplicationContext().getPackageManager();
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ApplicationInfo getAppInfo(String packageName, Context context) {
        try {
            return getPackageManager(context).getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getAppName(String packageName, Context context) {
        return getPackageManager(context).getApplicationLabel(Objects.requireNonNull(getAppInfo(
                packageName, context))) + (isEnabled(packageName, context) ? "" : " (Disabled)");
    }

    public static Drawable getAppIcon(String packageName, Context context) {
        return getPackageManager(context).getApplicationIcon(Objects.requireNonNull(getAppInfo(packageName, context)));
    }

    public static String getSourceDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).sourceDir;
    }

    public static String getParentDir(String packageName, Context context) {
        return Objects.requireNonNull(new File(Objects.requireNonNull(getAppInfo(packageName, context))
                .sourceDir).getParentFile()).toString();
    }

    public static String getNativeLibDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).nativeLibraryDir;
    }

    public static String getDataDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).dataDir;
    }

    public static String getVersionName(String packageName, Context context) {
        return Objects.requireNonNull(getPackageManager(context).getPackageArchiveInfo(packageName, 0)).versionName;
    }


    public static String getInstalledDate(String packageName, Context context) {
        return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(getPackageInfo(packageName, context)).firstInstallTime);
    }

    public static String getUpdatedDate(String packageName, Context context) {
        return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(getPackageInfo(packageName, context)).lastUpdateTime);
    }

    public static boolean isEnabled(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).enabled;
    }

    public static boolean isSystemApp(String packageName, Context context) {
        try {
            return (Objects.requireNonNull(getAppInfo(packageName, context)).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (NullPointerException ignored) {}
        return false;
    }

    public static CharSequence getAPKName(String apkPath, Context context) {
        PackageInfo pi = getPackageManager(context).getPackageArchiveInfo(apkPath, 0);
        if (pi != null) {
            return pi.applicationInfo.loadLabel(getPackageManager(context));
        } else {
            return null;
        }
    }

    public static String getAPKId(String apkPath, Context context) {
        PackageInfo pi = getPackageManager(context).getPackageArchiveInfo(apkPath, 0);
        if (pi != null) {
            return pi.applicationInfo.packageName;
        } else {
            return null;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getAPKIcon(String apkPath, Context context) {
        PackageInfo pi = getPackageManager(context).getPackageArchiveInfo(apkPath, 0);
        if (pi != null) {
            return pi.applicationInfo.loadIcon(getPackageManager(context));
        } else {
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_android);
            drawable.setTint(context.getResources().getColor(R.color.colorAccent));
            return drawable;
        }
    }

    public static File getPackageDir(Context context) {
        if (Build.VERSION.SDK_INT >= 30 && Utils.isPermissionDenied()) {
            return context.getExternalFilesDir("");
        } else {
            return new File(Environment.getExternalStorageDirectory(), "Package_Manager");
        }
    }

    public static void clearAppSettings(String packageID) {
        Utils.runCommand("pm clear " + packageID);
    }

    public static String getAPKSize(String path) {
        long size = new File(path).length() / 1024;
        long decimal = (size - 1024) / 1024;
        if (size > 1024) {
            return size / 1024 + "." + decimal + " MB";
        } else {
            return size  + " KB";
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
        return "\n" + sb.toString();
    }

    public static List<RecycleViewItem> getRawData() {
        return mRawData;
    }

    public static void setRawData(Context context) {
        mRawData = getRawData(context);
    }

}