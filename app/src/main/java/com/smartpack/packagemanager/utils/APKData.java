/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.content.Context;

import com.smartpack.packagemanager.R;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKData {

    private static File mAPK = null;
    private static List<String> mPermissions = null;
    private static String mCertificate = null, mManifest = null, mMinSDKVersion = null, mSDKVersion = null,
            mSize = null, mVersion = null;

    public static APKItems getAPKData(String apk, Context context) {
        try (ApkFile apkFile = new ApkFile(new File(apk))) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            APKItems mAPKData = new APKItems(apkMeta.getLabel(), apkMeta.getPackageName(),
                    apkMeta.getVersionName(), PackageExplorer.readManifest(apk),
                    apkMeta.getCompileSdkVersion(), apkMeta.getMinSdkVersion(),
                    sAPKUtils.getAPKIcon(apk, context), apkMeta.getVersionCode(),
                    apkMeta.getUsesPermissions());
            apkFile.close();
            return mAPKData;
        } catch (IOException ignored) {
        }
        return null;
    }

    public static File getAPKFile() {
        return mAPK;
    }

    public static List<String> getData() {
        List<String> mData = new ArrayList<>();
        try {
            if (mVersion != null) {
                mData.add(mVersion);
            }
            if (mSDKVersion != null) {
                mData.add(mSDKVersion);
            }
            if (mMinSDKVersion != null) {
                mData.add(mMinSDKVersion);
            }
            if (mSize != null) {
                mData.add(mSize);
            }
        } catch (Exception ignored) {
        }
        return mData;
    }

    public static List<String> getPermissions() {
        return mPermissions;
    }

    public static String getCertificate() {
        return mCertificate;
    }

    public static String getManifest() {
        return mManifest;
    }

    private static String sdkToAndroidVersion(String sdkVersion, Context context) {
        int sdk = Integer.parseInt(sdkVersion);
        switch (sdk) {
            case 31:
                return context.getString(R.string.android_version, "12 (S, " + sdkVersion + ")");
            case 30:
                return context.getString(R.string.android_version, "11 (R, " + sdkVersion + ")");
            case 29:
                return context.getString(R.string.android_version, "10 (Q, " + sdkVersion + ")");
            case 28:
                return context.getString(R.string.android_version, "9 (P, " + sdkVersion + ")");
            case 27:
                return context.getString(R.string.android_version, "8 (O_MR1, " + sdkVersion + ")");
            case 26:
                return context.getString(R.string.android_version, "8.0 (0, " + sdkVersion + ")");
            case 25:
                return context.getString(R.string.android_version, "7.1.1 (N_MRI, " + sdkVersion + ")");
            case 24:
                return context.getString(R.string.android_version, "7.0 (N, " + sdkVersion + ")");
            case 23:
                return context.getString(R.string.android_version, "6.0 (M, " + sdkVersion + ")");
            case 22:
                return context.getString(R.string.android_version, "5.1 (LOLLIPOP_MR1, " + sdkVersion + ")");
            case 21:
                return context.getString(R.string.android_version, "5.0 (LOLLIPOP, " + sdkVersion + ")");
            case 20:
                return context.getString(R.string.android_version, "4.4 (KITKAT_WATCH, " + sdkVersion + ")");
            case 19:
                return context.getString(R.string.android_version, "4.4 (KITKAT, " + sdkVersion + ")");
            case 18:
                return context.getString(R.string.android_version, "4.3 (JELLY_BEAN_MR2, " + sdkVersion + ")");
            case 17:
                return context.getString(R.string.android_version, "4.2 (JELLY_BEAN_MR1, " + sdkVersion + ")");
            case 16:
                return context.getString(R.string.android_version, "4.1 (JELLY_BEAN, " + sdkVersion + ")");
            case 15:
                return context.getString(R.string.android_version, "4.0.3 (ICE_CREAM_SANDWICH_MR1, " + sdkVersion + ")");
            case 14:
                return context.getString(R.string.android_version, "4.0 (ICE_CREAM_SANDWICH, " + sdkVersion + ")");
            case 13:
                return context.getString(R.string.android_version, "3.2 (HONEYCOMB_MR2, " + sdkVersion + ")");
            case 12:
                return context.getString(R.string.android_version, "3.1 (HONEYCOMB_MR1, " + sdkVersion + ")");
            case 11:
                return context.getString(R.string.android_version, "3.0 (HONEYCOMB, " + sdkVersion + ")");
            case 10:
                return context.getString(R.string.android_version, "2.3.3 (GINGERBREAD_MR1, " + sdkVersion + ")");
            case 9:
                return context.getString(R.string.android_version, "2.3 (GINGERBREAD, " + sdkVersion + ")");
            case 8:
                return context.getString(R.string.android_version, "2.2 (FROYO, " + sdkVersion + ")");
            case 7:
                return context.getString(R.string.android_version, "2.1 (ECLAIR_MR1, " + sdkVersion + ")");
            case 6:
                return context.getString(R.string.android_version, "2.0.1 (ECLAIR_0_1, " + sdkVersion + ")");
            case 5:
                return context.getString(R.string.android_version, "2.0 (ECLAIR, " + sdkVersion + ")");
            case 4:
                return context.getString(R.string.android_version, "1.6 (DONUT, " + sdkVersion + ")");
            case 3:
                return context.getString(R.string.android_version, "1.5 (CUPCAKE, " + sdkVersion + ")");
            case 2:
                return context.getString(R.string.android_version, "1.1 (BASE_1_1, " + sdkVersion + ")");
            case 1:
                return context.getString(R.string.android_version, "1.0 (BASE, " + sdkVersion + ")");
            default:
                return sdkVersion;
        }
    }

    public static void setAPKFile(File apk) {
        mAPK = apk;
    }

    public static void setCertificate(String certificate) {
        mCertificate = certificate;
    }

    public static void setManifest(String manifest) {
        mManifest = manifest;
    }

    public static void setMinSDKVersion(String minSDKVersion, Context context) {
        mMinSDKVersion = context.getString(R.string.sdk_minimum, sdkToAndroidVersion(minSDKVersion, context));
    }

    public static void setPermissions(List<String> permissions) {
        mPermissions = permissions;
    }

    public static void setSDKVersion(String sdkVersion, Context context) {
        mSDKVersion = context.getString(R.string.sdk_compile, sdkToAndroidVersion(sdkVersion, context));
    }

    public static void setSize(String size) {
        mSize = size;
    }

    public static void setVersionInfo(String version) {
        mVersion = version;
    }

}