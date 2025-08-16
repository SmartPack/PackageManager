/*
 * Copyright (C) 2024-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.Context;

import com.smartpack.packagemanager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sSerializableItems;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class AppSettings {

    public static int getAPKNameOptionsPosition(Context context) {
        if (sCommonUtils.getString("exportedAPKName", context.getString(R.string.package_id), context)
                .equals(context.getString(R.string.name))) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getExitMenuPosition(Context context) {
        if (sCommonUtils.getBoolean("exit_confirmation", true, context)) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getInstallerOptionsPosition(Context context) {
        if (sCommonUtils.getBoolean("neverShow", false, context)) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int getLanguagePosition(Context context) {
        String country = getCountry(context);
        switch (getLanguage(context)) {
            case "am":
                return 1;
            case "ar":
                return 2;
            case "cs":
                return 3;
            case "de":
                return 4;
            case "el":
                return 5;
            case "en":
                return country.equalsIgnoreCase("us") ? 6 : 0;
            case "es":
                return 7;
            case "fr":
                return 8;
            case "he":
                return 9;
            case "hu":
                return 10;
            case "it":
                return 11;
            case "ko":
                return 12;
            case "ml":
                return 13;
            case "pl":
                return 14;
            case "pt":
                return country.equalsIgnoreCase("BR") ? 15 : 16;
            case "ru":
                return 17;
            case "sk":
                return 18;
            case "tr":
                return 19;
            case "uk":
                return 20;
            case "vi":
                return 21;
            case "zh":
                return country.equalsIgnoreCase("HK") ? 24 : country.equalsIgnoreCase("CN") ? 23 : 22;
            default:
                return 0;
        }
    }

    public static List<sSerializableItems> getCredits() {
        List<sSerializableItems> mData = new ArrayList<>();
        mData.add(new sSerializableItems(null, "Willi Ye", "Kernel Adiutor", "https://github.com/Grarak/KernelAdiutor"));
        mData.add(new sSerializableItems(null, "topjohnwu", "libsu", "https://github.com/topjohnwu/libsu"));
        mData.add(new sSerializableItems(null, "RikkaApps", "Shizuku", "https://github.com/RikkaApps/Shizuku"));
        mData.add(new sSerializableItems(null, "Srikanth Reddy Lingala", "zip4j", "https://github.com/srikanth-lingala"));
        mData.add(new sSerializableItems(null, "Aefyr", "SAI", "https://github.com/Aefyr/SAI"));
        mData.add(new sSerializableItems(null, "Nitin Kalra", "Split Apk Install", "https://github.com/nkalra0123/"));
        mData.add(new sSerializableItems(null, "APK Explorer & Editor", "aXML", "https://github.com/apk-editor/aXML"));
        mData.add(new sSerializableItems(null, "Lennoard Silva", "Code Contributions & Portuguese (Brazilian) Translations", "https://github.com/Lennoard"));
        mData.add(new sSerializableItems(null, "Agnieszka C","Code Contributions, Testing & Polish Translations", "https://github.com/Aga-C"));
        mData.add(new sSerializableItems(null, "Toxinpiper", "App Icon", "https://t.me/toxinpiper"));
        mData.add(new sSerializableItems(null, "Valdnet", "Testing", "https://github.com/Valdnet"));
        mData.add(new sSerializableItems(null, "FiestaLake", "Korean Translations", "https://github.com/FiestaLake"));
        mData.add(new sSerializableItems(null, "Mikesew1320", "Amharic Translations", "https://github.com/Mikesew1320"));
        mData.add(new sSerializableItems(null, "tsiflimagas", "Greek Translations", "https://github.com/tsiflimagas"));
        mData.add(new sSerializableItems(null, "741™", "Greek Translations", null));
        mData.add(new sSerializableItems(null, "Nikita", "Russian & Ukrainian Translations", "https://t.me/MONSTER_PC"));
        mData.add(new sSerializableItems(null, "tommynok", "Ukrainian Translations", null));
        mData.add(new sSerializableItems(null, "B3Nd2R & Reno", "French Translations", null));
        mData.add(new sSerializableItems(null, "Jan & Ray", "German Translations", null));
        mData.add(new sSerializableItems(null, "omerakgoz34 (BSÇE)", "Turkish Translations", "https://github.com/omerakgoz34"));
        mData.add(new sSerializableItems(null, "Emrehelvaci83", "Turkish Translations", null));
        mData.add(new sSerializableItems(null, "Woytazzer", "Czech Translations", null));
        mData.add(new sSerializableItems(null, "Javi", "Spanish Translations", null));
        mData.add(new sSerializableItems(null, "Hoa Gia Đại Thiếu", "Vietnamese Translations", null));
        mData.add(new sSerializableItems(null, "jason5545", "Chinese (Simplified & Traditional) Translations", "https://github.com/jason5545"));
        mData.add(new sSerializableItems(null, "Cláudia Sebastião", "Portuguese (Portugal) Translations", null));
        mData.add(new sSerializableItems(null, "Carnum", "Portuguese (Portugal) Translations", null));
        mData.add(new sSerializableItems(null, "Erős Pista", "Hungarian Translations", null));
        mData.add(new sSerializableItems(null, "AbsurdUsername", "Italian Translations", "https://github.com/AbsurdUsername"));
        mData.add(new sSerializableItems(null, "rotid™", "Arabic Translations", null));
        mData.add(new sSerializableItems(null, "Pen™", "Slovakian Translations", null));
        mData.add(new sSerializableItems(null, "yair aaron", "Hebrew Translations", null));
        return mData;
    }

    public static String getExportedAPKName(Activity activity) {
        return sCommonUtils.getString("exportedAPKName", activity.getString(R.string.package_id), activity);
    }

    public static String getExitingStatus(Activity activity) {
        if (sCommonUtils.getBoolean("exit_confirmation", true, activity)) {
            return activity.getString(R.string.exit_confirmation);
        } else {
            return activity.getString(R.string.exit_simple);
        }
    }

    public static String getInstallerStatus(Activity activity) {
        if (sCommonUtils.getBoolean("neverShow", false, activity)) {
            return activity.getString(R.string.installer_file_picker);
        } else {
            return activity.getString(R.string.installer_instructions);
        }
    }

    public static String[] getAPKNameOptionsMenu(Context context) {
        return new String[] {
                context.getString(R.string.package_id),
                context.getString(R.string.name)
        };
    }

    public static String[] getExitOptionsMenu(Context context) {
        return new String[] {
                context.getString(R.string.exit_simple),
                context.getString(R.string.exit_confirmation)
        };
    }

    public static String[] getInstallerOptionsMenu(Context context) {
        return new String[] {
                context.getString(R.string.installer_instructions),
                context.getString(R.string.installer_file_picker)
        };
    }

    private static String getCountry(Context context) {
        return sCommonUtils.getString("country", java.util.Locale.getDefault().getLanguage(), context);
    }

    private static String getLanguage(Context context) {
        return sCommonUtils.getString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
    }

    public static String getLanguageDescription(Context context) {
        String country = getCountry(context);
        switch (getLanguage(context)) {
            case "am":
                return context.getString(R.string.language_am);
            case "ar":
                return context.getString(R.string.language_ar);
            case "cs":
                return context.getString(R.string.language_cs);
            case "de":
                return context.getString(R.string.language_de);
            case "el":
                return context.getString(R.string.language_el);
            case "en":
                return country.equalsIgnoreCase("US") ? context.getString(R.string.language_en, "US")
                        : context.getString(R.string.app_theme_auto);
            case "es":
                return context.getString(R.string.language_es, country.equalsIgnoreCase("MX") ? "MX" : "ES");
            case "fr":
                return context.getString(R.string.language_fr, "FR");
            case "he":
                return context.getString(R.string.language_he);
            case "hu":
                return context.getString(R.string.language_hu);
            case "it":
                return context.getString(R.string.language_it);
            case "ko":
                return context.getString(R.string.language_ko);
            case "ml":
                return context.getString(R.string.language_ml);
            case "pl":
                return context.getString(R.string.language_pl);
            case "pt":
                return context.getString(R.string.language_pt, country.equalsIgnoreCase("BR") ? "BR" : "PT");
            case "ru":
                return context.getString(R.string.language_ru);
            case "sk":
                return context.getString(R.string.language_sk);
            case "tr":
                return context.getString(R.string.language_tr);
            case "uk":
                return context.getString(R.string.language_uk);
            case "vi":
                return context.getString(R.string.language_vi);
            case "zh":
                return country.equalsIgnoreCase("HK") ? context.getString(R.string.language_zh, "Hant")
                        : country.equalsIgnoreCase("CN") ? context.getString(R.string.language_zh, "Hans")
                        : country.equalsIgnoreCase("TW") ? context.getString(R.string.language_zh, "TW")
                        : context.getString(R.string.app_theme_auto);
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    private static String[] getLanguageMenu(Context context) {
        return new String[] {
                context.getString(R.string.app_theme_auto),
                context.getString(R.string.language_am),
                context.getString(R.string.language_ar),
                context.getString(R.string.language_cs),
                context.getString(R.string.language_de),
                context.getString(R.string.language_el),
                context.getString(R.string.language_en, "US"),
                context.getString(R.string.language_es, "ES"),
                context.getString(R.string.language_fr, "FR"),
                context.getString(R.string.language_he),
                context.getString(R.string.language_hu),
                context.getString(R.string.language_it),
                context.getString(R.string.language_ko),
                context.getString(R.string.language_ml),
                context.getString(R.string.language_pl),
                context.getString(R.string.language_pt, "BR"),
                context.getString(R.string.language_pt, "PT"),
                context.getString(R.string.language_ru),
                context.getString(R.string.language_sk),
                context.getString(R.string.language_tr),
                context.getString(R.string.language_uk),
                context.getString(R.string.language_vi),
                context.getString(R.string.language_zh, "TW"),
                context.getString(R.string.language_zh, "Hans"),
                context.getString(R.string.language_zh, "Hant"),
        };
    }

    public static void setLanguage(Activity activity) {
        new sSingleChoiceDialog(R.drawable.ic_language, activity.getString(R.string.language),
                getLanguageMenu(activity), getLanguagePosition(activity), activity) {

            @Override
            public void onItemSelected(int position) {
                switch (position) {
                    case 0:
                        if (Objects.equals(getLanguage(activity), Locale.getDefault().getLanguage()) && Objects.equals(getCountry(activity), Locale.getDefault().getCountry())) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), activity);
                        sCommonUtils.saveString("country", java.util.Locale.getDefault().getCountry(), activity);
                        break;
                    case 1:
                        if (Objects.equals(getLanguage(activity), "am") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "am", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 2:
                        if (Objects.equals(getLanguage(activity), "ar") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ar", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 3:
                        if (Objects.equals(getLanguage(activity), "cs") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "cs", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 4:
                        if (Objects.equals(getLanguage(activity), "de") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "de", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 5:
                        if (Objects.equals(getLanguage(activity), "el") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "el", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 6:
                        if (Objects.equals(getLanguage(activity), "en") && Objects.equals(getCountry(activity), "US")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "en", activity);
                        sCommonUtils.saveString("country", "US", activity);
                        break;
                    case 7:
                        if (Objects.equals(getLanguage(activity), "es") && Objects.equals(getCountry(activity), "ES")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "es", activity);
                        sCommonUtils.saveString("country", "ES", activity);
                        break;
                    case 8:
                        if (Objects.equals(getLanguage(activity), "fr") && Objects.equals(getCountry(activity), "FR")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "fr", activity);
                        sCommonUtils.saveString("country", "FR", activity);
                        break;
                    case 9:
                        if (Objects.equals(getLanguage(activity), "he") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "he", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 10:
                        if (Objects.equals(getLanguage(activity), "hu") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "hu", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 11:
                        if (Objects.equals(getLanguage(activity), "it") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "it", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 12:
                        if (Objects.equals(getLanguage(activity), "ko") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ko", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 13:
                        if (Objects.equals(getLanguage(activity), "ml") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ml", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 14:
                        if (Objects.equals(getLanguage(activity), "pl") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "pl", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 15:
                        if (Objects.equals(getLanguage(activity), "pt") && Objects.equals(getCountry(activity), "BR")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "pt", activity);
                        sCommonUtils.saveString("country", "BR", activity);
                        break;
                    case 16:
                        if (Objects.equals(getLanguage(activity), "pt") && Objects.equals(getCountry(activity), "PT")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "pt", activity);
                        sCommonUtils.saveString("country", "PT", activity);
                        break;
                    case 17:
                        if (Objects.equals(getLanguage(activity), "ru") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ru", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 18:
                        if (Objects.equals(getLanguage(activity), "sk") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "sk", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 19:
                        if (Objects.equals(getLanguage(activity), "tr") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "tr", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 20:
                        if (Objects.equals(getLanguage(activity), "uk") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "uk", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 21:
                        if (Objects.equals(getLanguage(activity), "vi") && Objects.equals(getCountry(activity), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "vi", activity);
                        sCommonUtils.saveString("country", null, activity);
                        break;
                    case 22:
                        if (Objects.equals(getLanguage(activity), "zh") && Objects.equals(getCountry(activity), "TW")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "zh", activity);
                        sCommonUtils.saveString("country", "TW", activity);
                        break;
                    case 23:
                        if (Objects.equals(getLanguage(activity), "zh") && Objects.equals(getCountry(activity), "CN")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "zh", activity);
                        sCommonUtils.saveString("country", "CN", activity);
                        break;
                    case 24:
                        if (Objects.equals(getLanguage(activity), "zh") && Objects.equals(getCountry(activity), "HK")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "zh", activity);
                        sCommonUtils.saveString("country", "HK", activity);
                        break;
                }
                Utils.restartApp(activity);
            }
        }.show();
    }

}