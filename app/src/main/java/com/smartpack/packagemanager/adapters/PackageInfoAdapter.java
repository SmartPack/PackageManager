/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Serializables.PackageInfoItems;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 31, 2023
 */
public class PackageInfoAdapter extends RecyclerView.Adapter<PackageInfoAdapter.ViewHolder> {

    private final List<PackageInfoItems> data;
    private final OnItemClickListener clickListener;

    public PackageInfoAdapter(List<PackageInfoItems> data, OnItemClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PackageInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_packageinfo, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageInfoAdapter.ViewHolder holder, int position) {
        PackageInfoItems packageInfoItems = this.data.get(position);
        holder.mTitle.setText(packageInfoItems.getTitle());

        if (packageInfoItems.getDescription() != null) {
            holder.mDescription.setText(packageInfoItems.getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }

        if (packageInfoItems.getDescriptionOne() != null) {
            holder.mDescriptionOne.setText(packageInfoItems.getDescriptionOne());
            holder.mDescriptionOne.setVisibility(View.VISIBLE);
        } else {
            holder.mDescriptionOne.setVisibility(View.GONE);
        }

        if (packageInfoItems.getDescriptionTwo() != null) {
            holder.mDescriptionTwo.setText(packageInfoItems.getDescriptionTwo());
            holder.mDescriptionTwo.setVisibility(View.VISIBLE);
        } else {
            holder.mDescriptionTwo.setVisibility(View.GONE);
        }

        if (packageInfoItems.getActionIcon() != null && packageInfoItems.getActionText() != null) {
            holder.mIcon.setIcon(packageInfoItems.getActionIcon());
            holder.mIcon.setText(packageInfoItems.getActionText());
            holder.mIcon.setVisibility(View.VISIBLE);
            holder.mIcon.setOnClickListener(v -> clickListener.onItemClick(holder.getBindingAdapterPosition(), v));
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }

        AppSettings.setSlideInAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton mIcon;
        private final MaterialTextView mTitle, mDescription, mDescriptionOne, mDescriptionTwo;

        public ViewHolder(View view) {
            super(view);
            this.mIcon = view.findViewById(R.id.icon);
            this.mDescription = view.findViewById(R.id.description);
            this.mDescriptionOne = view.findViewById(R.id.description_one);
            this.mDescriptionTwo = view.findViewById(R.id.description_two);
            this.mTitle = view.findViewById(R.id.title);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }

}