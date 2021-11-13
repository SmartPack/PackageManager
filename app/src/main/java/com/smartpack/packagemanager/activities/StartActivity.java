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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sThemeUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

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

        LinearLayout mBottomLayout = findViewById(R.id.layout_bottom);
        MaterialCardView mDocumentationCard = findViewById(R.id.documentation_card);
        MaterialCardView mStartCard = findViewById(R.id.start_card);
        MaterialTextView mMainText = findViewById(R.id.main_text);
        ProgressBar mProgress = findViewById(R.id.progress);

        if (sUtils.getBoolean("welcomeMessage", true, this)) {
            mMainText.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);

            mDocumentationCard.setOnClickListener(v -> sUtils.launchUrl("https://smartpack.github.io/PackageManager/general/", this));

            mStartCard.setOnClickListener(v -> {
                mProgress.setVisibility(View.VISIBLE);
                mMainText.setText(getString(R.string.initializing));
                mBottomLayout.setVisibility(View.GONE);
                loadData(this);
                sUtils.saveBoolean("welcomeMessage",false, this);
            });
        } else {
            loadData(this);
        }
    }

    private static void loadData(Activity activity) {
        new sExecutor() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                PackageData.setRawData(activity);
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