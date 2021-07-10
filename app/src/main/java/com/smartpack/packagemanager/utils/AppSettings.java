/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */

import android.app.Activity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;

public class AppSettings {

    public static String getAppThemeDescription(Activity activity) {
        if (Utils.getBoolean("dark_theme", false, activity)) {
            return activity.getString(R.string.dark_theme_enable);
        } else if (Utils.getBoolean("light_theme", false, activity)) {
            return activity.getString(R.string.dark_theme_disable);
        } else {
            return activity.getString(R.string.dark_theme_auto);
        }
    }

    public static void setLanguage(Activity activity) {
        new MaterialAlertDialogBuilder(activity).setItems(activity.getResources().getStringArray(
                R.array.app_language), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    if (!Utils.getLanguage(activity).equals(java.util.Locale.getDefault().getLanguage())) {
                        Utils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 1:
                    if (!Utils.getLanguage(activity).equals("en_US")) {
                        Utils.saveString("appLanguage", "en_US", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 2:
                    if (!Utils.getLanguage(activity).equals("ko")) {
                        Utils.saveString("appLanguage", "ko", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 3:
                    if (!Utils.getLanguage(activity).equals("am")) {
                        Utils.saveString("appLanguage", "am", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 4:
                    if (!Utils.getLanguage(activity).equals("el")) {
                        Utils.saveString("appLanguage", "el", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 5:
                    if (!Utils.getLanguage(activity).equals("ml")) {
                        Utils.saveString("appLanguage", "ml", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 6:
                    if (!Utils.getLanguage(activity).equals("pt")) {
                        Utils.saveString("appLanguage", "pt", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 7:
                    if (!Utils.getLanguage(activity).equals("ru")) {
                        Utils.saveString("appLanguage", "ru", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 8:
                    if (!Utils.getLanguage(activity).equals("uk")) {
                        Utils.saveString("appLanguage", "uk", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 9:
                    if (!Utils.getLanguage(activity).equals("fr")) {
                        Utils.saveString("appLanguage", "fr", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 10:
                    if (!Utils.getLanguage(activity).equals("de")) {
                        Utils.saveString("appLanguage", "de", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 11:
                    if (!Utils.getLanguage(activity).equals("tr")) {
                        Utils.saveString("appLanguage", "tr", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 12:
                    if (!Utils.getLanguage(activity).equals("cs")) {
                        Utils.saveString("appLanguage", "cs", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 13:
                    if (!Utils.getLanguage(activity).equals("es")) {
                        Utils.saveString("appLanguage", "es", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 14:
                    if (!Utils.getLanguage(activity).equals("vi")) {
                        Utils.saveString("appLanguage", "vi", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 15:
                    if (!Utils.getLanguage(activity).equals("zh")) {
                        Utils.saveString("appLanguage", "zh", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 16:
                    if (!Utils.getLanguage(activity).equals("hu")) {
                        Utils.saveString("appLanguage", "hu", activity);
                        Utils.restartApp(activity);
                    }
                    break;
                case 17:
                    if (!Utils.getLanguage(activity).equals("pl")) {
                        Utils.saveString("appLanguage", "pl", activity);
                        Utils.restartApp(activity);
                    }
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static String getLanguage(Activity activity) {
        switch (Utils.getLanguage(activity)) {
            case "en_US":
                return activity.getString(R.string.language_en);
            case "ko":
                return activity.getString(R.string.language_ko);
            case "am":
                return activity.getString(R.string.language_am);
            case "el":
                return activity.getString(R.string.language_el);
            case "ml":
                return activity.getString(R.string.language_ml);
            case "pt":
                return activity.getString(R.string.language_pt);
            case "ru":
                return activity.getString(R.string.language_ru);
            case "uk":
                return activity.getString(R.string.language_uk);
            case "fr":
                return activity.getString(R.string.language_fr);
            case "de":
                return activity.getString(R.string.language_de);
            case "tr":
                return activity.getString(R.string.language_tr);
            case "cs":
                return activity.getString(R.string.language_cs);
            case "es":
                return activity.getString(R.string.language_es);
            case "vi":
                return activity.getString(R.string.language_vi);
            case "zh":
                return activity.getString(R.string.language_zh);
            case "hu":
                return activity.getString(R.string.language_hu);
            case "pl":
                return activity.getString(R.string.language_pl);
            default:
                return activity.getString(R.string.language_default) + " (" + java.util.Locale.getDefault().getLanguage() + ")";
        }
    }

}