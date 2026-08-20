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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.BottomMenuDialog;
import com.smartpack.packagemanager.dialogs.BundleInstallDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.APKFile;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.Serializables.APKPickerItems;
import com.smartpack.packagemanager.utils.Serializables.MenuItems;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ApksAdapter extends RecyclerView.Adapter<ApksAdapter.ViewHolder> {

    private final Activity activity;
    private final List<String> data, batchList;
    private final MaterialButton batchButton;
    private final OnInstallRequest mCallback;

    public ApksAdapter(List<String> data, List<String> batchList, MaterialButton batchButton, OnInstallRequest callback, Activity activity) {
        this.data = data;
        this.batchList = batchList;
        this.batchButton = batchButton;
        this.mCallback = callback;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ApksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ApksAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ApksAdapter.ViewHolder holder, int position) {
        String item = this.data.get(position);
        if (item.endsWith(".apk")) {
            new APKFile(item).load(holder.mIcon, holder.mTitle, holder.mDescription, holder.mSize);
        } else {
            holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_bundle, holder.mIcon.getContext()));
            holder.mTitle.setText(new File(item).getName());
            holder.mSize.setText(sAPKUtils.getAPKSize(new File(item).length()));
            holder.mDescription.setVisibility(GONE);
        }

        holder.mTitle.setTextColor(sThemeUtils.isDarkTheme(holder.mTitle.getContext()) ? Color.WHITE : Color.BLACK);
        holder.mAction.setIconResource(R.drawable.ic_menu);
        holder.mAction.setVisibility(VISIBLE);
        holder.mSize.setVisibility(VISIBLE);

        if (batchList.contains(item)) {
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
                batchList.add(this.data.get(currentPos));
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

        holder.mAction.setOnClickListener(v -> showActionsMenu(holder.getBindingAdapterPosition(), holder.mIcon.getDrawable(), holder.mTitle.getText().toString().trim(), holder.mDescription.getText().toString().trim(), v.getContext()));

        AppSettings.setSlideInAnimation(holder.mIcon, position);
    }

    public void updateData(List<String> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return data.size();
            }

            @Override
            public int getNewListSize() {
                return newData != null ? newData.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(data.get(oldItemPosition), newData.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                String oldItem = data.get(oldItemPosition);
                String newItem = newData.get(newItemPosition);

                return Objects.equals(oldItem, newItem);
            }
        });

        this.data.clear();
        if (newData != null) {
            this.data.addAll(newData);
        }

        diffResult.dispatchUpdatesTo(this);
    }

    private void showActionsMenu(int currentPos, Drawable icon, String title, String description, Context context) {
        if (currentPos == RecyclerView.NO_POSITION) return;
        String currentItem = this.data.get(currentPos);

        List<MenuItems> menuItems = new ArrayList<>();
        menuItems.add(new MenuItems(context.getString(R.string.share), R.drawable.ic_share, 0));
        menuItems.add(new MenuItems(context.getString(R.string.delete), R.drawable.ic_delete, 1));

        new BottomMenuDialog(menuItems, icon, null, title, description, context) {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onMenuItemClicked(int menuID) {
                switch (menuID) {
                    case 0:
                        Uri uriFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",
                                new File(currentItem));
                        Intent shareScript = new Intent(Intent.ACTION_SEND);
                        shareScript.setType(currentItem.endsWith(".apkm") ? "application/zip" : "application/java-archive");
                        shareScript.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_by, new File(currentItem).getName()));
                        shareScript.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                        shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                        shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(Intent.createChooser(shareScript, context.getString(R.string.share_with)));
                        break;
                    case 1:
                        sFileUtils.delete(new File(currentItem));
                        batchList.remove(currentItem);
                        data.remove(currentPos);
                        notifyItemRemoved(currentPos);
                        notifyItemRangeChanged(currentPos, getItemCount());
                        toggleBatchMenu();
                        break;
                }
            }
        };
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
            this.mAction = view.findViewById(R.id.open);
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