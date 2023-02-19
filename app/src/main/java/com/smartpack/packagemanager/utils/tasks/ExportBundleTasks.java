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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExportBundleTasks extends sExecutor {

    private final Activity mActivity;
    private static Drawable mIcon = null;
    private final LinearLayout mLinearLayout;
    private final MaterialTextView mTextView;
    private final ProgressBar mProgressBar;
    private static String mAPKPath = null, mName = null;

    public ExportBundleTasks(LinearLayout linearLayout, MaterialTextView textView, ProgressBar progressBar,
                             String path, String name, Drawable icon, Activity activity) {
        mLinearLayout = linearLayout;
        mTextView = textView;
        mProgressBar = progressBar;
        mAPKPath = path;
        mName = name;
        mIcon = icon;
        mActivity = activity;

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressBar.setIndeterminate(false);
        PackageDetails.showProgress(mLinearLayout, mTextView, mActivity.getString(R.string.exporting_bundle, mName) + "...");
        PackageData.makePackageFolder(mActivity);
    }

    @Override
    public void doInBackground() {
        sUtils.sleep(1);
        List<File> mFiles = new ArrayList<>();
        for (final String splitApps : SplitAPKInstaller.splitApks(mAPKPath)) {
            mFiles.add(new File(mAPKPath + "/" + splitApps));
        }
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(PackageData.getPackageDir(mActivity) + "/" + mName + "_" + sAPKUtils.getVersionCode(
                sPackageUtils.getSourceDir(Common.getApplicationID(), mActivity), mActivity) + ".apkm")) {
            zipFileUtils.zip(mFiles, mProgressBar);
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        PackageDetails.hideProgress(mLinearLayout, mTextView);
        mProgressBar.setIndeterminate(true);
        new MaterialAlertDialogBuilder(mActivity)
                .setIcon(mIcon)
                .setTitle(mName)
                .setMessage(mActivity.getString(R.string.export_bundle_summary, PackageData.getPackageDir(mActivity) + "/" + mName + ".apkm"))
                .setNegativeButton(mActivity.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(mActivity.getString(R.string.share), (dialog, id) -> {
                    Uri uriFile = FileProvider.getUriForFile(mActivity,
                            BuildConfig.APPLICATION_ID + ".provider", new File(PackageData.getPackageDir(mActivity) + "/" + mName + "_" +
                                    sAPKUtils.getVersionCode(sPackageUtils.getSourceDir(Common.getApplicationID(), mActivity), mActivity) + ".apkm"));
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