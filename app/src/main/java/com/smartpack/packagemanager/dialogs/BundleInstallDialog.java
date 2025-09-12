/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.APKPickerActivity;
import com.smartpack.packagemanager.adapters.APKPickerAdapter;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.SerializableItems.APKPickerItems;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 24, 2025
 */
public class BundleInstallDialog extends MaterialAlertDialogBuilder {

    public BundleInstallDialog(List<APKPickerItems> data, boolean finish, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_recyclerview, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(activity, PackageExplorer.getSpanCount(activity)));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new APKPickerAdapter(data));

        setView(rootView);
        setIcon(R.mipmap.ic_launcher);
        setTitle(R.string.split_apk_select);
        setCancelable(false);
        setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
            if (finish) {
                activity.finish();
            }
        });
        setPositiveButton(R.string.select, (dialogInterface, i) ->
                new sExecutor() {
                    boolean isEmpty = true;
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        for (APKPickerItems apkPickerItems : data) {
                            if (apkPickerItems.isSelected()) {
                                isEmpty = false;
                                return;
                            }
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        if (isEmpty) {
                            sCommonUtils.toast(R.string.split_apk_list_empty, activity).show();
                        } else if (data.size() == 1) {
                            Intent apkDetails = new Intent(activity, APKPickerActivity.class);
                            apkDetails.putExtra(APKPickerActivity.PATH_INTENT, data.get(0).getAPKPath());
                            apkDetails.putExtra(APKPickerActivity.NAME_INTENT, data.get(0).getAPKName());
                            activity.startActivity(apkDetails);
                        } else {
                            new SplitAPKsInstallationTasks(data, activity).execute();
                        }
                        if (finish) {
                            activity.finish();
                        }
                    }
                }.execute());
        show();
    }
}