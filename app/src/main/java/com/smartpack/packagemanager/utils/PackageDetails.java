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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.ADBUninstallActivity;
import com.smartpack.packagemanager.utils.SerializableItems.ActivityItems;
import com.smartpack.packagemanager.utils.SerializableItems.PermissionsItems;
import com.smartpack.packagemanager.utils.tasks.ExportAPKTasks;
import com.smartpack.packagemanager.utils.tasks.ExportBundleTasks;
import com.smartpack.packagemanager.utils.tasks.UninstallSystemAppsTasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2020
 */
public class PackageDetails {

    public static void exportApp(String packageName, String sourceDir, Drawable appIcon, Activity activity) {
        if (new File(sPackageUtils.getSourceDir(packageName, activity)).getName().equals("base.apk") && SplitAPKInstaller.splitApks(sPackageUtils.getParentDir(packageName, activity)).size() > 1) {
            new ExportBundleTasks(packageName, sPackageUtils.getParentDir(packageName, activity), PackageData.getFileName(packageName, activity),
                    appIcon, activity).execute();
        } else {
            new ExportAPKTasks(packageName, sourceDir, PackageData.getFileName(packageName, activity), appIcon, activity).execute();
        }
    }

    @SuppressLint("StringFormatInvalid")
    public static void uninstallSystemApp(String packageName, String appName, Drawable appIcon, Activity activity) {
        if (new RootShell().rootAccess() || new ShizukuShell().isReady()) {
            new MaterialAlertDialogBuilder(activity)
                    .setIcon(appIcon)
                    .setTitle(activity.getString(R.string.uninstall_title, appName))
                    .setMessage(activity.getString(R.string.uninstall_warning))
                    .setCancelable(false)
                    .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, id) ->
                            new UninstallSystemAppsTasks(packageName, appName, activity).execute())
                    .show();
        } else {
            Intent details = new Intent(activity, ADBUninstallActivity.class);
            details.putExtra(ADBUninstallActivity.PACKAGE_INTENT, packageName);
            activity.startActivity(details);
        }
    }

    public static List<PermissionsItems> getPermissions(String packageName, Context context) {
        List<PermissionsItems> perms = new ArrayList<>();
        try {
            for (int i = 0; i < Objects.requireNonNull(PackageData.getPackageInfo(packageName, context)).requestedPermissions.length; i++) {
                PackageInfo perm = Objects.requireNonNull(PackageData.getPackageInfo(packageName, context));
                perms.add(new PermissionsItems((perm.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0,
                        perm.requestedPermissions[i], sPermissionUtils.getDescription(perm.requestedPermissions[i]
                        .replace("android.permission.",""), context)));
            }
        } catch (NullPointerException ignored) {
        }
        return perms;
    }

    public static List<ActivityItems> getActivities(String packageName, Context context) {
        List<ActivityItems> activities = new ArrayList<>();

        try {
            ActivityInfo[] list = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities;
            if (list != null) {
                for (ActivityInfo info : list) {
                    activities.add(new ActivityItems(info.name, info.labelRes == 0 ? getAdjustedLabel(info.name) : info.loadLabel(context.getPackageManager()), info.loadIcon(context.getPackageManager()), info.exported));
                }
            }
        } catch (PackageManager.NameNotFoundException | NullPointerException ignored) {
        }

        return activities;
    }

    private static String getAdjustedLabel(String label) {
        int lastDot = label.lastIndexOf(".");
        if (lastDot != -1) {
            return label.substring(lastDot + 1);
        } else {
            return null;
        }
    }

    public static JSONObject getPackageDetails(String packageName, Context context) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("Name", PackageData.getAppName(packageName, context));
            obj.put("Package Name", packageName);
            obj.put("Version", sAPKUtils.getVersionName(sPackageUtils.getSourceDir(packageName, context), context));
            obj.put("Google Play", "https://play.google.com/store/apps/details?id=" + packageName);
            if (new File(sPackageUtils.getSourceDir(packageName, context)).getName().equals("base.apk") && SplitAPKInstaller
                    .splitApks(sPackageUtils.getParentDir(packageName, context)).size() > 1) {
                obj.put("App Bundle", true);
                obj.put("Bundle Size", PackageData.getBundleSize(sPackageUtils.getParentDir(packageName, context)));
                JSONArray apks = new JSONArray();
                for (String apk : SplitAPKInstaller
                        .splitApks(sPackageUtils.getParentDir(packageName, context))) {
                    apks.put(apk);
                }
                obj.put("Split APKs", apks);

            } else {
                obj.put("App Bundle", false);
                obj.put("APK Size", sAPKUtils.getAPKSize(new File(sPackageUtils.getSourceDir(packageName ,context)).length()));
            }
            obj.put("Installed", sPackageUtils.getInstalledDate(packageName, context));
            obj.put("Last updated", sPackageUtils.getUpdatedDate(packageName, context));
            JSONObject permissions = new JSONObject();
            JSONArray granted = new JSONArray();
            for (PermissionsItems grantedPermissions : PackageDetails.getPermissions(packageName, context)) {
                if (grantedPermissions.isGranted()) {
                    granted.put(grantedPermissions.getTitle());
                }
            }
            permissions.put("Granted", granted);
            JSONArray denied = new JSONArray();
            for (PermissionsItems grantedPermissions : PackageDetails.getPermissions(packageName, context)) {
                if (!grantedPermissions.isGranted()) {
                    granted.put(grantedPermissions.getTitle());
                }
            }
            permissions.put("Denied", denied);
            obj.put("Permissions", permissions);
            return obj;
        } catch (JSONException ignored) {
        }
        return null;
    }

}