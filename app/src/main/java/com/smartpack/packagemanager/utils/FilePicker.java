/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.FilePickerActivity;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class FilePicker {

    public static boolean isTextFile(String path) {
        return path.endsWith(".txt") || path.endsWith(".xml") || path.endsWith(".json") || path.endsWith(".properties")
                || path.endsWith(".version") || path.endsWith(".sh") || path.endsWith(".MF") || path.endsWith(".SF")
                || path.endsWith(".RSA") || path.endsWith(".html");
    }

    public static boolean isImageFile(String path) {
        return path.endsWith(".bmp") || path.endsWith(".png") || path.endsWith(".jpg");
    }

    public static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    public static boolean isFile(String path) {
        return new File(path).isFile();
    }

    public static int getSpanCount(Activity activity) {
        return Utils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    public static File[] getFilesList() {
        if (!Utils.mPath.endsWith("/")) {
            Utils.mPath = Utils.mPath + "/";
        }
        return new File(Utils.mPath).listFiles();
    }

    public static Uri getIconFromPath(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    public static void copyToStorage(String path, Activity activity) {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                Utils.copy(path, PackageTasks.getPackageDir(activity));
                return null;
            }

            @Override
            protected void onPostExecute(List<String> recyclerViewItems) {
                super.onPostExecute(recyclerViewItems);
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(new File(path).getName() + " " +
                                activity.getString(R.string.export_file_message, PackageTasks.getPackageDir(activity)))
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                        }).show();
            }
        }.execute();
    }

    public static void exploreAPK(String path, Activity activity) {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (new File(activity.getCacheDir().getPath() + "/apk").exists()) {
                    Utils.delete(activity.getCacheDir().getPath() + "/apk");
                }
                Utils.mkdir(activity.getCacheDir().getPath() + "/apk");
                Utils.mPath = activity.getCacheDir().getPath() + "/apk";
            }

            @Override
            protected List<String> doInBackground(Void... voids) {
                Utils.unzip(path,activity.getCacheDir().getPath() + "/apk");
                return null;
            }

            @Override
            protected void onPostExecute(List<String> recyclerViewItems) {
                super.onPostExecute(recyclerViewItems);
                Intent filePicker = new Intent(activity, FilePickerActivity.class);
                activity.startActivity(filePicker);
            }
        }.execute();
    }

}