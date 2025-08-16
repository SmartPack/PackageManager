/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.smartpack.packagemanager.dialogs.ExportSuccessDialog;
import com.smartpack.packagemanager.utils.PackageData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SaveIconTasks extends sExecutor {

    private final Context mActivity;
    private final Bitmap mBitmap;
    private File mParentFile;
    private final String mName, mPackageName;

    public SaveIconTasks(String name, String packageName, Bitmap bitmap, Activity activity) {
        this.mName = name;
        this.mPackageName = packageName;
        this.mBitmap = bitmap;
        this.mActivity = activity;

    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void doInBackground() {
        PackageData.makePackageFolder(mActivity);
        mParentFile = new File(PackageData.getPackageDir(mActivity), mPackageName);
        if (!mParentFile.exists()) {
            sFileUtils.mkdir(mParentFile);
        }
        try {
            FileOutputStream outStream = new FileOutputStream(new File(mParentFile, mName));
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void onPostExecute() {
        new ExportSuccessDialog(new File(mParentFile, mName).getAbsolutePath(), mActivity);
    }

}