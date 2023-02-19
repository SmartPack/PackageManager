/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.tasks;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import com.smartpack.packagemanager.activities.PackageExploreActivity;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExploreAPKTasks extends sExecutor {

    private final Activity mActivity;
    private final LinearLayout mLinearLayout;
    private final String mPath;

    public ExploreAPKTasks(LinearLayout linearLayout, String path, Activity activity) {
        mLinearLayout = linearLayout;
        mPath = path;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mLinearLayout.setVisibility(View.VISIBLE);
        if (sUtils.exist(new File(mActivity.getCacheDir().getPath(), "apk"))) {
            sUtils.delete(new File(mActivity.getCacheDir().getPath(), "apk"));
        }
        sUtils.mkdir(new File(mActivity.getCacheDir().getPath(), "apk"));
        Common.setPath(mActivity.getCacheDir().getPath() + "/apk");
    }

    @Override
    public void doInBackground() {
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(mPath)) {
            zipFileUtils.unzip(mActivity.getCacheDir().getPath() + "/apk");
        } catch (IOException ignored) {}
    }

    @Override
    public void onPostExecute() {
        mLinearLayout.setVisibility(View.GONE);
        Intent explorer = new Intent(mActivity, PackageExploreActivity.class);
        mActivity.startActivity(explorer);
    }

}