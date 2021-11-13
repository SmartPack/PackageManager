/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import java.io.Serializable;

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 * Modified by sunilpaulmathew <sunil.kde@gmail.com> on Mar 17, 2021
 */
public class RecycleViewAppOpsItem implements Serializable {
    private final String mTitle, mDescription;
    private final boolean mEnabled;

    public RecycleViewAppOpsItem(String title, String description, boolean enabled) {
        this.mTitle = title;
        this.mDescription = description;
        this.mEnabled = enabled;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

}