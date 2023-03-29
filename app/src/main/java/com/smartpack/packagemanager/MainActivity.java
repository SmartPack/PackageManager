/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.smartpack.packagemanager.fragments.PackageTasksFragment;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CrashReporter.sCrashReporter;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set App Language
        sThemeUtils.setLanguage(this);
        setContentView(R.layout.activity_main);

        // Record crashes
        sCrashReporter crashReporter = new sCrashReporter(this);
        crashReporter.setAccentColor(sCommonUtils.getColor(R.color.colorAccent, this));
        crashReporter.setTitleSize(20);
        crashReporter.initialize();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new PackageTasksFragment()).commit();
    }

}