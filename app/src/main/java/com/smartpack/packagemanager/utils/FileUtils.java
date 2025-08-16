/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import androidx.annotation.NonNull;

import com.smartpack.packagemanager.dialogs.ProgressDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 19, 2023
 */
public class FileUtils extends File {

    private final ProgressDialog mProgressDialog;

    public FileUtils(@NonNull File destFile, ProgressDialog progressDialog) {
        super(destFile.toURI());
        this.mProgressDialog = progressDialog;
    }

    public void copy(String sourcePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(sourcePath);
        FileOutputStream outputStream = new FileOutputStream(toString());

        copyStream(inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }

    private void copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = from.read(buf)) > 0) {
            to.write(buf, 0, len);
            mProgressDialog.updateProgress(len);
        }
    }

}