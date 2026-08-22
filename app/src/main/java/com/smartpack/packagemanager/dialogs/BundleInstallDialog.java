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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.activities.APKPickerActivity;
import com.smartpack.packagemanager.adapters.APKPickerAdapter;
import com.smartpack.packagemanager.utils.PackageExplorer;
import com.smartpack.packagemanager.utils.Serializables.APKPickerItems;
import com.smartpack.packagemanager.utils.tasks.SingleAPKTasks;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 24, 2025
 */
public class BundleInstallDialog extends BottomSheetDialog {

    public BundleInstallDialog(List<APKPickerItems> data, boolean finish, SingleAPKTasks.OnInstallRequest callback, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_bundle_install, null);

        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialButton select = rootView.findViewById(R.id.select);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(activity, PackageExplorer.getSpanCount(activity)));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new APKPickerAdapter(data));

        setContentView(rootView);
        setTitle(R.string.split_apk_select);

        cancel.setOnClickListener(v -> {
            if (finish) {
                activity.finish();
            }
            dismiss();
        });

        select.setOnClickListener(v -> new sExecutor() {
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
                    new SplitAPKsInstallationTasks(data, callback::onInstall, activity).execute();
                }
                if (finish) {
                    activity.finish();
                }
                dismiss();
            }
        }.execute());

        show();
    }
}