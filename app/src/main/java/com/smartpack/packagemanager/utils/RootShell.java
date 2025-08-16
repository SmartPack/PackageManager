/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smartpack.packagemanager.BuildConfig;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 29, 2022
 */
public class RootShell {

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
    }

    public RootShell() {
    }

    /*
     * The following code is partly taken from https://github.com/SmartPack/SmartPack-Kernel-Manager
     * Ref: https://github.com/SmartPack/SmartPack-Kernel-Manager/blob/beta/app/src/main/java/com/smartpack/kernelmanager/utils/root/RootUtils.java
     */
    public boolean rootAccess() {
        return Shell.getShell().isRoot();
    }

    private static String removeSuffix(@Nullable String s) {
        if (s != null && s.endsWith("\n")) {
            return s.substring(0, s.length() - "\n".length());
        }
        return s;
    }

    @NonNull
    public String runAndGetOutput(String command) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> outputs = Shell.cmd(command).exec().getOut();
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
    public String runAndGetError(String command) {
        StringBuilder sb = new StringBuilder();
        List<String> outputs = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        try {
            Shell.cmd(command).to(outputs, stderr).exec();
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

    public void closeSU() {
        try {
            Objects.requireNonNull(Shell.getCachedShell()).close();
        } catch (IOException ignored) {
        }
    }

    public void runCommand(String command) {
        Shell.cmd(command).exec();
    }

}