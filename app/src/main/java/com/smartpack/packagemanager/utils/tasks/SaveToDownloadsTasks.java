/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.smartpack.packagemanager.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SaveToDownloadsTasks extends sExecutor {

    private final Context mContext;
    private static File mSource = null;
    private static ProgressDialog mProgressDialog;

    public SaveToDownloadsTasks(File source, Context context) {
        mSource = source;
        mContext = context;

    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileInputStream inputStream = new FileInputStream(mSource);
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, mSource.getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = mContext.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                OutputStream outStream = mContext.getContentResolver().openOutputStream(uri);
                sUtils.copyStream(inputStream, outStream);
                inputStream.close();
                outStream.close();
            }
        } catch(Exception ignored) {
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
    }

}