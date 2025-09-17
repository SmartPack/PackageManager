/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.SerializableItems.ActivityItems;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    private final List<ActivityItems> data;
    private final String packageName;

    public ActivitiesAdapter(List<ActivityItems> data, String packageName) {
        this.data = data;
        this.packageName = packageName;
    }

    @NonNull
    @Override
    public ActivitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_activities, parent, false);
        return new ActivitiesAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ActivitiesAdapter.ViewHolder holder, int position) {
        if (data.get(position).getLabel() != null) {
            holder.mLabel.setText(data.get(position).getLabel());
            holder.mLabel.setVisibility(VISIBLE);
        } else {
            holder.mLabel.setVisibility(GONE);
        }
        holder.mName.setText(data.get(position).getName());
        holder.mIcon.setImageDrawable(data.get(position).getIcon());
        holder.mOpen.setVisibility(data.get(position).exported() ? VISIBLE : GONE);

        holder.mOpen.setOnClickListener(v -> {
            PackageManager pm = v.getContext().getPackageManager();
            ComponentName component = new ComponentName(packageName, data.get(position).getName());

            try {
                ActivityInfo info = pm.getActivityInfo(component, PackageManager.GET_META_DATA);

                if (info.exported) {
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                }
            } catch (PackageManager.NameNotFoundException | SecurityException ignored) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIcon;
        private final MaterialButton mOpen;
        private final MaterialTextView mLabel, mName;

        public ViewHolder(View view) {
            super(view);
            this.mLabel = view.findViewById(R.id.title);
            this.mName = view.findViewById(R.id.description);
            this.mIcon = view.findViewById(R.id.icon);
            this.mOpen = view.findViewById(R.id.open);
        }
    }

}