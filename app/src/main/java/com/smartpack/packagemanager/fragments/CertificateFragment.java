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

import com.apk.axml.APKParser;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 26, 2022
 */
public class CertificateFragment extends Fragment {

    private static final APKParser mAPKParser = new APKParser();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_certificate, container, false);

        MaterialTextView mText = mRootView.findViewById(R.id.text);

        if (mAPKParser.getCertificate() != null) {
            try {
                mText.setText(mAPKParser.getCertificate());
            } catch (Exception ignored) {
            }
        }

        return mRootView;
    }
    
}