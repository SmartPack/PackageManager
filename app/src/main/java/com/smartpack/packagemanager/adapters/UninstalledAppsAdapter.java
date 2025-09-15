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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RootShell;
import com.smartpack.packagemanager.utils.ShizukuShell;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 10, 2021
 */
public class UninstalledAppsAdapter extends RecyclerView.Adapter<UninstalledAppsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<String> data, restoreList;
    private static boolean batch = false;

    public UninstalledAppsAdapter(List<String> data, List<String> restoreList, Activity activity) {
        this.data = data;
        this.restoreList = restoreList;
        this.activity = activity;
        batch = !restoreList.isEmpty();
    }

    @NonNull
    @Override
    public UninstalledAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_uninstlled_apps, parent, false);
        return new UninstalledAppsAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull UninstalledAppsAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position));
        holder.mRestore.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.sure_question)
                .setMessage(v.getContext().getString(R.string.restore_message, data.get(position)))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.restore, (dialog, id) ->
                        restore(position, holder.mRestore.getContext()).execute()).show()
        );
        holder.mRestore.setVisibility(batch ? GONE : VISIBLE);
        holder.mCheckBox.setVisibility(batch ? VISIBLE : GONE);
        holder.mCheckBox.setChecked(restoreList.contains(data.get(position)));

        activity.findViewById(R.id.batch).setVisibility(restoreList.isEmpty() ? GONE : VISIBLE);

        holder.mCheckBox.setOnClickListener(v -> {
            if (restoreList.contains(data.get(position))) {
                restoreList.remove(data.get(position));
            } else {
                restoreList.add(data.get(position));
            }
            notifyItemChanged(position);
        });
    }

    private sExecutor restore(int position, Context context) {
        return new sExecutor() {
            private RootShell mRootShell = null;
            private ShizukuShell mShizukuShell = null;
            private String mOutput = null;

            @Override
            public void onPreExecute() {
                mRootShell = new RootShell();
                mShizukuShell = new ShizukuShell();
            }

            @Override
            public void doInBackground() {
                if (mRootShell.rootAccess()) {
                    mOutput = mRootShell.runAndGetError("cmd package install-existing " + data.get(position));
                } else {
                    mOutput = mShizukuShell.runAndGetOutput("cmd package install-existing " + data.get(position));
                }
            }

            @Override
            public void onPostExecute() {
                PackageData.setRawData(null, context);
                new MaterialAlertDialogBuilder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(mOutput.endsWith("installed for user: 0") ? context.getString(R.string.restore_success_message) : mOutput)
                        .setPositiveButton(R.string.cancel, (dialog, id) -> {
                        }).show();
                if (mOutput.contains("installed for user: 0")) {
                    data.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final MaterialButton mRestore;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mRestore = view.findViewById(R.id.restore);
            this.mTitle = view.findViewById(R.id.title);
            this.mCheckBox = view.findViewById(R.id.checkbox);

            view.setOnLongClickListener(v -> {
                if (batch) {
                    restoreList.clear();
                    batch = false;
                } else {
                    batch = true;
                    restoreList.add(data.get(getBindingAdapterPosition()));
                }
                activity.findViewById(R.id.batch).setVisibility(restoreList.isEmpty() ? GONE : VISIBLE);
                notifyItemRangeChanged(0, getItemCount());
                return true;
            });
        }

        @Override
        public void onClick(View view) {
            if (batch) {
                String packageName = data.get(getBindingAdapterPosition());
                if (restoreList.contains(packageName)) {
                    restoreList.remove(packageName);
                } else {
                    restoreList.add(packageName);
                }
                notifyItemRangeChanged(0, getItemCount());
            }
        }
    }

}