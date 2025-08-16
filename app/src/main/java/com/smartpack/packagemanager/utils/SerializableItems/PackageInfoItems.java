/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.SerializableItems;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 31, 2023
 */
public class PackageInfoItems implements Serializable {

    private final Drawable mActionIcon;
    private final String mTitle, mDescription, mDescriptionOne, mDescriptionTwo, mActionText;

    public PackageInfoItems(String title, String description, String descriptionOne, String descriptionTwo, String actionText, Drawable actionIcon) {
        this.mTitle = title;
        this.mDescription = description;
        this.mDescriptionOne = descriptionOne;
        this.mDescriptionTwo = descriptionTwo;
        this.mActionText = actionText;
        this.mActionIcon = actionIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDescriptionOne() {
        return mDescriptionOne;
    }

    public String getDescriptionTwo() {
        return mDescriptionTwo;
    }

    public String getActionText() {
        return mActionText;
    }

    public Drawable getActionIcon() {
        return mActionIcon;
    }

}