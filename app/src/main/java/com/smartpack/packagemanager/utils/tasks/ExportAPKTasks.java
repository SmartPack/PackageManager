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
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.FileUtils;
import com.smartpack.packagemanager.utils.PackageData;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExportAPKTasks extends sExecutor {

    private final Activity mActivity;
    private final String mPackageName;
    private final Drawable mIcon;
    private final String mAppName, mAPKPath;
    private File mAPKFile;
    private ProgressDialog mProgressDialog;

    public ExportAPKTasks(String appName, String packageName, String path, Drawable icon, Activity activity) {
        this.mAppName = appName;
        this.mPackageName = packageName;
        this.mAPKPath = path;
        this.mIcon = icon;
        this.mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.exporting, mAppName) + "...");
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        PackageData.makePackageFolder(mActivity);
        mAPKFile = new File(PackageData.getPackageDir(mActivity), AppSettings.generateExportedAPKName(mAppName, mPackageName,
                sAPKUtils.getVersionName(sPackageUtils.getSourceDir(mPackageName, mActivity), mActivity), ".apk", mActivity));
        try {
            FileUtils FileUtils = new FileUtils(mAPKFile, mProgressDialog);
            FileUtils.copy(mAPKPath);
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        new MaterialAlertDialogBuilder(mActivity)
                .setIcon(mIcon)
                .setTitle(mAppName)
                .setMessage(mActivity.getString(R.string.export_apk_summary, "Downloads -> " + mActivity.getString(R.string.app_name)))
                .setNegativeButton(mActivity.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(mActivity.getString(R.string.share), (dialog, id) -> {
                    Uri uriFile = FileProvider.getUriForFile(mActivity,
                            BuildConfig.APPLICATION_ID + ".provider", mAPKFile);
                    Intent shareScript = new Intent(Intent.ACTION_SEND);
                    shareScript.setType("application/java-archive");
                    shareScript.putExtra(Intent.EXTRA_SUBJECT, mActivity.getString(R.string.shared_by, mAPKFile.getName()));
                    shareScript.putExtra(Intent.EXTRA_TEXT, mActivity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                    shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                    shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mActivity.startActivity(Intent.createChooser(shareScript, mActivity.getString(R.string.share_with)));
                }).show();
    }

}