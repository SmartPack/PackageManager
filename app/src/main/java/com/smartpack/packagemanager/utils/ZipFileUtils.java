/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.app.ProgressDialog;
import android.widget.ProgressBar;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 19, 2023
 */
public class ZipFileUtils extends ZipFile {

    private ProgressBar mProgressBar = null;
    private ProgressDialog mProgressDialog = null;

    public ZipFileUtils(String zipFile) {
        super(zipFile);
    }

    public void setProgress(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public void setProgress(ProgressDialog progressDialog) {
        mProgressDialog = progressDialog;
    }

    private void setProgress() {
        if (mProgressBar != null) {
            if (mProgressBar.getProgress() < mProgressBar.getMax()) {
                mProgressBar.setProgress(mProgressBar.getProgress() + 1);
            } else {
                mProgressBar.setProgress(0);
            }
        } else if (mProgressDialog != null) {
            if (mProgressDialog.getProgress() < mProgressDialog.getMax()) {
                mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
            } else {
                mProgressDialog.setProgress(0);
            }
        }
    }

    private void setProgressMax(int max) {
        if (mProgressBar != null) {
            mProgressBar.setMax(max);
        } else if (mProgressDialog != null) {
            mProgressDialog.setMax(max);
        }
    }

    public void unzip(String path) throws ZipException {
        setProgressMax(getFileHeaders().size());
        for (FileHeader fileHeaders : getFileHeaders()) {
            extractFile(fileHeaders, path);
            setProgress();
        }
    }

    public void zip(List<File> files) throws ZipException {
        setProgressMax(files.size());
        for (File file : files) {
            addFile(file);
            setProgress();
        }
    }

}