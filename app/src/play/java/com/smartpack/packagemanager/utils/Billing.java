/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmailcom> on January 17, 2021
 */
public class Billing {

    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getPackageExt() {
        return "";
    }

    public static void showDonateOption(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.support_development)
                .setMessage(R.string.support_development_summary)
                .setNeutralButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.purchase_pro, (dialog, id) ->
                        sCommonUtils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.packagemanager.pro", activity)
                ).show();
    }

}