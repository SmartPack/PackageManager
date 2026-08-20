/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.BottomMenuDialog;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.Serializables.MenuItems;
import com.smartpack.packagemanager.utils.Serializables.PermissionsItems;
import com.smartpack.packagemanager.utils.ShizukuShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 * Modified by sunilpaulmathew <sunil.kde@gmail.com> on Mar 17, 2021
 */
public class AppOpsAdapter extends RecyclerView.Adapter<AppOpsAdapter.ViewHolder> {

    private final List<PermissionsItems> data;
    private final String packageName;

    public AppOpsAdapter(List<PermissionsItems> data, String packageName) {
        this.data = data;
        this.packageName = packageName;
    }

    @NonNull
    @Override
    public AppOpsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_appops, parent, false);
        return new AppOpsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull AppOpsAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(this.data.get(position).getTitle().toUpperCase(Locale.getDefault()));
        holder.mDescription.setText(sPermissionUtils.getDescription(this.data.get(position).getTitle(), holder.mDescription.getContext()));
        holder.mStatus.setText(holder.mStatus.getContext().getString(R.string.status, this.data.get(position).getDescription().toUpperCase(Locale.getDefault())));

        AppSettings.setSlideInAnimation(holder.mStatus, position);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public void updateData(@NonNull List<PermissionsItems> newData) {
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
                PermissionsItems oldItem = data.get(oldItemPosition);
                PermissionsItems newItem = newData.get(newItemPosition);
                return Objects.equals(oldItem.getTitle(), newItem.getTitle());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                PermissionsItems oldItem = data.get(oldItemPosition);
                PermissionsItems newItem = newData.get(newItemPosition);

                return oldItem == newItem;
            }
        });

        this.data.clear();
        this.data.addAll(newData);

        diffResult.dispatchUpdatesTo(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mDescription, mStatus, mTitle;

        public ViewHolder(View view) {
            super(view);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mStatus = view.findViewById(R.id.status);

            view.setOnClickListener(v -> {
                if (sCommonUtils.getBoolean("firstOpsAttempt", true, v.getContext())) {
                    new MaterialAlertDialogBuilder(Objects.requireNonNull(v.getContext()))
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(v.getContext().getString(R.string.warning))
                            .setMessage(v.getContext().getString(R.string.operations_warning))
                            .setCancelable(false)
                            .setPositiveButton(R.string.got_it, (dialog, id) -> sCommonUtils.saveBoolean("firstOpsAttempt", false, v.getContext())).show();
                } else {
                    List<MenuItems> menuItems = new ArrayList<>();
                    menuItems.add(new MenuItems(v.getContext().getString(R.string.operations_allow_title), v.getContext().getString(R.string.operations_allow_description), 0));
                    menuItems.add(new MenuItems(v.getContext().getString(R.string.operations_ignore_title), v.getContext().getString(R.string.operations_ignore_description), 1));
                    menuItems.add(new MenuItems(v.getContext().getString(R.string.operations_deny_title), v.getContext().getString(R.string.operations_deny_description), 2));
                    menuItems.add(new MenuItems(v.getContext().getString(R.string.operations_default_title), v.getContext().getString(R.string.operations_default_description), 3));
                    menuItems.add(new MenuItems(v.getContext().getString(R.string.operations_foreground_title), v.getContext().getString(R.string.operations_foreground_description), 4));

                    new BottomMenuDialog(menuItems, sCommonUtils.getDrawable(R.drawable.ic_shield, v.getContext()), data.get(getBindingAdapterPosition()).getDescription(), data.get(getBindingAdapterPosition()).getTitle().toUpperCase(Locale.getDefault()), sPermissionUtils.getDescription(data.get(getBindingAdapterPosition()).getTitle().toUpperCase(Locale.getDefault()), v.getContext()), v.getContext()) {
                        @Override
                        public void onMenuItemClicked(int menuID) {
                            new sExecutor() {
                                private boolean success = false;
                                private final RootShell mRootShell = new RootShell();
                                private final ShizukuShell mShizukuShell = new ShizukuShell();
                                private String newStatus;
                                @Override
                                public void onPreExecute() {
                                }

                                @Override
                                public void doInBackground() {
                                    String[] options = new String[] {
                                            "allow", "ignore", "deny", "default", "foreground"
                                    };

                                    String result;
                                    if (mRootShell.rootAccess()) {
                                        mRootShell.runCommand("cmd appops set " + packageName + " " +
                                                data.get(getBindingAdapterPosition()).getTitle() + " " + options[menuID]);
                                        result = mRootShell.runAndGetOutput("cmd appops get " + packageName + " " +
                                                data.get(getBindingAdapterPosition()).getTitle());
                                    } else {
                                        mShizukuShell.runCommand("cmd appops set " + packageName + " " +
                                                data.get(getBindingAdapterPosition()).getTitle() + " " + options[menuID]);
                                        result = mShizukuShell.runAndGetOutput("cmd appops get " + packageName + " " +
                                                data.get(getBindingAdapterPosition()).getTitle());
                                    }

                                    newStatus = result.trim().split(data.get(getBindingAdapterPosition()).getTitle() + ": ")[1];
                                    success = newStatus.contains(options[menuID]);
                                }

                                @SuppressLint("StringFormatInvalid")
                                @Override
                                public void onPostExecute() {
                                    if (success) {
                                        data.get(getBindingAdapterPosition()).setDescription(newStatus);
                                        notifyItemChanged(getBindingAdapterPosition());
                                    } else {
                                        sCommonUtils.toast(v.getContext().getString(R.string.operations_failed_toast, data.get(getBindingAdapterPosition()).getTitle().toUpperCase(Locale.getDefault())), v.getContext()).show();
                                    }
                                }
                            }.execute();
                        }
                    };
                }
            });
        }
    }

}