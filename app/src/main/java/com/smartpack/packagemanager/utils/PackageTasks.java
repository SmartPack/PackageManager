/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.root.RootFile;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageTasks {

    public static final String PACKAGES = Environment.getExternalStorageDirectory().toString() + "/Package_Manager";
    public static String mAppName;

    public static StringBuilder mBatchApps = null;
    public static StringBuilder mOutput = null;

    public static boolean mRunning = false;

    private static void makePackageFolder() {
        File file = new File(PACKAGES);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static void batchOption(String name) {
        if (PackageTasks.mBatchApps.toString().contains(name)) {
            int appID = PackageTasks.mBatchApps.indexOf(name);
            PackageTasks.mBatchApps.delete(appID, appID + name.length());
        } else {
            PackageTasks.mBatchApps.append(" ").append(name);
        }
    }

    public static void exportingTask(String apk, String name, Drawable icon, WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.exporting, name) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                makePackageFolder();
                Utils.sleep(1);
                Utils.copy(apk, PACKAGES + "/" + name + ".apk");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (Utils.existFile(PACKAGES + "/" + name + ".apk")) {
                    new Dialog(activityRef.get())
                            .setIcon(icon)
                            .setTitle(activityRef.get().getString(R.string.share) + " " + name + "?")
                            .setMessage(name + " " + activityRef.get().getString(R.string.export_summary, PACKAGES))
                            .setNeutralButton(activityRef.get().getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(activityRef.get().getString(R.string.share), (dialog, id) -> {
                                Uri uriFile = FileProvider.getUriForFile(activityRef.get(),
                                        BuildConfig.APPLICATION_ID + ".provider", new File(PACKAGES + "/" + name + ".apk"));
                                Intent shareScript = new Intent(Intent.ACTION_SEND);
                                shareScript.setType("application/java-archive");
                                shareScript.putExtra(Intent.EXTRA_SUBJECT, activityRef.get().getString(R.string.shared_by, name));
                                shareScript.putExtra(Intent.EXTRA_TEXT, activityRef.get().getString(R.string.share_message, BuildConfig.VERSION_NAME));
                                shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                                shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activityRef.get().startActivity(Intent.createChooser(shareScript, activityRef.get().getString(R.string.share_with)));
                            })

                            .show();
                }
            }
        }.execute();
    }

    public static void exportingBundleTask(String apk, String name, Drawable icon, WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.exporting_bundle, name) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                makePackageFolder();
                Utils.sleep(1);
                RootUtils.runCommand("cp -r " + apk + " " + PACKAGES + "/" + name);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (Utils.existFile(PACKAGES + "/" + name + "/base.apk")) {
                    new Dialog(activityRef.get())
                            .setIcon(icon)
                            .setTitle(name)
                            .setMessage(activityRef.get().getString(R.string.export_bundle_summary, PACKAGES))
                            .setPositiveButton(R.string.cancel, (dialog, id) -> {
                            })

                            .show();
                }
            }
        }.execute();
    }

    public static void backupApp(String app, String name) {
        makePackageFolder();
        Utils.sleep(2);
        RootUtils.runCommand("tar -zcvf " + PACKAGES + "/" +
                name + " /data/data/" + app);
    }

    public static void restoreApp(String path, WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.restoring, path) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(2);
                RootUtils.runCommand("tar -zxvf " + path + " -C /");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

    public static String getVersionName(String path, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
        if (info == null) return null;
        return info.versionName;
    }

    public static String getPermissions(String appID, Context context) {
        List<String> perms = new ArrayList<>();
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(appID, PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    perms.add(pi.requestedPermissions[i]);
                }
            }
        } catch (Exception ignored) {
        }
        return perms.toString().replace("[","").replace("]","").replace(", ","\n");
    }

    public static List<String> splitApks(String string) {
        RootFile file = new RootFile(string);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.list();
    }

    public static String listSplitAPKs(String string) {
        StringBuilder sb = new StringBuilder();
        for (final String splitApps : splitApks(string)) {
            if (splitApps.endsWith(".apk")) {
                sb.append(string).append(splitApps).append("\n");
            }
        }
        return sb.toString();
    }

    public static boolean isEnabled(String app, WeakReference<Activity> activityRef) {
        try {
            ApplicationInfo ai =
                    activityRef.get().getPackageManager().getApplicationInfo(app, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    /*
     * Inspired from the original implementation of split apk installer by @yeriomin on https://github.com/yeriomin/YalpStore/
     * Ref: https://github.com/yeriomin/YalpStore/blob/master/app/src/main/java/com/github/yeriomin/yalpstore/install/InstallerRoot.java
     */
    public static void installSplitAPKs(String dir, WeakReference<Activity> activityRef) {
        String sid = RootUtils.runAndGetOutput("pm install-create").replace(
                "Success: created install session [","").replace("]", "");
        mOutput.append(" - ").append(activityRef.get().getString(R.string.session_id, sid)).append("\n\n");
        mOutput.append("** ").append(activityRef.get().getString(R.string.creating_directory_message)).append(": ");
        RootUtils.runCommand("mkdir /data/local/tmp/pm/");
        mOutput.append(Utils.existFile("/data/local/tmp/pm/") ? activityRef.get().getString(R.string.done) + " *\n\n" : activityRef.get().getString(R.string.failed) + " *\n\n");
        mOutput.append("** ").append(activityRef.get().getString(R.string.copying_apk_message)).append(": ");
        RootUtils.runCommand("cp " + dir + "/* /data/local/tmp/pm/");
        mOutput.append(activityRef.get().getString(R.string.done)).append(" *\n\n");
        mOutput.append("** Bundle Path: ").append(dir).append("\n\n");
        mOutput.append("** ").append(activityRef.get().getString(R.string.split_apk_list)).append(" *\n");
        for (final String splitApps : splitApks("/data/local/tmp/pm")) {
            File file = new File("/data/local/tmp/pm/" + splitApps);
            mOutput.append(" - ").append(file.getName()).append(": ").append(file.length()).append(" KB\n");
            RootUtils.runCommand("pm install-write -S " + file.length() + " " + sid + " " + file.getName() + " " + file.toString());
        }
        mOutput.append("\n** ").append(activityRef.get().getString(R.string.cleaning_message)).append(": ");
        Utils.delete("/data/local/tmp/pm/");
        mOutput.append(Utils.existFile("/data/local/tmp/pm/") ? activityRef.get().getString(R.string.failed) +
                " *\n\n" : ": " + activityRef.get().getString(R.string.done) + " *\n\n");
        mOutput.append("** ").append(activityRef.get().getString(R.string.result, RootUtils.runAndGetError("pm install-commit " + sid)));
    }

}