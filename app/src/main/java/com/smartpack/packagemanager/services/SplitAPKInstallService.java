/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import in.sunilpaulmathew.sCommon.Utils.sInstallerUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2021
 * Based on the original work of nkalra0123 (Ref: https://github.com/nkalra0123/splitapkinstall)
 * & Aefyr (Ref: https://github.com/Aefyr/SAI)
 */
public class SplitAPKInstallService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sInstallerUtils.setStatus(intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999), intent, this);
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}