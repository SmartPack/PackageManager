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

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 01, 2021
 */
public class ManifestAdapter extends RecyclerView.Adapter<ManifestAdapter.ViewHolder> {

    private static List<String> data;

    public ManifestAdapter(List<String> data) {
        ManifestAdapter.data = data;
    }

    @NonNull
    @Override
    public ManifestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_textview, parent, false);
        return new ManifestAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ManifestAdapter.ViewHolder holder, int position) {
        holder.mNumber.setText(String.valueOf(position + 1));
        holder.mText.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mNumber, mText;

        public ViewHolder(View view) {
            super(view);
            this.mNumber = view.findViewById(R.id.number);
            this.mText = view.findViewById(R.id.text);
        }
    }

}