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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class SplitAPKsAdapter extends RecyclerView.Adapter<SplitAPKsAdapter.ViewHolder> {

    private static List<String> data;
    private final Activity activity;

    public SplitAPKsAdapter(List<String> data, Activity activity) {
        SplitAPKsAdapter.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SplitAPKsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new SplitAPKsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull SplitAPKsAdapter.ViewHolder holder, int position) {
        holder.mName.setText(data.get(position));
        holder.mSize.setText(sAPKUtils.getAPKSize(new File(sPackageUtils.getParentDir(Common.getApplicationID(), holder.mIcon
                .getContext()) + "/" + data.get(position)).length()));
        if (sAPKUtils.getAPKIcon(sPackageUtils.getParentDir(Common.getApplicationID(), holder.mIcon
                .getContext()) + "/" + data.get(position), holder.mIcon.getContext()) != null) {
            holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(sPackageUtils.getParentDir(Common.getApplicationID(), holder.mIcon
                    .getContext()) + "/" + data.get(position), holder.mIcon.getContext()));
        }
        holder.mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(v.getContext().getString(R.string.export_storage_message, new File(data.get(position)).getName()))
                .setNegativeButton(v.getContext().getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(v.getContext().getString(R.string.export), (dialogInterface, i) ->
                        PackageExplorer.copyToStorage(sPackageUtils.getParentDir(Common.getApplicationID(), holder.mIcon
                                .getContext()) + "/" + data.get(position), activity)
                ).show()
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIcon;
        private final MaterialButton mExport;
        private final MaterialTextView mName, mSize;

        public ViewHolder(View view) {
            super(view);
            this.mExport = view.findViewById(R.id.export);
            this.mIcon = view.findViewById(R.id.icon);
            this.mName = view.findViewById(R.id.name);
            this.mSize = view.findViewById(R.id.size);
        }
    }

}