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
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class UninstallSystemAppsTasks extends sExecutor {

    private final Activity mActivity;
    private final String mAppName, mPackageName;
    private static final RootShell mRootShell = new RootShell();
    private static final ShizukuShell mShizukuShell = new ShizukuShell();
    private ProgressDialog mProgressDialog;

    public UninstallSystemAppsTasks(String packageName, String appName, Activity activity) {
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.uninstall_summary, mAppName));
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        if (mRootShell.rootAccess()) {
            mRootShell.runCommand("pm uninstall --user 0 " + mPackageName);
        } else {
            mShizukuShell.runCommand("pm uninstall --user 0 " + mPackageName);
        }
    }

    @Override
    public void onPostExecute() {
        PackageData.setRawData(null, mActivity);
        mProgressDialog.dismiss();
        Intent intent = new Intent();
        intent.putExtra("packageName", mPackageName);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

}