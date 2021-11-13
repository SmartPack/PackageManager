/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 01, 2021
 */
public class RecycleViewManifestAdapter extends RecyclerView.Adapter<RecycleViewManifestAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewManifestAdapter(List<String> data) {
        RecycleViewManifestAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewManifestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_textview, parent, false);
        return new RecycleViewManifestAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewManifestAdapter.ViewHolder holder, int position) {
        holder.mNumber.setText(String.valueOf(position + 1));
        holder.mText.setText(data.get(position));
        if (data.get(position).contains("<manifest") || data.get(position).contains("</manifest>")) {
            holder.mText.setTextColor(Utils.getThemeAccentColor(holder.mText.getContext()));
        } else if (data.get(position).contains("<uses-permission")) {
            holder.mText.setTextColor(Color.RED);
        } else if (data.get(position).contains("<activity")) {
            holder.mText.setTextColor(sUtils.isDarkTheme(holder.mText.getContext()) ? Color.GREEN : Color.MAGENTA);
        } else if (data.get(position).contains("<service")) {
            holder.mText.setTextColor(sUtils.isDarkTheme(holder.mText.getContext()) ? Color.MAGENTA : Color.BLUE);
        } else if (data.get(position).contains("<provider") || data.get(position).contains("</provider>")) {
            holder.mText.setTextColor(sUtils.isDarkTheme(holder.mText.getContext()) ? Color.LTGRAY : Color.DKGRAY);
        } else {
            holder.mText.setTextColor(sUtils.isDarkTheme(holder.mText.getContext()) ? Color.WHITE : Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mNumber, mText;

        public ViewHolder(View view) {
            super(view);
            this.mNumber = view.findViewById(R.id.number);
            this.mText = view.findViewById(R.id.text);
        }
    }

}