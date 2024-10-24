/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2020
 */
public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private final Activity activity;
    private static ClickListener clickListener;

    private final List<String> data;

    public FilePickerAdapter(List<String> data, Activity activity) {
        this.activity = activity;
        this.data = data;
    }

    @NonNull
    @Override
    public FilePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
        return new FilePickerAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull FilePickerAdapter.ViewHolder holder, int position) {
        if (position == 0) {
            holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_dots, holder.mIcon.getContext()));
            holder.mIcon.setRotation(90);
            holder.mTitle.setText(null);
            holder.mSize.setText(null);
        } else if (new File(this.data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_folder, holder.mIcon.getContext()));
            if (sThemeUtils.isDarkTheme(holder.mIcon.getContext())) {
                holder.mIcon.setBackground(sCommonUtils.getDrawable(R.drawable.ic_background_circle, holder.mIcon.getContext()));
            }
            holder.mDescription.setVisibility(View.GONE);
            holder.mSize.setVisibility(View.GONE);
            holder.mCheckBox.setVisibility(View.GONE);
        } else if (this.data.get(position).endsWith(".apk")) {
            holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mIcon.getContext()));
            if (sAPKUtils.getPackageName(data.get(position), holder.mIcon.getContext()) != null) {
                holder.mDescription.setText(sAPKUtils.getPackageName(data.get(position), holder.mIcon.getContext()));
                holder.mDescription.setVisibility(View.VISIBLE);
            }
            holder.mCheckBox.setChecked(Common.getAppList().contains(this.data.get(position)));
            holder.mCheckBox.setOnClickListener(v -> {
                if (Common.getAppList().contains(this.data.get(position))) {
                    Common.getAppList().remove(this.data.get(position));
                } else {
                    Common.getAppList().add(this.data.get(position));
                }
                Common.getCardView(activity, R.id.select).setVisibility(Common.getAppList().isEmpty() ? View.GONE : View.VISIBLE);
            });
            holder.mSize.setText(sAPKUtils.getAPKSize(new File(data.get(position)).length()));
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_bundle, holder.mIcon.getContext()));
            holder.mIcon.setColorFilter(sThemeUtils.isDarkTheme(holder.mIcon.getContext()) ? sCommonUtils.getColor(R.color.colorWhite, holder.mIcon.getContext()) :
                    sCommonUtils.getColor(R.color.colorBlack, holder.mIcon.getContext()));
            holder.mSize.setText(sAPKUtils.getAPKSize(new File(data.get(position)).length()));
            holder.mSize.setVisibility(View.VISIBLE);
        }
        holder.mTitle.setText(new File(this.data.get(position)).getName());
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mTitle, mDescription, mSize;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        FilePickerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}