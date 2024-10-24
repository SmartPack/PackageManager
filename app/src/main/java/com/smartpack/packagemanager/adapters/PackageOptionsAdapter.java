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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageOptionsItems;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 31, 2023
 */
public class PackageOptionsAdapter extends RecyclerView.Adapter<PackageOptionsAdapter.ViewHolder> {

    private static ClickListener mClickListener;
    private static List<PackageOptionsItems> data;

    public PackageOptionsAdapter(List<PackageOptionsItems> data) {
        PackageOptionsAdapter.data = data;
    }

    @NonNull
    @Override
    public PackageOptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_packageoptions, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageOptionsAdapter.ViewHolder holder, int position) {
        holder.mText.setText(data.get(position).getText());
        holder.mIcon.setIcon(data.get(position).getIcon());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MaterialButton mIcon;
        private final MaterialTextView mText;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mText = view.findViewById(R.id.text);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        PackageOptionsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}