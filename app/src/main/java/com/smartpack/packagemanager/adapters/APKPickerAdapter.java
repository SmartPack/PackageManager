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
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.APKFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 25, 2025
 */
public class APKPickerAdapter extends RecyclerView.Adapter<APKPickerAdapter.ViewHolder> {

    private final ArrayList<String> selectedFiles;
    private final List<File> data;

    public APKPickerAdapter(List<File> data, ArrayList<String> selectedFiles) {
        this.data = data;
        this.selectedFiles = selectedFiles;
    }

    @NonNull
    @Override
    public APKPickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
        return new APKPickerAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull APKPickerAdapter.ViewHolder holder, int position) {
        new APKFile(data.get(position)).load(holder.mIcon, holder.mTitle, holder.mDescription, holder.mSize);
        holder.mSize.setVisibility(View.VISIBLE);
        holder.mCheckBox.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mTitle, mDescription, mSize;

        public ViewHolder(View view) {
            super(view);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);

            view.setOnClickListener(v -> {
                if (selectedFiles.contains(data.get(getAdapterPosition()).getAbsolutePath())) {
                    selectedFiles.remove(data.get(getAdapterPosition()).getAbsolutePath());
                } else {
                    selectedFiles.add(data.get(getAdapterPosition()).getAbsolutePath());
                }
                mCheckBox.setChecked(!mCheckBox.isChecked());
            });
        }
    }

}