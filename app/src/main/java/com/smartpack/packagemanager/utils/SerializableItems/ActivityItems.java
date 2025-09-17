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
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 17, 2025
 */
public class ActivityItems implements Serializable {

    private final boolean exported;
    private final CharSequence label;
    private final Drawable icon;
    private final String name;

    public ActivityItems(String name, CharSequence label, Drawable icon, boolean exported) {
        this.name = name;
        this.label = label;
        this.icon = icon;
        this.exported = exported;
    }

    public boolean exported() {
        return exported;
    }

    public CharSequence getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

}