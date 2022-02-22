/*
 * Copyright (C) 2022-2023 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.os.Build;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 06, 2022
 */
public class Flavor {

    public static boolean isFullVersion() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q;
    }

}