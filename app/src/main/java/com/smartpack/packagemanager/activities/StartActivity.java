/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AsyncTasks;
import com.smartpack.packagemanager.utils.PackageData;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 1, 2020
 */

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        new AsyncTasks() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                PackageData.setRawData(StartActivity.this);
            }

            @Override
            public void onPostExecute() {
                Intent mainActivity = new Intent(StartActivity.this, MainActivity.class);
                startActivity(mainActivity);
                finish();
            }
        }.execute();
    }

}