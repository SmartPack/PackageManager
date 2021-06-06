/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewExportedAppsAdapter;
import com.smartpack.packagemanager.utils.Downloads;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsActivity extends AppCompatActivity {

    private LinearLayout mProgressLayout;
    private RecyclerView mRecyclerView;
    private RecycleViewExportedAppsAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exported_apps);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        mProgressLayout = findViewById(R.id.progress_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        TabLayout mTabLayout = findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        mBack.setOnClickListener(v -> onBackPressed());

        if (Build.VERSION.SDK_INT < 30 && Utils.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(getString(R.string.permission_denied_write_storage));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                finish();
            });
            return;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        loadUI();

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(this))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = Utils.getString("downloadTypes", "apks", ExportedAppsActivity.this);
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            Utils.saveString("downloadTypes", "apks", ExportedAppsActivity.this);
                            loadUI();
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            Utils.saveString("downloadTypes", "bundles", ExportedAppsActivity.this);
                            loadUI();
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(Downloads.getData(this).get(position).endsWith(".apkm") ? R.string.bundle_install_apks
                            : R.string.install_question, new File(Downloads.getData(this).get(position)).getName()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.install, (dialog, id) -> {
                        if (Downloads.getData(this).get(position).endsWith(".apkm")) {
                            SplitAPKInstaller.handleAppBundle(mProgressLayout, Downloads.getData(this).get(position), this);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri uriFile;
                            uriFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                                    new File(Downloads.getData(this).get(position)));
                            intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
                            startActivity(Intent.createChooser(intent, ""));
                        }
                    }).show();
        });

    }

    private void loadUI() {
        mRecycleViewAdapter = new RecycleViewExportedAppsAdapter(Downloads.getData(this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);
    }

    private int getTabPosition(Activity activity) {
        if (Utils.getString("downloadTypes", "apks", activity).equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
    }

}