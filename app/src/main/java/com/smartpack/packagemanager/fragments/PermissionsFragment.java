/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PermissionsAdapter;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.SerializableItems.PermissionsItems;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class PermissionsFragment extends Fragment {

    private static final APKParser mAPKParser = new APKParser();
    private boolean mAPKPicked;
    private String mPackageName;

    public PermissionsFragment() {
    }

    public static PermissionsFragment newInstance(String packageName, boolean apkPicked) {
        PermissionsFragment fragment = new PermissionsFragment();

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

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.layout_recyclerview, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        PermissionsAdapter mRecycleViewAdapter = new PermissionsAdapter(mAPKParser.getPermissions()
                != null ? getPermissions(requireActivity()) : PackageDetails.getPermissions(mPackageName, requireActivity()), mPackageName, mAPKPicked);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        return mRootView;
    }

    private List<PermissionsItems> getPermissions(Context context) {
        List<PermissionsItems> perms = new ArrayList<>();
        try {
            for (String permissions : mAPKParser.getPermissions()) {
                perms.add(new PermissionsItems(false, permissions, sPermissionUtils.getDescription(permissions
                        .replace("android.permission.",""), context)));
            }
        } catch (NullPointerException ignored) {
        }
        return perms;
    }

}