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
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.APKPickerActivity;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.Common;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SingleAPKTasks extends sExecutor {

    private final Activity mActivity;
    private static File mAPKFile = null;
    private static String mFileName = null;
    private final Uri mURIFile;
    private static ProgressDialog mProgressDialog;

    public SingleAPKTasks(Uri uriFile, Activity activity) {
        mURIFile = uriFile;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.initializing));
        mProgressDialog.show();
        sFileUtils.delete(mActivity.getExternalFilesDir("APK"));
        Common.getAppList().clear();
    }

    @Override
    public void doInBackground() {
        mFileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mURIFile)).getName();
        mAPKFile = new File(mActivity.getExternalFilesDir("APK"), Objects.requireNonNull(mFileName));
        sFileUtils.copy(mURIFile, mAPKFile, mActivity);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        if (mFileName.endsWith(".apk")) {
            Intent apkDetails = new Intent(mActivity, APKPickerActivity.class);
            apkDetails.putExtra(APKPickerActivity.PATH_INTENT, mAPKFile.getAbsolutePath());
            apkDetails.putExtra(APKPickerActivity.NAME_INTENT, mFileName);
            mActivity.startActivity(apkDetails);
        } else if (mFileName.endsWith(".apkm") || mFileName.endsWith(".apks") || mFileName.endsWith(".xapk")) {
            new MaterialAlertDialogBuilder(mActivity)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.split_apk_installer)
                    .setMessage(mActivity.getString(R.string.bundle_install_apks, mFileName))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.install, (dialogInterface, i) ->
                            new AppBundleTasks(mAPKFile.getAbsolutePath(), false, mActivity).execute()
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