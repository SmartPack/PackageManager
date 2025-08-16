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

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class DisableAppTasks extends sExecutor {

    private final Activity mActivity;
    private static final RootShell mRootShell = new RootShell();
    private static final ShizukuShell mShizukuShell = new ShizukuShell();
    private static String mResult = null;
    private ProgressDialog mProgressDialog;

    public DisableAppTasks(Activity activity) {
        mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(sPackageUtils.isEnabled(Common.getApplicationID(), mActivity) ?
                mActivity.getString(R.string.disabling, Common.getApplicationName()) + "..." :
                mActivity.getString(R.string.enabling, Common.getApplicationName()) + "...");
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        sCommonUtils.sleep(1);
        if (mRootShell.rootAccess()) {
            mResult = mRootShell.runAndGetError((sPackageUtils.isEnabled(Common.getApplicationID(), mActivity) ? "pm disable " : "pm enable ") + Common.getApplicationID());
        } else {
            mResult = mShizukuShell.runAndGetOutput((sPackageUtils.isEnabled(Common.getApplicationID(), mActivity) ? "pm disable " : "pm enable ") + Common.getApplicationID());
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        if (mResult != null && (mResult.contains("new state: disabled") || mResult.contains("new state: enabled"))) {
            Common.reloadPage(true);
            mActivity.recreate();
        } else {
            sCommonUtils.snackBar(mActivity.findViewById(android.R.id.content), mActivity.getString(R.string.disable_failed_message, Common.getApplicationName())).show();
        }
    }

}