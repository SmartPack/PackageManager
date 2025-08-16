/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.content.Context;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.ExportSuccessDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.FileUtils;
import com.smartpack.packagemanager.utils.PackageData;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SaveToDownloadsTasks extends sExecutor {

    private final Context mContext;
    private File mParentFile;
    private final File mSource;
    private final String mPackageName;
    private static ProgressDialog mProgressDialog;

    public SaveToDownloadsTasks(File source, String packageName, Context context) {
        mSource = source;
        mPackageName = packageName;
        mContext = context;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.preparing_message);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        PackageData.makePackageFolder(mContext);
        mParentFile = new File(PackageData.getPackageDir(mContext), mPackageName);
        if (!mParentFile.exists()) {
            sFileUtils.mkdir(mParentFile);
        }
        FileUtils FileUtils = new FileUtils(new File(mParentFile, mSource.getName()), mProgressDialog);
        try {
            FileUtils.copy(mSource.getAbsolutePath());
        } catch (IOException ignored) {}
    }

    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        new ExportSuccessDialog(new File(mParentFile, mSource.getName()).getAbsolutePath(), mContext);
    }

}