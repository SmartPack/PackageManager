/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PackageExploreAdapter;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.io.File;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2020
 */
public class PackageExploreActivity extends BaseActivity {

    private AppCompatImageView mAppIcon;
    private MaterialTextView mAppNameTxt, mError, mPackageNameTxt, mTitle;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private PackageExploreAdapter mRecycleViewAdapter;
    private List<String> mData;
    private String mPackageName, mRootPath;
    public static final String PACKAGE_INTENT = "package";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithInsets(R.layout.activity_package_explorer, R.id.layout_root);

        mAppIcon = findViewById(R.id.app_image);
        MaterialButton mBack = findViewById(R.id.back);
        MaterialButton mSortButton = findViewById(R.id.sort);
        mAppNameTxt = findViewById(R.id.app_title);
        mPackageNameTxt = findViewById(R.id.version_text);
        mTitle = findViewById(R.id.title);
        mError = findViewById(R.id.error_status);
        mProgressBar = findViewById(R.id.progress);
        mRecyclerView = findViewById(R.id.recycler_view);

        mRootPath = new File(getCacheDir().getPath(),"apk").getAbsolutePath();

        mPackageName = getIntent().getStringExtra(PACKAGE_INTENT);

        mTitle.setText("Root");

        mBack.setOnClickListener(v -> {
            sFileUtils.delete(new File(getCacheDir().getPath(), "apk"));
            finish();
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PackageExplorer.getSpanCount(this)));

        reload(mRootPath, this);

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, "A-Z").setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, this), this);
                reload(mRootPath, this);
                return false;
            });
            popupMenu.show();
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressedEvent();
            }
        });
    }

    private void reload(String path, Activity activity) {
        new sExecutor() {
            private boolean failed = false;
            private Drawable appIcon = null;
            private String appName = null;

            @Override
            public void onPreExecute() {
                mProgressBar.setVisibility(VISIBLE);
            }

            @Override
            public void doInBackground() {
                try {
                    if (appIcon == null && appName == null) {
                        PackageManager pm = activity.getPackageManager();
                        ApplicationInfo ai = pm.getApplicationInfo(mPackageName, 0);

                        appIcon = pm.getApplicationIcon(ai);
                        appName = pm.getApplicationLabel(ai).toString();
                    }

                    mData = FilePicker.getData(path, activity, false);
                    mRecycleViewAdapter = new PackageExploreAdapter(mData, mPackageName, activity);
                } catch (Exception ignored) {
                    failed = true;
                }
            }

            @SuppressLint("StringFormatInvalid")
            private void onRecyclerViewClick() {
                mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
                    String mPath = mData.get(position);
                    if (position == 0) {
                        backPressedEvent();
                    } else if (new File(mPath).isDirectory()) {
                        reload(mPath, activity);
                    } else if (PackageExplorer.isTextFile(mPath)) {
                        Intent textView = new Intent(activity, TextViewActivity.class);
                        textView.putExtra(TextViewActivity.PATH_INTENT, mPath);
                        textView.putExtra(TextViewActivity.PACKAGE_INTENT, mPackageName);
                        startActivity(textView);
                    } else if (PackageExplorer.isImageFile(mPath)) {
                        Intent imageView = new Intent(activity, ImageViewActivity.class);
                        imageView.putExtra(ImageViewActivity.PATH_INTENT, mPath);
                        imageView.putExtra(ImageViewActivity.PACKAGE_INTENT, mPackageName);
                        startActivity(imageView);
                    } else {
                        new MaterialAlertDialogBuilder(activity)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(getString(R.string.open_failed_export_message, new File(mPath).getName()))
                                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                })
                                .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> PackageExplorer
                                        .copyToStorage(mPath, mPackageName, activity)).show();
                    }
                });
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                mProgressBar.setVisibility(GONE);
                if (failed) {
                    mRecyclerView.setVisibility(GONE);
                    mError.setText(getString(R.string.explore_error_status, appName));
                    mError.setVisibility(VISIBLE);
                } else {
                    mRootPath = path;
                    if (appIcon != null) {
                        mAppIcon.setImageDrawable(appIcon);
                    }
                    if (mAppNameTxt != null) {
                        mAppNameTxt.setText(appName);
                    }
                    mPackageNameTxt.setText(mPackageName);
                    mTitle.setText(path.equals(new File(getCacheDir(), "apk").getAbsolutePath()) ? "Root"
                            : new File(path).getName());
                    mRecyclerView.setAdapter(mRecycleViewAdapter);
                    onRecyclerViewClick();
                }
            }
        }.execute();
    }

    private void backPressedEvent() {
        if (new File(mRootPath).equals(new File(getCacheDir().toString(), "apk/"))) {
            sFileUtils.delete(new File(getCacheDir().getPath(),"apk"));
            finish();
        } else {
            reload(Objects.requireNonNull(new File(mRootPath).getParentFile()).getPath(), this);
        }
    }

}