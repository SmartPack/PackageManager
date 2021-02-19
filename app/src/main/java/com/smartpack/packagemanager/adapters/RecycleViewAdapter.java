/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.PackageDetailsActivity;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageList;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewAdapter (List<String> data) {
        RecycleViewAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
        if (!Utils.isPackageInstalled(data.get(position), holder.appID.getContext())) {
            return;
        }
        holder.appIcon.setImageDrawable(PackageData.getAppIcon(data.get(position), holder.appIcon.getContext()));
        if (!PackageList.mOEMApps && PackageData.mSearchText != null && data.get(position).toLowerCase().contains(PackageData.mSearchText)) {
            holder.appID.setText(Utils.fromHtml(data.get(position).toLowerCase().replace(PackageData.mSearchText,"<b><i><font color=\"" +
                    Color.RED + "\">" + PackageData.mSearchText + "</font></i></b>")));
        } else {
            holder.appID.setText(data.get(position));
        }
        if (!PackageList.mOEMApps && PackageData.mSearchText != null && PackageData.getAppName(data.get(position), holder.appName.getContext()).toLowerCase().contains(PackageData.mSearchText)) {
            holder.appName.setText(Utils.fromHtml(PackageData.getAppName(data.get(position), holder.appName.getContext()).toLowerCase().replace(PackageData.mSearchText,
                    "<b><i><font color=\"" + Color.RED + "\">" + PackageData.mSearchText + "</font></i></b>")));
        } else {
            holder.appName.setText(PackageData.getAppName(data.get(position), holder.appName.getContext()));
        }
        holder.checkBox.setChecked(PackageData.mBatchList.contains(data.get(position)));
        holder.checkBox.setOnClickListener(v -> {
            if (PackageData.mBatchList.contains(data.get(position))) {
                PackageData.mBatchList.remove(data.get(position));
                Utils.snackbar(holder.checkBox, holder.checkBox.getContext().getString(R.string.batch_list_removed, PackageData.getAppName(
                        data.get(position), holder.checkBox.getContext())));
            } else {
                PackageData.mBatchList.add(data.get(position));
                Utils.snackbar(holder.checkBox, holder.checkBox.getContext().getString(R.string.batch_list_added, PackageData.getAppName(
                        data.get(position), holder.checkBox.getContext())));
            }
            PackageTasks.mBatchOptions.setVisibility(PackageData.getBatchList().length() > 0 && PackageData
                    .getBatchList().contains(".") ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AppCompatImageButton appIcon;
        public MaterialCheckBox checkBox;
        private MaterialTextView appName;
        private MaterialTextView appID;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.appIcon = view.findViewById(R.id.icon);
            this.appName = view.findViewById(R.id.title);
            this.appID = view.findViewById(R.id.description);
            this.checkBox = view.findViewById(R.id.checkbox);
        }

        @Override
        public void onClick(View view) {
            if (!Utils.isPackageInstalled(this.appID.getText().toString(), view.getContext())) {
                Utils.snackbar(view, view.getContext().getString(R.string.package_removed));
                return;
            }
            PackageData.mApplicationID = this.appID.getText().toString();
            PackageData.mApplicationName = PackageData.getAppName(PackageData.mApplicationID, view.getContext());
            PackageData.mApplicationIcon = PackageData.getAppIcon(PackageData.mApplicationID, view.getContext());
            PackageData.mDirSource = PackageData.getSourceDir(PackageData.mApplicationID, view.getContext());
            PackageData.mDirData = PackageData.getDataDir(PackageData.mApplicationID, view.getContext());
            PackageData.mDirNatLib = PackageData.getNativeLibDir(PackageData.mApplicationID, view.getContext());
            PackageData.mSystemApp = PackageData.isSystemApp(PackageData.mApplicationID, view.getContext());
            Intent details = new Intent(view.getContext(), PackageDetailsActivity.class);
            view.getContext().startActivity(details);
        }
    }

}