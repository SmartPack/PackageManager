/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import com.apk.axml.aXMLDecoder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.tasks.SaveIconTasks;
import com.smartpack.packagemanager.utils.tasks.SaveToDownloadsTasks;

import java.io.File;
import java.io.FileInputStream;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class PackageExplorer {

    public static boolean isTextFile(String path) {
        return path.endsWith(".txt") || path.endsWith(".xml") || path.endsWith(".json") || path.endsWith(".properties")
                || path.endsWith(".version") || path.endsWith(".sh") || path.endsWith(".MF") || path.endsWith(".SF")
                || path.endsWith(".RSA") || path.endsWith(".html") || path.endsWith(".ini");
    }

    public static boolean isImageFile(String path) {
        return path.endsWith(".bmp") || path.endsWith(".png") || path.endsWith(".jpg");
    }

    public static boolean isBinaryXML(String path) {
        return path.endsWith(".xml") && (new File(path).getName().equals("AndroidManifest.xml") || path.contains("/apk/res"));
    }

    public static int getSpanCount(Activity activity) {
        return sCommonUtils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    @SuppressLint("StringFormatInvalid")
    public static String readXMLFromAPK(String path, Activity activity) {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            return new aXMLDecoder().decode(inputStream);
        } catch (Exception e) {
            sCommonUtils.toast(activity.getString(R.string.failed_decode_xml, new File(path).getName()), activity).show();
        }
        return null;
    }

    public static Uri getIconFromPath(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void saveIcon(Bitmap bitmap, String name, Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
            sPermissionUtils.requestPermission(new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    activity);
            sCommonUtils.toast(activity.getString(R.string.permission_denied_write_storage), activity).show();
            return;
        }
        new SaveIconTasks(name, bitmap, activity).execute();
    }

    public static void copyToStorage(String path, Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
            sPermissionUtils.requestPermission(new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    activity);
            sCommonUtils.toast(activity.getString(R.string.permission_denied_write_storage), activity).show();
            return;
        }
        new SaveToDownloadsTasks(new File(path), activity).execute();
    }

}