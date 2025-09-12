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
import com.smartpack.packagemanager.utils.SerializableItems.APKPickerItems;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static File mAPKFile = null;
    private static String mFileName = null;
    private final Uri mURIFile;
    private final List<APKPickerItems> mAPKs = new ArrayList<>();
    private ProgressDialog mProgressDialog;

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
    }

    @Override
    public void doInBackground() {
        mFileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mURIFile)).getName();
        if (Objects.requireNonNull(mFileName).endsWith(".apk")) {
            sFileUtils.delete(mActivity.getExternalFilesDir("APK"));
            mAPKFile = new File(mActivity.getExternalFilesDir("APK"), Objects.requireNonNull(mFileName));
            sFileUtils.copy(mURIFile, mAPKFile, mActivity);
            mAPKFile.deleteOnExit();
        } else if (mFileName.endsWith(".apkm") || mFileName.endsWith(".apks") || mFileName.endsWith(".xapk")) {
            for (File files : SplitAPKInstaller.getFilesList(mActivity.getCacheDir())) {
                sFileUtils.delete(files);
            }
            LocalFileHeader localFileHeader;
            int readLen;
            byte[] readBuffer = new byte[4096];
            try {
                InputStream inputStream = mActivity.getContentResolver().openInputStream(mURIFile);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                    if (localFileHeader.getFileName().endsWith(".apk")) {
                        File apkFile = new File(mActivity.getCacheDir(), localFileHeader.getFileName());
                        mAPKs.add(new APKPickerItems(apkFile, FilePicker.isSelectedAPK(apkFile, mActivity)));

                        try (FileOutputStream fileOutputStream = new FileOutputStream(apkFile)) {
                            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                                fileOutputStream.write(readBuffer, 0, readLen);
                            }
                        } catch (IOException ignored) {}
                    }
                }
            } catch (IOException ignored) {}
            mAPKs.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAPKName(), rhs.getAPKName()));
        }
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
            new BundleInstallDialog(mAPKs, false, mActivity);
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