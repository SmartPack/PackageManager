/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmailcom> on August 14, 2025
 */
public class Billing {

    public static String getAppVersion() {
        return "Pro " + BuildConfig.VERSION_NAME;
    }

    public static String getPackageExt() {
        return ".pro";
    }

    public static void showDonateOption(Context context) {
        sCommonUtils.toast(context.getString(R.string.purchase_pro_acknowledgement), context).show();
    }

}