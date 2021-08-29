/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.view.View;
import android.widget.LinearLayout;

import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.SplitAPKInstallerActivity;
import com.smartpack.packagemanager.services.SplitAPKInstallService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2020
 * Based on the original work of nkalra0123 (Ref: https://github.com/nkalra0123/splitapkinstall)
 * & Aefyr (Ref: https://github.com/Aefyr/SAI)
 */
public class SplitAPKInstaller {

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

    private static int runInstallCreate(InstallParams installParams, Activity activity) {
        return doCreateSession(installParams.sessionParams, activity);
    }

    private static int doCreateSession(PackageInstaller.SessionParams params, Activity activity) {
        int sessionId = 0 ;
        try {
            sessionId = getPackageInstaller(activity).createSession(params);
        } catch (IOException ignored) {
        }
        return sessionId;
    }

    private static void runInstallWrite(long size, int sessionId, String splitName, String path, Activity activity) {
        long sizeBytes;
        sizeBytes = size;
        doWriteSession(sessionId, path, sizeBytes, splitName, activity);
    }

    private static void doWriteSession(int sessionId, String path, long sizeBytes, String splitName, Activity activity) {
        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            session = getPackageInstaller(activity).openSession(sessionId);
            if (path != null) {
                in = new FileInputStream(path);
            }
            out = session.openWrite(splitName, 0, sizeBytes);
            byte[] buffer = new byte[65536];
            int c;
            assert in != null;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
        } catch (IOException ignored) {
        } finally {
            try {
                assert out != null;
                out.close();
                assert in != null;
                in.close();
                session.close();
            } catch (IOException ignored) {
            }
        }
    }
    
    private static void doCommitSession(int sessionId, Activity activity) {
        PackageInstaller.Session session = null;
        try {
            try {
                session = getPackageInstaller(activity).openSession(sessionId);
            } catch (IOException ignored) {
            }
            Intent callbackIntent = new Intent(activity, SplitAPKInstallService.class);
            PendingIntent pendingIntent = PendingIntent.getService(activity, 0, callbackIntent, 0);
            assert session != null;
            session.commit(pendingIntent.getIntentSender());
            session.close();
        } finally {
            assert session != null;
            session.close();
        }
    }

    private static InstallParams makeInstallParams(long totalSize) {
        final PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        final InstallParams params = new InstallParams();
        params.sessionParams = sessionParams;
        sessionParams.setSize(totalSize);
        return params;
    }

    private static PackageInstaller getPackageInstaller(Activity activity) {
        return PackageData.getPackageManager(activity).getPackageInstaller();
    }

    private static long getTotalSize() {
        int totalSize = 0;
        if (Common.getAppList().size() > 0) {
            for (String string : Common.getAppList()) {
                if (Utils.exist(string)) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
    }

    private static class InstallParams {
        PackageInstaller.SessionParams sessionParams;
    }

    public static void handleAppBundle(LinearLayout linearLayout, String path, Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                linearLayout.setVisibility(View.VISIBLE);
                Utils.delete(activity.getCacheDir().getPath() + "/splits");
                if (Utils.exist(activity.getCacheDir().getPath() + "/toc.pb")) {
                    Utils.delete(activity.getCacheDir().getPath() + "/toc.pb");
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
                if (Utils.getBoolean("filePicker", true, activity)) {
                    Common.getAppList().clear();
                    Common.setPath(activity.getCacheDir().getPath() + "/splits");
                    Intent filePicker = new Intent(activity, FilePickerActivity.class);
                    activity.startActivity(filePicker);
                } else {
                    Common.getAppList().clear();
                    if (Utils.exist(activity.getCacheDir().getPath() + "/splits")) {
                        for (final String splitApps : splitApks(activity.getCacheDir().getPath() + "/splits")) {
                            if (splitApps.endsWith(".apk")) {
                                Common.getAppList().add(activity.getCacheDir().getPath() + "/splits/" + splitApps);
                            }
                        }
                    }
                    installSplitAPKs(activity);
                }
            }
        }.execute();
    }

    public static void installSplitAPKs(Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                Intent installIntent = new Intent(activity, SplitAPKInstallerActivity.class);
                Utils.saveString("installationStatus", "waiting", activity);
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                long totalSize = getTotalSize();
                int sessionId;
                final InstallParams installParams = makeInstallParams(totalSize);
                sessionId = runInstallCreate(installParams, activity);
                try {
                    for (String string : Common.getAppList()) {
                        if (Utils.exist(string) && string.endsWith(".apk")) {
                            File mFile = new File(string);
                            if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    }
                } catch (NullPointerException ignored) {}
                doCommitSession(sessionId, activity);
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

}