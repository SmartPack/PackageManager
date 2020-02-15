/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
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

import com.smartpack.packagemanager.BuildConfig;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;
import com.smartpack.packagemanager.views.dialog.Dialog;
import com.smartpack.packagemanager.views.recyclerview.DescriptionView;
import com.smartpack.packagemanager.views.recyclerview.RecyclerViewItem;
import com.smartpack.packagemanager.views.recyclerview.SwitchView;
import com.smartpack.packagemanager.views.recyclerview.TitleView;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 */

public class AboutFragment extends RecyclerViewFragment {

    private static final LinkedHashMap<String, String> sCredits = new LinkedHashMap<>();

    static {
        sCredits.put("Kernel Adiutor\n(Code base),Grarak", "https://github.com/Grarak");
        sCredits.put("App Icon,Toxinpiper", "https://t.me/toxinpiper");
    }


    @Override
    protected void init() {
        super.init();

        addViewPagerFragment(new InfoFragment());
    }

    @Override
    public int getSpanCount() {
        return super.getSpanCount() + 1;
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        aboutInit(items);
    }

    private void aboutInit(List<RecyclerViewItem> items) {
        TitleView about = new TitleView();
        about.setText(getString(R.string.app_name));
        items.add(about);

        DescriptionView versioninfo = new DescriptionView();
        versioninfo.setDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        versioninfo.setTitle(getString(R.string.version));
        versioninfo.setSummary(BuildConfig.VERSION_NAME);

        items.add(versioninfo);

        DescriptionView changelogs = new DescriptionView();
        changelogs.setDrawable(getResources().getDrawable(R.drawable.ic_changelog));
        changelogs.setTitle(getString(R.string.change_logs));
        changelogs.setSummary(getString(R.string.change_logs_summary));
        changelogs.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://raw.githubusercontent.com/SmartPack/PackageManager/master/change-logs.md", getActivity());
            }
        });

        items.add(changelogs);

        SwitchView allow_ads = new SwitchView();
        allow_ads.setDrawable(getResources().getDrawable(R.drawable.ic_ads));
        allow_ads.setSummary(getString(R.string.allow_ads));
        allow_ads.setChecked(Utils.getBoolean("google_ads", true, getActivity()));
        allow_ads.addOnSwitchListener(new SwitchView.OnSwitchListener() {
            @Override
            public void onChanged(SwitchView switchview, boolean isChecked) {
                Utils.saveBoolean("google_ads", isChecked, getActivity());
                if (!isChecked) {
                    new Dialog(getActivity())
                            .setMessage(R.string.disable_ads_message)
                            .setPositiveButton(R.string.ok, (dialog, id) -> {
                            })
                            .show();
                } else {
                    Utils.toast(R.string.allow_ads_message, getActivity());
                }
            }
        });

        items.add(allow_ads);

        SwitchView dark_theme = new SwitchView();
        dark_theme.setDrawable(getResources().getDrawable(R.drawable.ic_color));
        dark_theme.setSummary(getString(R.string.dark_theme));
        dark_theme.setChecked(Utils.getBoolean("dark_theme", true, getActivity()));
        dark_theme.addOnSwitchListener(new SwitchView.OnSwitchListener() {
            @Override
            public void onChanged(SwitchView switchview, boolean isChecked) {
                Utils.saveBoolean("dark_theme", isChecked, getActivity());
                Utils.toast(getString(R.string.dark_theme_message, Utils.getBoolean("dark_theme", true,
                        getActivity()) ? "Dark" : "Light"), getActivity());
            }
        });

        items.add(dark_theme);

        DescriptionView sourcecode = new DescriptionView();
        sourcecode.setDrawable(getResources().getDrawable(R.drawable.ic_source));
        sourcecode.setTitle(getString(R.string.source_code));
        sourcecode.setSummary(getString(R.string.source_code_summary));
        sourcecode.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                if (!Utils.isNetworkAvailable(getActivity())) {
                    Utils.toast(R.string.no_internet, getActivity());
                    return;
                }
                Utils.launchUrl("https://github.com/SmartPack/PackageManager", requireActivity());
            }
        });

        items.add(sourcecode);

        DescriptionView support = new DescriptionView();
        support.setDrawable(getResources().getDrawable(R.drawable.ic_support));
        support.setTitle(getString(R.string.support));
        support.setSummary(getString(R.string.support_summary));
        support.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://t.me/smartpack_kmanager", getActivity());
            }
        });

        items.add(support);

        DescriptionView playstore = new DescriptionView();
        playstore.setDrawable(getResources().getDrawable(R.drawable.ic_playstore));
        playstore.setTitle(getString(R.string.playstore));
        playstore.setSummary(getString(R.string.playstore_summary));
        playstore.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.packagemanager", requireActivity());
            }
        });

        items.add(playstore);
        DescriptionView donatetome = new DescriptionView();
        donatetome.setDrawable(getResources().getDrawable(R.drawable.ic_donate));
        donatetome.setTitle(getString(R.string.donate_me));
        donatetome.setSummary(getString(R.string.donate_me_summary));
        donatetome.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
            @Override
            public void onClick(RecyclerViewItem item) {
                Dialog donate_to_me = new Dialog(getActivity());
                donate_to_me.setIcon(R.mipmap.ic_launcher);
                donate_to_me.setTitle(getString(R.string.donate_me));
                if (Utils.isDonated(requireActivity())) {
                    donate_to_me.setMessage(getString(R.string.donate_me_message));
                    donate_to_me.setNegativeButton(getString(R.string.donate_nope), (dialogInterface, i) -> {
                    });
                } else {
                    donate_to_me.setMessage(getString(R.string.donate_me_message) + getString(R.string.donate_me_playstore));
                    donate_to_me.setNegativeButton(getString(R.string.purchase_app), (dialogInterface, i) -> {
                        Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.donate", getActivity());
                    });
                }
                donate_to_me.setPositiveButton(getString(R.string.paypal_donation), (dialog1, id1) -> {
                    Utils.launchUrl("https://www.paypal.me/sunilpaulmathew", getActivity());
                });
                donate_to_me.show();
            }
        });

        items.add(donatetome);

        TitleView credits = new TitleView();
        credits.setText(getString(R.string.credits));
        items.add(credits);

        for (final String lib : sCredits.keySet()) {
            String title = lib.split(",")[1];
            String summary = lib.split(",")[0];
            DescriptionView descriptionView = new DescriptionView();
            switch (title) {
                case "Grarak":
                    descriptionView.setDrawable(getResources().getDrawable(R.drawable.ic_grarak));
                    break;
                case "Toxinpiper":
                    descriptionView.setDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                    break;
            }
            descriptionView.setTitle(title);
            descriptionView.setSummary(summary);
            descriptionView.setOnItemClickListener(new RecyclerViewItem.OnItemClickListener() {
                @Override
                public void onClick(RecyclerViewItem item) {
                    if (!Utils.isNetworkAvailable(getActivity())) {
                        Utils.toast(R.string.no_internet, getActivity());
                        return;
                    }
                    Utils.launchUrl(sCredits.get(lib), getActivity());
                }
            });

            items.add(descriptionView);
        }
    }

    public static class InfoFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);
            rootView.findViewById(R.id.image);
            return rootView;
        }
    }

}