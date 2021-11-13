/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import in.sunilpaulmathew.sCommon.Utils.sJSONUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */
public class ChangeLogsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelogs);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialTextView mChangeLogText = findViewById(R.id.changelog_text);

        mBack.setOnClickListener(v -> finish());
        mChangeLogText.setText(sJSONUtils.getString(sJSONUtils.getJSONObject(sUtils.readAssetFile(
                "changelogs.json", this)), "releaseNotes"));
    }

}