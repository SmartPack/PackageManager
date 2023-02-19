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
import android.widget.LinearLayout;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class AppBundleTasks extends sExecutor {

    private final Activity mActivity;
    private final LinearLayout mLinearLayout;
    private static ProgressDialog mProgressDialog;
    private final String mPath;

    public AppBundleTasks(LinearLayout linearLayout, String path, Activity activity) {
        mLinearLayout = linearLayout;
        mPath = path;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        if (mLinearLayout != null) {
            mLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(mActivity.getString(R.string.preparing_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setIcon(R.mipmap.ic_launcher);
            mProgressDialog.setTitle(R.string.app_name);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        if (sUtils.exist(mActivity.getCacheDir())) {
            for (File files : SplitAPKInstaller.getFilesList(mActivity.getCacheDir())) {
                sUtils.delete(files);
            }
        }
    }

    @Override
    public void doInBackground() {
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(mPath)) {
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
        if (mLinearLayout != null) {
            mLinearLayout.setVisibility(View.GONE);
        } else {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException ignored) {
            }
        }
        Common.getAppList().clear();
        Intent filePicker = new Intent(mActivity, FilePickerActivity.class);
        mActivity.startActivity(filePicker);
    }

}