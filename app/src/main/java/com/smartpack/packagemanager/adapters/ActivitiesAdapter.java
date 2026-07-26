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
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.SerializableItems.ActivityItems;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    private final List<ActivityItems> data;
    private final String packageName;

    public ActivitiesAdapter(List<ActivityItems> data, String packageName) {
        this.data = data;
        this.packageName = packageName;
    }

    @NonNull
    @Override
    public ActivitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_activities, parent, false);
        return new ActivitiesAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ActivitiesAdapter.ViewHolder holder, int position) {
        ActivityItems activityItems = this.data.get(position);
        if (activityItems.getLabel() != null) {
            holder.mLabel.setText(activityItems.getLabel());
            holder.mLabel.setVisibility(VISIBLE);
        } else {
            holder.mLabel.setVisibility(GONE);
        }
        holder.mName.setText(activityItems.getName());
        holder.mIcon.setImageDrawable(activityItems.getIcon());
        holder.mLaunch.setVisibility(activityItems.exported() ? VISIBLE : GONE);
        holder.mShortcut.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activityItems.exported() ? VISIBLE : GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.mShortcut.setOnClickListener(v -> createShortcut(activityItems.getName(), activityItems.getLabel() != null ?
                    activityItems.getLabel().toString() : activityItems.getName(), activityItems.getIcon(), v.getContext()));
        }

        holder.mLaunch.setOnClickListener(v -> {
            PackageManager pm = v.getContext().getPackageManager();
            ComponentName component = new ComponentName(packageName, this.data.get(holder.getBindingAdapterPosition()).getName());

            try {
                ActivityInfo info = pm.getActivityInfo(component, PackageManager.GET_META_DATA);

                if (info.exported) {
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                }
            } catch (PackageManager.NameNotFoundException | SecurityException ignored) {
            }
        });
        
        AppSettings.setSlideInAnimation(holder.mIconsLayout, position);
    }

    @SuppressLint("StringFormatInvalid")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createShortcut(String activityName, String label, Drawable drawable, Context context) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

        try {
            ComponentName component = new ComponentName(packageName, activityName);
            Intent shortcutIntent = new Intent();
            shortcutIntent.setComponent(component);
            shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.setAction(Intent.ACTION_VIEW);

            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                ShortcutInfo shortcut = new ShortcutInfo.Builder(context, packageName + "_" + activityName)
                        .setShortLabel(label)
                        .setIcon(Icon.createWithBitmap(PackageExplorer.drawableToBitmap(drawable)))
                        .setIntent(shortcutIntent)
                        .build();

                Intent pinnedShortcutCallbackIntent =
                        shortcutManager.createShortcutResultIntent(shortcut);
                PendingIntent successCallback = PendingIntent.getBroadcast(
                        context, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE);

                shortcutManager.requestPinShortcut(shortcut, successCallback.getIntentSender());
            }
        } catch (Exception e) {
            sCommonUtils.toast(context.getString(R.string.shortcut_create_failed_toast, e.getMessage()), context).show();
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIcon;
        private final LinearLayoutCompat mIconsLayout;
        private final MaterialButton mLaunch, mShortcut;
        private final MaterialTextView mLabel, mName;

        public ViewHolder(View view) {
            super(view);
            this.mLabel = view.findViewById(R.id.title);
            this.mIconsLayout = view.findViewById(R.id.icons_layout);
            this.mName = view.findViewById(R.id.description);
            this.mLaunch = view.findViewById(R.id.launch);
            this.mShortcut = view.findViewById(R.id.shortcut);
            this.mIcon = view.findViewById(R.id.icon);
        }
    }

}