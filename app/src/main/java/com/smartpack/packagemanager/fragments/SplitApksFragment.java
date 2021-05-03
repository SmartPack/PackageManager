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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.RecycleViewSplitAPKsAdapter;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class SplitApksFragment extends Fragment {

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_general, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        RecycleViewSplitAPKsAdapter mRecycleViewAdapter = new RecycleViewSplitAPKsAdapter(SplitAPKInstaller.splitApks(PackageData.getParentDir(Common.getApplicationID(), requireActivity())));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        return mRootView;
    }

}