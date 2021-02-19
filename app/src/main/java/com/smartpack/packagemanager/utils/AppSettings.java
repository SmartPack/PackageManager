/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */

import android.content.Context;

import com.smartpack.packagemanager.R;

public class AppSettings {

    public static String getAppThemeDescription(Context context) {
        if (Utils.getBoolean("dark_theme", false, context)) {
            return context.getString(R.string.dark_theme_enable);
        } else if (Utils.getBoolean("light_theme", false, context)) {
            return context.getString(R.string.dark_theme_disable);
        } else {
            return context.getString(R.string.dark_theme_auto);
        }
    }

    public static String getLanguage(Context context) {
        if (Utils.getBoolean("use_english", false, context)) {
            return context.getString(R.string.language_en);
        } else if (Utils.getBoolean("use_korean", false, context)) {
            return context.getString(R.string.language_ko);
        } else if (Utils.getBoolean("use_am", false, context)) {
            return context.getString(R.string.language_am);
        } else if (Utils.getBoolean("use_el", false, context)) {
            return context.getString(R.string.language_el);
        }else if (Utils.getBoolean("use_ml", false, context)) {
            return context.getString(R.string.language_ml);
        } else if (Utils.getBoolean("use_pt", false, context)) {
            return context.getString(R.string.language_pt);
        } else if (Utils.getBoolean("use_ru", false, context)) {
            return context.getString(R.string.language_ru);
        } else if (Utils.getBoolean("use_uk", false, context)) {
            return context.getString(R.string.language_uk);
        } else {
            return context.getString(R.string.language_default) + " (" + java.util.Locale.getDefault().getLanguage() + ")";
        }
    }

}