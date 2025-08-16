/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;

import com.smartpack.packagemanager.BuildConfig;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 17, 2021
 */
public class Billing {

    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getPackageExt() {
        return null;
    }

    public static void showDonateOption(Activity activity) {
        sCommonUtils.launchUrl("https://smartpack.github.io/PackageManager/donations/", activity);
    }

}