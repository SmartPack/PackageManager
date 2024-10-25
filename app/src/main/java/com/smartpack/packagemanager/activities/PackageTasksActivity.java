/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;

import java.lang.ref.WeakReference;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 28, 2020
 */
public class PackageTasksActivity extends AppCompatActivity {

    public static final String TITLE_START = "start", TITLE_FINISH = "finish";
    private MaterialCardView mCloseButton;
    private MaterialTextView mOutput, mPackageTitle;
    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagetasks);

        mCloseButton = findViewById(R.id.close_card);
        mPackageTitle = findViewById(R.id.package_title);
        mOutput = findViewById(R.id.result_text);

        mScrollView = findViewById(R.id.scroll_view);

        mPackageTitle.setText(getIntent().getStringExtra(TITLE_START));

        mCloseButton.setOnClickListener(v -> backPressedEvent());

        Thread mRefreshThread = new RefreshThread(this);
        mRefreshThread.start();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressedEvent();
            }
        });
    }

    private class RefreshThread extends Thread {
        WeakReference<PackageTasksActivity> mInstallerActivityRef;
        RefreshThread(PackageTasksActivity activity) {
            mInstallerActivityRef = new WeakReference<>(activity);
        }
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(250);
                    final PackageTasksActivity activity = mInstallerActivityRef.get();
                    if(activity == null){
                        break;
                    }
                    activity.runOnUiThread(() -> {
                        if (Common.getOutput() != null) {
                            mOutput.setText(Common.getOutput().toString());
                            mOutput.setVisibility(View.VISIBLE);
                            if (!Common.isRunning()) {
                                mPackageTitle.setText(getIntent().getStringExtra(TITLE_FINISH));
                                mCloseButton.setVisibility(View.VISIBLE);
                            } else {
                                mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                            }
                        }
                    });
                }
            } catch (InterruptedException ignored) {}
        }
    }

    private void backPressedEvent() {
        if (Common.isRunning()) {
            return;
        }
        finish();
    }

}