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
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.Flavor;
import com.smartpack.packagemanager.utils.tasks.MultipleAPKsTasks;
import com.smartpack.packagemanager.utils.tasks.SingleAPKTasks;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 12, 2021
 */
public class InstallerInstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer_instructions);

        MaterialButton mAdd = findViewById(R.id.add_button);
        SwitchMaterial mHideSwitch = findViewById(R.id.hide_switch);

        mHideSwitch.setChecked(sCommonUtils.getBoolean("neverShow", false, this));

        mHideSwitch.setOnClickListener(v -> sCommonUtils.saveBoolean("neverShow", !sCommonUtils.getBoolean(
                "neverShow",false, this), this));

        mAdd.setOnClickListener(v -> {
            if (Flavor.isFullVersion()) {
                Common.getAppList().clear();
                Common.setPath(FilePicker.getLastDirPath(this));
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                startActivity(filePicker);
                finish();
            } else {
                Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
                installer.setType("*/*");
                installer.addCategory(Intent.CATEGORY_OPENABLE);
                installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                installApp.launch(installer);
            }
        });
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