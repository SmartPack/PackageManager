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

import com.smartpack.packagemanager.activities.APKPickerActivity;
import com.smartpack.packagemanager.activities.InstallerActivity;
import com.smartpack.packagemanager.services.SplitAPKInstallService;
import com.smartpack.packagemanager.utils.SerializableItems.APKPickerItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final List<APKPickerItems> mAPKItems;
    private final OnInstallRequest mCallback;
    private final String mAPKPath;

    public SplitAPKsInstallationTasks(List<APKPickerItems> apkItems, OnInstallRequest callback, Activity activity) {
        this.mAPKItems = apkItems;
        this.mCallback = callback;
        this.mActivity = activity;
        this.mAPKList = null;
        this.mAPKPath = null;
    }

    public SplitAPKsInstallationTasks(ArrayList<String> apkList, OnInstallRequest callback, Activity activity) {
        this.mAPKList = apkList;
        this.mCallback = callback;
        this.mActivity = activity;
        this.mAPKItems = null;
        this.mAPKPath = null;
    }

    public SplitAPKsInstallationTasks(String apkPath, OnInstallRequest callback, Activity activity) {
        this.mCallback = callback;
        this.mAPKPath = apkPath;
        this.mActivity = activity;
        this.mAPKList = null;
        this.mAPKItems = null;
    }

    @Override
    public void onPreExecute() {
        sCommonUtils.saveString("installationStatus", "waiting", mActivity);
        mCallback.onInstall(installerIntent());
    }

    private Intent installerIntent() {
        Intent installIntent = new Intent(mActivity, InstallerActivity.class);
        if (mAPKItems != null) {
            installIntent.putStringArrayListExtra(InstallerActivity.APP_LIST_INTENT, getAPKList());
        } else if (mAPKList != null) {
            installIntent.putStringArrayListExtra(InstallerActivity.APP_LIST_INTENT, mAPKList);
        } else if (mAPKPath != null) {
            installIntent.putExtra(InstallerActivity.APK_PATH_INTENT, mAPKPath);
        }
        return installIntent;
    }

    private ArrayList<String> getAPKList() {
        ArrayList<String> apkList = new ArrayList<>();
        for (APKPickerItems items : Objects.requireNonNull(mAPKItems)) {
            if (items.isSelected()) {
                apkList.add(items.getAPKPath());
            }
        }
        return apkList;
    }

    private long getTotalSize() {
        long totalSize = 0;
        if (mAPKItems != null) {
            for (APKPickerItems items : mAPKItems) {
                if (items.isSelected()) {
                    totalSize += items.getAPKSize();
                }
            }
        } else if (mAPKList != null) {
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
        if (mAPKItems != null) {
            for (APKPickerItems items : mAPKItems) {
                if (items.isSelected()) {
                    sInstallerUtils.runInstallWrite(items.getAPKSize(), sessionId, items.getAPKName(), items.getAPKPath(), mActivity);
                }
            }
        } else if (mAPKList != null) {
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

    public interface OnInstallRequest {
        void onInstall(Intent intent);
    }

}