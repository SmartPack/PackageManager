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
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Serializables.PackageOptionsItems;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 31, 2023
 */
public class PackageOptionsAdapter extends RecyclerView.Adapter<PackageOptionsAdapter.ViewHolder> {

    private final List<PackageOptionsItems> data;
    private final OnItemClickListener clickListener;

    public PackageOptionsAdapter(List<PackageOptionsItems> data, OnItemClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PackageOptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_packageoptions, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageOptionsAdapter.ViewHolder holder, int position) {
        holder.mIcon.setText(this.data.get(position).getText());
        holder.mIcon.setIcon(this.data.get(position).getIcon());

        AppSettings.setSlideInAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MaterialButton mIcon;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getBindingAdapterPosition(), view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }

}