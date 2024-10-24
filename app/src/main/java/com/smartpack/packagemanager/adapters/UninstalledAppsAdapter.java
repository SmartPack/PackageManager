/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Common;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 10, 2021
 */
public class UninstalledAppsAdapter extends RecyclerView.Adapter<UninstalledAppsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<String> data;
    private static ClickListener mClickListener;

    public UninstalledAppsAdapter(List<String> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public UninstalledAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_uninstlled_apps, parent, false);
        return new UninstalledAppsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull UninstalledAppsAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position));
        holder.mCheckBox.setChecked(Common.getRestoreList().contains(data.get(position)));
        holder.mCheckBox.setOnClickListener(v -> {
            if (Common.getRestoreList().contains(data.get(position))) {
                Common.getRestoreList().remove(data.get(position));
            } else {
                Common.getRestoreList().add(data.get(position));
            }
            Common.getCardView(activity, R.id.restore).setVisibility(!Common.getRestoreList().isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mTitle = view.findViewById(R.id.title);
            this.mCheckBox = view.findViewById(R.id.checkbox);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        UninstalledAppsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}