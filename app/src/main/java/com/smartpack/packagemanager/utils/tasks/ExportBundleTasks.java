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
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExportBundleTasks extends sExecutor {

    private final Activity mActivity;
    private final String mPackageName;
    private final Drawable mIcon;
    private final String mAPKPath, mName;
    private ProgressDialog mProgressDialog;

    public ExportBundleTasks(String packageName, String path, String name, Drawable icon, Activity activity) {
        this.mPackageName = packageName;
        this.mAPKPath = path;
        this.mName = name;
        this.mIcon = icon;
        this.mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.exporting_bundle, mName) + "...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        List<File> mFiles = new ArrayList<>();
        for (final String splitApps : SplitAPKInstaller.splitApks(mAPKPath)) {
            mFiles.add(new File(mAPKPath, splitApps));
        }
        PackageData.makePackageFolder(mActivity);
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(PackageData.getPackageDir(mActivity) + "/" + mName + "_" + sAPKUtils.getVersionCode(
                sPackageUtils.getSourceDir(mPackageName, mActivity), mActivity) + ".apkm")) {
            zipFileUtils.setProgress(mProgressDialog);
            zipFileUtils.zip(mFiles);
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        new MaterialAlertDialogBuilder(mActivity)
                .setIcon(mIcon)
                .setTitle(mName)
                .setMessage(mActivity.getString(R.string.export_bundle_summary, PackageData.getPackageDir(mActivity) + "/" + mName + ".apkm"))
                .setNegativeButton(mActivity.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(mActivity.getString(R.string.share), (dialog, id) -> {
                    Uri uriFile = FileProvider.getUriForFile(mActivity,
                            BuildConfig.APPLICATION_ID + ".provider", new File(PackageData.getPackageDir(mActivity) + "/" + mName + "_" +
                                    sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(mPackageName, mActivity), mActivity) + ".apkm"));
                    Intent shareScript = new Intent(Intent.ACTION_SEND);
                    shareScript.setType("application/zip");
                    shareScript.putExtra(Intent.EXTRA_SUBJECT, mActivity.getString(R.string.shared_by, mName));
                    shareScript.putExtra(Intent.EXTRA_TEXT, mActivity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                    shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                    shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mActivity.startActivity(Intent.createChooser(shareScript, mActivity.getString(R.string.share_with)));
                }).show();
    }

}