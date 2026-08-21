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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.PagerAdapter;
import com.smartpack.packagemanager.dialogs.SortOptionsDialog;
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.RecyclerViewSwipeListener;
import com.smartpack.packagemanager.viewModels.ApplicationsViewModel;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 08, 2020
 */
public class InstalledAppsFragment extends Fragment {

    private ApplicationsViewModel mViewModel;
    private RecyclerViewSwipeListener mRecyclerViewSwipeListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_installed_apps, container, false);

        MaterialButton mSearch = mRootView.findViewById(R.id.search_icon);
        MaterialButton mSort = mRootView.findViewById(R.id.sort_icon);
        MaterialButton mReload = mRootView.findViewById(R.id.reload_icon);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        ViewPager2 mViewPager = mRootView.findViewById(R.id.view_pager);

        mViewModel = new ViewModelProvider(requireActivity()).get(ApplicationsViewModel.class);

        PagerAdapter mAdapter = new PagerAdapter(requireActivity());
        mAdapter.addFragment(ApplicationsFragment.newInstance(0), getString(R.string.show_apps_all));
        mAdapter.addFragment(ApplicationsFragment.newInstance(1), getString(R.string.show_apps_system));
        mAdapter.addFragment(ApplicationsFragment.newInstance(2), getString(R.string.show_apps_user));
        mViewPager.setAdapter(mAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(mAdapter.getPageTitle(position))
        ).attach();

        mViewPager.setCurrentItem(getTabPosition(), false);

        mSearch.setOnClickListener(v -> mViewModel.triggerSearch(true));

        mReload.setOnClickListener(v -> new sExecutor() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                PackageData.generateData(null, requireActivity());
            }

            @Override
            public void onPostExecute() {
                mViewModel.triggerReload();
            }
        }.execute());

        mSort.setOnClickListener(v -> new SortOptionsDialog(requireActivity()) {
            @Override
            public void onItemClicked() {
                mViewModel.triggerSort();
            }
        });

        View child = mViewPager.getChildAt(0);
        if (child instanceof RecyclerView) {
            mRecyclerViewSwipeListener = new RecyclerViewSwipeListener((RecyclerView) child) {
                @Override
                public void onSwipeLeft() {
                }

                @Override
                public void onSwipeRight() {
                    if (mViewPager.getCurrentItem() == 2) {
                        AppSettings.navigateToFragment(requireActivity(), 1);
                    }
                }
            };
        }

        return mRootView;
    }

    private int getTabPosition() {
        return sCommonUtils.getInt("showAppType", 2, requireActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mRecyclerViewSwipeListener != null) {
            mRecyclerViewSwipeListener.detach();
            mRecyclerViewSwipeListener = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mViewModel.triggerSearch(false);
    }

}