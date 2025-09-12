/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.SerializableItems;

import java.io.File;
import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 09, 2025
 */
public class APKPickerItems implements Serializable {

    private boolean selected;
    private final File apkFile;

    public APKPickerItems(File apkFile, boolean selected) {
        this.apkFile = apkFile;
        this.selected = selected;
    }

    public boolean isSelected() {
        return apkFile.exists() && apkFile.isFile() && apkFile.getName().endsWith(".apk") && selected;
    }

    public File getAPKFile() {
        return apkFile;
    }

    public long getAPKSize() {
        return apkFile.length();
    }

    public String getAPKName() {
        return apkFile.getName();
    }

    public String getAPKPath() {
        return apkFile.getAbsolutePath();
    }

    public void isSelected(boolean b) {
        selected = b;
    }

}