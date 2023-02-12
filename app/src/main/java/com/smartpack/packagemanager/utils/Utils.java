/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;

import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 07, 2020
 */
public class Utils {

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/ViewUtils.java
     */
    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public static boolean isNotDonated(Context context) {
        return !sPackageUtils.isPackageInstalled("com.smartpack.donate", context);
    }

    public static boolean isProUser(Context context) {
        return sUtils.getBoolean("support_received", false, context) || !isNotDonated(context);
    }

    public static void toggleKeyboard(int mode, AppCompatEditText editText, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (mode == 1) {
            if (editText.requestFocus()) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static CharSequence fromHtml(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void unzip(String zip, String path) {
        try (ZipFile zipFile = new ZipFile(zip)) {
            zipFile.extractAll(path);
        } catch (IOException ignored) {}
    }

    public static void zip(String zip, List<File> files) {
        try (ZipFile zipFile = new ZipFile(zip)) {
            zipFile.addFiles(files);
        } catch (IOException ignored) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void requestPermission(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static boolean isPermissionDenied() {
        return !Environment.isExternalStorageManager();
    }

    public static void restartApp(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

}