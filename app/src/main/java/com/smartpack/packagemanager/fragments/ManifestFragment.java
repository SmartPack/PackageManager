/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.apk.axml.aXMLDecoder;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.ManifestAdapter;
import com.smartpack.packagemanager.utils.Common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 08, 2021
 */
public class ManifestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_manifest, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        new sExecutor() {
            private List<String> mManifest = null;

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                if (Common.isAPKPicker()) {
                    mManifest = new ArrayList<>(Arrays.asList(new APKParser().getManifest().trim().split("\\r?\\n")));
                } else {
                    try (ZipFile zipFile = new ZipFile(sPackageUtils.getSourceDir(Common.getApplicationID(), requireActivity()))) {
                        InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("AndroidManifest.xml"));
                        mManifest = new ArrayList<>(Arrays.asList(Objects.requireNonNull(new aXMLDecoder().decode(inputStream).trim()).split("\\r?\\n")));
                    } catch (Exception ignored) {
                    }
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (mManifest != null && !mManifest.isEmpty()) {
                    mRecyclerView.setAdapter(new ManifestAdapter(mManifest));
                } else {
                    sCommonUtils.toast(getString(R.string.failed_decode_xml, "AndroidManifest.xml"), requireActivity()).show();
                }
            }
        }.execute();

        return mRootView;
    }

}