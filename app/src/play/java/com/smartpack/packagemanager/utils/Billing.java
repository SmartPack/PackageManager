/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Intent;

import com.smartpack.packagemanager.activities.BillingActivity;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 17, 2021
 */
public class Billing {

    public static void showDonateOption(Activity activity) {
        Intent donations = new Intent(activity, BillingActivity.class);
        activity.startActivity(donations);
    }

}