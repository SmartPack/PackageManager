/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.content.Intent;

import com.smartpack.packagemanager.activities.InstallerActivity;
import com.smartpack.packagemanager.services.SplitAPKInstallService;
import com.smartpack.packagemanager.utils.Common;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerParams;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SplitAPKsInstallationTasks extends sExecutor {

    private final Activity mActivity;

    public SplitAPKsInstallationTasks(Activity activity) {
        mActivity = activity;

    }

    private static long getTotalSize() {
        long totalSize = 0;
        if (!Common.getAppList().isEmpty()) {
            for (String string : Common.getAppList()) {
                if (sFileUtils.exist(new File(string))) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
    }

    @Override
    public void onPreExecute() {
        Intent installIntent = new Intent(mActivity, InstallerActivity.class);
        sCommonUtils.saveString("installationStatus", "waiting", mActivity);
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
        sInstallerUtils.doCommitSession(sessionId, new Intent(mActivity, SplitAPKInstallService.class), mActivity);
    }

    @Override
    public void onPostExecute() {
    }

}