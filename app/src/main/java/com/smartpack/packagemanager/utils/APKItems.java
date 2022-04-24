/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKItems implements Serializable {

    private final Drawable mIcon;
    private final List<String> mPermissions;
    private final long mVersionCode;
    private final String mManifest, mName, mPackageName, mVersionName, mSDKVersion, mMinSDKVersion;

    public APKItems(String name, String packageName, String versionName, String manifest, String sdkVersion,
                    String minSDKVersion, Drawable icon, long versionCode, List<String> permissions) {
        this.mName = name;
        this.mPackageName = packageName;
        this.mVersionName = versionName;
        this.mManifest = manifest;
        this.mSDKVersion = sdkVersion;
        this.mMinSDKVersion = minSDKVersion;
        this.mIcon = icon;
        this.mVersionCode = versionCode;
        this.mPermissions = permissions;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public long getVersionCode() {
        return mVersionCode;
    }

    public List<String> getPermissions() {
        return mPermissions;
    }

    public String getAppName() {
        return mName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public String getManifest() {
        return mManifest;
    }

    public String getSDKVersion() {
        return mSDKVersion;
    }

    public String getMinSDKVersion() {
        return mMinSDKVersion;
    }

}