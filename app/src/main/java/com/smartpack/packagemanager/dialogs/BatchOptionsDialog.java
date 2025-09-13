/*
 * Copyright (C) 2020-2025 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.dialogs;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.BatchOptionsAdapter;
import com.smartpack.packagemanager.utils.SerializableItems.BatchOptionsItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 13, 2025
 */
public abstract class BatchOptionsDialog extends MaterialAlertDialogBuilder {

    private List<BatchOptionsItems> mBatchOptionsItems;

    public BatchOptionsDialog(String title, String actionTitle, List<String> packageNames, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_batch_options, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        MaterialTextView titleText = rootView.findViewById(R.id.title);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        titleText.setText(title);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                BatchOptionsAdapter adapter = new BatchOptionsAdapter(getData(packageNames, activity));

                handler.post(() -> recyclerView.setAdapter(adapter));
            });
        }

        setView(rootView);
        setNeutralButton(R.string.cancel, (dialog, id) -> {
        });
        setPositiveButton(actionTitle, (dialog, id) ->
                apply(mBatchOptionsItems));
        show();
    }

    private List<BatchOptionsItems> getData(List<String> packageNames, Activity activity) {
        mBatchOptionsItems = new ArrayList<>();
        for (String packageID : packageNames) {
            if (packageID.contains(".") && sPackageUtils.isPackageInstalled(packageID, activity)) {
                mBatchOptionsItems.add(new BatchOptionsItems(sPackageUtils.getAppName(packageID, activity), packageID, sPackageUtils.getAppIcon(packageID, activity), true, Integer.MIN_VALUE));
            }
        }
        return mBatchOptionsItems;
    }

    public abstract void apply(List<BatchOptionsItems> data);

}