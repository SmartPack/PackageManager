/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 28, 2020
 */

public class PackageTasksActivity extends AppCompatActivity {

    public static final String TITLE_START = "start", TITLE_FINISH = "finish";

    private MaterialTextView mCancelButton, mOutput, mPackageTitle, mStatus;

    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagetasks);

        mCancelButton = findViewById(R.id.cancel_button);
        mPackageTitle = findViewById(R.id.package_title);
        mOutput = findViewById(R.id.result_text);
        mStatus = findViewById(R.id.status_text);

        mScrollView = findViewById(R.id.scroll_view);

        mPackageTitle.setText(getIntent().getStringExtra(TITLE_START));

        if (SplitAPKInstaller.mInstall) {
            mPackageTitle.setVisibility(View.VISIBLE);
            mStatus.setVisibility(View.VISIBLE);
        }

        mCancelButton.setOnClickListener(v -> onBackPressed());

        refreshStatus(this);
    }

    public void refreshStatus(Activity activity) {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(() -> {
                            if (SplitAPKInstaller.mInstall) {
                                String installationStatus = Utils.getString("installationStatus", null, activity);
                                if (installationStatus != null && installationStatus.equals("waiting")) {
                                    if (PackageTasks.mOutput != null) {
                                        mOutput.setText(PackageTasks.mOutput.toString());
                                        mOutput.setVisibility(View.VISIBLE);
                                    }
                                    mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                                } else if (installationStatus != null) {
                                    mStatus.setText(getString(R.string.result, installationStatus.equals("success") ? getString(R.string.installation_status_success)
                                            : getString(R.string.installation__status_failed)));
                                    mPackageTitle.setText(getIntent().getStringExtra(TITLE_FINISH));
                                    mCancelButton.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (PackageTasks.mOutput != null) {
                                    mOutput.setText(PackageTasks.mOutput.toString());
                                    mPackageTitle.setVisibility(View.VISIBLE);
                                    mOutput.setVisibility(View.VISIBLE);
                                    if (!PackageTasks.mRunning) {
                                        mPackageTitle.setText(getIntent().getStringExtra(TITLE_FINISH));
                                        mCancelButton.setVisibility(View.VISIBLE);
                                    } else {
                                        mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                                    }
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
        if (PackageTasks.mRunning) {
            return;
        }
        if (SplitAPKInstaller.mInstall) {
            SplitAPKInstaller.mInstall = false;
            if (Utils.getString("installationStatus", null, this).equals("success")) {
                PackageTasks.mReloadPage = true;
            }
        }
        super.onBackPressed();
    }

}