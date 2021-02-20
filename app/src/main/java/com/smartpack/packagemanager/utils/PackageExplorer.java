/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageExploreActivity;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class PackageExplorer {

    public static boolean isTextFile(String path) {
        return path.endsWith(".txt") || path.endsWith(".xml") || path.endsWith(".json") || path.endsWith(".properties")
                || path.endsWith(".version") || path.endsWith(".sh") || path.endsWith(".MF") || path.endsWith(".SF")
                || path.endsWith(".RSA") || path.endsWith(".html");
    }

    public static boolean isImageFile(String path) {
        return path.endsWith(".bmp") || path.endsWith(".png") || path.endsWith(".jpg");
    }

    public static int getSpanCount(Activity activity) {
        return Utils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    public static Uri getIconFromPath(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    public static void copyToStorage(String path, String dest, Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.permission_denied_write_storage));
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (!Utils.exist(dest)) {
                    Utils.mkdir(dest);
                }
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.copy(path, dest + "/" + new File(path).getName());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(new File(path).getName() + " " +
                                activity.getString(R.string.export_file_message, dest))
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                        }).show();
            }
        }.execute();
    }

    public static void exploreAPK(LinearLayout linearLayout, String path, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                linearLayout.setVisibility(View.VISIBLE);
                if (new File(activity.getCacheDir().getPath() + "/apk").exists()) {
                    Utils.delete(activity.getCacheDir().getPath() + "/apk");
                }
                Utils.mkdir(activity.getCacheDir().getPath() + "/apk");
                PackageData.mPath = activity.getCacheDir().getPath() + "/apk";
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Utils.unzip(path,activity.getCacheDir().getPath() + "/apk");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                linearLayout.setVisibility(View.GONE);
                Intent explorer = new Intent(activity, PackageExploreActivity.class);
                activity.startActivity(explorer);
            }
        }.execute();
    }

}