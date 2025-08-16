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

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageExploreActivity;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExploreAPKTasks extends sExecutor {

    private final Activity mActivity;
    private static File mFile;
    private final String mPath;
    private ProgressDialog mProgressDialog;

    public ExploreAPKTasks(String path, Activity activity) {
        mPath = path;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.exploring);
        mProgressDialog.show();
        mFile = new File(mActivity.getCacheDir(), "apk");
        if (sFileUtils.exist(mFile)) {
            sFileUtils.delete(mFile);
        }
        mFile.deleteOnExit();
        sFileUtils.mkdir(mFile);
        Common.setPath(mFile.getAbsolutePath());
    }

    @Override
    public void doInBackground() {
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(mPath)) {
            zipFileUtils.setProgress(mProgressDialog);
            zipFileUtils.unzip(mFile.getAbsolutePath());
        } catch (IOException ignored) {}
    }

    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        Intent explorer = new Intent(mActivity, PackageExploreActivity.class);
        mActivity.startActivity(explorer);
    }

}