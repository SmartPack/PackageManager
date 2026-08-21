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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

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
import com.smartpack.packagemanager.utils.AppSettings;
import com.smartpack.packagemanager.utils.RecyclerViewSwipeListener;
import com.smartpack.packagemanager.viewModels.ApplicationsViewModel;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 14, 2021
 */
public class ExportedAppsFragment extends Fragment {

    private ApplicationsViewModel mViewModel;
    private RecyclerViewSwipeListener mRecyclerViewSwipeListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_exported_apps, container, false);

        MaterialButton mSearch = mRootView.findViewById(R.id.search_icon);
        MaterialButton mSort = mRootView.findViewById(R.id.sort_icon);
        ViewPager2 mViewPager = mRootView.findViewById(R.id.view_pager);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);

        mViewModel = new ViewModelProvider(requireActivity()).get(ApplicationsViewModel.class);

        PagerAdapter mAdapter = new PagerAdapter(requireActivity());
        mAdapter.addFragment(ApksFragment.newInstance(0), getString(R.string.apks));
        mAdapter.addFragment(ApksFragment.newInstance(1), getString(R.string.bundles));
        mViewPager.setAdapter(mAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(mAdapter.getPageTitle(position))
        ).attach();

        mViewPager.setCurrentItem(getTabPosition(), false);

        mSearch.setOnClickListener(v -> mViewModel.triggerSearch(true));

        mSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
            Menu menu = popupMenu.getMenu();

            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.reverse_order))
                    .setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("reverse_order_exports", false, requireActivity()));

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    boolean current = sCommonUtils.getBoolean("reverse_order_exports", false, requireActivity());
                    sCommonUtils.saveBoolean("reverse_order_exports", !current, requireActivity());
                    mViewModel.triggerSort();
                }
                return false;
            });

            popupMenu.show();
        });

        View child = mViewPager.getChildAt(0);
        if (child instanceof RecyclerView) {
            mRecyclerViewSwipeListener = new RecyclerViewSwipeListener((RecyclerView) child) {
                @Override
                public void onSwipeLeft() {
                    if (mViewPager.getCurrentItem() == 0) {
                        AppSettings.navigateToFragment(requireActivity(), 0);
                    }
                }

                @Override
                public void onSwipeRight() {
                    if (mViewPager.getCurrentItem() == 1) {
                        AppSettings.navigateToFragment(requireActivity(), 2);
                    }
                }
            };
        }

        return mRootView;
    }

    private int getTabPosition() {
        return sCommonUtils.getInt("exportedTypes", 0, requireActivity());
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