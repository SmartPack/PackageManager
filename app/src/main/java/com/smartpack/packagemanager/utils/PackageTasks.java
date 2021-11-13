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
import android.content.Intent;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageTasksActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */
public class PackageTasks {

    public static void batchDisableTask(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                Common.isRunning(true);
                Common.getOutput().setLength(0);
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_processing_initialized)).append("...\n\n");
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent turnOffIntent = new Intent(activity, PackageTasksActivity.class);
                turnOffIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                turnOffIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(turnOffIntent);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void doInBackground() {
                for (String packageID : Common.getBatchList()) {
                    if (packageID.contains(".")) {
                        if (packageID.equals(activity.getPackageName())) {
                            Common.getOutput().append("** ").append(activity.getString(R.string.disabling, PackageData.getAppName(packageID, activity)));
                            Common.getOutput().append(": ").append(activity.getString(R.string.uninstall_nope)).append(" *\n\n");
                        } else {
                            Common.getOutput().append(sPackageUtils.isEnabled(packageID, activity) ? "** " +
                                    activity.getString(R.string.disabling, PackageData.getAppName(packageID, activity)) :
                                    "** " + activity.getString(R.string.enabling, PackageData.getAppName(packageID, activity)));
                            if (sPackageUtils.isEnabled(packageID, activity)) {
                                Utils.runCommand("pm disable " + packageID);
                            } else {
                                Utils.runCommand("pm enable " + packageID);
                            }
                            Common.getOutput().append(": ").append(activity.getString(R.string.done)).append(" *\n\n");
                        }
                        sUtils.sleep(1);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                Common.getOutput().append("** ").append(activity.getString(R.string.everything_done)).append(" *");
                Common.isRunning(false);
                Common.reloadPage(true);
            }
        }.execute();
    }

    public static void batchResetTask(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                Common.isRunning(true);
                Common.getOutput().setLength(0);
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_processing_initialized)).append("...\n\n");
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(activity, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(removeIntent);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void doInBackground() {
                for (String packageID : Common.getBatchList()) {
                    if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, activity)) {
                        if (packageID.equals(activity.getPackageName())) {
                            Common.getOutput().append("** ").append(activity.getString(R.string.reset_summary, PackageData.getAppName(packageID, activity)));
                            Common.getOutput().append(": ").append(activity.getString(R.string.uninstall_nope)).append(" *\n\n");
                        } else {
                            Common.getOutput().append("** ").append(activity.getString(R.string.reset_summary, PackageData.getAppName(packageID, activity)));
                            Utils.runCommand("pm clear " + packageID);
                            Common.getOutput().append(": ").append(activity.getString(R.string.done)).append(" *\n\n");
                        }
                        sUtils.sleep(1);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                Common.getOutput().append("** ").append(activity.getString(R.string.everything_done)).append(" *");
                Common.isRunning(false);
            }
        }.execute();
    }

    public static void batchExportTask(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                Common.isRunning(true);
                Common.getOutput().setLength(0);
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_processing_initialized)).append("...\n\n");
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(activity, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(removeIntent);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void doInBackground() {
                for (String packageID : Common.getBatchList()) {
                    if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, activity)) {
                        if (SplitAPKInstaller.isAppBundle(sPackageUtils.getParentDir(packageID, activity))) {
                            Common.getOutput().append("** ").append(activity.getString(R.string.exporting_bundle, PackageData.getAppName(packageID, activity)));
                            List<File> mFiles = new ArrayList<>();
                            for (final String splitApps : SplitAPKInstaller.splitApks(sPackageUtils.getParentDir(packageID, activity))) {
                                mFiles.add(new File(sPackageUtils.getParentDir(packageID, activity) + "/" + splitApps));
                            }
                            Utils.zip(PackageData.getPackageDir(activity) + "/" + PackageData.getFileName(packageID, activity) + ".apkm", mFiles);
                        } else {
                            Common.getOutput().append("** ").append(activity.getString(R.string.exporting, PackageData.getAppName(packageID, activity)));
                            sUtils.copy(new File(sPackageUtils.getSourceDir(packageID, activity)), new File(PackageData.getPackageDir(activity), PackageData.getFileName(packageID, activity) + ".apk"));
                        }
                        Common.getOutput().append(": ").append(activity.getString(R.string.done)).append(" *\n\n");
                        sUtils.sleep(1);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                Common.getOutput().append("** ").append(activity.getString(R.string.everything_done)).append(" *");
                Common.isRunning(false);
            }
        }.execute();
    }

    public static void batchUninstallTask(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                Common.isRunning(true);
                Common.getOutput().setLength(0);
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_processing_initialized)).append("...\n\n");
                Common.getOutput().append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(activity, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(removeIntent);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void doInBackground() {
                for (String packageID : Common.getBatchList()) {
                    if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, activity)) {
                        if (packageID.equals(activity.getPackageName())) {
                            Common.getOutput().append("** ").append(activity.getString(R.string.uninstall_summary, PackageData.getAppName(packageID, activity)));
                            Common.getOutput().append(": ").append(activity.getString(R.string.uninstall_nope)).append(" *\n\n");
                        } else {
                            Common.getOutput().append("** ").append(activity.getString(R.string.uninstall_summary, PackageData.getAppName(packageID, activity)));
                            Utils.runCommand("pm uninstall --user 0 " + packageID);
                            Common.getOutput().append(sPackageUtils.isPackageInstalled(packageID, activity) ? ": " +
                                    activity.getString(R.string.failed) + " *\n\n" : ": " + activity.getString(R.string.done) + " *\n\n");
                        }
                        sUtils.sleep(1);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                PackageData.setRawData(activity);
                Common.getOutput().append("** ").append(activity.getString(R.string.everything_done)).append(" *");
                Common.isRunning(false);
                Common.reloadPage(true);
            }
        }.execute();
    }

}