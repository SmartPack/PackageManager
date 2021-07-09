/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
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
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 25, 2020
 */

public class RecycleViewFilePickerAdapter extends RecyclerView.Adapter<RecycleViewFilePickerAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private final List<String> data;

    public RecycleViewFilePickerAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RecycleViewFilePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
        return new RecycleViewFilePickerAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewFilePickerAdapter.ViewHolder holder, int position) {
        if (new File(this.data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_folder));
            holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mIcon.getContext()));
            holder.mDescription.setVisibility(View.GONE);
            holder.mSize.setVisibility(View.GONE);
            holder.mCheckBox.setVisibility(View.GONE);
        } else if (this.data.get(position).endsWith(".apk")) {
            if (PackageData.getAPKIcon(data.get(position), holder.mIcon.getContext()) != null) {
                holder.mIcon.setImageDrawable(PackageData.getAPKIcon(data.get(position), holder.mIcon.getContext()));
            } else {
                holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mIcon.getContext()));
            }
            if (PackageData.getAPKId(data.get(position), holder.mIcon.getContext()) != null) {
                holder.mDescription.setText(PackageData.getAPKId(data.get(position), holder.mIcon.getContext()));
                holder.mDescription.setVisibility(View.VISIBLE);
            }
            holder.mCheckBox.setChecked(Common.getAppList().contains(this.data.get(position)));
            holder.mCheckBox.setOnClickListener(v -> {
                if (Common.getAppList().contains(this.data.get(position))) {
                    Common.getAppList().remove(this.data.get(position));
                } else {
                    Common.getAppList().add(this.data.get(position));
                }
                Common.getSelectCard().setVisibility(Common.getAppList().isEmpty() ? View.GONE : View.VISIBLE);
            });
            holder.mSize.setText(PackageData.getAPKSize(data.get(position)));
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_bundle));
            holder.mIcon.setColorFilter(Utils.isDarkTheme(holder.mIcon.getContext()) ? holder.mIcon.getContext()
                    .getResources().getColor(R.color.colorWhite) : holder.mIcon.getContext().getResources().getColor(R.color.colorBlack));
            holder.mSize.setText(PackageData.getAPKSize(data.get(position)));
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
        RecycleViewFilePickerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}