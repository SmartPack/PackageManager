/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SaveToDownloadsTasks extends sExecutor {

    private final Context mContext;
    private static File mSource = null;
    private static ProgressDialog mProgressDialog;

    public SaveToDownloadsTasks(File source, Context context) {
        mSource = source;
        mContext = context;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                FileUtils FileUtils = new FileUtils(mSource.getAbsolutePath());
                FileUtils.setProgress(mProgressDialog);
                FileUtils.copyToDownloads(mContext);
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
    }

}