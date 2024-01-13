/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.IUserService;
import com.smartpack.packagemanager.services.UserService;

import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2022
 */
public class ShizukuShell {

    private static IUserService mUserService = null;

    public ShizukuShell() {
    }

    public boolean ensureUserService() {
        if (mUserService != null) {
            return true;
        }

        Shizuku.UserServiceArgs mUserServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName()))
                .daemon(false)
                .processNameSuffix("service")
                .debuggable(BuildConfig.DEBUG)
                .version(BuildConfig.VERSION_CODE);
        Shizuku.bindUserService(mUserServiceArgs, mServiceConnection);

        return false;
    }

    public boolean isPermissionDenied() {
        return Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED;
    }

    public boolean isReady() {
        return isSupported() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isSupported() {
        return Shizuku.pingBinder() && Shizuku.getVersion() >= 11 && !Shizuku.isPreV11();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder == null || !iBinder.pingBinder()) {
                return;
            }

            mUserService = IUserService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public String runAndGetOutput(String command) {
        if (ensureUserService()) {
            try {
                return mUserService.runShellCommand(command);
            } catch (RemoteException ignored) {
            }
        }
        return "";
    }

    public void requestPermission() {
        Shizuku.requestPermission(0);
    }

    public void runCommand(String command) {
        if (ensureUserService()) {
            try {
                mUserService.runShellCommand(command);
            } catch (RemoteException ignored) {
            }
        }
    }

}