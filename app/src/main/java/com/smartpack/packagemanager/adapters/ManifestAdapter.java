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

import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 01, 2021
 */
public class ManifestAdapter extends RecyclerView.Adapter<ManifestAdapter.ViewHolder> {

    private static List<String> data;

    public ManifestAdapter(List<String> data) {
        ManifestAdapter.data = data;
    }

    @NonNull
    @Override
    public ManifestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_textview, parent, false);
        return new ManifestAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ManifestAdapter.ViewHolder holder, int position) {
        holder.mNumber.setText(String.valueOf(position + 1));
        holder.mText.setText(data.get(position));
        if (data.get(position).contains("<manifest") || data.get(position).contains("</manifest>")) {
            holder.mText.setTextColor(Utils.getThemeAccentColor(holder.mText.getContext()));
        } else if (data.get(position).trim().matches("<uses-permission|</uses-permission>")) {
            holder.mText.setTextColor(Color.RED);
        } else if (data.get(position).trim().matches("<activity|</activity>") || data.get(position).startsWith(".method") || data.get(position).startsWith(".annotation")) {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.GREEN : Color.MAGENTA);
        } else if (data.get(position).trim().matches("<service|</service>") || data.get(position).startsWith(".end method") || data.get(position).startsWith(".end annotation")) {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.MAGENTA : Color.BLUE);
        } else if (data.get(position).trim().matches("<provider|</provider>")) {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.LTGRAY : Color.DKGRAY);
        } else {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.WHITE : Color.BLACK);
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