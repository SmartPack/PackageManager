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
import com.smartpack.packagemanager.utils.SettingsItems;

import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 10, 2020
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private static ArrayList<SettingsItems> data;

    private static ClickListener mClickListener;

    public SettingsAdapter(ArrayList<SettingsItems> data) {
        SettingsAdapter.data = data;
    }

    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_settings, parent, false);
        return new SettingsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        if (data.get(position).getTitle() != null) {
            holder.mTitle.setText(data.get(position).getTitle());
        }
        if (data.get(position).isSectionHeading()) {
            holder.mDivider.setVisibility(View.VISIBLE);
        } else {
            holder.mDivider.setVisibility(View.GONE);
        }
        holder.mTitle.setTextSize(data.get(position).getSize());
        if (data.get(position).getDescription() != null) {
            holder.mDescription.setText(data.get(position).getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
            holder.mDescription.setTextColor(sThemeUtils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }
        if (data.get(position).getIcon() != null) {
            holder.mIcon.setImageDrawable(data.get(position).getIcon());
            holder.mIcon.setVisibility(View.VISIBLE);
            if (!sThemeUtils.isDarkTheme(holder.mIcon.getContext())) {
                holder.mIcon.setColorFilter(Color.BLACK);
            }
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageView mIcon;
        private final MaterialTextView mDescription, mTitle;
        private final View mDivider;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mDivider = view.findViewById(R.id.divider);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        SettingsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}