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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.FileProvider;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;

import java.io.File;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 22, 2020
 */

public class PackageDetailsActivity extends AppCompatActivity {

    private AppCompatTextView mDisableTitle;
    private AppCompatTextView mProgressMessage;
    private LinearLayout mOpenApp;
    private LinearLayout mProgressLayout;
    private String PACKAGES = Environment.getExternalStorageDirectory().toString() + "/Package_Manager";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packagedetails);

        mProgressLayout = findViewById(R.id.progress_layout);
        mProgressLayout.setBackgroundColor(Utils.isDarkTheme(this) ? Color.BLACK : Color.WHITE);
        mProgressMessage = findViewById(R.id.progress_message);
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
        LinearLayout mClear = findViewById(R.id.clear_app);
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
                Utils.snackbar(mProgressLayout, getString(R.string.open_message));
            } else {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(Utils.mApplicationID);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    onBackPressed();
                } else {
                    Utils.snackbar(mProgressLayout, getString(R.string.open_failed, Utils.mApplicationName));
                }
            }
        });
        mClear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage(getString(R.string.reset_message, Utils.mApplicationID))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    PackageTasks.clearAppSettings(Utils.mApplicationID);
                }).show());
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
            Utils.launchUrl("https://play.google.com/store/apps/details?id=" + Utils.mApplicationID, mProgressLayout,this);
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
            mClear.setVisibility(View.VISIBLE);
            mBackup.setVisibility(View.VISIBLE);
            mExport.setVisibility(View.VISIBLE);
            mDisable.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void disableApp(Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(PackageTasks.isEnabled(Utils.mApplicationID, context) ?
                        context.getString(R.string.disabling, Utils.mApplicationName) + "..." :
                        context.getString(R.string.enabling, Utils.mApplicationName) + "...");
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
                hideProgress();
                mDisableTitle.setText(PackageTasks.isEnabled(Utils.mApplicationID, context) ? R.string.disable : R.string.enable);
                mOpenApp.setVisibility(PackageTasks.isEnabled(Utils.mApplicationID, context) ? View.VISIBLE : View.GONE);
                Utils.mReloadPage = true;
            }
        }.execute();
    }

    private void uninstallApp(Activity activity) {
        if (Utils.mApplicationID.equals(BuildConfig.APPLICATION_ID)) {
            Utils.snackbar(mProgressLayout, getString(R.string.uninstall_nope));
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
                Utils.snackbar(mProgressLayout, getString(R.string.no_root));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void removeSystemApp(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(activity.getString(R.string.uninstall_summary, Utils.mApplicationName));
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
                hideProgress();
                onBackPressed();
                Utils.mReloadPage = true;
            }
        }.execute();
    }

    private void exportApp(Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.snackbar(mProgressLayout, getString(R.string.permission_denied_write_storage));
        } else {
            for (final String splitApps : PackageTasks.splitApks(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), ""))) {
                if (splitApps.contains("split_")) {
                    if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager/" + Utils.mApplicationID)) {
                        Utils.snackbar(mProgressLayout, getString(R.string.already_exists, Utils.mApplicationID));
                    } else {
                        exportingBundleTask(Utils.mDirSource.replace(new File(Utils.mDirSource).getName(), ""), Utils.mApplicationID,
                                Utils.mApplicationIcon, this);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }
                    return;
                }
            }
            exportingTask(Utils.mDirSource, Utils.mApplicationID, Utils.mApplicationIcon, this);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void exportingTask(String apk, String name, Drawable icon, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(activity.getString(R.string.exporting, name) + "...");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                PackageTasks.makePackageFolder();
                Utils.sleep(1);
                Utils.copy(apk, PACKAGES + "/" + name + ".apk");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress();
                if (Utils.existFile(PACKAGES + "/" + name + ".apk")) {
                    new androidx.appcompat.app.AlertDialog.Builder(activity)
                            .setIcon(icon)
                            .setTitle(activity.getString(R.string.share) + " " + name + "?")
                            .setMessage(name + " " + activity.getString(R.string.export_summary, PACKAGES))
                            .setNeutralButton(activity.getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(activity.getString(R.string.share), (dialog, id) -> {
                                Uri uriFile = FileProvider.getUriForFile(activity,
                                        BuildConfig.APPLICATION_ID + ".provider", new File(PACKAGES + "/" + name + ".apk"));
                                Intent shareScript = new Intent(Intent.ACTION_SEND);
                                shareScript.setType("application/java-archive");
                                shareScript.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.shared_by, name));
                                shareScript.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_message, BuildConfig.VERSION_NAME));
                                shareScript.putExtra(Intent.EXTRA_STREAM, uriFile);
                                shareScript.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(Intent.createChooser(shareScript, activity.getString(R.string.share_with)));
                            })

                            .show();
                }
            }
        }.execute();
    }

    private void backupApp(Activity activity) {
        if (Utils.isStorageWritePermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Utils.snackbar(mProgressLayout, getString(R.string.permission_denied_write_storage));
        } else {
            Utils.dialogEditText(Utils.mApplicationName.toString(),
                    (dialogInterface1, i1) -> {
                    }, new Utils.OnDialogEditTextListener() {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        public void onClick(String text) {
                            if (text.isEmpty()) {
                                Utils.snackbar(mProgressLayout, getString(R.string.name_empty));
                                return;
                            }
                            if (!text.endsWith(".tar.gz")) {
                                text += ".tar.gz";
                            }
                            if (text.contains(" ")) {
                                text = text.replaceAll(" ", "_");
                            }
                            if (Utils.existFile(Environment.getExternalStorageDirectory().toString() + "/Package_Manager" + "/" + text)) {
                                Utils.snackbar(mProgressLayout, getString(R.string.already_exists, text));
                                return;
                            }
                            final String path = text;
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    showProgress(getString(R.string.backing_up, Utils.mApplicationName) + "...");
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
                                    hideProgress();
                                }
                            }.execute();
                        }
                    }, this).setOnDismissListener(dialogInterface12 -> {
            }).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void exportingBundleTask(String apk, String name, Drawable icon, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(getString(R.string.exporting_bundle, name) + "...");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                PackageTasks.makePackageFolder();
                Utils.sleep(1);
                Utils.runCommand("cp -r " + apk + " " + PACKAGES + "/" + name);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress();
                if (Utils.existFile(PACKAGES + "/" + name + "/base.apk")) {
                    new androidx.appcompat.app.AlertDialog.Builder(activity)
                            .setIcon(icon)
                            .setTitle(name)
                            .setMessage(getString(R.string.export_bundle_summary, PACKAGES))
                            .setPositiveButton(R.string.cancel, (dialog, id) -> {
                            })

                            .show();
                }
            }
        }.execute();
    }

    private void showProgress(String message) {
        mProgressMessage.setText(message);
        mProgressMessage.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressMessage.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.GONE);
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