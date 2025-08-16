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
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FileUtils;
import com.smartpack.packagemanager.utils.PackageData;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExportAPKTasks extends sExecutor {

    private final Activity mActivity;
    private static Drawable mIcon = null;
    private static String mAPKPath = null, mName = null;
    private ProgressDialog mProgressDialog;

    public ExportAPKTasks(String path, String name, Drawable icon, Activity activity) {
        mAPKPath = path;
        mName = name;
        mIcon = icon;
        mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.exporting, mName) + "...");
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        sCommonUtils.sleep(1);
        PackageData.makePackageFolder(mActivity);
        try {
            FileUtils FileUtils = new FileUtils(new File(PackageData.getPackageDir(mActivity), mName + "_" + sAPKUtils.getVersionCode(
                    sPackageUtils.getSourceDir(Common.getApplicationID(), mActivity), mActivity) + ".apk"), mProgressDialog);
            FileUtils.copy(mAPKPath);
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        new MaterialAlertDialogBuilder(mActivity)
                .setIcon(mIcon)
                .setTitle(mName)
                .setMessage(mActivity.getString(R.string.export_apk_summary, PackageData.getPackageDir(mActivity)))
                .setNegativeButton(mActivity.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(mActivity.getString(R.string.share), (dialog, id) -> {
                    Uri uriFile = FileProvider.getUriForFile(mActivity,
                            BuildConfig.APPLICATION_ID + ".provider", new File(PackageData.getPackageDir(mActivity), mName + "_" +
                                    sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(Common.getApplicationID(), mActivity), mActivity) + ".apk"));
                    Intent shareScript = new Intent(Intent.ACTION_SEND);
                    shareScript.setType("application/java-archive");
                    shareScript.putExtra(Intent.EXTRA_SUBJECT, mActivity.getString(R.string.shared_by, mName));
                    shareScript.putExtra(Intent.EXTRA_TEXT, mActivity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                    shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                    shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mActivity.startActivity(Intent.createChooser(shareScript, mActivity.getString(R.string.share_with)));
                }).show();
    }

}