/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.ManifestAdapter;
import com.smartpack.packagemanager.utils.APKData;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageExplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        mRecyclerView.setAdapter(new ManifestAdapter(getData()));

        return mRootView;
    }

    private List<String> getData() {
        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(APKData.getManifest() != null
                ? APKData.getManifest() : PackageExplorer.readManifest(Common.getSourceDir())).split("\\r?\\n")));
    }

}