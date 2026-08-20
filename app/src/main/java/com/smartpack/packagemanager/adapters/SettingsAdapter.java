/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Serializables.SettingsItems;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private final List<SettingsItems> data;

    private final OnItemClickListener clickListener;

    public SettingsAdapter(List<SettingsItems> data, OnItemClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_settings, parent, false);
        return new SettingsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        SettingsItems items = this.data.get(position);

        if (items.getTitle() != null) {
            holder.mTitle.setTextColor(Utils.getColor(R.attr.colorPrimary, holder.mTitle.getContext()));
            holder.mTitle.setText(items.getTitle());
        } else {
            holder.mTitle.setTextColor(Utils.getColor(R.attr.editTextColor, holder.mTitle.getContext()));
        }

        if (items.isSectionHeading()) {
            holder.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        } else {
            holder.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        }

        if (items.getDescription() != null && !items.getDescription().trim().isEmpty()) {
            holder.mDescription.setText(items.getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }

        if (items.getIcon() != null) {
            holder.mIcon.setImageDrawable(items.getIcon());
            holder.mIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }

        AppSettings.setSlideInAnimation(holder.mTitle, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialTextView mDescription, mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
        }

        @Override
        public void onClick(View view) {
            SettingsItems items = data.get(getBindingAdapterPosition());
            clickListener.onItemClick(items, items.getId());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(SettingsItems items, int position);
    }

}