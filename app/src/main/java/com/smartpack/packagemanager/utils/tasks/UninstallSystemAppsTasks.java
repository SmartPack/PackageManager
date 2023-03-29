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
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class UninstallSystemAppsTasks extends sExecutor {

    private final Activity mActivity;
    private final LinearLayout mLinearLayout;
    private final MaterialTextView mTextView;
    private static final RootShell mRootShell = new RootShell();
    private static final ShizukuShell mShizukuShell = new ShizukuShell();

    public UninstallSystemAppsTasks(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        mLinearLayout = linearLayout;
        mTextView = textView;
        mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        PackageDetails.showProgress(mLinearLayout, mTextView, mActivity.getString(R.string.uninstall_summary, Common.getApplicationName()));
    }

    @Override
    public void doInBackground() {
        sCommonUtils.sleep(1);
        if (mRootShell.rootAccess()) {
            mRootShell.runCommand("pm uninstall --user 0 " + Common.getApplicationID());
        } else {
            mShizukuShell.runCommand("pm uninstall --user 0 " + Common.getApplicationID());
        }
    }

    @Override
    public void onPostExecute() {
        PackageData.setRawData(null, mActivity);
        PackageDetails.hideProgress(mLinearLayout, mTextView);
        mActivity.finish();
        Common.reloadPage(true);
    }

}