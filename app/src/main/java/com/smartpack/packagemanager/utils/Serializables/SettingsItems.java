/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.Serializables;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsItems implements Serializable {

    private String mDescription;
    private final String mTitle, mUrl;
    private final Drawable mIcon;
    private final int mID;

    public SettingsItems(String title, String description, Drawable icon, String url, int id) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
        this.mUrl = url;
        this.mID = id;
    }

    public SettingsItems(String title, String description, Drawable icon, int id) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
        this.mUrl = null;
        this.mID = id;
    }

    public SettingsItems(String title, String description, Drawable icon, String url) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
        this.mUrl = url;
        this.mID = 0;
    }

    public SettingsItems(String title) {
        this.mTitle = title;
        this.mDescription = null;
        this.mIcon = null;
        this.mUrl = null;
        this.mID = 0;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public boolean isSectionHeading() {
        return this.mID == 0 && this.mTitle != null && this.mUrl != null && this.mDescription == null;
    }

    public boolean isWebLink() {
        return this.mID == 0 && this.mUrl != null;
    }

    public int getId() {
        return this.mID;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

}