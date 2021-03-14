/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
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

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewSplitAPKsAdapter.ViewHolder holder, int position) {
        holder.mName.setText(data.get(position));
        holder.mSize.setText(PackageData.getAPKSize(PackageData.getParentDir(PackageData.mApplicationID, holder.mIcon
                .getContext()) + "/" + data.get(position)));
        if (PackageData.getAPKIcon(PackageData.getParentDir(PackageData.mApplicationID, holder.mIcon
                .getContext()) + "/" + data.get(position), holder.mIcon.getContext()) != null) {
            holder.mIcon.setImageDrawable(PackageData.getAPKIcon(PackageData.getParentDir(PackageData.mApplicationID, holder.mIcon
                    .getContext()) + "/" + data.get(position), holder.mIcon.getContext()));
        } else {
            holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mIcon.getContext()));
        }
        holder.mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(holder.mExport.getContext())
                .setMessage(holder.mExport.getContext().getString(R.string.export_storage_message, new File(data.get(position)).getName()))
                .setNegativeButton(holder.mExport.getContext().getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(holder.mExport.getContext().getString(R.string.export), (dialogInterface, i) -> {
                    PackageExplorer.copyToStorage(PackageData.getParentDir(PackageData.mApplicationID, holder.mIcon
                            .getContext()) + "/" + data.get(position), PackageData.getPackageDir() + "/" +
                            PackageData.mApplicationID, (Activity) holder.mExport.getContext());
                }).show());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageButton mExport, mIcon;
        private MaterialTextView mName, mSize;

        public ViewHolder(View view) {
            super(view);
            this.mExport = view.findViewById(R.id.export);
            this.mIcon = view.findViewById(R.id.icon);
            this.mName = view.findViewById(R.id.name);
            this.mSize = view.findViewById(R.id.size);
        }
    }

}