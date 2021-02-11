/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.PackageTasksActivity;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageTasks {

    public static StringBuilder mOutput = null;

    public static List<String> mBatchList = new ArrayList<>();

    public static boolean mAppType;
    public static boolean mRunning = false;

    public static void makePackageFolder(Context context) {
        File file = new File(getPackageDir(context));
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        List<ApplicationInfo> packages = getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        if (Utils.getBoolean("sort_name", true, context)) {
            Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(getPackageManager(context)));
        }
        for (ApplicationInfo packageInfo: packages) {
            if (Utils.getBoolean("system_apps", true, context)
                    && Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                        || (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else if (Utils.getBoolean("system_apps", true, context)
                    && !Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            } else if (!Utils.getBoolean("system_apps", true, context)
                    && Utils.getBoolean("user_apps", true, context)) {
                mAppType = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            } else if (Utils.getBoolean("google_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.google.android.");
            } else if (Utils.getBoolean("samsung_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.samsung.")
                        || packageInfo.packageName.startsWith("com.sec.android.");
            } else if (Utils.getBoolean("asus_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.asus.");
            } else if (Utils.getBoolean("moto_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.motorola.");
            } else if (Utils.getBoolean("oneplus_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.oneplus.");
            } else if (Utils.getBoolean("huawei_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.huawei.") || packageInfo.packageName.startsWith("com.huaweioverseas.")
                        || packageInfo.packageName.startsWith("com.bitaxon.app.");
            } else if (Utils.getBoolean("sony_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.sony.")
                        || packageInfo.packageName.startsWith("jp.sony.")
                        || packageInfo.packageName.startsWith("jp.co.sony.");
            } else if (Utils.getBoolean("lg_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.lge.") || packageInfo.packageName.startsWith("com.lgeha.")
                        || packageInfo.packageName.startsWith("ru.lgerp.");
            } else if (Utils.getBoolean("mi_apps", true, context)) {
                mAppType = packageInfo.packageName.startsWith("com.mi.") || packageInfo.packageName.startsWith("com.xiaomi.");
            } else {
                mAppType = false;
            }
            if (mAppType && packageInfo.packageName.contains(".")) {
                if (Utils.mSearchText == null) {
                    mData.add(packageInfo.packageName);
                } else if (getPackageManager(context).getApplicationLabel(packageInfo).toString().toLowerCase().contains(Utils.mSearchText.toLowerCase())) {
                    mData.add(packageInfo.packageName);
                }
            }
        }
        return mData;
    }

    public static void batchDisableTask(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRunning = true;
                if (mOutput == null) {
                    mOutput = new StringBuilder();
                } else {
                    mOutput.setLength(0);
                }
                mOutput.append("** ").append(activity.getString(R.string.batch_processing_initialized)).append("...\n\n");
                mOutput.append("** ").append(activity.getString(R.string.batch_list_summary)).append(showBatchList()).append("\n\n");
                Intent turnOffIntent = new Intent(activity, PackageTasksActivity.class);
                turnOffIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                turnOffIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(turnOffIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".")) {
                        mOutput.append(isEnabled(packageID, activity) ? "** " +
                                activity.getString(R.string.disabling, packageID) : "** " + activity.getString(R.string.enabling, packageID));
                        if (isEnabled(packageID, activity)) {
                            Utils.runCommand("pm disable " + packageID);
                        } else {
                            Utils.runCommand("pm enable " + packageID);
                        }
                        mOutput.append(": ").append(activity.getString(R.string.done)).append(" *\n\n");
                        Utils.sleep(1);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mOutput.append("** ").append(activity.getString(R.string.everything_done)).append(" *");
                mRunning = false;
                Utils.mReloadPage = true;
            }
        }.execute();
    }

    public static void batchResetTask(Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mOutput == null) {
                    mOutput = new StringBuilder();
                } else {
                    mOutput.setLength(0);
                }
                mOutput.append("** ").append(context.getString(R.string.batch_processing_initialized)).append("...\n\n");
                mOutput.append("** ").append(context.getString(R.string.batch_list_summary)).append(showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(context, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, context.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, context.getString(R.string.batch_processing_finished));
                context.startActivity(removeIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".") && Utils.isPackageInstalled(packageID, context)) {
                        mOutput.append("** ").append(context.getString(R.string.reset_summary, packageID));
                        Utils.runCommand("pm clear " + packageID);
                        mOutput.append(": ").append(context.getString(R.string.done)).append(" *\n\n");
                        Utils.sleep(1);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mOutput.append("** ").append(context.getString(R.string.everything_done)).append(" *");
                mRunning = false;
            }
        }.execute();
    }

    public static void batchUninstallTask(Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mOutput == null) {
                    mOutput = new StringBuilder();
                } else {
                    mOutput.setLength(0);
                }
                mOutput.append("** ").append(context.getString(R.string.batch_processing_initialized)).append("...\n\n");
                mOutput.append("** ").append(context.getString(R.string.batch_list_summary)).append(showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(context, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, context.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, context.getString(R.string.batch_processing_finished));
                context.startActivity(removeIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".") && Utils.isPackageInstalled(packageID, context)) {
                        mOutput.append("** ").append(context.getString(R.string.uninstall_summary, packageID));
                        Utils.runCommand("pm uninstall --user 0 " + packageID);
                        mOutput.append(Utils.isPackageInstalled(packageID, context) ? ": " +
                                context.getString(R.string.failed) + " *\n\n" : ": " + context.getString(R.string.done) + " *\n\n");
                        Utils.sleep(1);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mOutput.append("** ").append(context.getString(R.string.everything_done)).append(" *");
                mRunning = false;
            }
        }.execute();
    }

    /*
     * Inspired from the original implementation of split apk installer by @yeriomin on https://github.com/yeriomin/YalpStore/
     * Ref: https://github.com/yeriomin/YalpStore/blob/master/app/src/main/java/com/github/yeriomin/yalpstore/install/InstallerRoot.java
     */
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
                activity.startActivity(installIntent);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                String sid = Utils.runAndGetOutput("pm install-create").replace(
                        "Success: created install session [","").replace("]", "");
                mOutput.append(" - ").append(activity.getString(R.string.session_id, sid)).append("\n\n");
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                mOutput.append("** Bundle Path: ").append(dir).append("\n\n");
                if (dir.endsWith(".apks")) {
                    Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
                    mOutput.append("** ").append(activity.getString(R.string.bundle_extract_message, new File(dir).getName())).append(": ");
                    Utils.unzip(dir, activity.getCacheDir().getPath());
                    mOutput.append(Utils.existFile(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done)
                            : activity.getString(R.string.failed)).append("\n\n");
                } else if (dir.endsWith(".xapk")) {
                    mOutput.append("** ").append(activity.getString(R.string.bundle_extract_message, new File(dir).getName())).append(": ");
                    Utils.create(activity.getCacheDir().getPath() + "/splits");
                    Utils.unzip(dir, activity.getCacheDir().getPath() + "/splits");
                    mOutput.append(Utils.existFile(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done)
                            : activity.getString(R.string.failed)).append("\n\n");
                } else {
                    mOutput.append("** ").append(activity.getString(R.string.creating_directory_message)).append(": ");
                    Utils.create(activity.getCacheDir().getPath() + "/splits");
                    mOutput.append(Utils.existFile(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.done) + " *\n\n" : activity.getString(R.string.failed) + " *\n\n");
                    mOutput.append("** ").append(activity.getString(R.string.copying_apk_message)).append(": ");
                    Utils.runCommand("cp " + dir + "/* " + activity.getCacheDir().getPath() + "/splits");
                    mOutput.append(activity.getString(R.string.done)).append(" *\n\n");
                }
                mOutput.append("** ").append(activity.getString(R.string.split_apk_list)).append(" *\n");
                for (final String splitApps : splitApks(activity.getCacheDir().getPath() + "/splits")) {
                    if (splitApps.endsWith(".apk")) {
                        File file = new File(activity.getCacheDir().getPath() + "/splits/" + splitApps);
                        mOutput.append(" - ").append(file.getName()).append(": ").append(file.length()).append(" KB\n");
                        Utils.runCommand("pm install-write -S " + file.length() + " " + sid + " " + file.getName() + " " + file.toString());
                    }
                }
                mOutput.append("\n** ").append(activity.getString(R.string.cleaning_message)).append(": ");
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                if (dir.endsWith(".apks")) {
                    Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
                }
                mOutput.append(Utils.existFile(activity.getCacheDir().getPath() + "/splits") ? activity.getString(R.string.failed) +
                        " *\n\n" : activity.getString(R.string.done) + " *\n\n");
                mOutput.append("** ").append(activity.getString(R.string.result, Utils.runAndGetError("pm install-commit " + sid)));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                PackageTasks.mRunning = false;
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
                new File(activity.getCacheDir().getPath() + "/apk").mkdirs();
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

    public static PackageManager getPackageManager(Context context) {
        return context.getApplicationContext().getPackageManager();
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ApplicationInfo getAppInfo(String packageName, Context context) {
        try {
            return getPackageManager(context).getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getAppName(String packageName, Context context) {
        return getPackageManager(context).getApplicationLabel(Objects.requireNonNull(getAppInfo(
                packageName, context))) + (isEnabled(packageName, context) ? "" : " (Disabled)");
    }

    public static Drawable getAppIcon(String packageName, Context context) {
        return getPackageManager(context).getApplicationIcon(Objects.requireNonNull(getAppInfo(packageName, context)));
    }

    public static String getSourceDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).sourceDir;
    }

    public static String getNativeLibDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).nativeLibraryDir;
    }

    public static String getDataDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).dataDir;
    }

    public static String getVersionName(String path, Context context) {
        return Objects.requireNonNull(getPackageManager(context).getPackageArchiveInfo(path, 0)).versionName;
    }

    public static String getInstalledDate(String path, Context context) {
        return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(getPackageInfo(path, context)).firstInstallTime);
    }

    public static String getUpdatedDate(String path, Context context) {
        return DateFormat.getDateTimeInstance().format(Objects.requireNonNull(getPackageInfo(path, context)).lastUpdateTime);
    }

    public static boolean isEnabled(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).enabled;
    }

    public static boolean isSystemApp(String packageName, Context context) {
        return (Objects.requireNonNull(getAppInfo(packageName, context)).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static String getPackageDir(Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            return Objects.requireNonNull(context.getExternalFilesDir("")).toString();
        } else {
            return Environment.getExternalStorageDirectory().toString() + "/Package_Manager";
        }
    }

    public static String getPermissions(String packageName, Context context) {
        List<String> perms = new ArrayList<>();
        try {
            for (int i = 0; i < Objects.requireNonNull(getPackageInfo(packageName, context)).requestedPermissions.length; i++) {
                if ((Objects.requireNonNull(getPackageInfo(packageName, context)).requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    perms.add(Objects.requireNonNull(getPackageInfo(packageName, context)).requestedPermissions[i]);
                }
            }
        } catch (NullPointerException ignored) {
        }
        return perms.toString().replace("[","").replace("]","").replace(", ","\n");
    }

    public static void clearAppSettings(String packageID) {
        Utils.runCommand("pm clear " + packageID);
    }

    public static List<String> splitApks(String path) {
        List<String> list = new ArrayList<>();
        if (new File(path).exists()) {
            for (File mFile : Objects.requireNonNull(new File(path).listFiles())) {
                if (mFile.exists()) {
                    list.add(mFile.getAbsolutePath());
                }
            }
        }
        return list;
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

    public static String getBatchList() {
        return mBatchList.toString().substring(1, mBatchList.toString().length()-1);
    }

    public static String showBatchList() {
        String[] array = getBatchList().trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            if (s != null && !s.isEmpty())
                sb.append(" - ").append(s.replaceAll(","," ")).append("\n");
        }
        return "\n" + sb.toString();
    }

}