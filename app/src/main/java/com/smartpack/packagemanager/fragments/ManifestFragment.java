/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apk.axml.APKParser;
import com.apk.axml.ResourceTableParser;
import com.apk.axml.aXMLDecoder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

import java.io.InputStream;
import java.util.zip.ZipFile;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 08, 2021
 */
public class ManifestFragment extends Fragment {

    private boolean mAPKPicked;
    private String mPackageName;

    public ManifestFragment() {
    }

    public static ManifestFragment newInstance(String packageName, boolean apkPicked) {
        ManifestFragment fragment = new ManifestFragment();

        Bundle args = new Bundle();
        args.putString("packageNameIntent", packageName);
        args.putBoolean("apkPickedIntent", apkPicked);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPackageName = getArguments().getString("packageNameIntent");
            mAPKPicked = getArguments().getBoolean("apkPickedIntent");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.layout_textview, container, false);

        MaterialTextView mText = mRootView.findViewById(R.id.text);
        ProgressBar mProgress = mRootView.findViewById(R.id.progress);

        new sExecutor() {
            private String mManifest = null;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(VISIBLE);
            }

            @Override
            public void doInBackground() {
                if (mAPKPicked) {
                    mManifest = new APKParser().getManifestAsString();
                } else {
                    try (ZipFile zipFile = new ZipFile(sPackageUtils.getSourceDir(mPackageName, requireActivity()))) {
                        InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("AndroidManifest.xml"));
                        mManifest = new aXMLDecoder(inputStream, new ResourceTableParser(zipFile.getInputStream(zipFile.getEntry("resources.arsc"))).parse()).decodeAsString();
                    } catch (Exception ignored) {
                    }
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                mProgress.setVisibility(GONE);
                if (mManifest != null && !mManifest.isEmpty()) {
                    mText.setText(mManifest);
                } else {
                    sCommonUtils.toast(getString(R.string.failed_decode_xml, "AndroidManifest.xml"), requireActivity()).show();
                }
            }
        }.execute();

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == VISIBLE) {
                    return;
                }
                requireActivity().finish();
            }
        });

        return mRootView;
    }

}