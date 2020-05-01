/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.MainActivity;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageTasks;
import com.smartpack.packagemanager.utils.PackageTasksActivity;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.utils.root.RootUtils;
import com.smartpack.packagemanager.views.dialog.Dialog;

import java.lang.ref.WeakReference;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 01, 2020
 */

public class DescriptionFragment extends BaseFragment {
    @SuppressLint("StaticFieldLeak")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Fragment fragment = getParentFragment();
        if (!(fragment instanceof PackageTasksFragment)) {
            assert fragment != null;
            fragment = fragment.getParentFragment();
        }
        final PackageTasksFragment systemAppsFragment = (PackageTasksFragment) fragment;

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        AppCompatEditText keyEdit = rootView.findViewById(R.id.key_edittext);

        keyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                PackageTasks.mAppName = s.toString();
                assert systemAppsFragment != null;
                systemAppsFragment.reload();
            }
        });
        assert systemAppsFragment != null;
        if (PackageTasks.mAppName != null) {
            keyEdit.append(PackageTasks.mAppName);
        }

        AppCompatImageButton batch = rootView.findViewById(R.id.batch_icon);
        batch.setImageDrawable(getResources().getDrawable(R.drawable.ic_queue));
        batch.setOnClickListener(v -> {
            if (RootUtils.rootAccessDenied()) {
                Utils.toast(R.string.no_root, getActivity());
                return;
            }
            PopupMenu popupMenu = new PopupMenu(requireActivity(), batch);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.backup));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.turn_on_off));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.uninstall));
            menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.batch_list_clear));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        if (Utils.isStorageWritePermissionDenied(requireActivity())) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            Utils.toast(R.string.permission_denied_write_storage, getActivity());
                        } else if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog backup = new Dialog(requireActivity());
                            backup.setIcon(R.mipmap.ic_launcher);
                            backup.setTitle(R.string.sure_question);
                            backup.setMessage(getString(R.string.batch_list_backup) + "\n" + PackageTasks.mBatchApps.toString()
                                    .replaceAll("\\s+", "\n - "));
                            backup.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            backup.setPositiveButton(getString(R.string.backup), (dialogInterface, i) -> {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        PackageTasks.mRunning = true;
                                        if (PackageTasks.mOutput == null) {
                                            PackageTasks.mOutput = new StringBuilder();
                                        } else {
                                            PackageTasks.mOutput.setLength(0);
                                        }
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_processing_initialized)).append("...\n\n");
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_list_summary)).append(PackageTasks
                                                .mBatchApps.toString().replaceAll("\\s+", "\n - ")).append("\n\n");
                                        Intent backupIntent = new Intent(getActivity(), PackageTasksActivity.class);
                                        backupIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.batch_processing));
                                        backupIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.batch_processing_finished));
                                        startActivity(backupIntent);
                                    }

                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ", "")
                                                .replaceAll("\\s+", " ").split(" ");
                                        for (String packageID : batchApps) {
                                            if (packageID.contains(".")) {
                                                PackageTasks.mOutput.append("** ").append(getString(R.string.backing_summary, packageID));
                                                PackageTasks.backupApp(packageID, packageID + "_batch.tar.gz");
                                                PackageTasks.mOutput.append(": ").append(getString(R.string.done)).append(" *\n\n");
                                            }
                                        }
                                        systemAppsFragment.reload();
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.everything_done)).append(" ").append(getString(
                                                R.string.batch_backup_finished, PackageTasks.PACKAGES)).append(" *");
                                        PackageTasks.mRunning = false;
                                    }
                                }.execute();
                            });
                            backup.show();
                        }
                        break;
                    case 1:
                        if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog turnoff = new Dialog(requireActivity());
                            turnoff.setIcon(R.mipmap.ic_launcher);
                            turnoff.setTitle(R.string.sure_question);
                            turnoff.setMessage(getString(R.string.batch_list_disable) + "\n" + PackageTasks.mBatchApps.toString()
                                    .replaceAll("\\s+", "\n - "));
                            turnoff.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            turnoff.setPositiveButton(getString(R.string.turn_on_off), (dialogInterface, i) -> {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        PackageTasks.mRunning = true;
                                        if (PackageTasks.mOutput == null) {
                                            PackageTasks.mOutput = new StringBuilder();
                                        } else {
                                            PackageTasks.mOutput.setLength(0);
                                        }
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_processing_initialized)).append("...\n\n");
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_list_summary)).append(PackageTasks
                                                .mBatchApps.toString().replaceAll("\\s+", "\n - ")).append("\n\n");
                                        Intent turnOffIntent = new Intent(getActivity(), PackageTasksActivity.class);
                                        turnOffIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.batch_processing));
                                        turnOffIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.batch_processing_finished));
                                        startActivity(turnOffIntent);
                                    }

                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ", "")
                                                .replaceAll("\\s+", " ").split(" ");
                                        for (String packageID : batchApps) {
                                            if (packageID.contains(".")) {
                                                PackageTasks.mOutput.append(PackageTasks.isEnabled(packageID, new WeakReference<>(requireActivity())) ? "** " +
                                                        getString(R.string.disabling, packageID) : "** " + getString(R.string.enabling, packageID));
                                                if (PackageTasks.isEnabled(packageID, new WeakReference<>(requireActivity()))) {
                                                    RootUtils.runCommand("pm disable " + packageID);
                                                } else {
                                                    RootUtils.runCommand("pm enable " + packageID);
                                                }
                                                PackageTasks.mOutput.append(": ").append(getString(R.string.done)).append(" *\n\n");
                                                Utils.sleep(1);
                                            }
                                        }
                                        systemAppsFragment.reload();
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.everything_done)).append(" *");
                                        PackageTasks.mRunning = false;
                                    }
                                }.execute();
                            });
                            turnoff.show();
                        }
                        break;
                    case 2:
                        if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            Dialog uninstall = new Dialog(requireActivity());
                            uninstall.setIcon(R.mipmap.ic_launcher);
                            uninstall.setTitle(R.string.sure_question);
                            uninstall.setMessage(getString(R.string.batch_list_remove) + "\n" + PackageTasks.mBatchApps.toString().replaceAll("\\s+", "\n - "));
                            uninstall.setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            });
                            uninstall.setPositiveButton(getString(R.string.uninstall), (dialogInterface, i) -> {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        PackageTasks.mRunning = true;
                                        if (PackageTasks.mOutput == null) {
                                            PackageTasks.mOutput = new StringBuilder();
                                        } else {
                                            PackageTasks.mOutput.setLength(0);
                                        }
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_processing_initialized)).append("...\n\n");
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.batch_list_summary)).append(PackageTasks
                                                .mBatchApps.toString().replaceAll("\\s+", "\n - ")).append("\n\n");
                                        Intent removeIntent = new Intent(getActivity(), PackageTasksActivity.class);
                                        removeIntent.putExtra(PackageTasksActivity.TITLE_START, getString(R.string.batch_processing));
                                        removeIntent.putExtra(PackageTasksActivity.TITLE_FINISH, getString(R.string.batch_processing_finished));
                                        startActivity(removeIntent);
                                    }

                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        String[] batchApps = PackageTasks.mBatchApps.toString().replaceFirst(" ", "")
                                                .replaceAll("\\s+", " ").split(" ");
                                        for (String packageID : batchApps) {
                                            if (packageID.contains(".") && Utils.isPackageInstalled(packageID, requireActivity())) {
                                                PackageTasks.mOutput.append("** ").append(getString(R.string.uninstall_summary, packageID));
                                                RootUtils.runCommand("pm uninstall --user 0 " + packageID);
                                                PackageTasks.mOutput.append(Utils.isPackageInstalled(packageID, requireActivity()) ? ": " +
                                                        getString(R.string.failed) + " *\n\n" : ": " + getString(R.string.done) + " *\n\n");
                                                Utils.sleep(1);
                                            }
                                        }
                                        systemAppsFragment.reload();
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        PackageTasks.mOutput.append("** ").append(getString(R.string.everything_done)).append(" *");
                                        PackageTasks.mRunning = false;
                                    }
                                }.execute();
                            });
                            uninstall.show();
                        }
                        break;
                    case 3:
                        if (PackageTasks.mBatchApps.toString().isEmpty() || !PackageTasks.mBatchApps.toString().contains(".")) {
                            Utils.toast(getString(R.string.batch_list_empty), getActivity());
                        } else {
                            PackageTasks.mBatchApps.setLength(0);
                            systemAppsFragment.reload();
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        AppCompatImageButton settings = rootView.findViewById(R.id.settings_icon);
        settings.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings));
        settings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), settings);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.system)).setCheckable(true)
                    .setChecked(Utils.getBoolean("system_apps", true, getActivity()));
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.user)).setCheckable(true)
                    .setChecked(Utils.getBoolean("user_apps", true, getActivity()));
            SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));
            sort.add(Menu.NONE, 3, Menu.NONE, getString(R.string.name)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_name", true, getActivity()));
            sort.add(Menu.NONE, 4, Menu.NONE, getString(R.string.package_id)).setCheckable(true)
                    .setChecked(Utils.getBoolean("sort_id", false, getActivity()));
            String lang;
            if (Utils.getBoolean("use_english", false, getActivity())) {
                lang = "en_US";
            } else if (Utils.getBoolean("use_korean", false, getActivity())) {
                lang = "ko";
            } else if (Utils.getBoolean("use_am", false, getActivity())) {
                lang = "am";
            } else if (Utils.getBoolean("use_el", false, getActivity())) {
                lang = "el";
            } else if (Utils.getBoolean("use_ml", false, getActivity())) {
                lang = "ml";
            } else if (Utils.getBoolean("use_pt", false, getActivity())) {
                lang = "pt";
            } else {
                lang = java.util.Locale.getDefault().getLanguage();
            }
            SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, lang));
            language.add(Menu.NONE, 12, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                    .setChecked(Utils.languageDefault(getActivity()));
            language.add(Menu.NONE, 13, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_english", false, getActivity()));
            language.add(Menu.NONE, 14, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_korean", false, getActivity()));
            language.add(Menu.NONE, 15, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_am", false, getActivity()));
            language.add(Menu.NONE, 16, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_el", false, getActivity()));
            language.add(Menu.NONE, 17, Menu.NONE, getString(R.string.language_ml)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_ml", false, getActivity()));
            language.add(Menu.NONE, 18, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_pt", false, getActivity()));
            if (!Utils.isNotDonated(requireActivity())) {
                menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.allow_ads)).setCheckable(true)
                        .setChecked(Utils.getBoolean("allow_ads", true, getActivity()));
            }
            menu.add(Menu.NONE, 6, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true)
                    .setChecked(Utils.getBoolean("dark_theme", true, getActivity()));
            SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
            about.add(Menu.NONE, 7, Menu.NONE, getString(R.string.support));
            about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.more_apps));
            about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.report_issue));
            about.add(Menu.NONE, 10, Menu.NONE, getString(R.string.source_code));
            about.add(Menu.NONE, 11, Menu.NONE, getString(R.string.about));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        if (Utils.getBoolean("system_apps", true, getActivity())) {
                            Utils.saveBoolean("system_apps", false, getActivity());
                        } else {
                            Utils.saveBoolean("system_apps", true, getActivity());
                        }
                        systemAppsFragment.reload();
                        break;
                    case 2:
                        if (Utils.getBoolean("user_apps", true, getActivity())) {
                            Utils.saveBoolean("user_apps", false, getActivity());
                        } else {
                            Utils.saveBoolean("user_apps", true, getActivity());
                        }
                        systemAppsFragment.reload();
                        break;
                    case 3:
                        if (Utils.getBoolean("sort_name", true, getActivity())) {
                            Utils.saveBoolean("sort_name", false, getActivity());
                            Utils.saveBoolean("sort_id", true, getActivity());
                        } else {
                            Utils.saveBoolean("sort_name", true, getActivity());
                            Utils.saveBoolean("sort_id", false, getActivity());
                        }
                        systemAppsFragment.reload();
                        break;
                    case 4:
                        if (Utils.getBoolean("sort_id", false, getActivity())) {
                            Utils.saveBoolean("sort_id", false, getActivity());
                            Utils.saveBoolean("sort_name", true, getActivity());
                        } else {
                            Utils.saveBoolean("sort_id", true, getActivity());
                            Utils.saveBoolean("sort_name", false, getActivity());
                        }
                        systemAppsFragment.reload();
                        break;
                    case 5:
                        if (Utils.getBoolean("allow_ads", true, getActivity())) {
                            Utils.saveBoolean("allow_ads", false, getActivity());
                        } else {
                            Utils.saveBoolean("allow_ads", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 6:
                        if (Utils.getBoolean("dark_theme", true, getActivity())) {
                            Utils.saveBoolean("dark_theme", false, getActivity());
                        } else {
                            Utils.saveBoolean("dark_theme", true, getActivity());
                        }
                        restartApp();
                        break;
                    case 7:
                        Utils.launchUrl("https://t.me/smartpack_kmanager", getActivity());
                        break;
                    case 8:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(
                                "https://play.google.com/store/apps/developer?id=sunilpaulmathew"));
                        startActivity(intent);
                        break;
                    case 9:
                        Utils.launchUrl("https://github.com/SmartPack/PackageManager/issues/new", getActivity());
                        break;
                    case 10:
                        Utils.launchUrl("https://github.com/SmartPack/PackageManager/", getActivity());
                        break;
                    case 11:
                        aboutDialogue();
                        break;
                    case 12:
                        if (!Utils.languageDefault(getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 13:
                        if (!Utils.getBoolean("use_english", false, getActivity())) {
                            Utils.saveBoolean("use_english", true, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 14:
                        if (!Utils.getBoolean("use_korean", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", true, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 15:
                        if (!Utils.getBoolean("use_am", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", true, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 16:
                        if (!Utils.getBoolean("use_el", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", true, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 17:
                        if (!Utils.getBoolean("use_ml", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", true, getActivity());
                            Utils.saveBoolean("use_pt", false, getActivity());
                            restartApp();
                        }
                        break;
                    case 18:
                        if (!Utils.getBoolean("use_pt", false, getActivity())) {
                            Utils.saveBoolean("use_english", false, getActivity());
                            Utils.saveBoolean("use_korean", false, getActivity());
                            Utils.saveBoolean("use_am", false, getActivity());
                            Utils.saveBoolean("use_el", false, getActivity());
                            Utils.saveBoolean("use_ml", false, getActivity());
                            Utils.saveBoolean("use_pt", true, getActivity());
                            restartApp();
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        return rootView;
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void aboutDialogue() {
        new Dialog(requireActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name) + "\nv" + BuildConfig.VERSION_NAME)
                .setMessage(getText(R.string.about_summary))
                .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .show();
    }

}