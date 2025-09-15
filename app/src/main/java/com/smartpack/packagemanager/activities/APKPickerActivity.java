/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import com.apk.axml.APKParser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.dialogs.BundleInstallDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.fragments.APKDetailsFragment;
import com.smartpack.packagemanager.fragments.CertificateFragment;
import com.smartpack.packagemanager.fragments.ManifestFragment;
import com.smartpack.packagemanager.fragments.PermissionsFragment;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.SerializableItems.APKPickerItems;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;
import com.smartpack.packagemanager.utils.tasks.SplitAPKsInstallationTasks;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class APKPickerActivity extends AppCompatActivity {

    private APKParser mAPKParser;
    private AppCompatImageView mAppIcon;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialButton mCancel, mInstall;
    private MaterialTextView mAppName, mPackageID;
    private String mAPKPath = null, mFileName = null;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    public static final String NAME_INTENT = "name", PATH_INTENT = "path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize App Theme
        sThemeUtils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkpicker);

        mAppIcon = findViewById(R.id.app_image);
        mAppName = findViewById(R.id.app_title);
        mPackageID = findViewById(R.id.package_id);
        mMainLayout = findViewById(R.id.main_layout);
        mIconsLayout = findViewById(R.id.icons_layout);
        mInstall = findViewById(R.id.install);
        mCancel = findViewById(R.id.cancel);
        mTabLayout = findViewById(R.id.tab_Layout);
        mViewPager = findViewById(R.id.view_pager);

        mAPKPath = getIntent().getStringExtra(PATH_INTENT);
        mFileName = getIntent().getStringExtra(NAME_INTENT);

        if (mAPKPath != null) {
            manageInstallation(null, this).execute();
        } else if (getIntent().getData() != null) {
            manageInstallation(getIntent().getData(), this).execute();
        }
    }

    private sExecutor manageInstallation(Uri uri, Activity activity) {
        return new sExecutor() {
            private final List<APKPickerItems> mAPKs = new ArrayList<>();
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(activity.getString(R.string.initializing));
                mProgressDialog.show();
            }

            @Override
            public void doInBackground() {
                if (uri != null) {
                    mFileName = Objects.requireNonNull(DocumentFile.fromSingleUri(activity, uri)).getName();
                    if (Objects.requireNonNull(mFileName).endsWith(".apk")) {
                        sFileUtils.delete(getExternalFilesDir("APK"));
                        File tmpFile = new File(getExternalFilesDir("APK"), Objects.requireNonNull(mFileName));
                        tmpFile.deleteOnExit();
                        mAPKPath = tmpFile.getAbsolutePath();
                        sFileUtils.copy(uri, tmpFile, activity);
                    } else if (mFileName.endsWith(".apkm") || mFileName.endsWith(".apks") || mFileName.endsWith(".xapk")) {
                        for (File files : SplitAPKInstaller.getFilesList(activity.getCacheDir())) {
                            sFileUtils.delete(files);
                        }
                        LocalFileHeader localFileHeader;
                        int readLen;
                        byte[] readBuffer = new byte[4096];
                        try (InputStream inputStream = activity.getContentResolver().openInputStream(uri)) {
                            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                            while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                                if (localFileHeader.getFileName().endsWith(".apk")) {
                                    File apkFile = new File(activity.getCacheDir(), localFileHeader.getFileName());
                                    mAPKs.add(new APKPickerItems(apkFile, FilePicker.isSelectedAPK(apkFile, activity)));

                                    try (FileOutputStream fileOutputStream = new FileOutputStream(apkFile)) {
                                        while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                                            fileOutputStream.write(readBuffer, 0, readLen);
                                        }
                                    } catch (IOException ignored) {}
                                }
                            }
                        } catch (IOException ignored) {}
                        mAPKs.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAPKName(), rhs.getAPKName()));
                    }
                }

                if (mFileName.endsWith(".apk")) {
                    try {
                        mAPKParser = new APKParser();
                        mAPKParser.parse(mAPKPath, activity);
                    } catch (Exception ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                if (mAPKParser != null && mAPKParser.isParsed()) {
                    loadAPKDetails(activity);
                } else if (mFileName.endsWith(".apkm") || mFileName.endsWith(".apks") || mFileName.endsWith(".xapk")) {
                    new BundleInstallDialog(mAPKs, true, installApp::launch, activity);
                } else {
                    sCommonUtils.toast(getString(R.string.installation_status_bad_apks), activity).show();
                    activity.finish();
                }
            }
        };
    }

    private void loadAPKDetails(Activity activity) {
        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());
        if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
            mAppIcon.setImageDrawable(sPackageUtils.getAppIcon(mAPKParser.getPackageName(), activity));
            mAppName.setText(sPackageUtils.getAppName(mAPKParser.getPackageName(), activity));
            mPackageID.setText(mAPKParser.getPackageName());
            mPackageID.setVisibility(View.VISIBLE);
            mAppName.setVisibility(View.VISIBLE);
        } else {
            mAppName.setText(mAPKParser.getAppName());
            if (!mAPKParser.getAppName().trim().equalsIgnoreCase(mAPKParser.getPackageName().trim())) {
                mPackageID.setText(mAPKParser.getPackageName());
                mPackageID.setVisibility(View.VISIBLE);
            }
            if (mAPKParser.getAppIcon() != null) {
                mAppIcon.setImageDrawable(mAPKParser.getAppIcon());
            }
            mAppName.setVisibility(View.VISIBLE);
        }
        if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
            mInstall.setText(getString(R.string.update));
        }

        adapter.AddFragment(new APKDetailsFragment(), getString(R.string.app_info));
        if (mAPKParser.getPermissions() != null) {
            adapter.AddFragment(PermissionsFragment.newInstance(mAPKParser.getPackageName(), true), getString(R.string.permissions));
        }
        if (mAPKParser.getManifest() != null) {
            adapter.AddFragment(ManifestFragment.newInstance(mAPKParser.getPackageName(), true), getString(R.string.manifest));
        }
        if (mAPKParser.getCertificate() != null) {
            adapter.AddFragment(new CertificateFragment(), getString(R.string.certificate));
        }

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mMainLayout.setVisibility(View.VISIBLE);
        mIconsLayout.setVisibility(View.VISIBLE);

        mCancel.setOnClickListener(v -> finish());
        mInstall.setOnClickListener(v -> {
            if (mAPKParser.getPackageName() != null) {
                new SplitAPKsInstallationTasks(mAPKParser.getApkPath(), installApp::launch, activity).execute();
            } else {
                sCommonUtils.toast(activity.getString(R.string.installation_status_bad_apks), activity).show();
                finish();
            }
        });
    }

    private final ActivityResultLauncher<Intent> installApp = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean status = Objects.requireNonNull(data).getBooleanExtra("INSTALL_STATUS_UPDATE", false);
                    setResult(Activity.RESULT_OK, new Intent().putExtra("INSTALL_STATUS_UPDATE", status));
                    finish();
                }
            }
    );

}