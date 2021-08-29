/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.RecycleSettingsItem;
import com.smartpack.packagemanager.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */

public class RecycleViewSettingsAdapter extends RecyclerView.Adapter<RecycleViewSettingsAdapter.ViewHolder> {

    private static ArrayList<RecycleSettingsItem> data;

    private static ClickListener mClickListener;

    public RecycleViewSettingsAdapter(ArrayList<RecycleSettingsItem> data) {
        RecycleViewSettingsAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewSettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_settings, parent, false);
        return new RecycleViewSettingsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewSettingsAdapter.ViewHolder holder, int position) {
        if (data.get(position).getTitle() != null) {
            holder.mTitle.setText(data.get(position).getTitle());
            holder.mTitle.setTextColor(Utils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        }
        if (data.get(position).getDescription() != null) {
            holder.mDescription.setText(data.get(position).getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
            holder.mDescription.setTextColor(Utils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        }
        if (data.get(position).getIcon() != null) {
            holder.mIcon.setImageDrawable(data.get(position).getIcon());
            holder.mIcon.setVisibility(View.VISIBLE);
            if (position != 9 && !Utils.isDarkTheme(holder.mIcon.getContext())) {
                holder.mIcon.setColorFilter(Color.BLACK);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageView mIcon;
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
            mClickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecycleViewSettingsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}