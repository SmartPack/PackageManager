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
import android.os.AsyncTask;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageTasksActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 *
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 14, 2020
 *
 * Inspired from the original implementation of split apk installer by @yeriomin on https://github.com/yeriomin/YalpStore/
 * Ref: https://github.com/yeriomin/YalpStore/blob/master/app/src/main/java/com/github/yeriomin/yalpstore/install/InstallerRoot.java
 *
 */
public class SplitAPKInstaller {

    private static String mSid;

    private static String createInstallationSession() {
        return Utils.runAndGetOutput("pm install-create").replace(
                "Success: created install session [","").replace("]", "");
    }

    public static List<String> splitApks(String path) {
        List<String> list = new ArrayList<>();
        if (new File(path).exists()) {
            for (File mFile : Objects.requireNonNull(new File(path).listFiles())) {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    list.add(mFile.getName());
                }
            }
        }
        return list;
    }

    public static String listSplitAPKs(String path) {
        return splitApks(path).toString().substring(1, splitApks(path).toString().length() - 1).replace(", ", "\n");
    }

    public static boolean isAppBundle(String path) {
        return splitApks(path).size() > 1;
    }

    private static void installWrite(Activity activity) {
        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.split_apk_list)).append(" *\n");
        for (final String splitApps : splitApks(activity.getCacheDir().getPath() + "/splits")) {
            if (splitApps.endsWith(".apk")) {
                File file = new File(activity.getCacheDir().getPath() + "/splits/" + splitApps);
                PackageTasks.mOutput.append(" - ").append(file.getName()).append(": ").append(file.length()).append(" KB\n");
                Utils.runCommand("pm install-write -S " + file.length() + " " + mSid + " " + file.getName() + " " + file.toString());
            }
        }
    }

    private static String installCommit() {
        return Utils.runAndGetError("pm install-commit " + mSid);
    }

    private static void handleAPKs(String apks, Activity activity) {
        Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.bundle_extract_message, new File(apks).getName())).append(": ");
        Utils.runCommand((Utils.exist("/data/adb/magisk/busybox") ? "/data/adb/magisk/busybox unzip "
                : "unzip ") + apks + " -d " + activity.getCacheDir().getPath());
        PackageTasks.mOutput.append(Utils.exist(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done)
                : activity.getString(R.string.failed)).append("\n\n");
    }

    private static void handleXAPK(String xapk, Activity activity) {
        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.bundle_extract_message, new File(xapk).getName())).append(": ");
        Utils.mkdir(activity.getCacheDir().getPath() + "/splits");
        Utils.runCommand((Utils.exist("/data/adb/magisk/busybox") ? "/data/adb/magisk/busybox unzip "
                : "unzip ") + xapk + " -d " + activity.getCacheDir().getPath() + "/splits");
        PackageTasks.mOutput.append(Utils.exist(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done)
                : activity.getString(R.string.failed)).append("\n\n");
    }

    private static void handleMultipleAPKs(String dir, Activity activity) {
        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.creating_directory_message)).append(": ");
        Utils.mkdir(activity.getCacheDir().getPath() + "/splits");
        PackageTasks.mOutput.append(Utils.exist(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done) + " *\n\n" : activity.getString(R.string.failed) + " *\n\n");
        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.copying_apk_message)).append(": ");
        for (final String splitApps : splitApks(dir)) {
            Utils.copy(dir + "/" + splitApps, activity.getCacheDir().getPath() + "/splits/" + splitApps);
        }
        PackageTasks.mOutput.append(activity.getString(R.string.done)).append(" *\n\n");
    }

    public static void installSplitAPKs(String dir, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                PackageTasks.mRunning = true;
                if (PackageTasks.mOutput == null) {
                    PackageTasks.mOutput = new StringBuilder();
                } else {
                    PackageTasks.mOutput.setLength(0);
                }
                PackageTasks.mOutput.append("** ").append(activity.getString(R.string.install_bundle_initialized)).append("...\n\n");
                Intent installIntent = new Intent(activity, PackageTasksActivity.class);
                installIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.installing_bundle));
                installIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.installing_bundle_finished));
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                activity.startActivity(installIntent);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mSid = createInstallationSession();
                PackageTasks.mOutput.append(" - ").append(activity.getString(R.string.session_id, mSid)).append("\n\n");
                PackageTasks.mOutput.append("** Bundle Path: ").append(dir).append("\n\n");
                if (dir.endsWith(".apks")) {
                    handleAPKs(dir, activity);
                } else if (dir.endsWith(".xapk") || dir.endsWith(".apkm")) {
                    handleXAPK(dir, activity);
                } else {
                    handleMultipleAPKs(dir, activity);
                }

                installWrite(activity);

                PackageTasks.mOutput.append("\n** ").append(activity.getString(R.string.cleaning_message)).append(": ");
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                if (dir.endsWith(".apks")) {
                    Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
                }
                PackageTasks.mOutput.append(activity.getString(R.string.done)).append(" *\n\n");

                PackageTasks.mOutput.append("** ").append(activity.getString(R.string.result, installCommit()));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                PackageTasks.mRunning = false;
            }
        }.execute();
    }

}