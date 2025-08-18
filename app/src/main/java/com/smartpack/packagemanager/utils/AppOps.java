/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;

import com.smartpack.packagemanager.utils.SerializableItems.PermissionsItems;

import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 * Modified by sunilpaulmathew <sunil.kde@gmail.com> on Mar 17, 2021
 */
public class AppOps {

    public static ArrayList<PermissionsItems> getOps(Context context) {
        ArrayList<PermissionsItems> mData = new ArrayList<>();
        String[] appOpsList;
        if (new RootShell().rootAccess()) {
            appOpsList = new RootShell().runAndGetOutput("cmd appops get " + Common.getApplicationID())
                    .trim().split("\\r?\\n");
        } else {
            appOpsList = new ShizukuShell().runAndGetOutput("cmd appops get " + Common.getApplicationID())
                    .trim().split("\\r?\\n");
        }
        for (String line : appOpsList) {
            String[] splitOp = line.split(":");
            String name = splitOp[0];
            /*
             * We don't need a single "No operations." item if operations are empty.
             * Also, "Uid mode" needs more work (and likely never work)
             */
            if (!line.equals("No operations.") && !name.equals("Uid mode")) {
                mData.add(new PermissionsItems((line.contains("allow") || line.contains("ignore")), name, sPermissionUtils.getDescription(name, context)));
            }
        }
        return mData;
    }

}