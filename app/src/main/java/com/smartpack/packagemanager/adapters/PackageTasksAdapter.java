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
import com.smartpack.packagemanager.utils.Common;
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
    private final List<PackageItems> data;
    private static boolean batch = false;

    public PackageTasksAdapter(List<PackageItems> data, Activity activity) {
        this.data = data;
        this.activity = activity;
        batch = !Common.getBatchList().isEmpty();
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
            if (Common.getSearchText() != null && Common.isTextMatched(data.get(position).getPackageName())) {
                holder.appID.setTypeface(null, Typeface.BOLD);
                holder.appID.setText(Utils.fromHtml(data.get(position).getPackageName().replace(Common.getSearchText(), "<b><i><font color=\"" +
                        Color.RED + "\">" + Common.getSearchText() + "</font></i></b>")));
            } else {
                holder.appID.setText(data.get(position).getPackageName());
            }
            if (Common.getSearchText() != null && Common.isTextMatched(data.get(position).getAppName())) {
                holder.appName.setTypeface(null, Typeface.BOLD);
            }

            holder.checkBox.setVisibility(batch ? VISIBLE : GONE);
            if (data.get(position).launchIntent() != null && !batch) {
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
                Common.setApplicationName(data.get(position).getAppName());
                Common.setApplicationIcon(holder.appIcon.getDrawable());
                Intent imageView = new Intent(holder.appIcon.getContext(), ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PACKAGE_INTENT, data.get(position).getPackageName());
                holder.appIcon.getContext().startActivity(imageView);
            });
            holder.checkBox.setOnClickListener(v -> {
                if (Common.getBatchList().contains(data.get(position).getPackageName())) {
                    Common.getBatchList().remove(data.get(position).getPackageName());
                } else {
                    Common.getBatchList().add(data.get(position).getPackageName());
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
                        sCommonUtils.toast(v.getContext().getString(R.string.open_failed, Common.getApplicationName()), v.getContext()).show();
                    }
                }
            });
            holder.checkBox.setChecked(Common.getBatchList().contains(data.get(position).getPackageName()));
            holder.checkBox.setOnClickListener(v -> {
                if (!sPackageUtils.isPackageInstalled(data.get(position).getPackageName(), v.getContext())) {
                    sCommonUtils.toast(v.getContext().getString(R.string.package_removed), v.getContext()).show();
                    holder.checkBox.setChecked(false);
                    return;
                }
                if (Common.getBatchList().contains(data.get(position).getPackageName())) {
                    Common.getBatchList().remove(data.get(position).getPackageName());
                    sCommonUtils.toast(v.getContext().getString(R.string.batch_list_removed, data.get(position).getAppName()), v.getContext()).show();
                } else {
                    Common.getBatchList().add(data.get(position).getPackageName());
                    sCommonUtils.toast(v.getContext().getString(R.string.batch_list_added, data.get(position).getAppName()), v.getContext()).show();
                }
                notifyItemChanged(position);
            });

            MaterialButton batchButton = activity.findViewById(R.id.batch);
            batchButton.setVisibility(!Common.getBatchList().isEmpty() ? View.VISIBLE : View.GONE);
            batchButton.setText(activity.getString(R.string.batch_options, Common.getBatchList().size()));
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
                    Common.getBatchList().clear();
                    batch = false;
                } else {
                    batch = true;
                    Common.getBatchList().add(data.get(getBindingAdapterPosition()).getPackageName());
                }
                MaterialButton batchButton = activity.findViewById(R.id.batch);
                batchButton.setVisibility(!Common.getBatchList().isEmpty() ? View.VISIBLE : View.GONE);
                batchButton.setText(activity.getString(R.string.batch_options, Common.getBatchList().size()));
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
                if (Common.getBatchList().contains(packageItems.getPackageName())) {
                    Common.getBatchList().remove(packageItems.getPackageName());
                } else {
                    Common.getBatchList().add(packageItems.getPackageName());
                }
                notifyItemRangeChanged(0, getItemCount());
            } else {
                Common.setApplicationID(packageItems.getPackageName());
                Common.setApplicationName(packageItems.getAppName());
                Common.setApplicationIcon(appIcon.getDrawable());
                Common.setSourceDir(sPackageUtils.getSourceDir(Common.getApplicationID(), view.getContext()));
                Common.setDataDir(sPackageUtils.getDataDir(Common.getApplicationID(), view.getContext()));
                Common.setNativeLibsDir(sPackageUtils.getNativeLibDir(Common.getApplicationID(), view.getContext()));
                Common.isSystemApp(sPackageUtils.isSystemApp(Common.getApplicationID(), view.getContext()));
                Common.isAPKPicker(false);
                Intent details = new Intent(view.getContext(), PackageDetailsActivity.class);
                details.putExtra(PackageDetailsActivity.LAUNCH_INTENT, packageItems.launchIntent() != null);
                view.getContext().startActivity(details);
            }
        }
    }

}