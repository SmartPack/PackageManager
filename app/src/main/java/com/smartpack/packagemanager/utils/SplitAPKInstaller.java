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
import android.view.View;
import android.widget.LinearLayout;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.FilePickerActivity;
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

    public static boolean mInstall = false;
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
        if (mInstall) {
            for (final String splitApps : PackageExplorer.mAPKList) {
                if (Utils.exist(splitApps) && splitApps.endsWith(".apk")) {
                    File file = new File(splitApps);
                    PackageTasks.mOutput.append(" - ").append(file.getName()).append(": ").append(file.length()).append(" KB\n");
                    Utils.runCommand("pm install-write -S " + file.length() + " " + mSid + " " + file.getName() + " " + file.toString());
                }
            }
            mInstall = false;
        } else {
            for (final String splitApps : splitApks(activity.getCacheDir().getPath() + "/splits")) {
                if (splitApps.endsWith(".apk")) {
                    File file = new File(activity.getCacheDir().getPath() + "/splits/" + splitApps);
                    PackageTasks.mOutput.append(" - ").append(file.getName()).append(": ").append(file.length()).append(" KB\n");
                    Utils.runCommand("pm install-write -S " + file.length() + " " + mSid + " " + file.getName() + " " + file.toString());
                }
            }
        }
    }

    private static String installCommit() {
        return Utils.runAndGetError("pm install-commit " + mSid);
    }

    private static void handleAPKs(String apks, Activity activity) {
        Utils.runCommand((Utils.exist("/data/adb/magisk/busybox") ? "/data/adb/magisk/busybox unzip "
                : "unzip ") + apks + " -d " + activity.getCacheDir().getPath());
    }

    private static void handleXAPK(String xapk, Activity activity) {
        Utils.mkdir(activity.getCacheDir().getPath() + "/splits");
        Utils.runCommand((Utils.exist("/data/adb/magisk/busybox") ? "/data/adb/magisk/busybox unzip "
                : "unzip ") + xapk + " -d " + activity.getCacheDir().getPath() + "/splits");
    }

    private static void handleMultipleAPKs(Activity activity) {
        if (PackageExplorer.mAPKList.size() > 0) {
            PackageTasks.mOutput.append("** ").append(activity.getString(R.string.creating_directory_message)).append(": ");
            Utils.mkdir(activity.getCacheDir().getPath() + "/splits");
            PackageTasks.mOutput.append(Utils.exist(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done) + " *\n\n" : activity.getString(R.string.failed) + " *\n\n");
            PackageTasks.mOutput.append("** ").append(activity.getString(R.string.copying_apk_message)).append(": ");
            for (String string : PackageExplorer.mAPKList) {
                if (Utils.exist(string) && string.endsWith(".apk")) {
                    Utils.copy(string, activity.getCacheDir().getPath() + "/splits/" + new File(string).getName());
                }
            }
            PackageTasks.mOutput.append(activity.getString(R.string.done)).append(" *\n\n");
        }
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

    public static void handleAppBundle(LinearLayout linearLayout, String path, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                linearLayout.setVisibility(View.VISIBLE);
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                if (Utils.exist(activity.getCacheDir().getPath() + "/toc.pb")) {
                    Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
                }
            }
            @Override
            protected Void doInBackground(Void... voids) {
                if (path.endsWith(".apks")) {
                    handleAPKs(path, activity);
                } else if (path.endsWith(".xapk") || path.endsWith(".apkm")) {
                    handleXAPK(path, activity);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mInstall = true;
                PackageExplorer.mAPKList.clear();
                PackageData.mPath = activity.getCacheDir().getPath() + "/splits";
                Intent filePicker = new Intent(activity, FilePickerActivity.class);
                activity.startActivity(filePicker);
                linearLayout.setVisibility(View.GONE);
            }

        }.execute();
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
                if (dir != null) {
                    Utils.delete(activity.getCacheDir().getPath() + "/splits");
                    if (Utils.exist(activity.getCacheDir().getPath() + "/toc.pb")) {
                        Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
                    }
                }
                activity.startActivity(installIntent);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mSid = createInstallationSession();
                PackageTasks.mOutput.append(" - ").append(activity.getString(R.string.session_id, mSid)).append("\n\n");
                if (dir != null) {
                    PackageTasks.mOutput.append("** Bundle Path: ").append(dir).append("\n\n");
                    if (dir.endsWith(".apks")) {
                        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.bundle_extract_message, new File(dir).getName())).append(": ");
                        handleAPKs(dir, activity);
                        PackageTasks.mOutput.append(Utils.exist(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done)
                                : activity.getString(R.string.failed)).append("\n\n");
                    } else if (dir.endsWith(".xapk") || dir.endsWith(".apkm")) {
                        PackageTasks.mOutput.append("** ").append(activity.getString(R.string.bundle_extract_message, new File(dir).getName())).append(": ");
                        handleXAPK(dir, activity);
                        PackageTasks.mOutput.append(Utils.exist(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done)
                                : activity.getString(R.string.failed)).append("\n\n");
                    } else {
                        handleMultipleAPKs(dir, activity);
                    }
                } else {
                    handleMultipleAPKs(activity);
                }

                installWrite(activity);

                PackageTasks.mOutput.append("\n** ").append(activity.getString(R.string.cleaning_message)).append(": ");
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                if (Utils.exist(activity.getCacheDir().getPath() + "/toc.pb")) {
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