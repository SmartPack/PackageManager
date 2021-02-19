/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 18, 2020
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackageList {

    public static boolean mAppType= false, mOEMApps = false;

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (Utils.getBoolean("system_apps", true, context)
                    && Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                        || (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else if (Utils.getBoolean("system_apps", true, context)
                    && !Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            } else if (!Utils.getBoolean("system_apps", true, context)
                    && Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else {
                mAppType = false;
            }
            if (mAppType && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getGoogleList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.google.android.")
                    || packageInfo.packageName.startsWith("com.android.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getSamsungList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.samsung.")
                    || packageInfo.packageName.startsWith("com.sec.android.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getASUSList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.asus.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getMotoList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.motorola.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getOnePlusList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.oneplus.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getHuaweiList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.huawei.") || packageInfo.packageName.startsWith("com.huaweioverseas.")
                    || packageInfo.packageName.startsWith("com.bitaxon.app.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getSonyList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.sony.") || packageInfo.packageName.startsWith("jp.sony.")
                    || packageInfo.packageName.startsWith("jp.co.sony.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getLGList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.lge.") || packageInfo.packageName.startsWith("com.lgeha.")
                    || packageInfo.packageName.startsWith("ru.lgerp.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static List<String> getXiaomiList(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = PackageData.getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(PackageData.getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (packageInfo.packageName.startsWith("com.mi.") || packageInfo.packageName.startsWith("com.xiaomi.") && packageInfo.packageName.contains(".")) {
                if (PackageData.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (PackageData.getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(PackageData.mSearchText.toLowerCase())
                        || packageInfo.packageName.toLowerCase().contains(PackageData.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

}