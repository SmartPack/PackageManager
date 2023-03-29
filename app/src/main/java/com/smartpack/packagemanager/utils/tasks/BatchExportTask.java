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
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class BatchExportTask extends sExecutor {

    private final Activity mActivity;

    public BatchExportTask(Activity activity) {
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
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        if (!PackageData.getPackageDir(mActivity).exists()) {
            sFileUtils.mkdir(PackageData.getPackageDir(mActivity));
        }
        for (String packageID : Common.getBatchList()) {
            if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, mActivity)) {
                if (SplitAPKInstaller.isAppBundle(sPackageUtils.getParentDir(packageID, mActivity))) {
                    Common.getOutput().append("** ").append(mActivity.getString(R.string.exporting_bundle, PackageData.getAppName(packageID, mActivity)));
                    List<File> mFiles = new ArrayList<>();
                    for (final String splitApps : SplitAPKInstaller.splitApks(sPackageUtils.getParentDir(packageID, mActivity))) {
                        mFiles.add(new File(sPackageUtils.getParentDir(packageID, mActivity) + "/" + splitApps));
                    }
                    try (ZipFileUtils zipFileUtils = new ZipFileUtils(PackageData.getPackageDir(mActivity) + "/" + PackageData.getFileName(packageID, mActivity) + "_" +
                            sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(packageID, mActivity), mActivity) + ".apkm")) {
                        zipFileUtils.zip(mFiles);
                    } catch (IOException ignored) {}
                } else {
                    Common.getOutput().append("** ").append(mActivity.getString(R.string.exporting, PackageData.getAppName(packageID, mActivity)));
                    sFileUtils.copy(new File(sPackageUtils.getSourceDir(packageID, mActivity)), new File(PackageData.getPackageDir(mActivity), PackageData.getFileName(packageID, mActivity) + "_" +
                            sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(packageID, mActivity), mActivity) + ".apk"));
                }
                Common.getOutput().append(": ").append(mActivity.getString(R.string.done)).append(" *\n\n");
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