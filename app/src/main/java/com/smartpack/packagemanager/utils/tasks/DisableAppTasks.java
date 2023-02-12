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
import android.widget.LinearLayout;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class DisableAppTasks extends sExecutor {

    private final Activity mActivity;
    private final LinearLayout mLinearLayout;
    private final MaterialTextView mTextView;
    private static final RootShell mRootShell = new RootShell();
    private static final ShizukuShell mShizukuShell = new ShizukuShell();
    private static String mResult = null;

    public DisableAppTasks(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        mLinearLayout = linearLayout;
        mTextView = textView;
        mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        PackageDetails.showProgress(mLinearLayout, mTextView, sPackageUtils.isEnabled(Common.getApplicationID(), mActivity) ?
                mActivity.getString(R.string.disabling, Common.getApplicationName()) + "..." :
                mActivity.getString(R.string.enabling, Common.getApplicationName()) + "...");
    }

    @Override
    public void doInBackground() {
        sUtils.sleep(1);
        if (mRootShell.rootAccess()) {
            mResult = mRootShell.runAndGetError((sPackageUtils.isEnabled(Common.getApplicationID(), mActivity) ? "pm disable " : "pm enable ") + Common.getApplicationID());
        } else {
            mResult = mShizukuShell.runAndGetOutput((sPackageUtils.isEnabled(Common.getApplicationID(), mActivity) ? "pm disable " : "pm enable ") + Common.getApplicationID());
        }
    }

    @Override
    public void onPostExecute() {
        PackageDetails.hideProgress(mLinearLayout, mTextView);
        if (mResult != null && (mResult.contains("new state: disabled") || mResult.contains("new state: enabled"))) {
            Common.reloadPage(true);
            mActivity.recreate();
        } else {
            sUtils.snackBar(mActivity.findViewById(android.R.id.content), mActivity.getString(R.string.disable_failed_message, Common.getApplicationName())).show();
        }
    }

}