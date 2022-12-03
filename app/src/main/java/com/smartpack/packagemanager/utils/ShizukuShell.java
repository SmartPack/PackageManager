/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2022
 */
public class ShizukuShell {

    private static ShizukuRemoteProcess mProcess = null;

    public ShizukuShell() {
    }

    public boolean isPermissionDenied() {
        return Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED;
    }

    public boolean isReady() {
        return Shizuku.pingBinder() && Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED;
    }

    public boolean isSupported() {
        return Shizuku.pingBinder();
    }

    public String runAndGetOutput(String command) {
        StringBuilder sb = new StringBuilder();
        try {
            mProcess = Shizuku.newProcess(new String[] {"sh", "-c", command}, null, null);
            BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
            String line;
            while ((line = mInput.readLine()) != null) {
                sb.append(line).append("\n");
            }
            while ((line = mError.readLine()) != null) {
                sb.append(line).append("\n");
            }

            mProcess.waitFor();
        } catch (Exception ignored) {
        }
        return sb.toString();
    }

    public void destroy() {
        if (mProcess != null) mProcess.destroy();
    }

    public void requestPermission() {
        Shizuku.requestPermission(0);
    }

    public void runCommand(String command) {
        try {
            mProcess = Shizuku.newProcess(new String[] {"sh", "-c", command}, null, null);
            mProcess.waitFor();
        } catch (InterruptedException ignored) {
        }
    }

}