/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2020
 */
public class PackageDetails {

    @SuppressLint("StringFormatInvalid")
    public static void exportApp(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        if (Build.VERSION.SDK_INT < 30 && Utils.isPermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.permission_denied_write_storage));
        } else if (new File(PackageData.getSourceDir(Common.getApplicationID(), activity)).getName().equals("base.apk") && SplitAPKInstaller.splitApks(PackageData.getParentDir(Common.getApplicationID(), activity)).size() > 1) {
            exportingBundleTask(linearLayout, textView, PackageData.getParentDir(Common.getApplicationID(), activity), Common.getApplicationID(),
                    Common.getApplicationIcon(), activity);
        } else {
            exportingTask(linearLayout, textView, Common.getSourceDir(), Common.getApplicationID(), Common.getApplicationIcon(), activity);
        }
    }

    public static void exportingTask(LinearLayout linearLayout, MaterialTextView textView, String apk, String name, Drawable icon, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StringFormatInvalid")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(linearLayout, textView, activity.getString(R.string.exporting, name) + "...");
                PackageData.makePackageFolder(activity);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                Utils.copy(apk, PackageData.getPackageDir(activity) + "/" + name + ".apk");
                return null;
            }
            @SuppressLint("StringFormatInvalid")
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress(linearLayout, textView);
                new MaterialAlertDialogBuilder(activity)
                        .setIcon(icon)
                        .setTitle(activity.getString(R.string.share) + " " + name + "?")
                        .setMessage(name + " " + activity.getString(R.string.export_summary, PackageData.getPackageDir(activity)))
                        .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(activity.getString(R.string.share), (dialog, id) -> {
                            Uri uriFile = FileProvider.getUriForFile(activity,
                                    BuildConfig.APPLICATION_ID + ".provider", new File(PackageData.getPackageDir(activity), name + ".apk"));
                            Intent shareScript = new Intent(Intent.ACTION_SEND);
                            shareScript.setType("application/java-archive");
                            shareScript.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.shared_by, name));
                            shareScript.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                            shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                            shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(Intent.createChooser(shareScript, activity.getString(R.string.share_with)));
                        }).show();
            }
        }.execute();
    }

    public static void exportingBundleTask(LinearLayout linearLayout, MaterialTextView textView, String apk, String name, Drawable icon, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StringFormatInvalid")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(linearLayout, textView, activity.getString(R.string.exporting_bundle, name) + "...");
                PackageData.makePackageFolder(activity);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                List<File> mFiles = new ArrayList<>();
                for (final String splitApps : SplitAPKInstaller.splitApks(apk)) {
                    mFiles.add(new File(apk + "/" + splitApps));
                }
                Utils.zip(PackageData.getPackageDir(activity) + "/" + name + ".apkm", mFiles);
                return null;
            }
            @SuppressLint("StringFormatInvalid")
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress(linearLayout, textView);
                new MaterialAlertDialogBuilder(activity)
                        .setIcon(icon)
                        .setTitle(name)
                        .setMessage(activity.getString(R.string.export_bundle_summary, PackageData.getPackageDir(activity) + "/" + name + ".apkm"))
                        .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(activity.getString(R.string.share), (dialog, id) -> {
                            Uri uriFile = FileProvider.getUriForFile(activity,
                                    BuildConfig.APPLICATION_ID + ".provider", new File(PackageData.getPackageDir(activity) + "/" + name + ".apkm"));
                            Intent shareScript = new Intent(Intent.ACTION_SEND);
                            shareScript.setType("application/zip");
                            shareScript.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.shared_by, name));
                            shareScript.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                            shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                            shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(Intent.createChooser(shareScript, activity.getString(R.string.share_with)));
                        }).show();
            }
        }.execute();
    }

    public static void disableApp(LinearLayout progressLayout, LinearLayout openApp, MaterialTextView progressMessage,
                                  MaterialTextView statusMessage, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StringFormatInvalid")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(progressLayout, progressMessage, PackageData.isEnabled(Common.getApplicationID(), activity) ?
                        activity.getString(R.string.disabling, Common.getApplicationName()) + "..." :
                        activity.getString(R.string.enabling, Common.getApplicationName()) + "...");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                if (PackageData.isEnabled(Common.getApplicationID(), activity)) {
                    Utils.runCommand("pm disable " + Common.getApplicationID());
                } else {
                    Utils.runCommand("pm enable " + Common.getApplicationID());
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress(progressLayout, progressMessage);
                statusMessage.setText(PackageData.isEnabled(Common.getApplicationID(), activity) ? R.string.disable : R.string.enable);
                openApp.setVisibility(PackageData.isEnabled(Common.getApplicationID(), activity) ? View.VISIBLE : View.GONE);
                Common.reloadPage(true);
            }
        }.execute();
    }

    @SuppressLint("StringFormatInvalid")
    public static void uninstallSystemApp(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        if (Utils.rootAccess()) {
            new MaterialAlertDialogBuilder(activity)
                    .setIcon(Common.getApplicationIcon())
                    .setTitle(activity.getString(R.string.uninstall_title, Common.getApplicationName()))
                    .setMessage(activity.getString(R.string.uninstall_warning))
                    .setCancelable(false)
                    .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, id) -> new AsyncTask<Void, Void, Void>() {
                        @SuppressLint("StringFormatInvalid")
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            showProgress(linearLayout, textView, activity.getString(R.string.uninstall_summary, Common.getApplicationName()));
                        }
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Utils.sleep(1);
                            Utils.runCommand("pm uninstall --user 0 " + Common.getApplicationID());
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            hideProgress(linearLayout, textView);
                            activity.finish();
                            Common.reloadPage(true);
                        }
                    }.execute())
                    .show();
        } else {
            new MaterialAlertDialogBuilder(activity)
                    .setIcon(Common.getApplicationIcon())
                    .setTitle(activity.getString(R.string.uninstall_adb))
                    .setMessage(activity.getString(R.string.uninstall_adb_summary, Common.getApplicationName()) +
                            "\n\nadb shell pm uninstall -k --user 0 " + Common.getApplicationID())
                    .setNegativeButton(activity.getString(R.string.documentation), (dialog, id) ->
                            Utils.launchUrl("https://smartpack.github.io/adb-debloating/", activity))
                    .setPositiveButton(activity.getString(R.string.got_it), (dialog, id) -> {
                    })
                    .show();
        }
    }

    public static List<String> getPermissions(String packageName, Context context) {
        List<String> perms = new ArrayList<>();
        try {
            if (getPermissionsGranted(packageName, context).size() > 1) {
                perms.addAll(getPermissionsGranted(packageName, context));
            }
            if (getPermissionsDenied(packageName, context).size() > 1) {
                perms.addAll(getPermissionsDenied(packageName, context));
            }
        } catch (NullPointerException ignored) {}
        return perms;
    }

    public static List<String> getPermissionsGranted(String packageName, Context context) {
        List<String> perms = new ArrayList<>();
        try {
            perms.add("Granted");
            for (int i = 0; i < Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissions.length; i++) {
                if ((Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    perms.add(Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissions[i]);
                }
            }
        } catch (NullPointerException ignored) {
        }
        return perms;
    }

    public static List<String> getPermissionsDenied(String packageName, Context context) {
        List<String> perms = new ArrayList<>();
        try {
            perms.add("Denied");
            for (int i = 0; i < Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissions.length; i++) {
                if ((Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                    perms.add(Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissions[i]);
                }
            }
        } catch (NullPointerException ignored) {
        }
        return perms;
    }

    public static List<ActivityInfo> getActivities(String packageName, Context context) {
        List<ActivityInfo> activities = new ArrayList<>();
        try {
            try {
                ActivityInfo[] list = PackageData.getPackageManager(context).getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities;
                activities.addAll(Arrays.asList(list));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        } catch (NullPointerException ignored) {}
        return activities;
    }

    private static void showProgress(LinearLayout linearLayout, MaterialTextView textView, String message) {
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private static void hideProgress(LinearLayout linearLayout, MaterialTextView textView) {
        textView.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
    }

}