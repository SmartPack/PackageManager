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

import in.sunilpaulmathew.sCommon.Utils.sCrashReporterUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set App Language
        sUtils.setLanguage(this);
        setContentView(R.layout.activity_main);

        // Record crashes
        new sCrashReporterUtils(sUtils.getColor(R.color.colorAccent, this), 20, this).initialize();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new PackageTasksFragment()).commit();
    }

}