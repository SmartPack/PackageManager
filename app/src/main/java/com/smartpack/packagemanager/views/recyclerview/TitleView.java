/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.views.recyclerview;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.smartpack.packagemanager.R;

/**
 *  Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2020
 *
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class TitleView extends RecyclerViewItem {

    private AppCompatTextView mTitle;

    private CharSequence mTitleText;

    @Override
    public int getLayoutRes() {
        return R.layout.rv_title_view;
    }

    @Override
    public void onCreateView(View view) {
        mTitle = view.findViewById(R.id.title);

        setFullSpan(true);
        super.onCreateView(view);
    }

    public void setText(CharSequence text) {
        mTitleText = text;
        refresh();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mTitle != null && mTitleText != null) {
            mTitle.setText(mTitleText);
        }
    }

    @Override
    protected boolean cardCompatible() {
        return false;
    }
}
