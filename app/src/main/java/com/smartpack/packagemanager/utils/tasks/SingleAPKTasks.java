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
import com.smartpack.packagemanager.dialogs.BundleInstallDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.Serializables.APKPickerItems;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SingleAPKTasks extends sExecutor {

    private final Activity mActivity;
    private final List<APKPickerItems> mAPKs = new ArrayList<>();
    private final OnInstallRequest mCallback;
    private final Uri mURIFile;
    private static File mAPKFile = null;
    private static String mFileName = null;
    private ProgressDialog mProgressDialog;

    public SingleAPKTasks(Uri uriFile, OnInstallRequest callback, Activity activity) {
        this.mURIFile = uriFile;
        this.mCallback = callback;
        this.mActivity = activity;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.initializing));
        mProgressDialog.show();
    }

    private void generateAPKList(File parentFile) {
        for (File files : Objects.requireNonNull(parentFile.listFiles())) {
            if (files.isDirectory()) {
                generateAPKList(files);
            } else if (files.isFile() && files.getName().endsWith(".apk")) {
                mAPKs.add(new APKPickerItems(files, FilePicker.isSelectedAPK(files, mActivity)));
            }
        }
    }

    @Override
    public void doInBackground() {
        mFileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mURIFile)).getName();
        if (Objects.requireNonNull(mFileName).endsWith(".apk")) {
            sFileUtils.delete(Objects.requireNonNull(mActivity.getExternalFilesDir("APK")));
            mAPKFile = new File(mActivity.getExternalFilesDir("APK"), Objects.requireNonNull(mFileName));
            sFileUtils.copy(mURIFile, mAPKFile, mActivity);
            mAPKFile.deleteOnExit();
        } else if (mFileName.endsWith(".apkm") || mFileName.endsWith(".apks") || mFileName.endsWith(".xapk")) {
            for (File files : SplitAPKInstaller.getFilesList(mActivity.getCacheDir())) {
                sFileUtils.delete(files);
            }

            try (ZipFileUtils zipFileUtils = new ZipFileUtils(mURIFile, mActivity)) {
                zipFileUtils.setProgress(mProgressDialog);
                zipFileUtils.unzip(mActivity.getCacheDir().getAbsolutePath());
            } catch (IOException ignored) {}

            generateAPKList(mActivity.getCacheDir());

            mAPKs.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAPKName(), rhs.getAPKName()));
        }
    }

    private Intent apkDetailsIntent() {
        Intent apkDetails = new Intent(mActivity, APKPickerActivity.class);
        apkDetails.putExtra(APKPickerActivity.PATH_INTENT, mAPKFile.getAbsolutePath());
        apkDetails.putExtra(APKPickerActivity.NAME_INTENT, mFileName);
        return apkDetails;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        if (mFileName.endsWith(".apk")) {
            mCallback.onInstall(apkDetailsIntent());
        } else if (mFileName.endsWith(".apkm") || mFileName.endsWith(".apks") || mFileName.endsWith(".xapk")) {
            new BundleInstallDialog(mAPKs, false, mCallback, mActivity);
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

    public interface OnInstallRequest {
        void onInstall(Intent intent);
    }

}