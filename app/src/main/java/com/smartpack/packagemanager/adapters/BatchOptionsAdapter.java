/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.SerializableItems.BatchOptionsItems;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 13, 2025
 */
public class BatchOptionsAdapter extends RecyclerView.Adapter<BatchOptionsAdapter.ViewHolder> {

    private final List<BatchOptionsItems> data;

    public BatchOptionsAdapter(List<BatchOptionsItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public BatchOptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_batch_options, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull BatchOptionsAdapter.ViewHolder holder, int position) {
        holder.mAppIcon.setImageDrawable(data.get(position).getIcon());
        holder.mAppID.setText(data.get(position).getPackageName());
        holder.mAppName.setText(data.get(position).getName());
        holder.mCheckBox.setChecked(data.get(position).isChecked());

        if (data.get(position).getStatus() != Integer.MIN_VALUE) {
            holder.mInfoButton.setVisibility(VISIBLE);
            holder.mInfoButton.setImageDrawable(sCommonUtils.getDrawable(data.get(position).getStatus() == 0 ? R.drawable.ic_info
                    : R.drawable.ic_cancel, holder.mInfoButton.getContext()));
            holder.mCheckBox.setVisibility(GONE);
        } else {
            holder.mInfoButton.setVisibility(GONE);
            holder.mCheckBox.setVisibility(VISIBLE);
        }
        holder.mInfoButton.setOnClickListener(v -> sCommonUtils.toast(holder.mInfoButton.getContext().getString(R.string.status,
                data.get(position).getStatus() == 0 ? holder.mInfoButton.getContext().getString(R.string.ignored) : holder.mInfoButton.getContext()
                        .getString(R.string.failed)), holder.mInfoButton.getContext()).show());
        holder.mCheckBox.setOnClickListener(v -> data.get(position).setChecked(holder.mCheckBox.isChecked()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageButton mAppIcon, mInfoButton;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppID, mAppName;

        public ViewHolder(View view) {
            super(view);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mInfoButton = view.findViewById(R.id.status);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);

            view.setOnClickListener(v -> {
                BatchOptionsItems items = data.get(getBindingAdapterPosition());
                if (items.getStatus() == Integer.MIN_VALUE) {
                    items.setChecked(!items.isChecked());
                    notifyItemChanged(getBindingAdapterPosition());
                }
            });
        }
    }

}