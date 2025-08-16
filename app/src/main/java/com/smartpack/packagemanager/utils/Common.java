/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartpack.packagemanager.R;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 03, 2021
 */
public class Common {

    private static boolean mAPKPicker = false, mReloadPage = false,
            mSystemApp = false, mUninstall = false, mUpdating = false;

    private static CharSequence mApplicationName;

    private static Drawable mApplicationIcon;

    private static final List<String> mAPKList = new ArrayList<>(), mBatchList = new ArrayList<>(), mRestoreList = new ArrayList<>();

    private static String mApplicationID, mDirData, mDirNatLib, mDirSource, mPath, mSearchText;

    public static boolean isAPKPicker() {
        return mAPKPicker;
    }

    public static boolean isSystemApp() {
        return mSystemApp;
    }

    public static boolean isUninstall() {
        return mUninstall;
    }

    public static boolean isUpdating() {
        return mUpdating;
    }

    public static boolean reloadPage() {
        return mReloadPage;
    }

    public static boolean isTextMatched(String searchText) {
        for (int a = 0; a < searchText.length() - mSearchText.length() + 1; a++) {
            if (mSearchText.equalsIgnoreCase(searchText.substring(a, a + mSearchText.length()))) {
                return true;
            }
        }
        return false;
    }

    public static CharSequence getApplicationName() {
        return mApplicationName;
    }

    public static Drawable getApplicationIcon() {
        return mApplicationIcon;
    }

    public static String getApplicationID() {
        return mApplicationID;
    }

    public static String getDataDir() {
        return mDirData;
    }

    public static String getNativeLibsDir() {
        return mDirNatLib;
    }

    public static String getSourceDir() {
        return mDirSource;
    }

    public static String getPath() {
        return mPath;
    }

    public static String getSearchText() {
        return mSearchText;
    }

    public static List<String> getAppList() {
        return mAPKList;
    }

    public static List<String> getBatchList() {
        return mBatchList;
    }

    public static List<String> getRestoreList() {
        return mRestoreList;
    }

    public static void reloadPage(boolean b) {
        mReloadPage = b;
    }

    public static void isAPKPicker(boolean b) {
        mAPKPicker = b;
    }

    public static void isSystemApp(boolean b) {
        mSystemApp = b;
    }

    public static void isUninstall(boolean b) {
        mUninstall = b;
    }

    public static void isUpdating(boolean b) {
        mUpdating = b;
    }

    public static void navigateToFragment(Activity activity, int position) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(position);
    }

    public static void setApplicationName(CharSequence name) {
        mApplicationName = name;
    }

    public static void setApplicationIcon(Drawable icon) {
        mApplicationIcon = icon;
    }

    public static void setApplicationID(String packageID) {
        mApplicationID = packageID;
    }

    public static void setDataDir(String dataDir) {
        mDirData = dataDir;
    }

    public static void setNativeLibsDir(String dirNatLib) {
        mDirNatLib = dirNatLib;
    }

    public static void setSourceDir(String sourceDir) {
        mDirSource = sourceDir;
    }

    public static void setPath(String path) {
        mPath = path;
    }

    public static void setSearchText(String searchText) {
        mSearchText = searchText;
    }

}