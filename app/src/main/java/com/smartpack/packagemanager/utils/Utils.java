/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.snackbar.Snackbar;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 07, 2020
 */

public class Utils {

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
    }

    /*
     * The following code is partly taken from https://github.com/SmartPack/SmartPack-Kernel-Manager
     * Ref: https://github.com/SmartPack/SmartPack-Kernel-Manager/blob/beta/app/src/main/java/com/smartpack/kernelmanager/utils/root/RootUtils.java
     */
    public static boolean rootAccess() {
        return Shell.rootAccess();
    }

    public static void runCommand(String command) {
        if (rootAccess()) {
            Shell.su(command).exec();
        } else {
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception ignored) {
            }
        }
    }

    @NonNull
    static String runAndGetOutput(String command) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> outputs = Shell.su(command).exec().getOut();
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    @NonNull
    public static String runAndGetError(String command) {
        StringBuilder sb = new StringBuilder();
        List<String> outputs = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        try {
            Shell.su(command).to(outputs, stderr).exec();
            outputs.addAll(stderr);
            if (ShellUtils.isValidOutput(outputs)) {
                for (String output : outputs) {
                    sb.append(output).append("\n");
                }
            }
            return removeSuffix(sb.toString()).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static String removeSuffix(@Nullable String s) {
        if (s != null && s.endsWith("\n")) {
            return s.substring(0, s.length() - "\n".length());
        }
        return s;
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/ViewUtils.java
     */

    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/Prefs.java
     */
    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static String getString(String name, String defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(name, defaults);
    }

    public static void saveString(String name, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).apply();
    }

    /*
     * The following code is partly taken from https://github.com/Grarak/KernelAdiutor
     * Ref: https://github.com/Grarak/KernelAdiutor/blob/master/app/src/main/java/com/grarak/kerneladiutor/utils/Utils.java
     */

    public static boolean isPackageInstalled(String packageID, Context context) {
        try {
            context.getPackageManager().getApplicationInfo(packageID, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isNotDonated(Context context) {
        return !isPackageInstalled("com.smartpack.donate", context);
    }

    public static boolean isProUser(Context context) {
        return getBoolean("support_received", false, context) || !isNotDonated(context);
    }

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
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

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static void delete(String path) {
        if (new File(path).isDirectory()) {
            for (File files : Objects.requireNonNull(new File(path).listFiles()))
                delete(files.getAbsolutePath());
        }
        new File(path).delete();
    }

    public static void copy(String source, String dest) {
        if (!exist(Objects.requireNonNull(new File(dest).getParentFile()).toString())) {
            mkdir(Objects.requireNonNull(new File(dest).getParentFile()).toString());
        }
        try {
            FileInputStream inputStream = new FileInputStream(new File(source));
            FileOutputStream outputStream = new FileOutputStream(new File(dest));

            byte[] buf = new byte[1024 * 1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException ignored) {}
    }

    public static void mkdir(String path) {
       new File(path).mkdirs();
    }

    public static void sleep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ignored) {}
    }

    public static void snackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
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

    public static void launchUrl(String url, Activity activity) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            activity.startActivity(i);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static String read(String file) {
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

    public static boolean exist(String file) {
        if (!file.startsWith("/storage/") && rootAccess()) {
            String output = runAndGetOutput("[ -e " + file + " ] && echo true");
            return !output.isEmpty() && output.equals("true");
        } else {
            return new File(file).exists();
        }
    }

    public static void unzip(String zip, String path) {
        try {
            new ZipFile(zip).extractAll(path);
        } catch (ZipException ignored) {}
    }

    public static void zip(String zip, List<File> files) {
        try {
            new ZipFile(zip).addFiles(files);
        } catch (ZipException ignored) {
        }
    }

    @RequiresApi(30)
    public static void requestPermission(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
        activity.finish();
    }

    @RequiresApi(30)
    public static boolean isPermissionDenied() {
        return !Environment.isExternalStorageManager();
    }

    /*
     * Taken and used almost as such from the following stackoverflow discussion
     * Ref: https://stackoverflow.com/questions/7203668/how-permission-can-be-checked-at-runtime-without-throwing-securityexception
     */
    public static boolean isPermissionDenied(Context context) {
        return (context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    public static String readAssetFile(Context context, String file) {
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

    public static void create(String text, File path) {
        try {
            if (!path.exists()) {
                path.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(path);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.close();
            fOut.close();
        } catch (Exception ignored) {
        }
    }

    public static void restartApp(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public static String getLanguage(Context context) {
        return getString("appLanguage", java.util.Locale.getDefault().getLanguage(),
                context);
    }

    public static void setLanguage(Context context) {
        Locale myLocale = new Locale(getString("appLanguage", java.util.Locale.getDefault()
                .getLanguage(), context));
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}