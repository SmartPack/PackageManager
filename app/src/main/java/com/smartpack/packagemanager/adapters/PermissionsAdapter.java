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

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PermissionsItems;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class PermissionsAdapter extends RecyclerView.Adapter<PermissionsAdapter.ViewHolder> {

    private static List<PermissionsItems> mData;
    private static final RootShell mRootShell = new RootShell();
    private static final ShizukuShell mShizukuShell = new ShizukuShell();

    public PermissionsAdapter(List<PermissionsItems> data) {
        PermissionsAdapter.mData = data;
    }

    @NonNull
    @Override
    public PermissionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_appops, parent, false);
        return new PermissionsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionsAdapter.ViewHolder holder, int position) {
        holder.mTitle.setTextColor(Utils.getThemeAccentColor(holder.mTitle.getContext()));
        holder.mTitle.setText(mData.get(position).getTitle().replace("android.permission.",""));
        holder.mDescription.setText(mData.get(position).getDescription());
        holder.mGranted.setChecked(mData.get(position).isGranted());
        holder.mGranted.setOnClickListener(v -> {
            if (mRootShell.rootAccess()) {
                mRootShell.runCommand("pm " + (mData.get(position).isGranted() ? "revoke " : "grant ") +
                        Common.getApplicationID() + " " + mData.get(position).getTitle());
            } else {
                mShizukuShell.runCommand("pm " + (mData.get(position).isGranted() ? "revoke " : "grant ") +
                        Common.getApplicationID() + " " + mData.get(position).getTitle());
            }
        });
        holder.mGranted.setEnabled(!Common.isAPKPicker() && (mRootShell.rootAccess() || mShizukuShell.isReady()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCheckBox mGranted;
        private final MaterialTextView mTitle, mDescription;

        public ViewHolder(View view) {
            super(view);
            this.mGranted = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
        }
    }

}