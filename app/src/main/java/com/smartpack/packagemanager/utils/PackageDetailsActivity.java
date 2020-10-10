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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */

public class PackageDetailsActivity extends AppCompatActivity {

    private AppCompatTextView mDisableTitle;
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
        mDisableTitle.setText(PackageTasks.isEnabled(Utils.mApplicationID, this) ? R.string.disable : R.string.enable);
        mDataDir.setText(Utils.mDirData);
        mNatLib.setText(Utils.mDirNatLib);
        if (Utils.rootAccess()) {
            mAPKPath.setText(PackageTasks.listSplitAPKs(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), "")));
        } else {
            mAPKPath.setText(Utils.mDirSource);
        }
        mPermissions.setText(PackageTasks.getPermissions(Utils.mApplicationID, this));
        mOpenApp.setVisibility(PackageTasks.isEnabled(Utils.mApplicationID, this) ? View.VISIBLE : View.GONE);
        mOpenApp.setOnClickListener(v -> {
            if (Utils.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
                Utils.snackbar(getString(R.string.open_message));
            } else {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(Utils.mApplicationID);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    onBackPressed();
                } else {
                    Utils.snackbar(getString(R.string.open_failed, Utils.mApplicationName));
                }
            }
        });
        mBackup.setOnClickListener(v -> backupApp(this));
        mExport.setOnClickListener(v -> exportApp(this));
        mDisable.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setIcon(Utils.mApplicationIcon)
                .setTitle(Utils.mApplicationName)
                .setMessage(Utils.mApplicationName + " " + getString(R.string.disable_message,
                        PackageTasks.isEnabled(Utils.mApplicationID, this) ?
                                getString(R.string.disabled) : getString(R.string.enabled)))
                .setCancelable(false)
                .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    disableApp(this);
                })
                .show());
        mOpenStore.setOnClickListener(v -> {
            Utils.launchUrl("https://play.google.com/store/apps/details?id=" + Utils.mApplicationID, this);
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
        if (Utils.rootAccess()) {
            mBackup.setVisibility(View.VISIBLE);
            mExport.setVisibility(View.VISIBLE);
            mDisable.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void disableApp(Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(PackageTasks.isEnabled(Utils.mApplicationID, context) ?
                        context.getString(R.string.disabling, Utils.mApplicationName) + "..." :
                       context.getString(R.string.enabling, Utils.mApplicationName) + "...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                if (PackageTasks.isEnabled(Utils.mApplicationID, context)) {
                    Utils.runCommand("pm disable " + Utils.mApplicationID);
                } else {
                    Utils.runCommand("pm enable " + Utils.mApplicationID);
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
                mDisableTitle.setText(PackageTasks.isEnabled(Utils.mApplicationID, context) ? R.string.disable : R.string.enable);
                mOpenApp.setVisibility(PackageTasks.isEnabled(Utils.mApplicationID, context) ? View.VISIBLE : View.GONE);
                Utils.mReloadPage = true;
            }
        }.execute();
    }

    private void uninstallApp(Activity activity) {
        if (Utils.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
            Utils.snackbar(getString(R.string.uninstall_nope));
        } else if (!Utils.mSystemApp) {
            Intent remove = new Intent(Intent.ACTION_DELETE);
            remove.setData(Uri.parse("package:" + Utils.mApplicationID));
            startActivity(remove);
            Utils.mReloadPage = true;
            onBackPressed();
        } else {
            if (Utils.rootAccess()) {
                new AlertDialog.Builder(activity)
                        .setIcon(Utils.mApplicationIcon)
                        .setTitle(getString(R.string.uninstall_title, Utils.mApplicationName))
                        .setMessage(getString(R.string.uninstall_warning))
                        .setCancelable(false)
                        .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                            removeSystemApp(this);
                        })
                        .show();
            } else {
                Utils.snackbar(getString(R.string.no_root));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void removeSystemApp(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.uninstall_summary, Utils.mApplicationName));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.sleep(1);
                Utils.runCommand("pm uninstall --user 0 " + Utils.mApplicationID);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                onBackPressed();
                Utils.mReloadPage = true;
            }
        }.execute();
    }

    private void exportApp(Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.snackbar(getString(R.string.permission_denied_write_storage));
        } else {
            for (final String splitApps : PackageTasks.splitApks(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), ""))) {
                if (splitApps.contains("split_")) {
                    if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager/" + Utils.mApplicationID)) {
                        Utils.snackbar(getString(R.string.already_exists, Utils.mApplicationID));
                    } else {
                        PackageTasks.exportingBundleTask(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), ""), Utils.mApplicationID,
                                Utils.mApplicationIcon, this);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }
                    return;
                }
            }
            PackageTasks.exportingTask(Utils.mDirSource, Utils.mApplicationID, Utils.mApplicationIcon, this);
        }
    }

    private void backupApp(Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.snackbar(getString(R.string.permission_denied_write_storage));
        } else {
            Utils.dialogEditText(Utils.mApplicationName.toString(),
                    (dialogInterface1, i1) -> {
                    }, new Utils.OnDialogEditTextListener() {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        public void onClick(String text) {
                            if (text.isEmpty()) {
                                Utils.snackbar(getString(R.string.name_empty));
                                return;
                            }
                            if (!text.endsWith(".tar.gz")) {
                                text += ".tar.gz";
                            }
                            if (text.contains(" ")) {
                                text = text.replaceAll(" ", "_");
                            }
                            if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager" + "/" + text)) {
                                Utils.snackbar(getString(R.string.already_exists, text));
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