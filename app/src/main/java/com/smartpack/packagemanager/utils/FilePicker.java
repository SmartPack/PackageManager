/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */

import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;

import java.io.File;
import java.util.List;

public class FilePicker {

    public static boolean isTextFile(String path) {
        return path.endsWith(".txt") || path.endsWith(".xml") || path.endsWith(".json") || path.endsWith(".properties")
                || path.endsWith(".html") || path.endsWith(".version") || path.endsWith(".sh") || path.endsWith(".MF")
                || path.endsWith(".SF") || path.endsWith(".RSA");
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

}