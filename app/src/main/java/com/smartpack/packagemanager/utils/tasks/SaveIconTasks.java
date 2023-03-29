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
import android.graphics.Bitmap;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SaveIconTasks extends sExecutor {

    private final Activity mActivity;
    private final Bitmap mBitmap;
    private final String mDest;

    public SaveIconTasks(Bitmap bitmap, String dest, Activity activity) {
        mBitmap = bitmap;
        mDest = dest;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        PackageData.makePackageFolder(mActivity);
    }

    @Override
    public void doInBackground() {
        File file = new File(mDest);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        new MaterialAlertDialogBuilder(mActivity)
                .setMessage(Common.getApplicationName() + " icon " + mActivity.getString(R.string.export_file_message,
                        Objects.requireNonNull(new File(mDest).getParentFile()).toString()))
                .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                }).show();
    }

}