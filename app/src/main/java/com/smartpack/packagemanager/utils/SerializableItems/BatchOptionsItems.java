/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.SerializableItems;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 13, 2025
 */
public class BatchOptionsItems implements Serializable {

    private boolean mChecked;
    private final CharSequence mName;
    private final Drawable mIcon;
    private final int mStatus;
    private final String mPackageName;

    public BatchOptionsItems(CharSequence name, String packageName, Drawable icon, boolean checked, int status) {
        this.mName = name;
        this.mPackageName = packageName;
        this.mIcon = icon;
        this.mChecked = checked;
        this.mStatus = status;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getName() {
        return mName.toString();
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setChecked(boolean b) {
        mChecked = b;
    }

}