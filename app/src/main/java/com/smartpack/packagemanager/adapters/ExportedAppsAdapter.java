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
import android.app.Activity;
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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.BundleInstallDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.APKFile;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.SerializableItems.APKPickerItems;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsAdapter extends RecyclerView.Adapter<ExportedAppsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<String> data, batchList;
    private final MaterialButton batchButton;
    private final OnInstallRequest mCallback;

    public ExportedAppsAdapter(List<String> data, List<String> batchList, MaterialButton batchButton, OnInstallRequest callback, Activity activity) {
        this.data = data;
        this.batchList = batchList;
        this.batchButton = batchButton;
        this.mCallback = callback;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ExportedAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_exported_apps, parent, false);
        return new ExportedAppsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ExportedAppsAdapter.ViewHolder holder, int position) {
        if (data.get(position).endsWith(".apk")) {
            new APKFile(data.get(position)).load(holder.mIcon, holder.mTitle, holder.mDescription, holder.mSize);
        } else {
            holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_bundle, holder.mIcon.getContext()));
            holder.mTitle.setText(new File(data.get(position)).getName());
            holder.mSize.setText(sAPKUtils.getAPKSize(new File(data.get(position)).length()));
            holder.mDescription.setVisibility(GONE);
        }

        holder.mTitle.setTextColor(sThemeUtils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        holder.mSize.setVisibility(VISIBLE);
        holder.mAction.setIcon(sCommonUtils.getDrawable(R.drawable.ic_menu, holder.mAction.getContext()));

        if (batchList.contains(this.data.get(position))) {
            holder.mCheckBox.setVisibility(VISIBLE);
            holder.mIcon.setVisibility(GONE);
            holder.mCheckBox.setChecked(true);
        } else {
            holder.mCheckBox.setVisibility(GONE);
            holder.mIcon.setVisibility(VISIBLE);
            holder.mCheckBox.setChecked(false);
        }

        toggleBatchMenu();

        holder.mIcon.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                batchList.add(this.data.get(position));
                notifyItemChanged(currentPos);
                toggleBatchMenu();
            }
        });

        holder.mCheckBox.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                batchList.remove(this.data.get(currentPos));
                notifyItemChanged(currentPos);
                toggleBatchMenu();
            }
        });

        holder.mAction.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, v.getContext().getString(R.string.share)).setIcon(R.drawable.ic_share);
            menu.add(Menu.NONE, 1, Menu.NONE, v.getContext().getString(R.string.delete)).setIcon(R.drawable.ic_delete);
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        Uri uriFile = FileProvider.getUriForFile(v.getContext(), BuildConfig.APPLICATION_ID + ".provider",
                                new File(data.get(currentPos)));
                        Intent shareScript = new Intent(Intent.ACTION_SEND);
                        shareScript.setType(data.get(currentPos).endsWith(".apkm") ? "application/zip" : "application/java-archive");
                        shareScript.putExtra(Intent.EXTRA_SUBJECT, v.getContext().getString(R.string.shared_by, new File(data.get(currentPos)).getName()));
                        shareScript.putExtra(Intent.EXTRA_TEXT, v.getContext().getString(R.string.share_message, BuildConfig.VERSION_NAME));
                        shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                        shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        v.getContext().startActivity(Intent.createChooser(shareScript, v.getContext().getString(R.string.share_with)));
                        break;
                    case 1:
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(v.getContext().getString(R.string.delete_question, new File(data.get(currentPos)).getName()))
                                .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(v.getContext().getString(R.string.delete), (dialog, id) -> v.post(() -> {
                                    sFileUtils.delete(new File(data.get(currentPos)));
                                    batchList.remove(this.data.get(currentPos));
                                    this.data.remove(currentPos);
                                    notifyItemRemoved(currentPos);
                                    notifyItemRangeChanged(0, this.data.size());
                                    toggleBatchMenu();
                                })).show();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        AppSettings.setSlideInAnimation(holder.mIcon, position);
    }

    private void toggleBatchMenu() {
        if (batchList.isEmpty()) {
            batchButton.setVisibility(GONE);
        } else {
            batchButton.setVisibility(VISIBLE);
        }
    }

    public interface OnInstallRequest {
        void onInstall(Intent intent);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialButton mAction;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mTitle, mDescription, mSize;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAction = view.findViewById(R.id.export);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            String path = data.get(currentPos);
            if (batchList.contains(path)) {
                view.post(() -> {
                    batchList.remove(path);
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                });
                return;
            }

            if (path.endsWith(".apkm")) {
                new sExecutor() {
                    private final List<APKPickerItems> mAPKs = new ArrayList<>();
                    private ProgressDialog mProgressDialog;

                    @Override
                    public void onPreExecute() {
                        mProgressDialog = new ProgressDialog(activity);
                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                        mProgressDialog.setTitle(R.string.initializing);
                        mProgressDialog.show();
                    }

                    @Override
                    public void doInBackground() {
                        for (File files : SplitAPKInstaller.getFilesList(activity.getCacheDir())) {
                            sFileUtils.delete(files);
                        }

                        try (ZipFile zipFile = new ZipFile(path)) {
                            mProgressDialog.setMax(zipFile.getFileHeaders().size());
                            for (FileHeader fileHeaders : zipFile.getFileHeaders()) {
                                if (fileHeaders.getFileName().endsWith(".apk")) {
                                    File apkFile = new File(activity.getCacheDir(), fileHeaders.getFileName());
                                    zipFile.extractFile(fileHeaders, activity.getCacheDir().getAbsolutePath());
                                    mAPKs.add(new APKPickerItems(apkFile, FilePicker.isSelectedAPK(apkFile, activity)));

                                    mProgressDialog.updateProgress(1);
                                }
                            }
                        } catch (IOException ignored) {}
                        mAPKs.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAPKName(), rhs.getAPKName()));
                    }

                    @Override
                    public void onPostExecute() {
                        mProgressDialog.dismiss();
                        new BundleInstallDialog(mAPKs, false, mCallback::onInstall, activity);
                    }
                }.execute();
            } else {
                new MaterialAlertDialogBuilder(view.getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(view.getContext().getString(R.string.install_question, new File(path).getName()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.install, (dialog, id) -> new SplitAPKsInstallationTasks(path, mCallback::onInstall, activity).execute()
                        ).show();
            }
        }
    }

}