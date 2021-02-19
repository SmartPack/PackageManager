/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.Objects;

/*
 *
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2020
 *
 */
public class PackageDetails {

    public static void exportApp(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.permission_denied_write_storage));
        } else if (SplitAPKInstaller.splitApks(PackageData.getParentDir(PackageData.mApplicationID, activity)).size() > 1) {
            if (Utils.exist(PackageData.getPackageDir(activity) + "/" + PackageData.mApplicationID)) {
                Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.already_exists, PackageData.mApplicationID));
            } else {
                exportingBundleTask(linearLayout, textView, PackageData.getParentDir(PackageData.mApplicationID, activity), PackageData.mApplicationID,
                        PackageData.mApplicationIcon, activity);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
        } else {
            exportingTask(linearLayout, textView, PackageData.mDirSource, PackageData.mApplicationID, PackageData.mApplicationIcon, activity);
        }
    }

    public static void exportingTask(LinearLayout linearLayout, MaterialTextView textView, String apk, String name, Drawable icon, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
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
                                    BuildConfig.APPLICATION_ID + ".provider", new File(PackageData.getPackageDir(activity) + "/" + name + ".apk"));
                            Intent shareScript = new Intent(Intent.ACTION_SEND);
                            shareScript.setType("application/java-archive");
                            shareScript.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.shared_by, name));
                            shareScript.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                            shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                            shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(Intent.createChooser(shareScript, activity.getString(R.string.share_with)));
                        })

                        .show();
            }
        }.execute();
    }

    public static void exportingBundleTask(LinearLayout linearLayout, MaterialTextView textView, String apk, String name, Drawable icon, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(linearLayout, textView, activity.getString(R.string.exporting_bundle, name) + "...");
                PackageData.makePackageFolder(activity);
                Utils.mkdir(PackageData.getPackageDir(activity) + "/" + name);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                for (final String splitApps : SplitAPKInstaller.splitApks(apk)) {
                    Utils.copy(apk + "/" + splitApps, PackageData.getPackageDir(activity) + "/" + name + "/" + splitApps);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress(linearLayout, textView);
                new MaterialAlertDialogBuilder(activity)
                        .setIcon(icon)
                        .setTitle(name)
                        .setMessage(activity.getString(R.string.export_bundle_summary, PackageData.getPackageDir(activity)))
                        .setPositiveButton(R.string.cancel, (dialog, id) -> {
                        })

                        .show();
            }
        }.execute();
    }

    public static void disableApp(LinearLayout progressLayout, LinearLayout openApp, MaterialTextView progressMessage,
                                  MaterialTextView statusMessage, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(progressLayout, progressMessage, PackageData.isEnabled(PackageData.mApplicationID, activity) ?
                        activity.getString(R.string.disabling, PackageData.mApplicationName) + "..." :
                        activity.getString(R.string.enabling, PackageData.mApplicationName) + "...");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                if (PackageData.isEnabled(PackageData.mApplicationID, activity)) {
                    Utils.runCommand("pm disable " + PackageData.mApplicationID);
                } else {
                    Utils.runCommand("pm enable " + PackageData.mApplicationID);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress(progressLayout, progressMessage);
                statusMessage.setText(PackageData.isEnabled(PackageData.mApplicationID, activity) ? R.string.disable : R.string.enable);
                openApp.setVisibility(PackageData.isEnabled(PackageData.mApplicationID, activity) ? View.VISIBLE : View.GONE);
                PackageTasks.mReloadPage = true;
            }
        }.execute();
    }

    public static void uninstallApp(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        if (PackageData.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
            Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.uninstall_nope));
        } else if (!PackageData.mSystemApp) {
            Intent remove = new Intent(Intent.ACTION_DELETE);
            remove.setData(Uri.parse("package:" + PackageData.mApplicationID));
            activity.startActivity(remove);
            PackageTasks.mReloadPage = true;
            activity.finish();
        } else {
            if (Utils.rootAccess()) {
                new MaterialAlertDialogBuilder(activity)
                        .setIcon(PackageData.mApplicationIcon)
                        .setTitle(activity.getString(R.string.uninstall_title, PackageData.mApplicationName))
                        .setMessage(activity.getString(R.string.uninstall_warning))
                        .setCancelable(false)
                        .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(activity.getString(R.string.yes), (dialog, id) -> {
                            removeSystemApp(linearLayout, textView, activity);
                        })
                        .show();
            } else {
                Utils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.no_root));
            }
        }
    }

    public static void removeSystemApp(LinearLayout linearLayout, MaterialTextView textView, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(linearLayout, textView, activity.getString(R.string.uninstall_summary, PackageData.mApplicationName));
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                Utils.runCommand("pm uninstall --user 0 " + PackageData.mApplicationID);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress(linearLayout, textView);
                activity.finish();
                PackageTasks.mReloadPage = true;
            }
        }.execute();
    }

    public static List<String> getPermissions(String packageName, Context context) {
        List<String> perms = new ArrayList<>();
        if (getPermissionsGranted(packageName, context).size() > 1) {
            perms.addAll(getPermissionsGranted(packageName, context));
        }
        if (getPermissionsDenied(packageName, context).size() > 1) {
            perms.addAll(getPermissionsDenied(packageName, context));
        }
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