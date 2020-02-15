/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.views.recyclerview;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;

import com.smartpack.packagemanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 *  Created by sunilpaulmathew <sunil.kde@gmail.com> on February 12, 2020
 *
 * Adapted from https://github.com/Grarak/KernelAdiutor by Willi Ye.
 */

public class SwitchView extends RecyclerViewItem {

    public interface OnSwitchListener {
        void onChanged(SwitchView switchView, boolean isChecked);
    }

    private AppCompatImageView mImageView;
    private AppCompatTextView mSummary;
    private SwitchCompat mSwitcher;

    private Drawable mImage;
    private CharSequence mSummaryText;
    private boolean mChecked;

    private List<OnSwitchListener> mOnSwitchListeners = new ArrayList<>();

    @Override
    public int getLayoutRes() {
        return R.layout.rv_switch_view;
    }

    @Override
    public void onCreateView(View view) {
        mImageView = view.findViewById(R.id.image);
        mSummary = view.findViewById(R.id.summary);
        mSwitcher = view.findViewById(R.id.switcher);

        super.onCreateView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitcher.setChecked(!mChecked);
            }
        });
        mSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mChecked = isChecked;
                List<OnSwitchListener> applied = new ArrayList<>();
                for (OnSwitchListener onSwitchListener : mOnSwitchListeners) {
                    if (applied.indexOf(onSwitchListener) == -1) {
                        onSwitchListener.onChanged(SwitchView.this, isChecked);
                        applied.add(onSwitchListener);
                    }
                }
            }
        });
    }

    public void setDrawable(Drawable drawable) {
        mImage = drawable;
        refresh();
    }

    public void setSummary(CharSequence summary) {
        mSummaryText = summary;
        refresh();
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        refresh();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void addOnSwitchListener(OnSwitchListener onSwitchListener) {
        mOnSwitchListeners.add(onSwitchListener);
    }

    public void clearOnSwitchListener() {
        mOnSwitchListeners.clear();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mImageView != null && mImage != null) {
            mImageView.setImageDrawable(mImage);
            mImageView.setVisibility(View.VISIBLE);
        }
        if (mSummary != null && mSummaryText != null) {
            mSummary.setText(mSummaryText);
        }
        if (mSwitcher != null) {
            mSwitcher.setChecked(mChecked);
        }
    }
}
