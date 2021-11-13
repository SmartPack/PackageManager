/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class RecycleViewPermissionsAdapter extends RecyclerView.Adapter<RecycleViewPermissionsAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewPermissionsAdapter(List<String> data) {
        RecycleViewPermissionsAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewPermissionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_permissions, parent, false);
        return new RecycleViewPermissionsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewPermissionsAdapter.ViewHolder holder, int position) {
        if (data.get(position).equals("Granted") || data.get(position).equals("Denied")) {
            holder.mName.setTextColor(Utils.getThemeAccentColor(holder.mName.getContext()));
            holder.mName.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            holder.mName.setTextColor(sUtils.isDarkTheme(holder.mName.getContext()) ? Color.WHITE : Color.BLACK);
        }
        holder.mName.setText(data.get(position));
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