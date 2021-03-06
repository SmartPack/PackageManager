/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
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

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2020
 * Based on the original work of nkalra0123 for Split Apk Install
 * Ref: https://github.com/nkalra0123/splitapkinstall
 */
public class SplitAPKInstallService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Utils.saveString("installationStatus", "waiting", this);
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                assert confirmationIntent != null;
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(confirmationIntent);
                } catch (Exception ignored) {
                }
                break;
            case PackageInstaller.STATUS_SUCCESS:
                Utils.saveString("installationStatus", getString(R.string.installation_status_success), this);
                break;
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                Utils.saveString("installationStatus", getString(R.string.installation_status_aborted), this);
                break;
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                Utils.saveString("installationStatus", getString(R.string.installation_status_blocked), this);
                break;
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                Utils.saveString("installationStatus", getString(R.string.installation_status_conflict), this);
                break;
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                Utils.saveString("installationStatus", getString(R.string.installation_status_incompatible), this);
                break;
            case PackageInstaller.STATUS_FAILURE_INVALID:
                Utils.saveString("installationStatus", getString(R.string.installation_status_bad_apks), this);
                break;
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                Utils.saveString("installationStatus", getString(R.string.installation_status_storage), this);
                break;
            default:
                Utils.saveString("installationStatus", getString(R.string.installation_status_failed), this);
                break;
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}