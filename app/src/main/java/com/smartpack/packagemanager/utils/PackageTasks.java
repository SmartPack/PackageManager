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
import com.smartpack.packagemanager.utils.root.RootFile;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.io.File;
import java.util.List;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 12, 2020
 */

public class PackageTasks {

    private static final String PACKAGES = Environment.getExternalStorageDirectory().toString() + "/Package_Manager";

    public static StringBuilder mBatchApps = null;

    private static void makePackageFolder() {
        File file = new File(PACKAGES);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }

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
                    Utils.getInstance().showInterstitialAd(context);
                    new Dialog(context)
                            .setIcon(icon)
                            .setTitle(context.getString(R.string.share) + " " + name + "?")
                            .setMessage(name + " " + context.getString(R.string.export_summary, PACKAGES))
                            .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(context.getString(R.string.share), (dialog, id) -> {
                                Uri uriFile = FileProvider.getUriForFile(context,
                                        BuildConfig.APPLICATION_ID + ".provider", new File(PACKAGES + "/" + name + ".apk"));
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

    public static void exportingBundleTask(String apk, String name, Drawable icon, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exporting_bundle, name) + "...");
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
                    Utils.getInstance().showInterstitialAd(context);
                    new Dialog(context)
                            .setIcon(icon)
                            .setTitle(name)
                            .setMessage(context.getString(R.string.export_bundle_summary, PACKAGES))
                            .setPositiveButton(context.getString(R.string.cancel), (dialog, id) -> {
                            })

                            .show();
                }
            }
        }.execute();
    }

    public static void disableApp(String app, String name, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(isEnabled(app, context) ?
                        context.getString(R.string.disabling, name) + "..." :
                        context.getString(R.string.enabling, name) + "...");
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

    public static void removeSystemApp(String app, String name, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.uninstall_summary, name));
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
        makePackageFolder();
        Utils.sleep(2);
        RootUtils.runCommand("tar -zcvf " + PACKAGES + "/" +
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

    public static List<String> splitApks(String string) {
        RootFile file = new RootFile(string);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.list();
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

    /*
     * Inspired from the original implementation of split apk installer by @yeriomin on https://github.com/yeriomin/YalpStore/
     * Ref: https://github.com/yeriomin/YalpStore/blob/master/app/src/main/java/com/github/yeriomin/yalpstore/install/InstallerRoot.java
     */
    private static String installSplitAPKs(String dir) {
        RootUtils.runCommand("pm install-create | tee '" + PACKAGES + "'/installer_log");
        RootUtils.runCommand("mkdir /data/local/tmp/pm/");
        RootUtils.runCommand("cp " + dir + "/* /data/local/tmp/pm/");
        String sid = Utils.readFile(PACKAGES + "/installer_log").replace("Success: created install session [",
                "").replace("]", "");
        Utils.create("# Split APKs Installer log\n# Created by Package Manager\n", PACKAGES + "/installer_log");
        Utils.append("Session ID: " + sid, PACKAGES + "/installer_log");
        Utils.append("\nBundle Path: " + dir, PACKAGES + "/installer_log");
        Utils.append("\n# List of split APKs", PACKAGES + "/installer_log");
        for (final String splitApps : splitApks("/data/local/tmp/pm")) {
            File file = new File("/data/local/tmp/pm/" + splitApps);
            RootUtils.runCommand("pm install-write -S " + file.length() + " " + sid + " " + file.getName() + " " + file.toString());
            Utils.append(file.getName() + ": " + file.length() + " KB", PACKAGES + "/installer_log");
        }
        RootUtils.runCommand("rm -r /data/local/tmp/pm/");
        return RootUtils.runCommand("pm install-commit " + sid);
    }

    public static void installSplitAPKs(String dir, Context context){
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.installing) + " ...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                return installSplitAPKs(dir);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (s != null && !s.isEmpty()) {
                    Utils.append("\n# Result: " + s + "\n\n# The END", PACKAGES + "/installer_log");
                    Dialog result = new Dialog(context);
                    if (s.equals("Success")) {
                        result.setMessage(R.string.install_success);
                    } else {
                        result.setMessage(s);
                    }
                    result.setCancelable(false);
                    result.setPositiveButton("Cancel", (dialog, id) -> {
                    });
                    result.show();
                } else {
                    Utils.append("\n# Result: Installation failed\n\n# The END", PACKAGES + "/installer_log");
                }
            }
        }.execute();
    }

}