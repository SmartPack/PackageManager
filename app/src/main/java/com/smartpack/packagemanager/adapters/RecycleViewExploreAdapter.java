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

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */

public class RecycleViewExploreAdapter extends RecyclerView.Adapter<RecycleViewExploreAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private static List<String> data;

    public RecycleViewExploreAdapter(List<String> data) {
        RecycleViewExploreAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewExploreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_packageexplorer, parent, false);
        return new RecycleViewExploreAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewExploreAdapter.ViewHolder holder, int position) {
        if (new File(data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(holder.mTitle.getContext().getResources().getDrawable(R.drawable.ic_folder));
            if (Utils.isDarkTheme(holder.mIcon.getContext())) {
                holder.mIcon.setBackground(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_background_circle));
            }
            holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mTitle.getContext()));
        } else if (PackageExplorer.isImageFile(data.get(position))) {
            if (PackageExplorer.getIconFromPath(data.get(position)) != null) {
                holder.mIcon.setImageURI(PackageExplorer.getIconFromPath(data.get(position)));
            } else {
                holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file);
            }
            holder.mIcon.setBackground(null);
        } else {
            holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file));
            holder.mIcon.setColorFilter(Utils.isDarkTheme(holder.mIcon.getContext()) ? holder.mIcon.getContext()
                    .getResources().getColor(R.color.colorWhite) : holder.mIcon.getContext().getResources().getColor(R.color.colorBlack));
            holder.mIcon.setBackground(null);
        }
        holder.mTitle.setText(new File(data.get(position)).getName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialTextView mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public static void setOnItemClickListener(ClickListener clickListener) {
        RecycleViewExploreAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}