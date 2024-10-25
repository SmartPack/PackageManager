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
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class SaveIconTasks extends sExecutor {

    private final Context mActivity;
    private final Bitmap mBitmap;
    private final String mName;

    public SaveIconTasks(String name, Bitmap bitmap, Activity activity) {
        mName = name;
        mBitmap = bitmap;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void doInBackground() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream(mBitmap.getByteCount());
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                saveToDownloads(mName, new ByteArrayInputStream(outStream.toByteArray()), mActivity);
            } else {
                FileOutputStream outStream = new FileOutputStream(new File(Environment.DIRECTORY_DOWNLOADS, mName));
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            }
        } catch (IOException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        sCommonUtils.toast(Common.getApplicationName() + " icon " + mActivity.getString(R.string.export_file_message,
                Environment.DIRECTORY_DOWNLOADS), mActivity).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static void saveToDownloads(String name, InputStream inputStream, Context context) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

        sFileUtils.copyStream(inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }

}