/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageTasks;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 28, 2020
 */

public class PackageTasksActivity extends AppCompatActivity {

    public static final String TITLE_START = "start";
    public static final String TITLE_FINISH = "finish";

    private static MaterialTextView mCancelButton;
    private static MaterialTextView mPackageTitle;
    private static MaterialTextView mOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagetasks);

        mCancelButton = findViewById(R.id.cancel_button);
        mPackageTitle = findViewById(R.id.package_title);
        mOutput = findViewById(R.id.result_text);
        mCancelButton.setOnClickListener(v -> {
            onBackPressed();
        });
        refreshStatus();
    }

    public void refreshStatus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(() -> {
                            if (!PackageTasks.mRunning) {
                                mPackageTitle.setText(getIntent().getStringExtra(TITLE_FINISH));
                            } else {
                                mPackageTitle.setText(getIntent().getStringExtra(TITLE_START));
                            }
                            if (PackageTasks.mOutput != null) {
                                mOutput.setText(PackageTasks.mOutput.toString());
                                mPackageTitle.setVisibility(View.VISIBLE);
                                mOutput.setVisibility(View.VISIBLE);
                                if (!PackageTasks.mRunning) {
                                    mCancelButton.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (PackageTasks.mRunning) return;
        super.onBackPressed();
    }

}
