/*
 * Copyright (C) 2024-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.services;

import androidx.annotation.Keep;

import com.smartpack.packagemanager.IUserService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UserService extends IUserService.Stub {

    @Keep
    public UserService() {
    }

    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public String runShellCommand(String command) {
        Process process = null;
        StringBuilder output = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(command, null, null);
            BufferedReader mInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            process.waitFor();

            String line;
            while ((line = mInput.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = mError.readLine()) != null) {
                output.append(line).append("\n");
            }

        }
        catch (Exception ignored) {
        }
        finally {
            if (process != null)
                process.destroy();
        }

        return output.toString();
    }

}