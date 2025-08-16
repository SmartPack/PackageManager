/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 1, 2020
 */
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        sThemeUtils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        LinearLayoutCompat mBottomLayout = findViewById(R.id.layout_bottom);
        MaterialButton mDocumentation = findViewById(R.id.documentation);
        MaterialButton mStart = findViewById(R.id.start);
        MaterialTextView mMainText = findViewById(R.id.main_text);
        ContentLoadingProgressBar mProgress = findViewById(R.id.progress);

        if (sCommonUtils.getBoolean("welcomeMessage", true, this)) {
            mMainText.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);

            mDocumentation.setOnClickListener(v -> sCommonUtils.launchUrl("https://smartpack.github.io/PackageManager/general/", this));

            mStart.setOnClickListener(v -> {
                mProgress.setVisibility(View.VISIBLE);
                mMainText.setText(getString(R.string.initializing));
                mBottomLayout.setVisibility(View.GONE);
                loadData(mProgress, this);
                sCommonUtils.saveBoolean("welcomeMessage",false, this);
            });
        } else {
            loadData(mProgress, this);
        }
    }

    private static void loadData(ContentLoadingProgressBar progressBar, Activity activity) {
        new sExecutor() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                PackageData.setRawData(progressBar, activity);
            }

            @Override
            public void onPostExecute() {
                Intent mainActivity = new Intent(activity, MainActivity.class);
                activity.startActivity(mainActivity);
                activity.finish();
            }
        }.execute();
    }

}