/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.material.snackbar.Snackbar;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.root.RootFile;
import com.smartpack.packagemanager.utils.root.RootUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class Utils {

    public static AppCompatImageButton mBack;
    public static AppCompatImageView mAppIcon;
    public static AppCompatTextView mCardTitle;
    public static AppCompatTextView mAppName;
    public static AppCompatTextView mAboutApp;
    public static AppCompatTextView mDevelopedBy;
    public static AppCompatTextView mCreditsTitle;
    public static AppCompatTextView mCredits;
    public static AppCompatTextView mForegroundText;
    public static AppCompatTextView mCancel;
    public static AppCompatImageView mDeveloper;
    public static boolean mForegroundActive = false;
    public static CardView mForegroundCard;

    public static boolean isPackageInstalled(String packageID, Context context) {
        try {
            context.getPackageManager().getApplicationInfo(packageID, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isNotDonated(Context context) {
        if (BuildConfig.DEBUG) return false;
        return !isPackageInstalled("com.smartpack.donate", context);
    }

    public static void initializeAppTheme(Context context) {
        if (getBoolean("dark_theme", false, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else if (getBoolean("light_theme", false, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void initializeFaceBookAds(Context context) {
        AudienceNetworkAds.initialize(context);
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static String readAssetFile(Context context, String file) {
        InputStream input = null;
        BufferedReader buf = null;
        try {
            StringBuilder s = new StringBuilder();
            input = context.getAssets().open(file);
            buf = new BufferedReader(new InputStreamReader(input));

            String str;
            while ((str = buf.readLine()) != null) {
                s.append(str).append("\n");
            }
            return s.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (input != null) input.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    public static void aboutDialogue(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mCardTitle.setText(R.string.about);
        mAppName.setText(activity.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mCredits.setText(activity.getString(R.string.credits_summary));
        mCardTitle.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        mAppIcon.setVisibility(View.VISIBLE);
        mAppName.setVisibility(View.VISIBLE);
        mAboutApp.setVisibility(View.VISIBLE);
        mDevelopedBy.setVisibility(View.VISIBLE);
        mDeveloper.setVisibility(View.VISIBLE);
        mCreditsTitle.setVisibility(View.VISIBLE);
        mCredits.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
        mForegroundActive = true;
        mForegroundCard.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public static void changeLogs(Activity activity) {
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(readAssetFile(
                    activity, "changelogs.json"))).getString("releaseNotes");
        } catch (JSONException ignored) {
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mAppName.setText(activity.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mForegroundText.setText(change_log);
        mAppIcon.setVisibility(View.VISIBLE);
        mAppName.setVisibility(View.VISIBLE);
        mForegroundText.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
        mForegroundActive = true;
        mForegroundCard.setVisibility(View.VISIBLE);
    }

    public static void closeForeground(Activity activity) {
        mCardTitle.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        mAppIcon.setVisibility(View.GONE);
        mAppName.setVisibility(View.GONE);
        mAboutApp.setVisibility(View.GONE);
        mDevelopedBy.setVisibility(View.GONE);
        mDeveloper.setVisibility(View.GONE);
        mCreditsTitle.setVisibility(View.GONE);
        mCredits.setVisibility(View.GONE);
        mForegroundText.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
        mForegroundCard.setVisibility(View.GONE);
        mForegroundActive = false;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static void showSnackbar(View view, String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static CharSequence htmlFrom(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static String readFile(String file) {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new FileReader(file));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = buf.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean existFile(String file) {
        return existFile(file, true);
    }

    private static boolean existFile(String file, boolean root) {
        return !root ? new File(file).exists() : new RootFile(file).exists();
    }

    static void copy(String source, String dest) {
        RootUtils.runCommand("cp -r " + source + " " + dest);
    }

    public static void create(String text, String path) {
        try {
            File logFile = new File(path);
            logFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(logFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.close();
            fOut.close();
        } catch (Exception ignored) {
        }
    }

    static void delete(String path) {
        if (Utils.existFile(path)) {
            RootUtils.runCommand("rm -r " + path);
        }
    }

    public static void sleep(int sec) {
        RootUtils.runCommand("sleep " + sec);
    }

    public static String getPath(File file) {
        String path = file.getAbsolutePath();
        if (path.startsWith("/document/raw:")) {
            path = path.replace("/document/raw:", "");
        } else if (path.startsWith("/document/primary:")) {
            path = (Environment.getExternalStorageDirectory() + ("/") + path.replace("/document/primary:", ""));
        } else if (path.startsWith("/document/")) {
            path = path.replace("/document/", "/storage/").replace(":", "/");
        }
        if (path.startsWith("/storage_root/storage/emulated/0")) {
            path = path.replace("/storage_root/storage/emulated/0", "/storage/emulated/0");
        } else if (path.startsWith("/storage_root")) {
            path = path.replace("storage_root", "storage/emulated/0");
        }
        if (path.startsWith("/external")) {
            path = path.replace("external", "storage/emulated/0");
        } if (path.startsWith("/root/")) {
            path = path.replace("/root", "");
        }
        if (path.contains("file%3A%2F%2F%2F")) {
            path = path.replace("file%3A%2F%2F%2F", "").replace("%2F", "/");
        }
        return path;
    }

    public static boolean isDocumentsUI(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /*
     * Taken and used almost as such from the following stackoverflow discussion
     * Ref: https://stackoverflow.com/questions/7203668/how-permission-can-be-checked-at-runtime-without-throwing-securityexception
     */
    public static boolean isStorageWritePermissionDenied(Context context) {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res != PackageManager.PERMISSION_GRANTED);
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static void setDefaultLanguage(Context context) {
        Utils.saveBoolean("use_english", false, context);
        Utils.saveBoolean("use_korean", false, context);
        Utils.saveBoolean("use_am", false, context);
        Utils.saveBoolean("use_el", false, context);
        Utils.saveBoolean("use_ml", false, context);
        Utils.saveBoolean("use_pt", false, context);
        Utils.saveBoolean("use_ru", false, context);
        Utils.saveBoolean("use_uk", false, context);
    }

    public static boolean languageDefault(Context context) {
        return !Utils.getBoolean("use_english", false, context)
                && !Utils.getBoolean("use_korean", false, context)
                && !Utils.getBoolean("use_am", false, context)
                && !Utils.getBoolean("use_el", false, context)
                && !Utils.getBoolean("use_ml", false, context)
                && !Utils.getBoolean("use_pt", false, context)
                && !Utils.getBoolean("use_ru", false, context)
                && !Utils.getBoolean("use_uk", false, context);
    }

    public static String getLanguage(Context context) {
        if (getBoolean("use_english", false, context)) {
            return  "en_US";
        } else if (getBoolean("use_korean", false, context)) {
            return  "ko";
        } else if (getBoolean("use_am", false, context)) {
            return  "am";
        } else if (getBoolean("use_el", false, context)) {
            return  "el";
        } else if (getBoolean("use_ml", false, context)) {
            return  "ml";
        } else if (getBoolean("use_pt", false, context)) {
            return  "pt";
        } else if (getBoolean("use_ru", false, context)) {
            return  "ru";
        } else if (getBoolean("use_uk", false, context)) {
            return  "uk";
        } else {
            return java.util.Locale.getDefault().getLanguage();
        }
    }

    public static void setLanguage(Context context) {
        Locale myLocale = new Locale(getLanguage(context));
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public static void resetDefault(Context context) {
        Utils.saveBoolean("asus_apps", false, context);
        Utils.saveBoolean("google_apps", false, context);
        Utils.saveBoolean("huawei_apps", false, context);
        Utils.saveBoolean("lg_apps", false, context);
        Utils.saveBoolean("mi_apps", false, context);
        Utils.saveBoolean("moto_apps", false, context);
        Utils.saveBoolean("oneplus_apps", false, context);
        Utils.saveBoolean("samsung_apps", false, context);
        Utils.saveBoolean("sony_apps", false, context);
        Utils.saveBoolean("system_apps", false, context);
        Utils.saveBoolean("user_apps", false, context);
    }

}