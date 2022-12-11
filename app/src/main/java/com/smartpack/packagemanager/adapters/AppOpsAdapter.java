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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AppOps;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.RecycleViewAppOpsItem;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 * Modified by sunilpaulmathew <sunil.kde@gmail.com> on Mar 17, 2021
 */
public class AppOpsAdapter extends RecyclerView.Adapter<AppOpsAdapter.ViewHolder> {

    private static ArrayList<RecycleViewAppOpsItem> data;

    public AppOpsAdapter(ArrayList<RecycleViewAppOpsItem> data) {
        AppOpsAdapter.data = data;
    }

    @NonNull
    @Override
    public AppOpsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_appops, parent, false);
        return new AppOpsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull AppOpsAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).getTitle().toUpperCase(Locale.getDefault()));
        holder.mDescription.setText(data.get(position).getDescription());
        holder.mCheckBox.setChecked(data.get(position).isEnabled());
        holder.mCheckBox.setOnClickListener(v -> {
            if (sUtils.getBoolean("firstOpsAttempt", true, v.getContext())) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(v.getContext()))
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(v.getContext().getString(R.string.warning))
                        .setMessage(v.getContext().getString(R.string.operations_warning))
                        .setCancelable(false)
                        .setPositiveButton(R.string.got_it, (dialog, id) -> {
                            sUtils.saveBoolean("firstOpsAttempt", false, v.getContext());
                            holder.mCheckBox.setChecked(data.get(position).isEnabled());
                        }).show();
            } else {
                if (new RootShell().rootAccess()) {
                    new RootShell().runCommand(AppOps.getCommandPrefix() + " appops set " + Common.getApplicationID() + " " +
                            data.get(position).getTitle() + (data.get(position).isEnabled() ? " deny" : " allow"));
                } else {
                    new ShizukuShell().runCommand(AppOps.getCommandPrefix() + " appops set " + Common.getApplicationID() + " " +
                            data.get(position).getTitle() + (data.get(position).isEnabled() ? " deny" : " allow"));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mDescription, mTitle;

        public ViewHolder(View view) {
            super(view);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mCheckBox = view.findViewById(R.id.checkbox);
        }
    }

}