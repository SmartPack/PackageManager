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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.ApksAdapter;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.Downloads;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.viewModels.ApplicationsViewModel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ApksFragment extends Fragment {

    private ApksAdapter mRecycleViewAdapter;
    private int mTabPosition = 2;
    private List<String> mData;
    private final List<String> mBatchList = new CopyOnWriteArrayList<>();
    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mBatch, mFAB;
    private ProgressBar mProgress;
    private String mSearchText = null;

    public ApksFragment() {
    }

    public static ApksFragment newInstance(int position) {
        ApksFragment fragment = new ApksFragment();

        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTabPosition = getArguments().getInt("tabPosition");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_applications, container, false);

        mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatch = mRootView.findViewById(R.id.batch);
        mFAB = mRootView.findViewById(R.id.fab);
        mProgress = mRootView.findViewById(R.id.progress);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mBatch.setText(R.string.delete_selected);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setHasFixedSize(true);

        mRecycleViewAdapter = new ApksAdapter(
                new CopyOnWriteArrayList<>(),
                mBatchList,
                mBatch,
                callback(),
                requireActivity()
        );
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mFAB.setOnClickListener(v -> AppSettings.launchInstaller(requireActivity()));

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
            LinearLayout mPermissionLayout = mRootView.findViewById(R.id.permission_layout);
            MaterialButton mPermissionGrant = mRootView.findViewById(R.id.grant);
            MaterialTextView mPermissionText = mRootView.findViewById(R.id.permission_text);
            mPermissionText.setText(getString(R.string.permission_denied_write_storage));
            mPermissionLayout.setVisibility(VISIBLE);
            mRecyclerView.setVisibility(GONE);
            mPermissionGrant.setOnClickListener(v -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                mFAB.setVisibility(newState == RecyclerView.SCROLL_STATE_IDLE ? View.VISIBLE : View.GONE);
            }
        });

        loadUI(mSearchText).execute();

        AppSettings.applyMargin(mRecyclerView, requireActivity());

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
                loadUI(s.toString().trim()).execute();
            }
        });

        ApplicationsViewModel viewModel = new ViewModelProvider(requireActivity()).get(ApplicationsViewModel.class);

        viewModel.getSearchTrigger().observe(getViewLifecycleOwner(), isVisible -> {
            if (isVisible) {
                mSearchWord.setVisibility(View.VISIBLE);
                if (!mSearchWord.hasFocus()) {
                    mSearchWord.requestFocus();
                    Utils.toggleKeyboard(1, mSearchWord, requireActivity());
                }
            } else {
                mSearchWord.setVisibility(View.GONE);
                Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            }
        });

        viewModel.getSortTrigger().observe(getViewLifecycleOwner(), view -> loadUI(mSearchText).execute());

        mBatch.setOnClickListener(v ->
                new sExecutor() {

                    @Override
                    public void onPreExecute() {
                        mProgress.setVisibility(VISIBLE);
                    }

                    @Override
                    public void doInBackground() {
                        mProgress.setMax(mBatchList.size());
                        for (String apkPath : mBatchList) {
                            sFileUtils.delete(apkPath);
                            mProgress.setProgress(mProgress.getProgress() + 1);
                        }
                        mBatchList.clear();
                    }

                    @Override
                    public void onPostExecute() {
                        mProgress.setVisibility(GONE);
                        loadUI(mSearchText).execute();
                    }
                }.execute()
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == VISIBLE) {
                    return;
                }
                if (mSearchWord.getVisibility() == VISIBLE) {
                    if (mSearchText != null) {
                        mSearchText = null;
                        mSearchWord.setText(null);
                    }
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                AppSettings.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

    private ApksAdapter.OnInstallRequest callback() {
        return installApp::launch;
    }

    private sExecutor loadUI(String searchTxt) {
        return new sExecutor() {
            @Override
            public void onPreExecute() {
                mFAB.setVisibility(GONE);
                mProgress.setVisibility(VISIBLE);
            }

            @Override
            public void doInBackground() {
                mData = Downloads.getData(searchTxt, mTabPosition, requireActivity());
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }

                mRecycleViewAdapter.updateData(mData);

                mSearchText =searchTxt;
                mSearchWord.setHint(getString(R.string.search_market_message, mRecycleViewAdapter.getItemCount() + " " + getString(R.string.exported_apps)));
                mBatch.setVisibility(mBatchList.isEmpty() ? GONE : VISIBLE);
                mProgress.setVisibility(GONE);
                mFAB.setVisibility(VISIBLE);
            }
        };
    }

    private final ActivityResultLauncher<Intent> installApp = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    requireActivity().recreate();
                }
            }
    );

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSearchText != null) {
            mSearchWord.setText(null);
            mSearchWord.setVisibility(GONE);
        }

        mBatchList.clear();
    }

}