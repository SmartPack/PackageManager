/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewAdapter;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.PackageList;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 19, 2020
 */
public class OEMPackageActivity extends AppCompatActivity {

    private AppCompatImageButton mSettings;
    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private MaterialTextView mOEMTitle;
    private LinearLayout mProgressLayout;
    private List<String> mOEMList;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packageoem);

        mOEMTitle = findViewById(R.id.title);
        mProgressLayout = findViewById(R.id.progress_layout);
        PackageTasks.mBatchOptions = findViewById(R.id.batch_options);
        mRecyclerView = findViewById(R.id.recycler_view);
        AppCompatImageButton mBack = findViewById(R.id.back);
        mSettings = findViewById(R.id.settings_icon);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mOEMTitle.setText(getTitle(this));

        loadUI(this);

        mBack.setOnClickListener(v -> {
            onBackPressed();
        });

        mSettings.setOnClickListener(v -> settingsMenu(this));

        PackageTasks.mBatchOptions.setOnClickListener(v -> batchOptionsMenu(this));

    }

    private static List<String> getDefault(Activity activity) {
        if (Build.BRAND.equalsIgnoreCase("oneplus")) {
            return PackageList.getOnePlusList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("asus")) {
            return PackageList.getASUSList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("motorola")) {
            return PackageList.getMotoList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("huawei")) {
            return PackageList.getHuaweiList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("lg")) {
            return PackageList.getLGList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("samsung")) {
            return PackageList.getSamsungList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("sony")) {
            return PackageList.getSonyList(activity);
        } else if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            return PackageList.getXiaomiList(activity);
        } else {
            return PackageList.getGoogleList(activity);
        }
    }

    private static String getTitle(Activity activity) {
        if (Build.BRAND.equalsIgnoreCase("oneplus")) {
            return activity.getString(R.string.oem_oneplus);
        } else if (Build.BRAND.equalsIgnoreCase("asus")) {
            return activity.getString(R.string.oem_asus);
        } else if (Build.BRAND.equalsIgnoreCase("motorola")) {
            return activity.getString(R.string.oem_moto);
        } else if (Build.BRAND.equalsIgnoreCase("huawei")) {
            return activity.getString(R.string.oem_huawei);
        } else if (Build.BRAND.equalsIgnoreCase("lg")) {
            return activity.getString(R.string.oem_lg);
        } else if (Build.BRAND.equalsIgnoreCase("samsung")) {
            return activity.getString(R.string.oem_samsung);
        } else if (Build.BRAND.equalsIgnoreCase("sony")) {
            return activity.getString(R.string.oem_sony);
        } else if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            return activity.getString(R.string.oem_mi);
        } else {
            return activity.getString(R.string.oem_google);
        }
    }

    private void settingsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSettings);
        Menu menu = popupMenu.getMenu();
        if (PackageList.getASUSList(activity).size() > 0) {
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.oem_asus));
        }
        if (PackageList.getGoogleList(activity).size() > 0) {
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.oem_google));
        }
        if (PackageList.getSamsungList(activity).size() > 0) {
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.oem_samsung));
        }
        if (PackageList.getMotoList(activity).size() > 0) {
            menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.oem_moto));
        }
        if (PackageList.getOnePlusList(activity).size() > 0) {
            menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.oem_oneplus));
        }
        if (PackageList.getHuaweiList(activity).size() > 0) {
            menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.oem_huawei));
        }
        if (PackageList.getSonyList(activity).size() > 0) {
            menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.oem_sony));
        }
        if (PackageList.getLGList(activity).size() > 0) {
            menu.add(Menu.NONE, 7, Menu.NONE, getString(R.string.oem_lg));
        }
        if (PackageList.getXiaomiList(activity).size() > 0) {
            menu.add(Menu.NONE, 8, Menu.NONE, getString(R.string.oem_mi));
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    mOEMTitle.setText(getString(R.string.oem_asus));
                    mOEMList = PackageList.getASUSList(activity);
                    reload(activity);
                    break;
                case 1:
                    mOEMTitle.setText(getString(R.string.oem_google));
                    mOEMList = PackageList.getGoogleList(activity);
                    reload(activity);
                    break;
                case 2:
                    mOEMTitle.setText(getString(R.string.oem_samsung));
                    mOEMList = PackageList.getSamsungList(activity);
                    reload(activity);
                    break;
                case 3:
                    mOEMTitle.setText(getString(R.string.oem_moto));
                    mOEMList = PackageList.getMotoList(activity);
                    reload(activity);
                    break;
                case 4:
                    mOEMTitle.setText(getString(R.string.oem_oneplus));
                    mOEMList = PackageList.getOnePlusList(activity);
                    reload(activity);
                    break;
                case 5:
                    mOEMTitle.setText(getString(R.string.oem_huawei));
                    mOEMList = PackageList.getHuaweiList(activity);
                    reload(activity);
                    break;
                case 6:
                    mOEMTitle.setText(getString(R.string.oem_sony));
                    mOEMList = PackageList.getSonyList(activity);
                    reload(activity);
                    break;
                case 7:
                    mOEMTitle.setText(getString(R.string.oem_lg));
                    mOEMList = PackageList.getLGList(activity);
                    reload(activity);
                    break;
                case 8:
                    mOEMTitle.setText(getString(R.string.oem_mi));
                    mOEMList = PackageList.getXiaomiList(activity);
                    reload(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void batchOptionsMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, PackageTasks.mBatchOptions);
        Menu menu = popupMenu.getMenu();
        if (Utils.rootAccess()) {
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.turn_on_off));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.uninstall));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.reset));
        }
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.export));
        menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.batch_list_clear));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.sure_question)
                            .setMessage(getString(R.string.batch_list_disable) + "\n" + PackageData.showBatchList())
                            .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) -> {
                                PackageTasks.batchDisableTask(activity);
                            })
                            .show();
                    break;
                case 1:
                    MaterialAlertDialogBuilder uninstall = new MaterialAlertDialogBuilder(activity);
                    uninstall.setIcon(R.mipmap.ic_launcher);
                    uninstall.setTitle(R.string.sure_question);
                    uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageData.showBatchList());
                    uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) -> {
                        PackageTasks.batchUninstallTask(activity);
                    });
                    uninstall.show();
                    break;
                case 2:
                    MaterialAlertDialogBuilder reset = new MaterialAlertDialogBuilder(activity);
                    reset.setIcon(R.mipmap.ic_launcher);
                    reset.setTitle(R.string.sure_question);
                    reset.setMessage(getString(R.string.batch_list_reset) + "\n" + PackageData.showBatchList());
                    reset.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    reset.setPositiveButton(getString(R.string.reset), (dialogInterface, i) -> {
                        PackageTasks.batchResetTask(activity);
                    });
                    reset.show();
                    break;
                case 3:
                    MaterialAlertDialogBuilder export = new MaterialAlertDialogBuilder(activity);
                    export.setIcon(R.mipmap.ic_launcher);
                    export.setTitle(R.string.sure_question);
                    export.setMessage(getString(R.string.batch_list_export) + "\n" + PackageData.showBatchList());
                    export.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    export.setPositiveButton(getString(R.string.export), (dialogInterface, i) -> {
                        PackageTasks.batchExportTask(activity);
                    });
                    export.show();
                    break;
                case 4:
                    PackageData.mBatchList.clear();
                    reload(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadUI(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewAdapter(getDefault(activity));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private void reload(Activity activity) {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mProgressLayout.setVisibility(View.VISIBLE);
                            PackageTasks.mBatchOptions.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.GONE);
                            if (!PackageData.getBatchList().isEmpty() && PackageData.getBatchList().contains(".")) {
                                PackageData.mBatchList.clear();
                            }
                            mRecyclerView.removeAllViews();
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAdapter(mOEMList != null ? mOEMList : getDefault(activity));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            PackageTasks.mBatchOptions.setVisibility(PackageData.getBatchList().length() > 0 && PackageData
                                    .getBatchList().contains(".") ? View.VISIBLE : View.GONE);
                            mProgressLayout.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onBackPressed() {
        if (!PackageData.getBatchList().isEmpty() && PackageData.getBatchList().contains(".")) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.batch_warning)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        PackageList.mOEMApps = false;
                        finish();
                    })
                    .show();
        } else {
            PackageList.mOEMApps = false;
            finish();
        }
    }

}