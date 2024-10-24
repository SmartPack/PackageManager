/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 19, 2023
 */
public class FileUtils extends File {

    private ProgressBar mProgressBar = null;
    private ProgressDialog mProgressDialog = null;

    public FileUtils(@NonNull String path) {
        super(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void copyToDownloads(Context context) throws IOException {
        setProgressMax((int) length());
        FileInputStream inputStream = new FileInputStream(getAbsoluteFile());
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

        sFileUtils.copyStream(inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }

    public void copy(File dest) throws IOException {
        setProgressMax((int) length());
        FileInputStream inputStream = new FileInputStream(getAbsoluteFile());
        FileOutputStream outputStream = new FileOutputStream(dest);

        sFileUtils.copyStream(inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }

    public void copy(Uri uri, Context context) throws IOException {
        @SuppressLint("Recycle")
        AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri , "r");
        long fileSize = fileDescriptor.getLength();
        setProgressMax((int) fileSize);
        FileOutputStream outputStream = new FileOutputStream(toString(), false);
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        sFileUtils.copyStream(inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }

    public void setProgress(int progress) {
        if (mProgressBar != null) {
            if (mProgressBar.getProgress() < mProgressBar.getMax()) {
                mProgressBar.setProgress(mProgressBar.getProgress() + progress);
            } else {
                mProgressBar.setProgress(0);
            }
        } else if (mProgressDialog != null) {
            if (mProgressDialog.getProgress() < mProgressDialog.getMax()) {
                mProgressDialog.setProgress(mProgressDialog.getProgress() + progress);
            } else {
                mProgressDialog.setProgress(0);
            }
        }
    }

    public void setProgressMax(int max) {
        if (mProgressBar != null) {
            mProgressBar.setMax(max);
        } else if (mProgressDialog != null) {
            mProgressDialog.setMax(max);
        }
    }

    public void setProgress(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public void setProgress(ProgressDialog progressDialog) {
        mProgressDialog = progressDialog;
    }

}