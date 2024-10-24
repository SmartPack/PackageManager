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
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.smartpack.packagemanager.MainActivity;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 07, 2020
 */
public class Utils {

    public static boolean isProUser(Context context) {
        return sCommonUtils.getBoolean("support_received", false, context);
    }

    public static void toggleKeyboard(int mode, AppCompatAutoCompleteTextView editText, Activity activity) {
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
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
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