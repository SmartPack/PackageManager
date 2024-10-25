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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.tasks.SaveToDownloadsTasks;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsAdapter extends RecyclerView.Adapter<ExportedAppsAdapter.ViewHolder> {

    private static List<String> data;

    private static ClickListener mClickListener;

    public ExportedAppsAdapter(List<String> data) {
        ExportedAppsAdapter.data = data;
    }

    @NonNull
    @Override
    public ExportedAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ExportedAppsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ExportedAppsAdapter.ViewHolder holder, int position) {
        if (data.get(position).endsWith(".apk")) {
            if (sPackageUtils.isPackageInstalled(new File(data.get(position)).getName().replace(".apk", ""), holder.mIcon.getContext())) {
                holder.mIcon.setImageDrawable(sPackageUtils.getAppIcon(new File(data.get(position)).getName().replace(".apk", ""), holder.mIcon.getContext()));
            } else {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mIcon.getContext()));
            }
            holder.mTitle.setText(new File(data.get(position)).getName().replace(".apk", ""));
        } else {
            if (sPackageUtils.isPackageInstalled(new File(data.get(position)).getName().replace(".apkm", ""), holder.mIcon.getContext())) {
                holder.mIcon.setImageDrawable(sPackageUtils.getAppIcon(new File(data.get(position)).getName().replace(".apkm", ""), holder.mIcon.getContext()));
            } else {
                holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_bundle, holder.mIcon.getContext()));
            }
            holder.mTitle.setText(new File(data.get(position)).getName().replace(".apkm", ""));
        }
        holder.mTitle.setTextColor(sThemeUtils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        holder.mSize.setText(sAPKUtils.getAPKSize(new File(data.get(position)).length()));
        holder.mAction.setIcon(sCommonUtils.getDrawable(R.drawable.ic_doubledots, holder.mAction.getContext()));
        holder.mAction.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, v.getContext().getString(R.string.share));
            if (PackageData.getPackageDir(v.getContext()).equals(v.getContext().getExternalFilesDir(""))) {
                menu.add(Menu.NONE, 1, Menu.NONE, R.string.save_to_downloads);
            }
            menu.add(Menu.NONE, 2, Menu.NONE, v.getContext().getString(R.string.delete));
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
                        new SaveToDownloadsTasks(new File(data.get(position)), v.getContext()).execute();
                        break;
                    case 2:
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                                .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(v.getContext().getString(R.string.delete), (dialog, id) -> {
                                    sFileUtils.delete(new File(data.get(position)));
                                    data.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, data.size());
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
        private final AppCompatImageButton mIcon;
        private final MaterialButton mAction;
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
        ExportedAppsAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}