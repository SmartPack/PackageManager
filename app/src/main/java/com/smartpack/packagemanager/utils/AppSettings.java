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

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sSerializableItems;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

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

    private static int getLanguagePosition(Activity activity) {
        for (int i = 0; i < getLanguageMenu(activity).length; i++) {
            if (getLanguage(activity).equals(getLanguageMenu(activity)[i])) {
                return i;
            }
        }
        return 0;
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

    public static String getLanguage(Activity activity) {
        switch (sThemeUtils.getLanguage(activity)) {
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
            case "it":
                return activity.getString(R.string.language_it);
            case "ar":
                return activity.getString(R.string.language_ar);
            case "sk":
                return activity.getString(R.string.language_sk);
            case "he":
                return activity.getString(R.string.language_he);
            default:
                return activity.getString(R.string.language_default) + " (" + java.util.Locale.getDefault().getLanguage() + ")";
        }
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

    private static String[] getLanguageMenu(Context context) {
        return new String[] {
                context.getString(R.string.language_default),
                context.getString(R.string.language_en),
                context.getString(R.string.language_ko),
                context.getString(R.string.language_am),
                context.getString(R.string.language_el),
                context.getString(R.string.language_ml),
                context.getString(R.string.language_pt),
                context.getString(R.string.language_ru),
                context.getString(R.string.language_uk),
                context.getString(R.string.language_fr),
                context.getString(R.string.language_de),
                context.getString(R.string.language_tr),
                context.getString(R.string.language_cs),
                context.getString(R.string.language_es),
                context.getString(R.string.language_vi),
                context.getString(R.string.language_zh),
                context.getString(R.string.language_hu),
                context.getString(R.string.language_pl),
                context.getString(R.string.language_it),
                context.getString(R.string.language_ar),
                context.getString(R.string.language_sk),
                context.getString(R.string.language_he)
        };
    }

    public static void setLanguage(Activity activity) {
        new sSingleChoiceDialog(R.drawable.ic_language, activity.getString(R.string.language),
                getLanguageMenu(activity), getLanguagePosition(activity), activity) {

            @Override
            public void onItemSelected(int position) {
                switch (position) {
                    case 0:
                        if (!sThemeUtils.getLanguage(activity).equals(java.util.Locale.getDefault().getLanguage())) {
                            sCommonUtils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 1:
                        if (!sThemeUtils.getLanguage(activity).equals("en_US")) {
                            sCommonUtils.saveString("appLanguage", "en_US", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 2:
                        if (!sThemeUtils.getLanguage(activity).equals("ko")) {
                            sCommonUtils.saveString("appLanguage", "ko", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 3:
                        if (!sThemeUtils.getLanguage(activity).equals("am")) {
                            sCommonUtils.saveString("appLanguage", "am", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 4:
                        if (!sThemeUtils.getLanguage(activity).equals("el")) {
                            sCommonUtils.saveString("appLanguage", "el", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 5:
                        if (!sThemeUtils.getLanguage(activity).equals("ml")) {
                            sCommonUtils.saveString("appLanguage", "ml", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 6:
                        if (!sThemeUtils.getLanguage(activity).equals("pt")) {
                            sCommonUtils.saveString("appLanguage", "pt", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 7:
                        if (!sThemeUtils.getLanguage(activity).equals("ru")) {
                            sCommonUtils.saveString("appLanguage", "ru", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 8:
                        if (!sThemeUtils.getLanguage(activity).equals("uk")) {
                            sCommonUtils.saveString("appLanguage", "uk", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 9:
                        if (!sThemeUtils.getLanguage(activity).equals("fr")) {
                            sCommonUtils.saveString("appLanguage", "fr", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 10:
                        if (!sThemeUtils.getLanguage(activity).equals("de")) {
                            sCommonUtils.saveString("appLanguage", "de", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 11:
                        if (!sThemeUtils.getLanguage(activity).equals("tr")) {
                            sCommonUtils.saveString("appLanguage", "tr", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 12:
                        if (!sThemeUtils.getLanguage(activity).equals("cs")) {
                            sCommonUtils.saveString("appLanguage", "cs", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 13:
                        if (!sThemeUtils.getLanguage(activity).equals("es")) {
                            sCommonUtils.saveString("appLanguage", "es", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 14:
                        if (!sThemeUtils.getLanguage(activity).equals("vi")) {
                            sCommonUtils.saveString("appLanguage", "vi", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 15:
                        if (!sThemeUtils.getLanguage(activity).equals("zh")) {
                            sCommonUtils.saveString("appLanguage", "zh", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 16:
                        if (!sThemeUtils.getLanguage(activity).equals("hu")) {
                            sCommonUtils.saveString("appLanguage", "hu", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 17:
                        if (!sThemeUtils.getLanguage(activity).equals("pl")) {
                            sCommonUtils.saveString("appLanguage", "pl", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 18:
                        if (!sThemeUtils.getLanguage(activity).equals("it")) {
                            sCommonUtils.saveString("appLanguage", "it", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 19:
                        if (!sThemeUtils.getLanguage(activity).equals("ar")) {
                            sCommonUtils.saveString("appLanguage", "ar", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 20:
                        if (!sThemeUtils.getLanguage(activity).equals("sk")) {
                            sCommonUtils.saveString("appLanguage", "sk", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                    case 21:
                        if (!sThemeUtils.getLanguage(activity).equals("he")) {
                            sCommonUtils.saveString("appLanguage", "he", activity);
                            Utils.restartApp(activity);
                        }
                        break;
                }
            }
        }.show();
    }

}