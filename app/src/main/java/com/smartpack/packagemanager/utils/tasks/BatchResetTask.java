/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageTasksActivity;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class BatchResetTask extends sExecutor {

    private final Activity mActivity;
    private static RootShell mRootShell = null;
    private static ShizukuShell mShizukuShell = null;

    public BatchResetTask(Activity activity) {
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        Common.isRunning(true);
        Common.getOutput().setLength(0);
        Common.getOutput().append("** ").append(mActivity.getString(R.string.batch_processing_initialized)).append("...\n\n");
        Common.getOutput().append("** ").append(mActivity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
        Intent removeIntent = new Intent(mActivity, PackageTasksActivity.class);
        removeIntent.putExtra(PackageTasksActivity.TITLE_START, mActivity.getString(R.string.batch_processing));
        removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, mActivity.getString(R.string.batch_processing_finished));
        mActivity.startActivity(removeIntent);
        if (mRootShell == null) {
            mRootShell = new RootShell();
        }
        if (mShizukuShell == null) {
            mShizukuShell = new ShizukuShell();
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        for (String packageID : Common.getBatchList()) {
            if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, mActivity)) {
                if (packageID.equals(mActivity.getPackageName())) {
                    Common.getOutput().append("** ").append(mActivity.getString(R.string.reset_summary, PackageData.getAppName(packageID, mActivity)));
                    Common.getOutput().append(": ").append(mActivity.getString(R.string.uninstall_nope)).append(" *\n\n");
                } else {
                    Common.getOutput().append("** ").append(mActivity.getString(R.string.reset_summary, PackageData.getAppName(packageID, mActivity)));
                    if (mRootShell.rootAccess()) {
                        mRootShell.runCommand("pm clear " + packageID);
                    } else {
                        mShizukuShell.runCommand("pm clear " + packageID);
                    }
                    Common.getOutput().append(": ").append(mActivity.getString(R.string.done)).append(" *\n\n");
                }
                sCommonUtils.sleep(1);
            }
        }
    }

    @Override
    public void onPostExecute() {
        Common.getOutput().append("** ").append(mActivity.getString(R.string.everything_done)).append(" *");
        Common.isRunning(false);
    }

}