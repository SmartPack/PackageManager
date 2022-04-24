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

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKDetailsAdapter extends RecyclerView.Adapter<APKDetailsAdapter.ViewHolder> {

    private static List<String> data;

    public APKDetailsAdapter(List<String> data) {
        APKDetailsAdapter.data = data;
    }

    @NonNull
    @Override
    public APKDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apkdetails, parent, false);
        return new APKDetailsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull APKDetailsAdapter.ViewHolder holder, int position) {
        holder.mText.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mText;

        public ViewHolder(View view) {
            super(view);
            this.mText = view.findViewById(R.id.text);
        }
    }

}