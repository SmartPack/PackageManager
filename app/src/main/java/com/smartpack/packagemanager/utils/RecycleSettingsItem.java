/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */

public class RecycleSettingsItem implements Serializable {
    private final String mTitle, mDescription, mUrl;
    private final Drawable mIcon;

    public RecycleSettingsItem(String title, String description, Drawable icon, String url) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
        this.mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public String getUrl() {
        return mUrl;
    }

}