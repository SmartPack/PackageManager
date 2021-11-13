/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class RecycleViewActivitiesAdapter extends RecyclerView.Adapter<RecycleViewActivitiesAdapter.ViewHolder> {

    private static List<ActivityInfo> data;

    public RecycleViewActivitiesAdapter(List<ActivityInfo> data) {
        RecycleViewActivitiesAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewActivitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_activities, parent, false);
        return new RecycleViewActivitiesAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewActivitiesAdapter.ViewHolder holder, int position) {
        holder.mName.setText(data.get(position).name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mName;

        public ViewHolder(View view) {
            super(view);
            this.mName = view.findViewById(R.id.name);
        }
    }

}