/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class Downloads {

    private static String mSearchText;

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : getDownloadList(context)) {
            if (sUtils.getString("downloadTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.getName().endsWith(".apkm")) {
                    if (mSearchText == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (isTextMatched(mFile.getName())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    if (mSearchText == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (isTextMatched(mFile.getName())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            }
        }
        if (sUtils.getBoolean("reverse_order_exports", false, context)) {
            Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(rhs, lhs));
        } else {
            Collections.sort(mData, String.CASE_INSENSITIVE_ORDER);
        }
        return mData;
    }

    private static boolean isTextMatched(String searchText) {
        for (int a = 0; a < searchText.length() - mSearchText.length() + 1; a++) {
            if (mSearchText.equalsIgnoreCase(searchText.substring(a, a + mSearchText.length()))) {
                return true;
            }
        }
        return false;
    }

    private static File[] getDownloadList(Context context) {
        if (!PackageData.getPackageDir(context).exists()) {
            PackageData.getPackageDir(context).mkdirs();
        }
        return PackageData.getPackageDir(context).listFiles();
    }

    public static sExecutor saveToDownloads(File source, Context context) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
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
                        FileInputStream inputStream = new FileInputStream(source);
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, source.getName());
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                        Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                        OutputStream outStream = context.getContentResolver().openOutputStream(uri);
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
        };
    }

    public static String getSearchText() {
        return mSearchText;
    }

    public static void setSearchText(String searchText) {
        mSearchText = searchText;
    }

}