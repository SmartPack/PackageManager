/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.smartpack.packagemanager.activities.InstallerActivity;
import com.smartpack.packagemanager.services.SplitAPKInstallService;
import com.smartpack.packagemanager.utils.Common;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sInstallerParams;
import in.sunilpaulmathew.sCommon.Utils.sInstallerUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SplitAPKsInstallationTasks extends sExecutor {

    private final Activity mActivity;

    public SplitAPKsInstallationTasks(Activity activity) {
        mActivity = activity;

    }

    private static long getTotalSize() {
        int totalSize = 0;
        if (Common.getAppList().size() > 0) {
            for (String string : Common.getAppList()) {
                if (sUtils.exist(new File(string))) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
    }

    private static Intent getInstallerCallbackIntent(Context context) {
        return new Intent(context, SplitAPKInstallService.class);
    }

    @Override
    public void onPreExecute() {
        Intent installIntent = new Intent(mActivity, InstallerActivity.class);
        sUtils.saveString("installationStatus", "waiting", mActivity);
        mActivity.startActivity(installIntent);
    }

    @Override
    public void doInBackground() {
        long totalSize = getTotalSize();
        int sessionId;
        final sInstallerParams installParams = sInstallerUtils.makeInstallParams(totalSize);
        sessionId = sInstallerUtils.runInstallCreate(installParams, mActivity);
        try {
            for (String string : Common.getAppList()) {
                File mFile = new File(string);
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), mActivity);
                }
            }
        } catch (NullPointerException ignored) {
        }
        sInstallerUtils.doCommitSession(sessionId, getInstallerCallbackIntent(mActivity), mActivity);
    }

    @Override
    public void onPostExecute() {
    }

}