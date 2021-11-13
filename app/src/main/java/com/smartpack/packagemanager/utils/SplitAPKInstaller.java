/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.InstallerActivity;
import com.smartpack.packagemanager.services.SplitAPKInstallService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sInstallerParams;
import in.sunilpaulmathew.sCommon.Utils.sInstallerUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2020
 */
public class SplitAPKInstaller {

    public static boolean isAppBundle(String path) {
        return splitApks(path).size() > 1;
    }

    private static long getTotalSize() {
        int totalSize = 0;
        if (Common.getAppList().size() > 0) {
            for (String string : Common.getAppList()) {
                if (sUtils.exist(new File(string))) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
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

    public static void handleAppBundle(LinearLayout linearLayout, String path, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                linearLayout.setVisibility(View.VISIBLE);
                sUtils.delete(new File(activity.getCacheDir().getPath(), "splits"));
                if (sUtils.exist(new File(activity.getCacheDir().getPath(), "toc.pb"))) {
                    sUtils.delete(new File(activity.getCacheDir().getPath(), "toc.pb"));
                }
            }

            @Override
            public void doInBackground() {
                if (path.endsWith(".apks")) {
                    Utils.unzip(path,  activity.getCacheDir().getPath());
                } else if (path.endsWith(".xapk") || path.endsWith(".apkm")) {
                    Utils.unzip(path,  activity.getCacheDir().getPath() + "/splits");
                }
            }

            @Override
            public void onPostExecute() {
                linearLayout.setVisibility(View.GONE);
                Common.getAppList().clear();
                Common.setPath(activity.getCacheDir().getPath() + "/splits");
                Intent filePicker = new Intent(activity, FilePickerActivity.class);
                activity.startActivity(filePicker);
            }
        }.execute();
    }

    public static void installSplitAPKs(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                sUtils.saveString("installationStatus", "waiting", activity);
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                long totalSize = getTotalSize();
                int sessionId;
                final sInstallerParams installParams = sInstallerUtils.makeInstallParams(totalSize);
                sessionId = sInstallerUtils.runInstallCreate(installParams, activity);
                try {
                    for (String string : Common.getAppList()) {
                        if (sUtils.exist(new File(string)) && string.endsWith(".apk")) {
                            File mFile = new File(string);
                            if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    }
                } catch (NullPointerException ignored) {}
                sInstallerUtils.doCommitSession(sessionId, getInstallerCallbackIntent(activity), activity);
            }

            @Override
            public void onPostExecute() {
            }
        }.execute();
    }

    private static Intent getInstallerCallbackIntent(Context context) {
        return new Intent(context, SplitAPKInstallService.class);
    }

}