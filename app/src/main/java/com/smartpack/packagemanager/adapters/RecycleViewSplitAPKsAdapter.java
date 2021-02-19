/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
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
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */

public class RecycleViewSplitAPKsAdapter extends RecyclerView.Adapter<RecycleViewSplitAPKsAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewSplitAPKsAdapter(List<String> data) {
        RecycleViewSplitAPKsAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewSplitAPKsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new RecycleViewSplitAPKsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewSplitAPKsAdapter.ViewHolder holder, int position) {
        holder.mName.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AppCompatImageButton mAction;
        private MaterialTextView mName;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAction = view.findViewById(R.id.action);
            this.mName = view.findViewById(R.id.name);
        }

        @Override
        public void onClick(View view) {
        }
    }

}