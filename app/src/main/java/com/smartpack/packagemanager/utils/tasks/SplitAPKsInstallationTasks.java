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

import java.io.File;
import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SplitAPKsInstallationTasks extends sExecutor {

    private final Activity mActivity;
    private final ArrayList<String> mAPKList;
    private final String mAPKPath;

    public SplitAPKsInstallationTasks(ArrayList<String> apkList, Activity activity) {
        this.mActivity = activity;
        this.mAPKList = apkList;
        this.mAPKPath = null;
    }

    public SplitAPKsInstallationTasks(String apkPath, Activity activity) {
        this.mActivity = activity;
        this.mAPKList = null;
        this.mAPKPath = apkPath;
    }

    @Override
    public void onPreExecute() {
        Intent installIntent = new Intent(mActivity, InstallerActivity.class);
        if (mAPKList != null) {
            installIntent.putStringArrayListExtra(InstallerActivity.APP_LIST_INTENT, mAPKList);
        } else if (mAPKPath != null) {
            installIntent.putExtra(InstallerActivity.APK_PATH_INTENT, mAPKPath);
        }
        sCommonUtils.saveString("installationStatus", "waiting", mActivity);
        mActivity.startActivity(installIntent);
    }

    private long getTotalSize() {
        long totalSize = 0;
        if (mAPKList != null) {
            for (String filePath : mAPKList) {
                File file = new File(filePath);
                if (sFileUtils.exist(file) && file.getName().endsWith(".apk")) {
                    totalSize += file.length();
                }
            }
        } else if (mAPKPath != null) {
            totalSize = new File(mAPKPath).length();
        }
        return totalSize;
    }

    @Override
    public void doInBackground() {
        int sessionId;
        sessionId = sInstallerUtils.runInstallCreate(getTotalSize(), mActivity);
        if (mAPKList != null) {
            for (String filePath : mAPKList) {
                File file = new File(filePath);
                if (file.exists() && file.getName().endsWith(".apk")) {
                    sInstallerUtils.runInstallWrite(file.length(), sessionId, file.getName(), file.toString(), mActivity);
                }
            }
        } else if (mAPKPath != null) {
            File file = new File(mAPKPath);
            sInstallerUtils.runInstallWrite(file.length(), sessionId, file.getName(), mAPKPath, mActivity);
        }
        sInstallerUtils.doCommitSession(sessionId, new Intent(mActivity, SplitAPKInstallService.class), mActivity);
    }

    @Override
    public void onPostExecute() {
    }

}