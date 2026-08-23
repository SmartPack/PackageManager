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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.RestoreAppDialog;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.Serializables.PackageItems;
import com.smartpack.packagemanager.utils.ShizukuShell;

import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 10, 2021
 */
public class UninstalledAppsAdapter extends RecyclerView.Adapter<UninstalledAppsAdapter.ViewHolder> {

    private final List<PackageItems> data;
    private final List<String> restoreList;
    private final MaterialButton batchButton;
    private static final RootShell mRootShell = new RootShell();
    private static final ShizukuShell mShizukuShell = new ShizukuShell();

    public UninstalledAppsAdapter(List<PackageItems> data, List<String> restoreList, MaterialButton batchButton) {
        this.data = data;
        this.restoreList = restoreList;
        this.batchButton = batchButton;
    }

    @NonNull
    @Override
    public UninstalledAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new UninstalledAppsAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull UninstalledAppsAdapter.ViewHolder holder, int position) {
        PackageItems packageItems = this.data.get(position);
        packageItems.loadAppIcon(holder.mAppIcon);
        holder.mAppName.setText(packageItems.getAppName());
        holder.mAppID.setText(packageItems.getPackageName());
        holder.mRestore.setIconResource(R.drawable.ic_restore);
        holder.mRestore.setVisibility(VISIBLE);

        if (restoreList.contains(packageItems.getPackageName())) {
            holder.mCheckBox.setVisibility(VISIBLE);
            holder.mAppIcon.setVisibility(GONE);
            holder.mCheckBox.setChecked(true);
        } else {
            holder.mCheckBox.setVisibility(GONE);
            holder.mAppIcon.setVisibility(VISIBLE);
            holder.mCheckBox.setChecked(false);
        }

        toggleBatchMenu();

        holder.mAppIcon.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                restoreList.add(this.data.get(currentPos).getPackageName());
                notifyItemChanged(currentPos);
                toggleBatchMenu();
            }
        });

        holder.mCheckBox.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                restoreList.remove(this.data.get(currentPos).getPackageName());
                notifyItemChanged(currentPos);
                toggleBatchMenu();
            }
        });

        AppSettings.setSlideInAnimation(holder.mAppIcon, position);
    }

    public void updateData(@NonNull List<PackageItems> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return data.size();
            }

            @Override
            public int getNewListSize() {
                return newData.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                PackageItems oldItem = data.get(oldItemPosition);
                PackageItems newItem = newData.get(newItemPosition);
                return Objects.equals(oldItem.getPackageName(), newItem.getPackageName());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                PackageItems oldItem = data.get(oldItemPosition);
                PackageItems newItem = newData.get(newItemPosition);

                return Objects.equals(oldItem.getPackageName(), newItem.getPackageName())
                        && Objects.equals(oldItem.isRemoved(), newItem.isRemoved());
            }
        });

        this.data.clear();
        this.data.addAll(newData);

        diffResult.dispatchUpdatesTo(this);
    }

    private void toggleBatchMenu() {
        if (restoreList.isEmpty()) {
            batchButton.setVisibility(GONE);
        } else {
            batchButton.setVisibility(VISIBLE);
        }
    }

    private sExecutor restore(int position, Context context) {
        return new sExecutor() {
            private String mOutput = null;

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                PackageItems packageItems = data.get(position);
                if (mRootShell.rootAccess()) {
                    mOutput = mRootShell.runAndGetError("cmd package install-existing " + packageItems.getPackageName());
                } else {
                    mOutput = mShizukuShell.runAndGetOutput("cmd package install-existing " + packageItems.getPackageName());
                }

                PackageData.getRemovedPackagesData().remove(packageItems);
                PackageData.getRawData().add(new PackageItems(packageItems.getPackageName(), packageItems.getAppName(), packageItems.getSourceDir(), false, context));
                restoreList.remove(packageItems.getPackageName());
            }

            @Override
            public void onPostExecute() {
                new MaterialAlertDialogBuilder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(mOutput.endsWith("installed for user: 0") ? context.getString(R.string.restore_success_message) : mOutput)
                        .setPositiveButton(R.string.cancel, (dialog, id) -> {
                        }).show();
                if (mOutput.contains("installed for user: 0")) {
                    data.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    toggleBatchMenu();
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final MaterialButton mRestore;
        private final AppCompatImageButton mAppIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppName;
        private final MaterialTextView mAppID;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mRestore = view.findViewById(R.id.open);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);
            this.mCheckBox = view.findViewById(R.id.checkbox);

            this.mRestore.setClickable(false);
            this.mRestore.setFocusable(false);
        }

        @Override
        public void onClick(View view) {
            if (!mRootShell.rootAccess() && !mShizukuShell.isReady()) {
                return;
            }
            int currentPos = getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (restoreList.contains(data.get(currentPos).getPackageName())) {
                view.post(() -> {
                    restoreList.remove(data.get(currentPos).getPackageName());
                    notifyItemChanged(currentPos);
                });
                return;
            }

            if (!mRootShell.rootAccess() && !mShizukuShell.isReady()) {
                sCommonUtils.toast(view.getContext().getString(R.string.feature_unavailable_message), view.getContext()).show();
                return;
            }

            new RestoreAppDialog(this.mAppIcon.getDrawable(), this.mAppName.getText().toString().trim(), view.getContext()) {
                @Override
                public void onRestoring() {
                    restore(currentPos, view.getContext()).execute();
                }
            };
        }
    }

}