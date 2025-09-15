/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.documentfile.provider.DocumentFile;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.ProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class MultipleAPKsTasks extends sExecutor {

    private final Activity mActivity;
    private static ArrayList<String> mAPKs;
    private final ActivityResultLauncher<Intent> mInstallApp;
    private static ProgressDialog mProgressDialog;
    private final ClipData mURIFiles;

    public MultipleAPKsTasks(ClipData uriFiles, ActivityResultLauncher<Intent> installApp, Activity activity) {
        this.mURIFiles = uriFiles;
        this.mInstallApp = installApp;
        this.mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(mActivity.getString(R.string.preparing_message));
        mProgressDialog.show();

        sFileUtils.delete(mActivity.getExternalFilesDir("APK"));
        mAPKs = new ArrayList<>();
    }

    @Override
    public void doInBackground() {
        mProgressDialog.setMax(mURIFiles.getItemCount());
        for (int i = 0; i < mURIFiles.getItemCount(); i++) {
            String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mURIFiles.getItemAt(i).getUri())).getName();
            // In this case, we don't really care about app bundles!
            if (Objects.requireNonNull(fileName).endsWith(".apk")) {
                File mFile = new File(mActivity.getExternalFilesDir("APK"), fileName);
                try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                    InputStream inputStream = mActivity.getContentResolver().openInputStream(mURIFiles.getItemAt(i).getUri());
                    int read;
                    byte[] bytes = new byte[8192];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    mAPKs.add(mFile.getAbsolutePath());
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            mProgressDialog.updateProgress(1);
        }
    }

    @Override
    public void onPostExecute() {
        mProgressDialog.dismiss();
        new SplitAPKsInstallationTasks(mAPKs, mInstallApp::launch, mActivity).execute();
    }

}