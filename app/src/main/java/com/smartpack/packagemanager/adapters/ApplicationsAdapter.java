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
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.ImageViewActivity;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Serializables.PackageItems;

import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

    private final List<PackageItems> data;
    private final List<String> batchList;
    private final MaterialButton batchButton;
    private final OnItemClickListener clickListener;

    public ApplicationsAdapter(@NonNull List<PackageItems> data, @NonNull List<String> batchList, @NonNull MaterialButton batchButton, @NonNull OnItemClickListener clickListener) {
        this.data = data;
        this.batchList = batchList;
        this.batchButton = batchButton;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ApplicationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
    @Override
    public void onBindViewHolder(@NonNull ApplicationsAdapter.ViewHolder holder, int position) {
        try {
            PackageItems packageItems = this.data.get(position);

            if (!sPackageUtils.isPackageInstalled(packageItems.getPackageName(), holder.appID.getContext())) {
                return;
            }
            packageItems.loadAppIcon(holder.appIcon);

            if (batchList.contains(packageItems.getPackageName())) {
                holder.checkBox.setVisibility(VISIBLE);
                holder.appIcon.setVisibility(GONE);
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setVisibility(GONE);
                holder.appIcon.setVisibility(VISIBLE);
                holder.checkBox.setChecked(false);
            }

            toggleBatchMenu();

            holder.appID.setText(packageItems.getPackageName());
            holder.appName.setText(packageItems.getAppName());

            if (packageItems.launchIntent() != null) {
                holder.open.setVisibility(View.VISIBLE);
                holder.open.setIconResource(R.drawable.ic_open);
            } else {
                holder.open.setVisibility(View.GONE);
            }

            holder.appName.setText(packageItems.getAppName());

            holder.appIcon.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    sCommonUtils.toast(v.getContext().getString(R.string.batch_list_added, data.get(currentPos).getAppName()), v.getContext()).show();
                    batchList.add(this.data.get(currentPos).getPackageName());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.checkBox.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    if (!sPackageUtils.isPackageInstalled(data.get(currentPos).getPackageName(), v.getContext())) {
                        sCommonUtils.toast(v.getContext().getString(R.string.package_removed), v.getContext()).show();
                        return;
                    }
                    sCommonUtils.toast(v.getContext().getString(R.string.batch_list_removed, data.get(position).getAppName()), v.getContext()).show();

                    batchList.remove(this.data.get(currentPos).getPackageName());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.appIcon.setOnLongClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    if (!sPackageUtils.isPackageInstalled(data.get(currentPos).getPackageName(), v.getContext())) {
                        sCommonUtils.toast(v.getContext().getString(R.string.package_removed), v.getContext()).show();
                        return true;
                    }
                    Intent imageView = new Intent(holder.appIcon.getContext(), ImageViewActivity.class);
                    imageView.putExtra(ImageViewActivity.APP_NAME_INTENT, data.get(currentPos).getAppName());
                    imageView.putExtra(ImageViewActivity.PACKAGE_INTENT, data.get(currentPos).getPackageName());
                    holder.appIcon.getContext().startActivity(imageView);
                }
                return true;
            });

            holder.open.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    if (this.data.get(currentPos).getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        sCommonUtils.toast(v.getContext().getString(R.string.open_message), v.getContext()).show();
                    } else {
                        Intent launchIntent = v.getContext().getPackageManager().getLaunchIntentForPackage(holder.appID.getText().toString().trim());
                        if (launchIntent != null) {
                            v.getContext().startActivity(launchIntent);
                        } else {
                            sCommonUtils.toast(v.getContext().getString(R.string.open_failed, data.get(currentPos).getAppName()), v.getContext()).show();
                        }
                    }
                }
            });

            AppSettings.setSlideInAnimation(holder.appIcon, position);
        } catch (IndexOutOfBoundsException ignored) {}
    }

    @SuppressLint("StringFormatInvalid")
    private void toggleBatchMenu() {
        if (batchList.isEmpty()) {
            batchButton.setVisibility(GONE);
        } else {
            batchButton.setText(batchButton.getContext().getString(R.string.batch_options, batchList.size()));
            batchButton.setVisibility(VISIBLE);
        }
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
                        && Objects.equals(oldItem.getUpdatedTime(), newItem.getUpdatedTime());
            }
        });

        this.data.clear();
        this.data.addAll(newData);

        diffResult.dispatchUpdatesTo(this);
    }

    public void refreshData() {
        if (!this.data.isEmpty()) {
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton appIcon;
        private final MaterialButton open;
        private final MaterialCheckBox checkBox;
        private final MaterialTextView appName;
        private final MaterialTextView appID;

        @SuppressLint("StringFormatInvalid")
        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.appIcon = view.findViewById(R.id.icon);
            this.open = view.findViewById(R.id.open);
            this.appName = view.findViewById(R.id.title);
            this.appID = view.findViewById(R.id.description);
            this.checkBox = view.findViewById(R.id.checkbox);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            PackageItems packageItems = data.get(currentPos);
            if (!sPackageUtils.isPackageInstalled(packageItems.getPackageName(), view.getContext())) {
                sCommonUtils.toast(view.getContext().getString(R.string.package_removed), view.getContext()).show();
                return;
            }

            if (batchList.contains(packageItems.getPackageName())) {
                sCommonUtils.toast(view.getContext().getString(R.string.batch_list_removed, data.get(currentPos).getAppName()), view.getContext()).show();
                view.post(() -> {
                    batchList.remove(packageItems.getPackageName());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                });
                return;
            }

            clickListener.onItemClick(packageItems);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(PackageItems packageItems);
    }

}