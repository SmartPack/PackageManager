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
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.ExportedAppsAdapter;
import com.smartpack.packagemanager.utils.Downloads;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsFragment extends Fragment {

    private boolean mRefresh = false;
    private List<String> mBatchList = null;
    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mBatch, mSearch, mSort;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgress;
    private ExportedAppsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_exported_apps, container, false);

        mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatch = mRootView.findViewById(R.id.batch);
        mSearch = mRootView.findViewById(R.id.search_icon);
        mSort = mRootView.findViewById(R.id.sort_icon);
        mProgress = mRootView.findViewById(R.id.progress);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        FloatingActionButton mFAB = requireActivity().findViewById(R.id.fab);

        mFAB.setVisibility(VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSort);
            Menu menu = popupMenu.getMenu();
            if (!Downloads.getData(mProgress, requireActivity()).isEmpty()) {
                menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order)).setCheckable(true)
                        .setChecked(sCommonUtils.getBoolean("reverse_order_exports", false, requireActivity()));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("reverse_order_exports", !sCommonUtils.getBoolean("reverse_order_exports", false, requireActivity()), requireActivity());
                    loadUI().execute();
                }
                return false;
            });
            popupMenu.show();
        });

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
            LinearLayout mPermissionLayout = mRootView.findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = mRootView.findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = mRootView.findViewById(R.id.permission_text);
            mPermissionText.setText(getString(R.string.permission_denied_write_storage));
            mPermissionLayout.setVisibility(VISIBLE);
            mRecyclerView.setVisibility(GONE);
            mTabLayout.setVisibility(GONE);
            mPermissionGrant.setOnClickListener(v -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                mFAB.setVisibility(newState == RecyclerView.SCROLL_STATE_IDLE ? View.VISIBLE : View.GONE);
            }
        });

        loadUI().execute();

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("downloadTypes", "apks", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            sCommonUtils.saveString("downloadTypes", "apks", requireActivity());
                            loadUI().execute();
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            sCommonUtils.saveString("downloadTypes", "bundles", requireActivity());
                            loadUI().execute();
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSearch.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == VISIBLE) {
                mSearchWord.setVisibility(GONE);
                Utils.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(VISIBLE);
                Utils.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

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
                Downloads.setSearchText(s.toString());
                loadUI().execute();
            }
        });

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
                        loadUI().execute();
                    }
                }.execute()
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Downloads.getSearchText() != null) {
                    mSearchWord.setText(null);
                    Downloads.setSearchText(null);
                    return;
                }
                if (mSearchWord.getVisibility() == VISIBLE) {
                    mSearchWord.setVisibility(GONE);
                    return;
                }

                mBatchList.clear();

                Utils.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }

    private sExecutor loadUI() {
        return new sExecutor() {
            @Override
            public void onPreExecute() {
                mProgress.setVisibility(VISIBLE);
                if (mBatchList == null) {
                    mBatchList = new ArrayList<>();
                }
            }

            @Override
            public void doInBackground() {
                File[] oldList = requireActivity().getExternalFilesDir("").listFiles();
                PackageData.makePackageFolder(requireActivity());
                for (File files : Objects.requireNonNull(oldList)) {
                    if (files.isFile() && (files.getName().endsWith(".apk") || files.getName().endsWith(".apkm"))) {
                        sFileUtils.copy(files, new File(PackageData.getPackageDir(requireActivity()), files.getName()));
                        sFileUtils.delete(files);
                    }
                }
                mRecycleViewAdapter = new ExportedAppsAdapter(Downloads.getData(mProgress, requireActivity()), mBatchList, installApp::launch, requireActivity());
            }

            @Override
            public void onPostExecute() {
                mSearch.setEnabled(mRecycleViewAdapter.getItemCount() >= 5);
                mSort.setEnabled(mRecycleViewAdapter.getItemCount() >= 5);
                mBatch.setVisibility(mBatchList.isEmpty() ? GONE : VISIBLE);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgress.setVisibility(GONE);
            }
        };
    }

    private int getTabPosition(Activity activity) {
        if (sCommonUtils.getString("downloadTypes", "apks", activity).equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
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
    public void onStart() {
        super.onStart();
        if (mRefresh) {
            requireActivity().recreate();
            mRefresh = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBatchList.clear();
    }

}