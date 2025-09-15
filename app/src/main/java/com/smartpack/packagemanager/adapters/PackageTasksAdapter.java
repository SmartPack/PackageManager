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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.ImageViewActivity;
import com.smartpack.packagemanager.activities.PackageDetailsActivity;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.SerializableItems.PackageItems;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class PackageTasksAdapter extends RecyclerView.Adapter<PackageTasksAdapter.ViewHolder> {

    private final Activity activity;
    private final ActivityResultLauncher<Intent> uninstallApps;
    private final List<PackageItems> data;
    private final List<String> batchList;
    private final String searchText;
    private static boolean batch = false;

    public PackageTasksAdapter(List<PackageItems> data, String searchText, List<String> batchList, ActivityResultLauncher<Intent> uninstallApps, Activity activity) {
        this.data = data;
        this.searchText = searchText;
        this.uninstallApps = uninstallApps;
        this.activity = activity;
        this.batchList = batchList;
        batch = !batchList.isEmpty();
    }

    @NonNull
    @Override
    public PackageTasksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
    @Override
    public void onBindViewHolder(@NonNull PackageTasksAdapter.ViewHolder holder, int position) {
        try {
            if (!sPackageUtils.isPackageInstalled(data.get(position).getPackageName(), holder.appID.getContext())) {
                return;
            }
            this.data.get(position).loadAppIcon(holder.appIcon);
            if (searchText != null && PackageData.isTextMatched(data.get(position).getPackageName(), searchText)) {
                holder.appID.setTypeface(null, Typeface.BOLD);
                holder.appID.setText(Utils.fromHtml(data.get(position).getPackageName().replace(searchText, "<b><i><font color=\"" +
                        Color.RED + "\">" + searchText + "</font></i></b>")));
            } else {
                holder.appID.setText(data.get(position).getPackageName());
            }
            if (searchText != null && PackageData.isTextMatched(data.get(position).getAppName(), searchText)) {
                holder.appName.setTypeface(null, Typeface.BOLD);
            }

            holder.checkBox.setVisibility(batch ? VISIBLE : GONE);
            if (data.get(position).launchIntent(holder.open.getContext()) != null && !batch) {
                holder.open.setVisibility(View.VISIBLE);
            } else {
                holder.open.setVisibility(View.GONE);
            }
            holder.appName.setText(data.get(position).getAppName());
            holder.appIcon.setOnClickListener(v -> {
                if (!sPackageUtils.isPackageInstalled(data.get(position).getPackageName(), v.getContext())) {
                    sCommonUtils.toast(v.getContext().getString(R.string.package_removed), v.getContext()).show();
                    return;
                }
                Intent imageView = new Intent(holder.appIcon.getContext(), ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.APP_NAME_INTENT, data.get(position).getAppName());
                imageView.putExtra(ImageViewActivity.PACKAGE_INTENT, data.get(position).getPackageName());
                holder.appIcon.getContext().startActivity(imageView);
            });
            holder.checkBox.setOnClickListener(v -> {
                if (batchList.contains(data.get(position).getPackageName())) {
                    batchList.remove(data.get(position).getPackageName());
                } else {
                    batchList.add(data.get(position).getPackageName());
                }
            });
            holder.open.setOnClickListener(v -> {
                if (holder.appID.getText().toString().trim().equals(BuildConfig.APPLICATION_ID)) {
                    sCommonUtils.toast(v.getContext().getString(R.string.open_message), v.getContext()).show();
                } else {
                    Intent launchIntent = v.getContext().getPackageManager().getLaunchIntentForPackage(holder.appID.getText().toString().trim());
                    if (launchIntent != null) {
                        v.getContext().startActivity(launchIntent);
                    } else {
                        sCommonUtils.toast(v.getContext().getString(R.string.open_failed, data.get(position).getAppName()), v.getContext()).show();
                    }
                }
            });
            holder.checkBox.setChecked(batchList.contains(data.get(position).getPackageName()));
            holder.checkBox.setOnClickListener(v -> {
                if (!sPackageUtils.isPackageInstalled(data.get(position).getPackageName(), v.getContext())) {
                    sCommonUtils.toast(v.getContext().getString(R.string.package_removed), v.getContext()).show();
                    holder.checkBox.setChecked(false);
                    return;
                }
                if (batchList.contains(data.get(position).getPackageName())) {
                    batchList.remove(data.get(position).getPackageName());
                    sCommonUtils.toast(v.getContext().getString(R.string.batch_list_removed, data.get(position).getAppName()), v.getContext()).show();
                } else {
                    batchList.add(data.get(position).getPackageName());
                    sCommonUtils.toast(v.getContext().getString(R.string.batch_list_added, data.get(position).getAppName()), v.getContext()).show();
                }
                notifyItemChanged(position);
            });

            MaterialButton batchButton = activity.findViewById(R.id.batch);
            batchButton.setVisibility(!batchList.isEmpty() ? View.VISIBLE : View.GONE);
            batchButton.setText(activity.getString(R.string.batch_options, batchList.size()));
        } catch (IndexOutOfBoundsException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
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

            view.setOnLongClickListener(v -> {
                if (batch) {
                    batchList.clear();
                    batch = false;
                } else {
                    batch = true;
                    batchList.add(data.get(getBindingAdapterPosition()).getPackageName());
                }
                MaterialButton batchButton = activity.findViewById(R.id.batch);
                batchButton.setVisibility(!batchList.isEmpty() ? View.VISIBLE : View.GONE);
                batchButton.setText(activity.getString(R.string.batch_options, batchList.size()));
                notifyItemRangeChanged(0, getItemCount());
                return true;
            });
        }

        @Override
        public void onClick(View view) {
            PackageItems packageItems = data.get(getBindingAdapterPosition());
            if (!sPackageUtils.isPackageInstalled(packageItems.getPackageName(), view.getContext())) {
                sCommonUtils.toast(view.getContext().getString(R.string.package_removed), view.getContext()).show();
                return;
            }
            if (batch) {
                if (batchList.contains(packageItems.getPackageName())) {
                    batchList.remove(packageItems.getPackageName());
                } else {
                    batchList.add(packageItems.getPackageName());
                }
                notifyItemRangeChanged(0, getItemCount());
            } else {
                Intent details = new Intent(view.getContext(), PackageDetailsActivity.class);
                details.putExtra(PackageDetailsActivity.DATA_INTENT, packageItems);
                details.putExtra(PackageDetailsActivity.APK_PICKED, false);
                uninstallApps.launch(details);
            }
        }
    }

}