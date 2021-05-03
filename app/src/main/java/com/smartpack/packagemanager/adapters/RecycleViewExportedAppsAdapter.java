/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Downloads;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */

public class RecycleViewExportedAppsAdapter extends RecyclerView.Adapter<RecycleViewExportedAppsAdapter.ViewHolder> {

    private static List<String> data;

    private static ClickListener mClickListener;

    public RecycleViewExportedAppsAdapter(List<String> data) {
        RecycleViewExportedAppsAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewExportedAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new RecycleViewExportedAppsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "StringFormatInvalid"})
    @Override
    public void onBindViewHolder(@NonNull RecycleViewExportedAppsAdapter.ViewHolder holder, int position) {
        if (data.get(position).endsWith(".apk")) {
            if (PackageData.getAPKName(data.get(position), holder.mIcon.getContext()) != null) {
                holder.mTitle.setText(PackageData.getAPKName(data.get(position), holder.mTitle.getContext()));
            } else {
                holder.mTitle.setText(new File(data.get(position)).getName().replace(".apk", ""));
            }
            if (PackageData.getAPKIcon(data.get(position), holder.mIcon.getContext()) != null) {
                holder.mIcon.setImageDrawable(PackageData.getAPKIcon(data.get(position), holder.mIcon.getContext()));
            } else {
                holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mIcon.getContext()));
            }
        } else {
            if (Utils.isPackageInstalled(new File(data.get(position)).getName().replace(".apkm", ""), holder.mIcon.getContext())) {
                holder.mIcon.setImageDrawable(PackageData.getAppIcon(new File(data.get(position)).getName().replace(".apkm", ""), holder.mIcon.getContext()));
                holder.mTitle.setText(Downloads.getAppName(new File(data.get(position)).getName().replace(".apkm", ""), holder.mIcon.getContext()));
            } else {
                holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_bundle));
                holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mIcon.getContext()));
                holder.mTitle.setText(new File(data.get(position)).getName().replace(".apkm", ""));
            }
        }
        holder.mTitle.setTextColor(Utils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        holder.mSize.setText(PackageData.getAPKSize(data.get(position)));
        holder.mAction.setImageDrawable(holder.mAction.getContext().getResources().getDrawable(R.drawable.ic_settings));
        holder.mAction.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, v.getContext().getString(R.string.share));
            menu.add(Menu.NONE, 1, Menu.NONE, v.getContext().getString(R.string.delete));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        Uri uriFile = FileProvider.getUriForFile(v.getContext(), BuildConfig.APPLICATION_ID + ".provider",
                                new File(data.get(position)));
                        Intent shareScript = new Intent(Intent.ACTION_SEND);
                        shareScript.setType(data.get(position).endsWith(".apkm") ? "application/zip" : "application/java-archive");
                        shareScript.putExtra(Intent.EXTRA_SUBJECT, v.getContext().getString(R.string.shared_by, new File(data.get(position)).getName()));
                        shareScript.putExtra(Intent.EXTRA_TEXT, v.getContext().getString(R.string.share_message, BuildConfig.VERSION_NAME));
                        shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                        shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        v.getContext().startActivity(Intent.createChooser(shareScript, v.getContext().getString(R.string.share_with)));
                        break;
                    case 1:
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                                .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(v.getContext().getString(R.string.delete), (dialog, id) -> {
                                    Utils.delete(data.get(position));
                                    data.remove(position);
                                    notifyDataSetChanged();
                                }).show();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final  AppCompatImageButton mAction, mIcon;
        private final MaterialTextView mTitle, mSize;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAction = view.findViewById(R.id.export);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.name);
            this.mSize = view.findViewById(R.id.size);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecycleViewExportedAppsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}