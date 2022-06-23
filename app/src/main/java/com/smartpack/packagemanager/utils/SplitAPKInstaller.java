/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.APKPickerActivity;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.InstallerActivity;
import com.smartpack.packagemanager.services.SplitAPKInstallService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sInstallerParams;
import in.sunilpaulmathew.sCommon.Utils.sInstallerUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2020
 */
public class SplitAPKInstaller {

    public static boolean isAppBundle(String path) {
        return splitApks(path).size() > 1;
    }

    private static File[] getFilesList(File dir) {
        return Objects.requireNonNull(dir).listFiles();
    }

    private static long getTotalSize() {
        int totalSize = 0;
        if (Common.getAppList().size() > 0) {
            for (String string : Common.getAppList()) {
                if (sUtils.exist(new File(string))) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
    }

    public static List<String> splitApks(String path) {
        List<String> list = new ArrayList<>();
        if (new File(path).exists()) {
            for (File mFile : getFilesList(new File(path))) {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    list.add(mFile.getName());
                }
            }
        }
        return list;
    }

    public static void handleAppBundle(LinearLayout linearLayout, String path, Activity activity) {
        new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                if (linearLayout != null) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    mProgressDialog = new ProgressDialog(activity);
                    mProgressDialog.setMessage(activity.getString(R.string.preparing_message));
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setIcon(R.mipmap.ic_launcher);
                    mProgressDialog.setTitle(R.string.app_name);
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }
                if (sUtils.exist(activity.getCacheDir())) {
                    for (File files : getFilesList(activity.getCacheDir())) {
                        sUtils.delete(files);
                    }
                }
            }

            @Override
            public void doInBackground() {
                Utils.unzip(path, activity.getCacheDir().getAbsolutePath());
                for (File files : getFilesList(activity.getCacheDir())) {
                    if (files.isFile() && files.getName().endsWith(".apk")) {
                        Common.setPath(activity.getCacheDir().getAbsolutePath());
                    } else if (files.isDirectory()) {
                        for (File dirs : getFilesList(new File(activity.getCacheDir(), files.getName()))) {
                            if (dirs.isFile() && dirs.getName().endsWith(".apk")) {
                                Common.setPath(new File(activity.getCacheDir(), dirs.getName()).getAbsolutePath());
                            }
                        }
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (linearLayout != null) {
                    linearLayout.setVisibility(View.GONE);
                } else {
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                Common.getAppList().clear();
                Intent filePicker = new Intent(activity, FilePickerActivity.class);
                activity.startActivity(filePicker);
            }
        }.execute();
    }

    public static sExecutor handleSingleInstallationEvent(Uri uriFile, Activity activity) {
        return new sExecutor() {
            private File mFile = null;
            private String mExtension = null;
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setMessage("\n" + activity.getString(R.string.preparing_message));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                sUtils.delete(activity.getExternalFilesDir("APK"));
                Common.getAppList().clear();
            }

            @Override
            public void doInBackground() {
                mExtension = Utils.getExtension(uriFile, activity);
                mFile = new File(activity.getExternalFilesDir("APK"), "APK." + mExtension);
                sUtils.copy(uriFile, mFile, activity);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (mExtension.equals("apk")) {
                    APKData.setAPKFile(mFile);
                    Intent apkDetails = new Intent(activity, APKPickerActivity.class);
                    activity.startActivity(apkDetails);
                } else if (mExtension.equals("apkm") || mExtension.equals("apks") || mExtension.equals("xapk")) {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(activity.getString(R.string.bundle_install_question))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .setPositiveButton(R.string.install, (dialogInterface, i) ->
                                    handleAppBundle(null, mFile.getAbsolutePath(), activity)
                            ).show();
                } else {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(activity.getString(R.string.wrong_extension, ".apks/.apkm/.xapk"))
                            .setCancelable(false)
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            }).show();
                }
            }
        };
    }

    public static sExecutor handleMultipleAPKs(ClipData uriFiles, Activity activity) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.preparing_message));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                sUtils.delete(activity.getExternalFilesDir("APK"));
                Common.getAppList().clear();
            }

            @Override
            public void doInBackground() {
                for (int i = 0; i < uriFiles.getItemCount(); i++) {
                    String mExtension = Utils.getExtension(uriFiles.getItemAt(i).getUri(), activity);
                    File mFile = new File(activity.getExternalFilesDir("APK"), "APK" + i + "." + mExtension);
                    try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                        InputStream inputStream = activity.getContentResolver().openInputStream(uriFiles.getItemAt(i).getUri());
                        int read;
                        byte[] bytes = new byte[8192];
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        // In this case, we don't really care about app bundles!
                        if (Objects.equals(mExtension, "apk")) {
                            Common.getAppList().add(mFile.getAbsolutePath());
                        }
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
                installSplitAPKs(activity);
            }
        };
    }

    public static void installSplitAPKs(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                sUtils.saveString("installationStatus", "waiting", activity);
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                long totalSize = getTotalSize();
                int sessionId;
                final sInstallerParams installParams = sInstallerUtils.makeInstallParams(totalSize);
                sessionId = sInstallerUtils.runInstallCreate(installParams, activity);
                try {
                    for (String string : Common.getAppList()) {
                        if (sUtils.exist(new File(string)) && string.endsWith(".apk")) {
                            File mFile = new File(string);
                            if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    }
                } catch (NullPointerException ignored) {}
                sInstallerUtils.doCommitSession(sessionId, getInstallerCallbackIntent(activity), activity);
            }

            @Override
            public void onPostExecute() {
            }
        }.execute();
    }

    private static Intent getInstallerCallbackIntent(Context context) {
        return new Intent(context, SplitAPKInstallService.class);
    }

}