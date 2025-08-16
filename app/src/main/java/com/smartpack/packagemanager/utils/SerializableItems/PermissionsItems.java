/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.SerializableItems;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 08, 2022
 */
public class PermissionsItems implements Serializable {

    private final boolean mGranted;
    private final String mTitle, mDescription;

    public PermissionsItems(boolean granted, String title, String description) {
        this.mGranted = granted;
        this.mTitle = title;
        this.mDescription = description;
    }

    public boolean isGranted() {
        return mGranted;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

}