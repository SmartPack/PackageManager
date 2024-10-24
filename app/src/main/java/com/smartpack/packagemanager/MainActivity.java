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
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpack.packagemanager.activities.FilePickerActivity;
import com.smartpack.packagemanager.activities.InstallerInstructionsActivity;
import com.smartpack.packagemanager.fragments.ExportedAppsFragment;
import com.smartpack.packagemanager.fragments.PackageTasksFragment;
import com.smartpack.packagemanager.fragments.SettingsFragment;
import com.smartpack.packagemanager.fragments.UninstalledAppsFragment;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.Flavor;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.tasks.MultipleAPKsTasks;
import com.smartpack.packagemanager.utils.tasks.SingleAPKTasks;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CrashReporter.sCrashReporter;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */
public class MainActivity extends AppCompatActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set App Language
        sThemeUtils.setLanguage(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton mFAB = findViewById(R.id.fab);

        // Record crashes
        sCrashReporter crashReporter = new sCrashReporter(this);
        crashReporter.setAccentColor(Integer.MIN_VALUE);
        crashReporter.setTitleSize(20);
        crashReporter.initialize();

        Menu menu = mBottomNav.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, null).setIcon(R.drawable.ic_apps);
        menu.add(Menu.NONE, 1, Menu.NONE, null).setIcon(R.drawable.ic_apks);
        if (new RootShell().rootAccess() || new ShizukuShell().isReady()) {
            menu.add(Menu.NONE, 2, Menu.NONE, null).setIcon(R.drawable.ic_delete);
        }
        menu.add(Menu.NONE, 3, Menu.NONE, null).setIcon(R.drawable.ic_settings);

        mBottomNav.setOnItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case 0:
                            mFragment = new PackageTasksFragment();
                            break;
                        case 1:
                            mFragment = new ExportedAppsFragment();
                            break;
                        case 2:
                            mFragment = new UninstalledAppsFragment();
                            break;
                        case 3:
                            mFragment = new SettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mFragment).commit();
                    return true;
                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PackageTasksFragment()).commit();
        }

        mFAB.setOnClickListener(v -> {
            if (sCommonUtils.getBoolean("neverShow", false, this)) {
                if (Flavor.isFullVersion()) {
                    Common.getAppList().clear();
                    Common.setPath(FilePicker.getLastDirPath(this));
                    Intent filePicker = new Intent(this, FilePickerActivity.class);
                    startActivity(filePicker);
                } else {
                    Intent installer = getInstallerIntent();
                    installApp.launch(installer);
                }
            } else {
                Intent installer = new Intent(this, InstallerInstructionsActivity.class);
                startActivity(installer);
            }
        });
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

    ActivityResultLauncher<Intent> installApp = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (data.getClipData() != null) {
                        new MultipleAPKsTasks(data.getClipData(), this).execute();
                    } else if (uriFile != null) {
                        new SingleAPKTasks(uriFile, this).execute();
                    }
                }
            }
    );

}