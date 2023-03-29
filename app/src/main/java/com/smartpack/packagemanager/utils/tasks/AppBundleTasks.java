/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class AppBundleTasks extends sExecutor {

    private final Activity mActivity;
    private final boolean mExit;
    private final ProgressBar mProgressBar;
    private static ProgressDialog mProgressDialog;
    private final String mPath;

    public AppBundleTasks(ProgressBar progressBar, String path, boolean exit, Activity activity) {
        mProgressBar = progressBar;
        mPath = path;
        mExit = exit;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(mActivity.getString(R.string.preparing_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setIcon(R.mipmap.ic_launcher);
            mProgressDialog.setTitle(R.string.app_name);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        if (sFileUtils.exist(mActivity.getCacheDir())) {
            for (File files : SplitAPKInstaller.getFilesList(mActivity.getCacheDir())) {
                sFileUtils.delete(files);
            }
        }
    }

    @Override
    public void doInBackground() {
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(mPath)) {
            if (mProgressBar != null) {
                zipFileUtils.setProgress(mProgressBar);
            } else {
                zipFileUtils.setProgress(mProgressDialog);
            }
            zipFileUtils.unzip(mActivity.getCacheDir().getAbsolutePath());
        } catch (IOException ignored) {}
        for (File files : SplitAPKInstaller.getFilesList(mActivity.getCacheDir())) {
            if (files.isFile() && files.getName().endsWith(".apk")) {
                Common.setPath(mActivity.getCacheDir().getAbsolutePath());
            } else if (files.isDirectory()) {
                for (File dirs : SplitAPKInstaller.getFilesList(new File(mActivity.getCacheDir(), files.getName()))) {
                    if (dirs.isFile() && dirs.getName().endsWith(".apk")) {
                        Common.setPath(new File(mActivity.getCacheDir(), dirs.getName()).getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    public void onPostExecute() {
        Common.getAppList().clear();
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setIndeterminate(true);
        } else {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException ignored) {
            }
        }
        Intent filePicker = new Intent(mActivity, FilePickerActivity.class);
        mActivity.startActivity(filePicker);
        if (mExit) mActivity.finish();
    }

}