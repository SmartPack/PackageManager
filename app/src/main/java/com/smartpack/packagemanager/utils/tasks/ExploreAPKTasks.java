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
import android.widget.ProgressBar;

import com.smartpack.packagemanager.activities.PackageExploreActivity;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.ZipFileUtils;

import java.io.File;
import java.io.IOException;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2023
 */
public class ExploreAPKTasks extends sExecutor {

    private final Activity mActivity;
    private final LinearLayout mLinearLayout;
    private final ProgressBar mProgressBar;
    private final String mPath;

    public ExploreAPKTasks(LinearLayout linearLayout, ProgressBar progressBar, String path, Activity activity) {
        mLinearLayout = linearLayout;
        mProgressBar = progressBar;
        mPath = path;
        mActivity = activity;

    }

    @Override
    public void onPreExecute() {
        mProgressBar.setIndeterminate(false);
        mLinearLayout.setVisibility(View.VISIBLE);
        if (sFileUtils.exist(new File(mActivity.getCacheDir().getPath(), "apk"))) {
            sFileUtils.delete(new File(mActivity.getCacheDir().getPath(), "apk"));
        }
        sFileUtils.mkdir(new File(mActivity.getCacheDir().getPath(), "apk"));
        Common.setPath(mActivity.getCacheDir().getPath() + "/apk");
    }

    @Override
    public void doInBackground() {
        try (ZipFileUtils zipFileUtils = new ZipFileUtils(mPath)) {
            zipFileUtils.setProgress(mProgressBar);
            zipFileUtils.unzip(mActivity.getCacheDir().getPath() + "/apk");
        } catch (IOException ignored) {}
    }

    @Override
    public void onPostExecute() {
        mLinearLayout.setVisibility(View.GONE);
        mProgressBar.setIndeterminate(true);
        Intent explorer = new Intent(mActivity, PackageExploreActivity.class);
        mActivity.startActivity(explorer);
    }

}