/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.smartpack.packagemanager.activities.BaseActivity;
import com.smartpack.packagemanager.activities.InstallerInstructionsActivity;
import com.smartpack.packagemanager.activities.SettingsActivity;
import com.smartpack.packagemanager.fragments.ExportedAppsFragment;
import com.smartpack.packagemanager.fragments.InstalledAppsFragment;
import com.smartpack.packagemanager.fragments.UninstalledAppsFragment;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.tasks.MultipleAPKsTasks;
import com.smartpack.packagemanager.utils.tasks.SingleAPKTasks;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CrashReporter.sCrashReporter;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;
import navView.NavView;
import navView.serializables.NavViewEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set App Language
        sThemeUtils.setLanguage(this);
        setContentViewWithInsets(R.layout.activity_main, R.id.layout_root);

        NavView mNavView = findViewById(R.id.nav_view);

        // Record crashes
        sCrashReporter crashReporter = new sCrashReporter(this);
        crashReporter.setAccentColor(Integer.MIN_VALUE);
        crashReporter.setTitleSize(20);
        crashReporter.initialize();

        List<NavViewEntry> data = new CopyOnWriteArrayList<>();
        data.add(new NavViewEntry(InstalledAppsFragment::new, R.drawable.ic_apps));
        data.add(new NavViewEntry(ExportedAppsFragment::new, R.drawable.ic_apks));
        data.add(new NavViewEntry(UninstalledAppsFragment::new, R.drawable.ic_delete));

        mNavView.setNavigationItems(data);

        mNavView.setOnItemSelectedListener((fragment, position) -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        );

        mNavView.setOnMenuButtonClicked(R.drawable.ic_settings, () -> startActivity(new Intent(this, SettingsActivity.class)));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new InstalledAppsFragment()).commit();
        }
    }

    public void launchInstaller() {
        if (sCommonUtils.getBoolean("neverShow", false, this)) {
            Intent installer = getInstallerIntent();
            filePickerIntent.launch(installer);
        } else {
            Intent installer = new Intent(this, InstallerInstructionsActivity.class);
            installApp.launch(installer);
        }
    }

    @NonNull
    private static Intent getInstallerIntent() {
        Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
        installer.setType("*/*");
        String[] mimeTypes = {
                "application/vnd.android.package-archive",
                "application/xapk-package-archive",
                "application/octet-stream",
                "application/vnd.apkm"
        };
        installer.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        installer.addCategory(Intent.CATEGORY_OPENABLE);
        installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        return installer;
    }

    private final ActivityResultLauncher<Intent> installApp = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean update = Objects.requireNonNull(data).getBooleanExtra("INSTALL_STATUS_UPDATE", false);

                    if (!update) {
                        AppSettings.navigateToFragment(this, 0);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> filePickerIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (data.getClipData() != null) {
                        new MultipleAPKsTasks(data.getClipData(), installApp, this).execute();
                    } else if (uriFile != null) {
                        new SingleAPKTasks(uriFile, installApp::launch, this).execute();
                    }
                }
            }
    );

}