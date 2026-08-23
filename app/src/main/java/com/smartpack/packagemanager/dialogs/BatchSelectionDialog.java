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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.BatchOptionsAdapter;
import com.smartpack.packagemanager.utils.Serializables.BatchOptionsItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 13, 2025
 */
public abstract class BatchSelectionDialog extends BottomSheetDialog {

    boolean empty = true;
    private List<BatchOptionsItems> mBatchOptionsItems;

    public BatchSelectionDialog(List<String> packageNames, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_batch_selection, null);
        MaterialButton action = rootView.findViewById(R.id.action);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                BatchOptionsAdapter adapter = new BatchOptionsAdapter(getData(packageNames, activity));

                handler.post(() -> recyclerView.setAdapter(adapter));
            });
        }

        cancel.setOnClickListener(v -> dismiss());

        action.setOnClickListener(v -> {
            try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    List<BatchOptionsItems> selectedItems = new CopyOnWriteArrayList<>();
                    for (BatchOptionsItems items : mBatchOptionsItems) {
                        if (items.isChecked()) {
                            selectedItems.add(items);
                            if (empty) empty = false;
                        }
                    }

                    handler.post(() -> {
                        if (empty) return;
                        apply(selectedItems);
                        dismiss();
                    });
                });
            }
        });

        setContentView(rootView);

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