/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.io.File;
import java.lang.ref.WeakReference;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */

public class PackageDetailsActivity extends AppCompatActivity {

    private AppCompatTextView mDisableTitle;
    private LinearLayout mDetailsLayout;
    private LinearLayout mOpenApp;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagedetails);

        AppCompatImageView mAppIcon = findViewById(R.id.app_image);
        AppCompatTextView mAppName = findViewById(R.id.app_title);
        AppCompatTextView mPackageID = findViewById(R.id.package_id_text);
        AppCompatTextView mVersion = findViewById(R.id.version_text);
        AppCompatTextView mDataDir = findViewById(R.id.data_dir_text);
        AppCompatTextView mNatLib = findViewById(R.id.native_lib_text);
        AppCompatTextView mAPKPath = findViewById(R.id.apk_path_text);
        AppCompatTextView mPermissions = findViewById(R.id.permissions_text);
        mDisableTitle = findViewById(R.id.enable_title);
        AppCompatTextView mCancelButton = findViewById(R.id.cancel_button);
        mDetailsLayout = findViewById(R.id.layout_details);
        mOpenApp = findViewById(R.id.open_app);
        LinearLayout mBackup = findViewById(R.id.backup_app);
        LinearLayout mExport = findViewById(R.id.export_app);
        LinearLayout mDisable = findViewById(R.id.disable_app);
        LinearLayout mOpenStore = findViewById(R.id.playstore_app);
        LinearLayout mUninstallApp = findViewById(R.id.remove_app);
        LinearLayout mOpenSettings = findViewById(R.id.info_app);
        mAppIcon.setImageDrawable(Utils.mApplicationIcon);
        mAppName.setText(Utils.mApplicationName);
        mPackageID.setText(Utils.mApplicationID);
        mVersion.setText(getString(R.string.version, PackageTasks.getVersionName(Utils.mDirSource, this)));
        mDisableTitle.setText(PackageTasks.isEnabled(Utils.mApplicationID, new WeakReference<>(this)) ? R.string.disable : R.string.enable);
        mDataDir.setText(Utils.mDirData);
        mNatLib.setText(Utils.mDirNatLib);
        if (RootUtils.rootAccessDenied()) {
            mAPKPath.setText(Utils.mDirSource);
        } else {
            mAPKPath.setText(PackageTasks.listSplitAPKs(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), "")));
        }
        mPermissions.setText(PackageTasks.getPermissions(Utils.mApplicationID, this));
        mOpenApp.setVisibility(PackageTasks.isEnabled(Utils.mApplicationID, new WeakReference<>(this)) ? View.VISIBLE : View.GONE);
        mOpenApp.setOnClickListener(v -> {
            if (Utils.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
                Utils.showSnackbar(mDetailsLayout, getString(R.string.open_message));
            } else {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(Utils.mApplicationID);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    onBackPressed();
                } else {
                    Utils.showSnackbar(mDetailsLayout, getString(R.string.open_failed, Utils.mApplicationName));
                }
            }
        });
        mBackup.setOnClickListener(v -> backupApp(this));
        mExport.setOnClickListener(v -> exportApp(this));
        mDisable.setOnClickListener(v -> new Dialog(this)
                .setIcon(Utils.mApplicationIcon)
                .setTitle(Utils.mApplicationName)
                .setMessage(Utils.mApplicationName + " " + getString(R.string.disable_message,
                        PackageTasks.isEnabled(Utils.mApplicationID, new WeakReference<>(this)) ?
                                getString(R.string.disabled) : getString(R.string.enabled)))
                .setCancelable(false)
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    disableApp(new WeakReference<>(this));
                })
                .show());
        mOpenStore.setOnClickListener(v -> {
            try {
                Intent ps = new Intent(Intent.ACTION_VIEW);
                ps.setData(Uri.parse(
                        "https://play.google.com/store/apps/details?id=" + Utils.mApplicationID));
                startActivity(ps);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            onBackPressed();
        });
        mUninstallApp.setOnClickListener(v -> uninstallApp(this));
        mOpenSettings.setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", Utils.mApplicationID, null);
            settings.setData(uri);
            startActivity(settings);
            onBackPressed();
        });
        mCancelButton.setOnClickListener(v -> {
            onBackPressed();
        });
        if (!RootUtils.rootAccessDenied()) {
            mBackup.setVisibility(View.VISIBLE);
            mExport.setVisibility(View.VISIBLE);
            mDisable.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void disableApp(WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(PackageTasks.isEnabled(Utils.mApplicationID, activityRef) ?
                        activityRef.get().getString(R.string.disabling, Utils.mApplicationName) + "..." :
                        activityRef.get().getString(R.string.enabling, Utils.mApplicationName) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                if (PackageTasks.isEnabled(Utils.mApplicationID, activityRef)) {
                    RootUtils.runCommand("pm disable " + Utils.mApplicationID);
                } else {
                    RootUtils.runCommand("pm enable " + Utils.mApplicationID);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                mDisableTitle.setText(PackageTasks.isEnabled(Utils.mApplicationID, new WeakReference<>(activityRef.get())) ? R.string.disable : R.string.enable);
                mOpenApp.setVisibility(PackageTasks.isEnabled(Utils.mApplicationID, new WeakReference<>(activityRef.get())) ? View.VISIBLE : View.GONE);
                Utils.mReloadPage = true;
            }
        }.execute();
    }

    private void uninstallApp(Activity activity) {
        if (Utils.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
            Utils.showSnackbar(mDetailsLayout, getString(R.string.uninstall_nope));
        } else if (!Utils.mSystemApp) {
            Intent remove = new Intent(Intent.ACTION_DELETE);
            remove.setData(Uri.parse("package:" + Utils.mApplicationID));
            startActivity(remove);
            Utils.mReloadPage = true;
            onBackPressed();
        } else {
            if (RootUtils.rootAccessDenied()) {
                Utils.showSnackbar(mDetailsLayout, getString(R.string.no_root));
            } else {
                new Dialog(activity)
                        .setIcon(Utils.mApplicationIcon)
                        .setTitle(getString(R.string.uninstall_title, Utils.mApplicationName))
                        .setMessage(getString(R.string.uninstall_warning))
                        .setCancelable(false)
                        .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                            removeSystemApp(new WeakReference<>(activity));
                        })
                        .show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void removeSystemApp(WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.uninstall_summary, Utils.mApplicationName));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                RootUtils.runCommand("pm uninstall --user 0 " + Utils.mApplicationID);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Utils.mReloadPage = true;
                onBackPressed();
            }
        }.execute();
    }

    private void exportApp(Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.showSnackbar(mDetailsLayout, getString(R.string.permission_denied_write_storage));
        } else {
            for (final String splitApps : PackageTasks.splitApks(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), ""))) {
                if (splitApps.contains("split_")) {
                    if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager/" + Utils.mApplicationID)) {
                        Utils.showSnackbar(mDetailsLayout, getString(R.string.already_exists, Utils.mApplicationID));
                    } else {
                        PackageTasks.exportingBundleTask(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), ""), Utils.mApplicationID,
                                Utils.mApplicationIcon, new WeakReference<>(this));
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }
                    return;
                }
            }
            PackageTasks.exportingTask(Utils.mDirSource, Utils.mApplicationID,
                    Utils.mApplicationIcon, new WeakReference<>(this));
        }
    }

    private void backupApp(Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.showSnackbar(mDetailsLayout, getString(R.string.permission_denied_write_storage));
        } else {
            ViewUtils.dialogEditText(Utils.mApplicationName.toString(),
                    (dialogInterface1, i1) -> {
                    }, new ViewUtils.OnDialogEditTextListener() {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        public void onClick(String text) {
                            if (text.isEmpty()) {
                                Utils.showSnackbar(mDetailsLayout, getString(R.string.name_empty));
                                return;
                            }
                            if (!text.endsWith(".tar.gz")) {
                                text += ".tar.gz";
                            }
                            if (text.contains(" ")) {
                                text = text.replaceAll(" ", "_");
                            }
                            if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager" + "/" + text)) {
                                Utils.showSnackbar(mDetailsLayout, getString(R.string.already_exists, text));
                                return;
                            }
                            final String path = text;
                            new AsyncTask<Void, Void, Void>() {
                                private ProgressDialog mProgressDialog;

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    mProgressDialog = new ProgressDialog(activity);
                                    mProgressDialog.setMessage(getString(R.string.backing_up, Utils.mApplicationName) + "...");
                                    mProgressDialog.setCancelable(false);
                                    mProgressDialog.show();
                                }

                                @Override
                                protected Void doInBackground(Void... voids) {
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                                    PackageTasks.backupApp(Utils.mApplicationID, path);
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    try {
                                        mProgressDialog.dismiss();
                                    } catch (IllegalArgumentException ignored) {
                                    }
                                }
                            }.execute();
                        }
                    }, this).setOnDismissListener(dialogInterface12 -> {
            }).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.mSystemApp = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}