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

import com.google.android.material.card.MaterialCardView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageTasksActivity;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageTasks {

    public static boolean mReloadPage = true;
    public static StringBuilder mOutput = null;
    public static boolean mRunning = false;
    public static MaterialCardView mBatchOptions;

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
                mOutput.append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent turnOffIntent = new Intent(activity, PackageTasksActivity.class);
                turnOffIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                turnOffIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(turnOffIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = PackageData.getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".")) {
                        mOutput.append(PackageData.isEnabled(packageID, activity) ? "** " +
                                activity.getString(R.string.disabling, PackageData.getAppName(packageID, activity)) :
                                "** " + activity.getString(R.string.enabling, PackageData.getAppName(packageID, activity)));
                        if (PackageData.isEnabled(packageID, activity)) {
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
                PackageTasks.mReloadPage = true;
            }
        }.execute();
    }

    public static void batchResetTask(Activity activity) {
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
                mOutput.append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(activity, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(removeIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = PackageData.getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".") && Utils.isPackageInstalled(packageID, activity)) {
                        mOutput.append("** ").append(activity.getString(R.string.reset_summary, PackageData.getAppName(packageID, activity)));
                        Utils.runCommand("pm clear " + packageID);
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
            }
        }.execute();
    }

    public static void batchExportTask(Activity activity) {
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
                mOutput.append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(activity, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(removeIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = PackageData.getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".") && Utils.isPackageInstalled(packageID, activity)) {
                        if (SplitAPKInstaller.isAppBundle(PackageData.getParentDir(packageID, activity))) {
                            if (Utils.exist(PackageData.getPackageDir(activity) + "/" + packageID)) {
                                mOutput.append("** ").append(activity.getString(R.string.already_exists, packageID)).append(" *\n\n");
                            } else {
                                Utils.mkdir(PackageData.getPackageDir(activity) + "/" + packageID);
                                mOutput.append("** ").append(activity.getString(R.string.exporting_bundle, PackageData.getAppName(packageID, activity)));
                                for (final String splitApps : SplitAPKInstaller.splitApks(PackageData.getParentDir(packageID, activity))) {
                                    Utils.copy(PackageData.getParentDir(packageID, activity) + "/" + splitApps, PackageData.getPackageDir(activity) + "/" + packageID + "/" + splitApps);
                                }
                                mOutput.append(": ").append(activity.getString(R.string.done)).append(" *\n\n");
                            }
                        } else {
                            mOutput.append("** ").append(activity.getString(R.string.exporting, PackageData.getAppName(packageID, activity)));
                            Utils.copy(PackageData.getSourceDir(packageID, activity), PackageData.getPackageDir(activity) + "/" + packageID + ".apk");
                            mOutput.append(": ").append(activity.getString(R.string.done)).append(" *\n\n");
                        }
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
            }
        }.execute();
    }

    public static void batchUninstallTask(Activity activity) {
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
                mOutput.append("** ").append(activity.getString(R.string.batch_list_summary)).append(PackageData.showBatchList()).append("\n\n");
                Intent removeIntent = new Intent(activity, PackageTasksActivity.class);
                removeIntent.putExtra(PackageTasksActivity.TITLE_START, activity.getString(R.string.batch_processing));
                removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, activity.getString(R.string.batch_processing_finished));
                activity.startActivity(removeIntent);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] batchApps = PackageData.getBatchList().replaceAll(","," ").split(" ");
                for (String packageID : batchApps) {
                    if (packageID.contains(".") && Utils.isPackageInstalled(packageID, activity)) {
                        mOutput.append("** ").append(activity.getString(R.string.uninstall_summary, PackageData.getAppName(packageID, activity)));
                        Utils.runCommand("pm uninstall --user 0 " + packageID);
                        mOutput.append(Utils.isPackageInstalled(packageID, activity) ? ": " +
                                activity.getString(R.string.failed) + " *\n\n" : ": " + activity.getString(R.string.done) + " *\n\n");
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
            }
        }.execute();
    }

}