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

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.AppOpsAdapter;
import com.smartpack.packagemanager.utils.AppOps;
import com.smartpack.packagemanager.utils.Serializables.PermissionsItems;
import com.smartpack.packagemanager.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 * Modified by sunilpaulmathew <sunil.kde@gmail.com> on Mar 17, 2021
 */
public class AppOpsFragment extends Fragment {

    private AppOpsAdapter mRecycleViewAdapter;
    private String mPackageName, mSearchTxt = null;

    public AppOpsFragment() {
    }

    public static AppOpsFragment newInstance(String packageName) {
        AppOpsFragment fragment = new AppOpsFragment();

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
        View mRootView = inflater.inflate(R.layout.fragment_permissions, container, false);

        MaterialAutoCompleteTextView mSearchWord = mRootView.findViewById(R.id.search_word);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        mRecycleViewAdapter = new AppOpsAdapter(new CopyOnWriteArrayList<>(), mPackageName);
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
                List<PermissionsItems> data = new CopyOnWriteArrayList<>();
                try {
                    for (PermissionsItems appOps : AppOps.getOps(mPackageName)) {
                        if (searchTxt == null || appOps.getTitle().toLowerCase().contains(searchTxt.toLowerCase())) {
                            data.add(appOps);
                        }
                    }
                } catch (NullPointerException ignored) {
                }
                return data;
            }

            @Override
            public void doInBackground() {
                data = getData();
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