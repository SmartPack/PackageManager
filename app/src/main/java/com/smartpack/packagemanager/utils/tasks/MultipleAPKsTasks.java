/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;

import androidx.documentfile.provider.DocumentFile;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class MultipleAPKsTasks extends sExecutor {

    private final Activity mActivity;
    private static ProgressDialog mProgressDialog;
    private final ClipData mURIFiles;

    public MultipleAPKsTasks(ClipData uriFiles, Activity activity) {
        mURIFiles = uriFiles;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(mActivity.getString(R.string.preparing_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        sUtils.delete(mActivity.getExternalFilesDir("APK"));
        Common.getAppList().clear();
    }

    @Override
    public void doInBackground() {
        for (int i = 0; i < mURIFiles.getItemCount(); i++) {
            String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mURIFiles.getItemAt(i).getUri())).getName();
            File mFile = new File(mActivity.getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
            try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                InputStream inputStream = mActivity.getContentResolver().openInputStream(mURIFiles.getItemAt(i).getUri());
                int read;
                byte[] bytes = new byte[8192];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                // In this case, we don't really care about app bundles!
                if (mFile.getName().endsWith(".apk")) {
                    Common.getAppList().add(mFile.getAbsolutePath());
                }
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        new SplitAPKsInstallationTasks(mActivity).execute();
    }

}