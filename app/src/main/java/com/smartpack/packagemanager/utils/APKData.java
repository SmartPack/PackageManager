/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.axml.APKParser;
import com.smartpack.packagemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKData {

    private static final APKParser mAPKParser = new APKParser();
    private static File mAPK = null;

    public static File getAPKFile() {
        return mAPK;
    }

    @SuppressLint("StringFormatInvalid")
    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        if (mAPKParser.getVersionName() != null) {
            mData.add(context.getString(R.string.version, mAPKParser.getVersionName() + " (" + mAPKParser.getVersionCode() + ")"));
        }
        if (mAPKParser.getCompiledSDKVersion() != null) {
            mData.add(context.getString(R.string.sdk_compile, sdkToAndroidVersion(mAPKParser.getCompiledSDKVersion(), context)));
        }
        if (mAPKParser.getMinSDKVersion() != null) {
            mData.add(context.getString(R.string.sdk_minimum, sdkToAndroidVersion(mAPKParser.getMinSDKVersion(), context)));
        }
        if (mAPKParser.getAPKSize() != Integer.MIN_VALUE) {
            mData.add(context.getString(R.string.size_apk, sAPKUtils.getAPKSize(mAPKParser.getAPKSize()) + " (" + mAPKParser.getAPKSize() + " bytes)"));
        }
        return mData;
    }

    @SuppressLint("StringFormatInvalid")
    private static String sdkToAndroidVersion(String sdkVersion, Context context) {
        int sdk = Integer.parseInt(sdkVersion.trim());
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

}