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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class CopyToStorageTasks extends sExecutor {

    private final Activity mActivity;
    private final String mDest, mPath;

    public CopyToStorageTasks(String path, String dest, Activity activity) {
        mPath = path;
        mDest = dest;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        if (!sFileUtils.exist(new File(mDest))) {
            sFileUtils.mkdir(new File(mDest));
        }
    }

    @Override
    public void doInBackground() {
        sFileUtils.copy(new File(mPath), new File(mDest, new File(mPath).getName()));
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        new MaterialAlertDialogBuilder(mActivity)
                .setMessage(new File(mPath).getName() + " " +
                        mActivity.getString(R.string.export_file_message, mDest))
                .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                }).show();
    }

}