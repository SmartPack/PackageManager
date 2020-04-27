/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.root;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smartpack.packagemanager.BuildConfig;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.util.ArrayList;
import java.util.List;

public class RootUtils {

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */

        //Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR); (not yet, make it optional cuz it may break current stuff)
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    public static boolean rootAccessDenied() {
        return !Shell.rootAccess();
    }

    static void chmod(String file, String permission) {
        Shell.su("chmod " + permission + " " + file).submit();
    }

    @NonNull
    public static String runCommand(String command) {
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

}