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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageInfoItems;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 31, 2023
 */
public class PackageInfoAdapter extends RecyclerView.Adapter<PackageInfoAdapter.ViewHolder> {

    private static ClickListener mClickListener;
    private static List<PackageInfoItems> data;

    public PackageInfoAdapter(List<PackageInfoItems> data) {
        PackageInfoAdapter.data = data;
    }

    @NonNull
    @Override
    public PackageInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_packageinfo, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageInfoAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).getTitle());

        if (data.get(position).getDescription() != null) {
            holder.mDescription.setText(data.get(position).getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }

        if (data.get(position).getDescriptionOne() != null) {
            holder.mDescriptionOne.setText(data.get(position).getDescriptionOne());
            holder.mDescriptionOne.setVisibility(View.VISIBLE);
        } else {
            holder.mDescriptionOne.setVisibility(View.GONE);
        }

        if (data.get(position).getDescriptionTwo() != null) {
            holder.mDescriptionTwo.setText(data.get(position).getDescriptionTwo());
            holder.mDescriptionTwo.setVisibility(View.VISIBLE);
        } else {
            holder.mDescriptionTwo.setVisibility(View.GONE);
        }

        if (data.get(position).getActionIcon() != null && data.get(position).getActionText() != null) {
            holder.mActionIcon.setIcon(data.get(position).getActionIcon());
            holder.mActionText.setText(data.get(position).getActionText());
            holder.mActionLayout.setVisibility(View.VISIBLE);
            holder.mActionLayout.setOnClickListener(v -> mClickListener.onItemClick(position, v));
        } else {
            holder.mActionLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton mActionIcon;
        private final LinearLayoutCompat mActionLayout;
        private final MaterialTextView mTitle, mDescription, mDescriptionOne, mDescriptionTwo, mActionText;

        public ViewHolder(View view) {
            super(view);
            this.mActionIcon = view.findViewById(R.id.action_icon);
            this.mActionLayout = view.findViewById(R.id.action_layout);
            this.mActionText = view.findViewById(R.id.action_text);
            this.mDescription = view.findViewById(R.id.description);
            this.mDescriptionOne = view.findViewById(R.id.description_one);
            this.mDescriptionTwo = view.findViewById(R.id.description_two);
            this.mTitle = view.findViewById(R.id.title);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        PackageInfoAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}