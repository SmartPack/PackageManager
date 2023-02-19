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

import java.io.File;
import java.io.IOException;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 19, 2023
 */
public class ZipFileUtils extends ZipFile {

    public ZipFileUtils(String zipFile) {
        super(zipFile);
    }

    public void unzip(String path) {
        try (ZipFile zipFile = new ZipFile(toString())) {
            zipFile.extractAll(path);
        } catch (IOException ignored) {}
    }

    public void zip(List<File> files) {
        try (ZipFile zipFile = new ZipFile(toString())) {
            zipFile.addFiles(files);
        } catch (IOException ignored) {
        }
    }

    public void zip(List<File> files, ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setMax(files.size());
        }
        for (File file : files) {
            try (ZipFile zipFile = new ZipFile(toString())) {
                zipFile.addFile(file);
                if (progressBar != null) {
                    if (progressBar.getProgress() < files.size()) {
                        progressBar.setProgress(progressBar.getProgress() + 1);
                    } else {
                        progressBar.setProgress(0);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

}