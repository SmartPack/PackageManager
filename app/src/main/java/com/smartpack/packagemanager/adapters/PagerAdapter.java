/*
 * Copyright (C) 2021-2026 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 30, 2026
 */
public class PagerAdapter extends FragmentStateAdapter {

    private final List<PageItem> pages = new ArrayList<>();

    public PagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pages.get(position).fragment;
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public void addFragment(@NonNull Fragment fragment, @NonNull String title) {
        pages.add(new PageItem(fragment, title));
    }

    public CharSequence getPageTitle(int position) {
        return pages.get(position).title;
    }

    private static class PageItem {
        final Fragment fragment;
        final String title;

        PageItem(Fragment fragment, String title) {
            this.fragment = fragment;
            this.title = title;
        }
    }

}