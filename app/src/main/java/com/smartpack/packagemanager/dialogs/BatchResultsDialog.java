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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 13, 2025
 */
public class BatchResultsDialog extends BottomSheetDialog {

    public BatchResultsDialog(List<BatchOptionsItems> data, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_batch_results, null);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                BatchOptionsAdapter adapter = new BatchOptionsAdapter(data);

                handler.post(() -> recyclerView.setAdapter(adapter));
            });
        }

        setContentView(rootView);

        cancel.setOnClickListener(v -> dismiss());

        show();
    }

}