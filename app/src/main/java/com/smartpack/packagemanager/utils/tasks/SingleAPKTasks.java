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
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.APKPickerActivity;
import com.smartpack.packagemanager.utils.APKData;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SingleAPKTasks extends sExecutor {

    private final Activity mActivity;
    private static File mFile = null;
    private static ProgressDialog mProgressDialog;
    private final Uri mURIFile;

    public SingleAPKTasks(Uri uriFile, Activity activity) {
        mURIFile = uriFile;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setMessage("\n" + mActivity.getString(R.string.preparing_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        sFileUtils.delete(mActivity.getExternalFilesDir("APK"));
        Common.getAppList().clear();
    }

    @Override
    public void doInBackground() {
        String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mURIFile)).getName();
        mFile = new File(mActivity.getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
        try {
            FileUtils FileUtils = new FileUtils(mFile.getAbsolutePath());
            FileUtils.setProgress(mProgressDialog);
            FileUtils.copy(mURIFile, mActivity);
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        if (mFile.getName().endsWith("apk")) {
            APKData.setAPKFile(mFile);
            Intent apkDetails = new Intent(mActivity, APKPickerActivity.class);
            mActivity.startActivity(apkDetails);
        } else if (mFile.getName().endsWith("apkm") || mFile.getName().endsWith("apks") || mFile.getName().endsWith("xapk")) {
            new MaterialAlertDialogBuilder(mActivity)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.split_apk_installer)
                    .setMessage(mActivity.getString(R.string.bundle_install_question))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.install, (dialogInterface, i) ->
                            new AppBundleTasks(null, mFile.getAbsolutePath(), false, mActivity).execute()
                    ).show();
        } else {
            new MaterialAlertDialogBuilder(mActivity)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.split_apk_installer)
                    .setMessage(mActivity.getString(R.string.wrong_extension, ".apks/.apkm/.xapk"))
                    .setCancelable(false)
                    .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                    }).show();
        }
    }

}