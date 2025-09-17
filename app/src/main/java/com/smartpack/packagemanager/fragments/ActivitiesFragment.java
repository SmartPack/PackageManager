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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.ActivitiesAdapter;
import com.smartpack.packagemanager.utils.PackageDetails;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class ActivitiesFragment extends Fragment {

    private String mPackageName;

    public ActivitiesFragment() {
    }

    public static ActivitiesFragment newInstance(String packageName) {
        ActivitiesFragment fragment = new ActivitiesFragment();

        Bundle args = new Bundle();
        args.putString("packageNameIntent", packageName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPackageName = getArguments().getString("packageNameIntent");
        }
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_activities, container, false);

        new sExecutor() {
            private ActivitiesAdapter mRecycleViewAdapter;
            private ProgressBar mProgressBar;
            private RecyclerView mRecyclerView;
            @Override
            public void onPreExecute() {
                mProgressBar = mRootView.findViewById(R.id.progress);
                mRecyclerView = mRootView.findViewById(R.id.recycler_view);
                mProgressBar.setVisibility(VISIBLE);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new ActivitiesAdapter(PackageDetails.getActivities(mPackageName, requireActivity()), mPackageName);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }
                mProgressBar.setVisibility(GONE);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
            }
        }.execute();

        return mRootView;
    }

}