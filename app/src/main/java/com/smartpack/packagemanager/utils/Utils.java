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
import android.text.Html;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.ContextCompat;

import com.smartpack.packagemanager.MainActivity;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 07, 2020
 */
public class Utils {

    public static CharSequence fromHtml(String text) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
    }

    public static int getColor(int resID, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resID, typedValue, true);
        return ContextCompat.getColor(context, typedValue.resourceId);
    }

    public static void restartApp(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
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

}