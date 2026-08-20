/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import static com.smartpack.packagemanager.R.layout.fragment_permissions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PermissionsAdapter;
import com.smartpack.packagemanager.utils.PackageDetails;
import com.smartpack.packagemanager.utils.Serializables.PermissionsItems;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class PermissionsFragment extends Fragment {

    private boolean mAPKPicked;
    private PermissionsAdapter mRecycleViewAdapter;
    private String mPackageName, mSearchTxt = null;
    private static final APKParser mAPKParser = new APKParser();

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
        View mRootView = inflater.inflate(fragment_permissions, container, false);

        MaterialAutoCompleteTextView mSearchWord = mRootView.findViewById(R.id.search_word);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        mRecycleViewAdapter = new PermissionsAdapter(new CopyOnWriteArrayList<>(), mPackageName, mAPKPicked);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        loadUI(mSearchTxt);

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            mSearchWord.clearFocus();
            return true;
        });

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadUI(s.toString().trim().toLowerCase());
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!isAdded()) return;

                if (mSearchTxt != null && !mSearchTxt.trim().isEmpty()) {
                    mSearchTxt = null;
                    mSearchWord.setText(null);
                    return;
                }

                requireActivity().finish();
            }
        });

        return mRootView;
    }

    private void loadUI(String searchTxt) {
        new sExecutor() {
            private List<PermissionsItems> data;
            @Override
            public void onPreExecute() {
            }

            private List<PermissionsItems> getData() {
                List<PermissionsItems> perms = new CopyOnWriteArrayList<>();
                for (PermissionsItems permissions : PackageDetails.getPermissions(mPackageName, requireActivity())) {
                    if (searchTxt == null || permissions.getTitle().toLowerCase().contains(searchTxt.toLowerCase())) {
                        perms.add(permissions);
                    }
                }
                return perms;
            }

            private List<PermissionsItems> getPermissions() {
                List<PermissionsItems> perms = new CopyOnWriteArrayList<>();
                try {
                    for (String permissions : mAPKParser.getPermissions()) {
                        if (searchTxt == null || permissions.toLowerCase().contains(searchTxt.toLowerCase())) {
                            perms.add(new PermissionsItems(false, permissions, sPermissionUtils.getDescription(permissions
                                    .replace("android.permission.", ""), requireActivity())));
                        }
                    }
                } catch (NullPointerException ignored) {
                }
                return perms;
            }

            @Override
            public void doInBackground() {
                data = mAPKParser.getPermissions() != null ? getPermissions()
                        : getData();
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) return;
                mRecycleViewAdapter.updateData(data);
                mSearchTxt = searchTxt;
            }
        }.execute();
    }

}