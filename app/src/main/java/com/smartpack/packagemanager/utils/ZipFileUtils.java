/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

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

    public ZipFileUtils(String zipFile) {
        super(zipFile);
    }

    public void unzip(String path) throws ZipException {
        extractAll(path);
    }

    public void unzip(String path, ProgressBar progressBar) throws ZipException {
        if (progressBar != null) {
            progressBar.setMax(getFileHeaders().size());
        }
        for (FileHeader fileHeaders : getFileHeaders()) {
            extractFile(fileHeaders, path);
            if (progressBar != null) {
                if (progressBar.getProgress() < getFileHeaders().size()) {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                } else {
                    progressBar.setProgress(0);
                }
            }
        }
    }

    public void zip(List<File> files) throws ZipException {
        addFiles(files);
    }

    public void zip(List<File> files, ProgressBar progressBar) throws ZipException {
        if (progressBar != null) {
            progressBar.setMax(files.size());
        }
        for (File file : files) {
            addFile(file);
            if (progressBar != null) {
                if (progressBar.getProgress() < files.size()) {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                } else {
                    progressBar.setProgress(0);
                }
            }
        }
    }

}