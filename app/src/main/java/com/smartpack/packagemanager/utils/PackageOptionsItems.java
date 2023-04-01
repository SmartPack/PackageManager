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
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 31, 2023
 */
public class PackageOptionsItems implements Serializable {

    private final Drawable mIcon;
    private final int mPosition;
    private final String mText;

    public PackageOptionsItems(Drawable icon, String text, int position) {
        this.mIcon = icon;
        this.mText = text;
        this.mPosition = position;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public int getPosition() {
        return mPosition;
    }

    public String getText() {
        return mText;
    }

}