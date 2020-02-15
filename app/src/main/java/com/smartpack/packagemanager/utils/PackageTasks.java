/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.io.File;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageTasks {

    private static final String DOWNLOADS = Environment.getExternalStorageDirectory().toString() + "/Download";

    public static void exportingTask(String apk, String name, Drawable icon, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exporting, name) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                Utils.copy(apk, DOWNLOADS + "/" + name + ".apk");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (Utils.existFile(DOWNLOADS + "/" + name + ".apk")) {
                    new Dialog(context)
                            .setIcon(icon)
                            .setMessage(name + " " + context.getString(R.string.export_summary, DOWNLOADS))
                            .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(context.getString(R.string.share), (dialog, id) -> {
                                Uri uriFile = FileProvider.getUriForFile(context,
                                        BuildConfig.APPLICATION_ID + ".provider", new File(DOWNLOADS + "/" + name + ".apk"));
                                Intent shareScript = new Intent(Intent.ACTION_SEND);
                                shareScript.setType("application/java-archive");
                                shareScript.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_by, name));
                                shareScript.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                                shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                                shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                context.startActivity(Intent.createChooser(shareScript, context.getString(R.string.share_with)));
                            })

                            .show();
                }
            }
        }.execute();
    }

    public static void disableApp(String app, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(isEnabled(app, context) ?
                        context.getString(R.string.disabling, app) + "..." :
                        context.getString(R.string.enabling, app) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                if (isEnabled(app, context)) {
                    RootUtils.runCommand("pm disable " + app);
                } else {
                    RootUtils.runCommand("pm enable " + app);
                }
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

    public static void removeSystemApp(String app, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.uninstall_summary, app));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                RootUtils.runCommand("pm uninstall --user 0 " + app);
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

    public static void backupApp(String app, String name) {
        Utils.sleep(2);
        RootUtils.runCommand("tar -zcvf " + DOWNLOADS + "/" +
                name + " /data/data/" + app);
    }

    public static void restoreApp(String path, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.restoring, path) + "...");
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

    public static boolean isEnabled(String app, Context context) {
        try {
            ApplicationInfo ai =
                    context.getPackageManager().getApplicationInfo(app, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

}